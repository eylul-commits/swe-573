<template>
  <div class="flex-1 flex flex-col bg-white">
    <!-- Header -->
    <div class="border-b border-gray-200 px-6 py-4">
      <div class="flex items-center justify-between">
        <div>
          <h1 class="text-gray-900 flex items-center gap-2">
            <MessageSquare class="w-6 h-6" />
            Messages
          </h1>
          <p class="text-sm text-gray-600 mt-1">
            Chat with your handshake partners
          </p>
        </div>
        <Button @click="refreshHandshakes" :disabled="loading" variant="outline" size="sm">
          <RefreshCw :class="['w-4 h-4', loading ? 'animate-spin' : '']" />
        </Button>
      </div>
    </div>

    <div class="flex-1 flex overflow-hidden">
      <!-- Handshakes List (Conversations) -->
      <div :class="[selectedHandshake ? 'hidden md:block' : 'block', 'w-full md:w-80 border-r border-gray-200 flex flex-col']">
        <div class="px-4 py-3 border-b border-gray-200">
          <div class="flex items-center justify-between mb-2">
            <h3 class="text-sm text-gray-700">Conversations</h3>
            <Badge v-if="filteredHandshakes.length" variant="secondary">
              {{ filteredHandshakes.length }}
            </Badge>
          </div>
          <div class="flex items-center gap-2">
            <Button
              @click="toggleHideCompleted"
              variant="ghost"
              size="sm"
              class="text-xs h-7 px-2"
            >
              <EyeOff v-if="hideCompleted" class="w-3 h-3 mr-1" />
              <Eye v-else class="w-3 h-3 mr-1" />
              {{ hideCompleted ? 'Show completed/canceled' : 'Hide completed/canceled' }}
            </Button>
          </div>
        </div>
        
        <!-- Loading State -->
        <div v-if="loading" class="flex items-center justify-center py-8">
          <div class="text-gray-500 text-sm">Loading conversations...</div>
        </div>

        <!-- Handshakes List -->
        <ScrollArea v-else class="flex-1">
          <div class="divide-y divide-gray-100">
            <template v-for="handshake in filteredHandshakes" :key="handshake?.id">
              <button
                v-if="handshake && handshake.id"
                @click="selectHandshake(handshake)"
                :class="[
                  'w-full px-4 py-3 hover:bg-gray-50 transition-colors text-left',
                  selectedHandshake?.id === handshake.id ? 'bg-amber-50' : ''
                ]"
              >
              <div class="flex items-start gap-3">
                <div class="relative">
                  <Avatar class="w-12 h-12">
                    <AvatarImage :src="getAvatarUrl(getOtherUser(handshake).avatar, getOtherUser(handshake).name)" :alt="getOtherUser(handshake).name" />
                    <AvatarFallback>{{ getOtherUser(handshake).name.charAt(0) }}</AvatarFallback>
                  </Avatar>
                  <!-- Status Indicator -->
                  <div 
                    :class="[
                      'absolute -bottom-1 -right-1 w-4 h-4 rounded-full border-2 border-white',
                      handshake?.status === 'PENDING' ? 'bg-amber-500' :
                      handshake?.status === 'CONFIRMED' ? 'bg-green-500' :
                      handshake?.status === 'COMPLETED' ? 'bg-blue-500' : 'bg-gray-400'
                    ]"
                  />
                </div>
                
                <div class="flex-1 min-w-0">
                  <div class="flex items-center justify-between mb-1">
                    <span class="text-sm font-medium text-gray-900 truncate">
                      {{ getOtherUser(handshake)?.name || 'Unknown' }}
                    </span>
                    <Badge :variant="getStatusVariant(handshake?.status || '')" class="text-xs">
                      {{ handshake?.status || '' }}
                    </Badge>
                  </div>
                  
                  <p class="text-xs text-gray-600 truncate mb-1">{{ handshake?.offerTitle || '' }}</p>
                  
                  <div class="flex items-center gap-2 text-xs text-gray-500">
                    <HandshakeIcon class="w-3 h-3" />
                    <span>{{ handshake?.durationHours || 0 }}h</span>
                    <span>•</span>
                    <span>{{ handshake?.createdAt ? formatDate(handshake.createdAt) : '' }}</span>
                  </div>
                </div>
              </div>
              </button>
            </template>
          </div>

          <!-- Empty State in List -->
          <div v-if="!loading && filteredHandshakes.length === 0" class="px-4 py-8 text-center">
            <MessageSquare class="w-12 h-12 text-gray-300 mx-auto mb-3" />
            <p class="text-sm text-gray-600">
              {{ hideCompleted && handshakeStore.handshakes.length > 0 
                ? 'No active conversations' 
                : 'No conversations yet' }}
            </p>
            <p class="text-xs text-gray-500 mt-1">
              {{ hideCompleted && handshakeStore.handshakes.length > 0
                ? 'All conversations are completed or canceled'
                : 'Accept an offer to start chatting' }}
            </p>
          </div>
        </ScrollArea>
      </div>

      <!-- Chat Area -->
      <div v-if="selectedHandshake" class="flex-1 flex flex-col">
        <!-- Mobile Back Button -->
        <div class="md:hidden px-4 py-3 border-b border-gray-200">
          <Button
            variant="ghost"
            size="sm"
            @click="selectedHandshake = null"
          >
            <ArrowLeft class="w-4 h-4 mr-2" />
            Back to conversations
          </Button>
        </div>

        <!-- ChatWindow Component -->
        <ChatWindow
          :handshake="selectedHandshake"
          :stream-chat-enabled="streamChatEnabled"
          :show-stream-chat-info="false"
          @open-confirm-modal="openConfirmModal"
          @handshake-cancelled="onHandshakeCancelled"
        />
      </div>

      <!-- Empty State -->
      <div v-else class="hidden md:flex flex-1 items-center justify-center bg-gray-50">
        <div class="text-center">
          <MessageSquare class="w-16 h-16 text-gray-300 mx-auto mb-4" />
          <h3 class="text-lg font-semibold text-gray-900 mb-2">Select a conversation</h3>
          <p class="text-sm text-gray-600">
            Choose a handshake to start messaging
          </p>
        </div>
      </div>
    </div>

    <!-- Confirm Handshake Modal -->
    <ConfirmHandshakeModal
      v-model="confirmModalOpen"
      :handshake="selectedHandshake"
      @confirmed="onHandshakeConfirmed"
    />

    <!-- Rating Modal (if enabled from chat) -->
    <RatingModal
      v-if="ratingModalOpen"
      v-model="ratingModalOpen"
      :handshake="selectedHandshake"
      @rated="onRatingSubmitted"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue';
import { storeToRefs } from 'pinia';
import { MessageSquare, ArrowLeft, RefreshCw, Handshake as HandshakeIcon, Eye, EyeOff } from 'lucide-vue-next';
import Avatar from '../ui/Avatar.vue';
import AvatarImage from '../ui/AvatarImage.vue';
import AvatarFallback from '../ui/AvatarFallback.vue';
import Button from '../ui/Button.vue';
import Badge from '../ui/Badge.vue';
import ScrollArea from '../ui/ScrollArea.vue';
import ChatWindow from '../ChatWindow.vue';
import ConfirmHandshakeModal from '../ConfirmHandshakeModal.vue';
import RatingModal from '../RatingModal.vue';
import { useHandshakeStore } from '../../stores/handshakeStore';
import { useAppStore } from '../../stores/appStore';
import { isStreamChatInitialized } from '../../clients/streamChatClient';
import { getAvatarUrl } from '../../utils/avatarUtils';
import type { Handshake, AuthorSummary } from '../../types';

const handshakeStore = useHandshakeStore();
const appStore = useAppStore();
const { streamChatReady } = storeToRefs(appStore);

const selectedHandshake = ref<Handshake | null>(null);
const loading = ref(false);
const confirmModalOpen = ref(false);
const ratingModalOpen = ref(false);
const streamChatEnabled = computed(() => streamChatReady.value && isStreamChatInitialized());

// Filter for hiding completed/canceled conversations
const HIDE_COMPLETED_KEY = 'messaging-hide-completed';
const getInitialHideCompleted = (): boolean => {
  const stored = localStorage.getItem(HIDE_COMPLETED_KEY);
  return stored === 'true';
};
const hideCompleted = ref<boolean>(getInitialHideCompleted());

// Filtered handshakes based on hideCompleted preference
const filteredHandshakes = computed(() => {
  if (!handshakeStore.handshakes || handshakeStore.handshakes.length === 0) {
    return [];
  }
  if (!hideCompleted.value) {
    return [...handshakeStore.handshakes];
  }
  return handshakeStore.handshakes.filter(
    (h) => h && h.status !== 'COMPLETED' && h.status !== 'CANCELLED'
  );
});

function toggleHideCompleted() {
  hideCompleted.value = !hideCompleted.value;
  localStorage.setItem(HIDE_COMPLETED_KEY, hideCompleted.value.toString());
  
  // If the selected handshake is now hidden, clear selection
  if (hideCompleted.value && selectedHandshake.value) {
    const isHidden = selectedHandshake.value.status === 'COMPLETED' || 
                     selectedHandshake.value.status === 'CANCELLED';
    if (isHidden) {
      selectedHandshake.value = null;
    }
  }
}

async function refreshHandshakes() {
  loading.value = true;
  try {
    await handshakeStore.loadHandshakes();
    // Sync selectedHandshake with updated store data
    if (selectedHandshake.value && handshakeStore.handshakes) {
      const updatedHandshake = handshakeStore.handshakes.find(
        (h) => h && h.id === selectedHandshake.value?.id
      );
      if (updatedHandshake) {
        selectedHandshake.value = updatedHandshake;
      } else {
        // If the selected handshake no longer exists, clear selection
        selectedHandshake.value = null;
      }
    }
  } finally {
    loading.value = false;
  }
}

function selectHandshake(handshake: Handshake) {
  selectedHandshake.value = handshake;
}

function getOtherUser(handshake: Handshake): AuthorSummary {
  if (!handshake || !appStore.currentUser) return handshake?.provider || { id: '', name: 'Unknown', avatar: '' };
  
  const currentUserId = appStore.currentUser.id.toString();
  const isSeeker = handshake.seeker?.id?.toString() === currentUserId;
  
  return isSeeker ? (handshake.provider || { id: '', name: 'Unknown', avatar: '' }) : (handshake.seeker || { id: '', name: 'Unknown', avatar: '' });
}

function getStatusVariant(status: string): 'default' | 'success' | 'warning' | 'error' {
  switch (status) {
    case 'COMPLETED':
      return 'success';
    case 'CONFIRMED':
      return 'default';
    case 'PENDING':
      return 'warning';
    case 'CANCELLED':
      return 'error';
    default:
      return 'default';
  }
}

function formatDate(dateString: string): string {
  const date = new Date(dateString);
  const now = new Date();
  const diffMs = now.getTime() - date.getTime();
  const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));
  
  if (diffDays === 0) return 'Today';
  if (diffDays === 1) return 'Yesterday';
  if (diffDays < 7) return `${diffDays}d ago`;
  
  return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
}

function openConfirmModal() {
  confirmModalOpen.value = true;
}

function onHandshakeConfirmed(handshake: Handshake) {
  selectedHandshake.value = handshake;
  refreshHandshakes();
}

function onRatingSubmitted(handshake: Handshake) {
  selectedHandshake.value = handshake;
  refreshHandshakes();
}

function onHandshakeCancelled(handshake: Handshake) {
  selectedHandshake.value = handshake;
  refreshHandshakes();
}

onMounted(() => {
  refreshHandshakes();
});

onUnmounted(() => {
  // Clean up selected handshake to prevent errors during unmounting
  selectedHandshake.value = null;
});
</script>

