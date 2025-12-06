<template>
  <div class="flex flex-col h-full bg-white rounded-lg shadow-lg overflow-hidden">
    <!-- Header -->
    <div class="bg-amber-500 text-white px-4 py-3 flex items-center justify-between">
      <div class="flex items-center gap-3">
        <Avatar class="w-10 h-10">
          <AvatarImage :src="otherUser?.avatar" :alt="otherUser?.name" />
          <AvatarFallback>{{ otherUser?.name?.charAt(0) || '?' }}</AvatarFallback>
        </Avatar>
        <div>
          <h3 class="font-semibold">{{ otherUser?.name || 'Loading...' }}</h3>
          <p v-if="handshake" class="text-xs text-amber-100">{{ handshake.offerTitle }}</p>
        </div>
      </div>
    </div>

    <!-- Status Banner (for handshake confirmation) -->
    <div v-if="handshake && handshake.status === 'PENDING'" class="bg-amber-50 border-b border-amber-200 px-4 py-2 text-sm">
      <div class="flex items-center justify-between">
        <div>
          <span class="font-medium text-amber-900">⏳ Waiting for confirmation</span>
          <p class="text-amber-700 text-xs mt-1">
            {{ confirmationStatus }}
          </p>
        </div>
        <Button
          v-if="!isCurrentUserConfirmed"
          @click="$emit('openConfirmModal')"
          size="sm"
          class="bg-amber-500 hover:bg-amber-600"
        >
          Confirm Handshake
        </Button>
      </div>
    </div>

    <div v-else-if="handshake && handshake.status === 'CONFIRMED'" class="bg-green-50 border-b border-green-200 px-4 py-2 text-sm">
      <span class="text-green-700">✓ Service confirmed for {{ formatDate(handshake.completedAt) }}</span>
    </div>

    <!-- Chat Messages Container -->
    <div ref="messagesContainer" class="flex-1 overflow-y-auto p-4 space-y-4">
      <!-- Loading State -->
      <div v-if="loading" class="flex items-center justify-center h-full">
        <div class="text-gray-500">Loading messages...</div>
      </div>

      <!-- Stream Chat Messages (Custom UI) -->
      <div v-if="streamChatEnabled && channel" class="space-y-4">
        <div
          v-for="message in streamMessages"
          :key="message.id"
          :class="[
            'flex',
            isCurrentUserMessage(message) ? 'justify-end' : 'justify-start'
          ]"
        >
          <div
            :class="[
              'max-w-[70%] rounded-lg px-4 py-2',
              isCurrentUserMessage(message)
                ? 'bg-amber-500 text-white'
                : 'bg-gray-100 text-gray-900'
            ]"
          >
            <p v-if="!isCurrentUserMessage(message)" class="text-xs font-semibold mb-1">
              {{ message.user?.name || 'User' }}
            </p>
            <p class="text-sm whitespace-pre-wrap">{{ message.text }}</p>
            <p :class="[
              'text-xs mt-1',
              isCurrentUserMessage(message) ? 'text-amber-100' : 'text-gray-500'
            ]">
              {{ formatTime(message.created_at) }}
            </p>
          </div>
        </div>

        <!-- Empty State for Stream Chat -->
        <div v-if="!loading && streamMessages.length === 0" class="flex items-center justify-center h-full text-gray-500 text-center">
          <div>
            <p class="font-medium">No messages yet</p>
            <p class="text-sm mt-1">Start the conversation!</p>
          </div>
        </div>
      </div>

      <!-- Fallback: Stream Chat not enabled -->
      <div v-else-if="!streamChatEnabled" class="flex items-center justify-center h-full text-gray-500 text-center">
        <div>
          <p class="font-medium">Chat not available</p>
          <p class="text-sm mt-1">Stream Chat is not enabled</p>
        </div>
      </div>
    </div>

    <!-- Message Input (Stream Chat) -->
    <div v-if="streamChatEnabled && channel" class="border-t border-gray-200 p-4">
      <form @submit.prevent="sendStreamMessage" class="flex gap-2">
        <Input
          v-model="newMessage"
          placeholder="Type a message..."
          class="flex-1"
          :disabled="sending"
        />
        <Button
          type="submit"
          :disabled="!newMessage.trim() || sending"
          class="bg-amber-500 hover:bg-amber-600"
        >
          {{ sending ? 'Sending...' : 'Send' }}
        </Button>
      </form>
    </div>


    <!-- Stream Chat Instructions -->
    <div v-if="showStreamChatInfo" class="border-t border-gray-200 p-4 bg-blue-50">
      <p class="text-sm text-blue-900">
        <strong>Note:</strong> Install Stream Chat SDK to enable real-time messaging:
        <code class="bg-blue-100 px-2 py-1 rounded text-xs ml-1">npm install stream-chat stream-chat-vue</code>
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick, watch, onUnmounted } from 'vue';
import Avatar from './ui/Avatar.vue';
import AvatarImage from './ui/AvatarImage.vue';
import AvatarFallback from './ui/AvatarFallback.vue';
import Button from './ui/Button.vue';
import Input from './ui/Input.vue';
import type { Handshake, AuthorSummary } from '../types';
import { useAppStore } from '../stores/appStore';
import {
  getStreamChatClient,
  getHandshakeChannel,
  createHandshakeChannel,
  markChannelAsRead,
  clearActiveChannel,
  setActiveChannel,
} from '../clients/streamChatClient';
import type {
  Channel as StreamChannel,
  DefaultGenerics,
  FormatMessageResponse,
} from 'stream-chat';

const props = defineProps<{
  handshake: Handshake | null;
  streamChatEnabled?: boolean;
  showStreamChatInfo?: boolean;
}>();

defineEmits<{
  'openConfirmModal': [];
}>();

const appStore = useAppStore();

const messagesContainer = ref<HTMLElement | null>(null);
const newMessage = ref('');
const loading = ref(false);
const sending = ref(false);
const chatClient = ref(getStreamChatClient());
const channel = ref<StreamChannel | null>(null);
const streamMessages = ref<FormatMessageResponse<DefaultGenerics>[]>([]);

const otherUser = computed<AuthorSummary | null>(() => {
  if (!props.handshake || !appStore.currentUser) return null;
  
  const currentUserId = appStore.currentUser.id.toString();
  const isSeeker = props.handshake.seeker.id.toString() === currentUserId;
  
  return isSeeker ? props.handshake.provider : props.handshake.seeker;
});

const confirmationStatus = computed(() => {
  if (!props.handshake) return '';
  
  const { seekerConfirmed, providerConfirmed } = props.handshake;
  
  if (seekerConfirmed && providerConfirmed) {
    return 'Both parties confirmed!';
  } else if (seekerConfirmed || providerConfirmed) {
    return 'Waiting for other party to confirm';
  }
  return 'Both parties need to confirm';
});

const isCurrentUserConfirmed = computed(() => {
  if (!props.handshake || !appStore.currentUser) return false;
  
  const currentUserId = appStore.currentUser.id.toString();
  const isSeeker = props.handshake.seeker.id.toString() === currentUserId;
  
  return isSeeker ? props.handshake.seekerConfirmed : props.handshake.providerConfirmed;
});

function formatDate(dateString: string | null): string {
  if (!dateString) return '';
  const date = new Date(dateString);
  return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
}

function formatTime(timestamp: string | Date | null | undefined): string {
  if (!timestamp) return '';
  const date = typeof timestamp === 'string' ? new Date(timestamp) : timestamp;
  return date.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' });
}

async function sendStreamMessage() {
  if (!newMessage.value.trim() || sending.value || !channel.value) return;

  sending.value = true;
  
  try {
    await channel.value.sendMessage({
      text: newMessage.value,
    });

    newMessage.value = '';
    
    // Scroll to bottom
    await nextTick();
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight;
    }
  } catch (error) {
    console.error('Failed to send message:', error);
  } finally {
    sending.value = false;
  }
}


function isCurrentUserMessage(message: FormatMessageResponse<DefaultGenerics>): boolean {
  return message.user?.id === appStore.currentUser?.id.toString();
}

// Initialize Stream Chat when enabled or handshake changes
watch(
  () => props.handshake,
  async (newHandshake, oldHandshake) => {
    // Clear the previous active channel
    if (oldHandshake) {
      clearActiveChannel();
    }
    
    // Set the new active channel
    if (newHandshake && props.streamChatEnabled) {
      await initializeStreamChat();
      setActiveChannel(newHandshake.id);
    }
  },
  { immediate: true }
);

async function initializeStreamChat() {
  if (!props.handshake) return;

  loading.value = true;
  
  try {
    chatClient.value = getStreamChatClient();
    
    if (!chatClient.value) {
      console.warn('Stream Chat client not initialized. User may need to login.');
      return;
    }

    // Get or create channel
    let existingChannel = await getHandshakeChannel(props.handshake.id);
    
    if (!existingChannel) {
      // Create new channel
      existingChannel = await createHandshakeChannel(props.handshake);
    }
    
    channel.value = existingChannel;
    
    // Load existing messages
    const state = existingChannel.state;
    if (state.messages) {
      streamMessages.value = [...state.messages];
    }
    
    // Listen for new messages
    existingChannel.on('message.new', () => {
      streamMessages.value = [...existingChannel.state.messages];
      nextTick(() => {
        if (messagesContainer.value) {
          messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight;
        }
      });
    });
    
    // Mark as read
    await markChannelAsRead(existingChannel);
    
    console.log('Stream Chat channel loaded:', existingChannel.id);
  } catch (error) {
    console.error('Failed to initialize Stream Chat:', error);
  } finally {
    loading.value = false;
  }
}

onMounted(async () => {
  if (props.streamChatEnabled && props.handshake) {
    await initializeStreamChat();
  }
});

onUnmounted(() => {
  if (props.streamChatEnabled && props.handshake) {
    clearActiveChannel();
  }
});
</script>

<style scoped>
/* Custom scrollbar */
.overflow-y-auto::-webkit-scrollbar {
  width: 6px;
}

.overflow-y-auto::-webkit-scrollbar-track {
  background: #f1f1f1;
}

.overflow-y-auto::-webkit-scrollbar-thumb {
  background: #888;
  border-radius: 3px;
}

.overflow-y-auto::-webkit-scrollbar-thumb:hover {
  background: #555;
}
</style>

