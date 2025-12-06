import { StreamChat, Channel, DefaultGenerics } from 'stream-chat';
import type { Event } from 'stream-chat';
import { ElMessage } from 'element-plus';
import type { Handshake } from '../types';
import { useHandshakeStore } from '../stores/handshakeStore';

let chatClient: StreamChat | null = null;
let currentUserId: string | null = null;
let hasNewMessageListener = false;

async function setupGlobalMessageListener() {
  if (!chatClient || hasNewMessageListener) return;

  try {
    // get all handshakes from store
    const handshakeStore = useHandshakeStore();
    await handshakeStore.loadHandshakes();
    const allHandshakes = handshakeStore.handshakes;
    
    // get channels for each handshake
    const channelPromises = allHandshakes.map(handshake => getHandshakeChannel(handshake.id));
    const channelResults = await Promise.all(channelPromises);

    // error varsa null gönderiyordu, o yüzden böyle
    const channels = channelResults.filter((channel): channel is Channel<DefaultGenerics> => channel !== null);

    // set up message listener on each channel
    channels.forEach((channel) => {
      addChannelMessageListener(channel);
    });

    hasNewMessageListener = true;
  } catch (error) {
    console.error('Failed to setup message listeners:', error);
  }
}

/**
 * Initialize Stream Chat client and connect user
 */
export async function initializeStreamChat(
  userId: string,
  userName: string,
  userToken: string
): Promise<StreamChat> {
  try {
    const apiKey = import.meta.env.VITE_STREAM_CHAT_API_KEY;
    
    if (!apiKey) {
      throw new Error('Stream Chat API key not configured. Set VITE_STREAM_CHAT_API_KEY in .env');
    }

    // Get or create client instance
    chatClient = StreamChat.getInstance(apiKey);

    // Connect user
    await chatClient.connectUser(
      {
        id: userId,
        name: userName,
      },
      userToken
    );

    currentUserId = userId;

    // Set up global new-message notification listener once per client
    setupGlobalMessageListener();

    console.log('Stream Chat initialized for user:', userName);

    return chatClient;
  } catch (error) {
    console.error('Failed to initialize Stream Chat:', error);
    throw error;
  }
}

/**
 * Create a channel for a handshake
 */
export async function createHandshakeChannel(
  handshake: Handshake
): Promise<Channel<DefaultGenerics>> {  // im returning a promise of channel with "DefaultGenerics" (Stream Chat standard type without extensions)
  if (!chatClient) {
    throw new Error('Stream Chat client not initialized. Call initializeStreamChat first.');
  }

  try {
    const channelId = `handshake-${handshake.id}`;
    
    // Create or get existing channel
    const channel = chatClient.channel('messaging', channelId, {
      name: handshake.offerTitle,
      members: [
        handshake.seeker.id.toString(),
        handshake.provider.id.toString(),
      ],
      // Custom data
      handshake_id: handshake.id,
      offer_id: handshake.offerId || null,
      request_id: handshake.requestId || null,
      agreed_hours: handshake.agreedHours,
      status: handshake.status,
    });

    // Watch the channel (creates if doesn't exist)
    await channel.watch();
    
    console.log('Channel created/accessed:', channelId);
    addChannelMessageListener(channel);
    
    return channel;
  } catch (error) {
    console.error('Failed to create handshake channel:', error);
    throw error;
  }
}

/**
 * Get an existing channel by handshake ID
 */
export async function getHandshakeChannel(
  handshakeId: number
): Promise<Channel<DefaultGenerics> | null> {
  if (!chatClient) {
    throw new Error('Stream Chat client not initialized');
  }

  try {
    const channelId = `handshake-${handshakeId}`;
    const channel = chatClient.channel('messaging', channelId);
    
    // Check if channel exists
    await channel.watch();
    
    return channel;
  } catch (error) {
    console.error('Failed to get channel:', error);
    return null;
  }
}

/**
 * Update channel data when handshake status changes
 */
export async function updateChannelStatus(
  handshakeId: number,
  status: string
): Promise<void> {
  if (!chatClient) return;

  try {
    const channel = await getHandshakeChannel(handshakeId);
    if (!channel) return;

    await channel.updatePartial({
      set: {
        status: status,
      },
    });

  } catch (error) {
    console.error('Failed to update channel status:', error);
  }
}

/**
 * Get all channels for current user
 */
export async function getUserChannels(): Promise<Channel<DefaultGenerics>[]> {
  if (!chatClient || !currentUserId) {
    throw new Error('Stream Chat client not initialized');
  }

  try {
    const filter = {
      type: 'messaging',
      members: { $in: [currentUserId] },
    };
    
    const sort = [{ last_message_at: -1 as const }];
    const channels = await chatClient.queryChannels(filter, sort, {
      watch: true,
      state: true,
    });

    return channels;
  } catch (error) {
    console.error('Failed to get user channels:', error);
    return [];
  }
}

/**
 * Disconnect from Stream Chat
 */
export async function disconnectStreamChat(): Promise<void> {
  if (chatClient && currentUserId) {
    try {
      await chatClient.disconnectUser();
      console.log('Stream Chat disconnected');
      chatClient = null;
      currentUserId = null;
    } catch (error) {
      console.error('Failed to disconnect from Stream Chat:', error);
    }
  }
}

/**
 * Get the current Stream Chat client
 */
export function getStreamChatClient(): StreamChat | null {
  return chatClient;
}

/**
 * Check if Stream Chat is initialized
 */
export function isStreamChatInitialized(): boolean {
  return chatClient !== null && currentUserId !== null;
}

/**
 * Mark channel as read
 */
export async function markChannelAsRead(channel: Channel<DefaultGenerics>): Promise<void> {
  try {
    await channel.markRead();
  } catch (error) {
    console.error('Failed to mark channel as read:', error);
  }
}

function addChannelMessageListener(channel: Channel<DefaultGenerics>) {
  channel.on('message.new', (event: Event) => {
    const message = event.message;
    const user = message?.user || event.user;

    // ignore messages sent by the current user
    if (user?.id && user.id === currentUserId) return;

    
    const senderName = (user && (user.name as string)) || user?.id || 'New message';
    const channelName = channel.data?.name || 'Chat';

    ElMessage({
      message: `New message from ${senderName} in ${channelName}`,
      type: 'success',
      showClose: true,
      duration: 3000,
    });
  });
}