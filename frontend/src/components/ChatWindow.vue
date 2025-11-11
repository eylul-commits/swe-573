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

      <!-- Stream Chat Integration Point -->
      <div v-else-if="!streamChatEnabled" class="space-y-4">
        <!-- Fallback: Simple message display -->
        <div
          v-for="message in messages"
          :key="message.id"
          :class="[
            'flex',
            message.isCurrentUser ? 'justify-end' : 'justify-start'
          ]"
        >
          <div
            :class="[
              'max-w-[70%] rounded-lg px-4 py-2',
              message.isCurrentUser
                ? 'bg-amber-500 text-white'
                : 'bg-gray-100 text-gray-900'
            ]"
          >
            <p class="text-sm">{{ message.text }}</p>
            <p class="text-xs mt-1 opacity-70">
              {{ formatTime(message.timestamp) }}
            </p>
          </div>
        </div>
      </div>

      <!-- Stream Chat will be mounted here -->
      <div v-else id="stream-chat-container" class="h-full"></div>

      <!-- Empty State -->
      <div v-if="!loading && messages.length === 0 && !streamChatEnabled" class="flex items-center justify-center h-full text-gray-500 text-center">
        <div>
          <p class="font-medium">No messages yet</p>
          <p class="text-sm mt-1">Start the conversation!</p>
        </div>
      </div>
    </div>

    <!-- Message Input -->
    <div v-if="!streamChatEnabled" class="border-t border-gray-200 p-4">
      <form @submit.prevent="sendMessage" class="flex gap-2">
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
import { ref, computed, onMounted, nextTick, watch } from 'vue';
import Avatar from './ui/Avatar.vue';
import AvatarImage from './ui/AvatarImage.vue';
import AvatarFallback from './ui/AvatarFallback.vue';
import Button from './ui/Button.vue';
import Input from './ui/Input.vue';
import type { Handshake, AuthorSummary } from '../types';
import { useAppStore } from '../stores/appStore';

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

// Mock messages for demo (replace with Stream Chat)
const messages = ref<Array<{
  id: string;
  text: string;
  isCurrentUser: boolean;
  timestamp: string;
}>>([]);

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

function formatTime(timestamp: string): string {
  const date = new Date(timestamp);
  return date.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' });
}

async function sendMessage() {
  if (!newMessage.value.trim() || sending.value) return;

  sending.value = true;
  
  // Simulate sending (replace with Stream Chat API)
  await new Promise(resolve => setTimeout(resolve, 500));
  
  messages.value.push({
    id: `msg-${Date.now()}`,
    text: newMessage.value,
    isCurrentUser: true,
    timestamp: new Date().toISOString(),
  });

  newMessage.value = '';
  sending.value = false;

  // Scroll to bottom
  await nextTick();
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight;
  }
}

// Initialize Stream Chat when enabled
watch(() => props.streamChatEnabled, (enabled: boolean | undefined) => {
  if (enabled && props.handshake) {
    initializeStreamChat();
  }
});

async function initializeStreamChat() {
  // This is where you'd initialize Stream Chat
  // Example:
  // const chatClient = StreamChat.getInstance('YOUR_API_KEY');
  // const channel = chatClient.channel('messaging', `handshake-${props.handshake.id}`);
  // await channel.watch();
  // 
  // Mount Stream Chat UI component here
  console.log('Stream Chat would be initialized here with handshake:', props.handshake?.id);
}

onMounted(() => {
  if (props.streamChatEnabled && props.handshake) {
    initializeStreamChat();
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

