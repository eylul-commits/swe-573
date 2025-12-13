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
        <div class="px-4 py-3 border-b border-gray-200 flex items-center justify-between">
          <h3 class="text-sm text-gray-700">Conversations</h3>
          <Badge v-if="handshakeStore.handshakes.length" variant="secondary">
            {{ handshakeStore.handshakes.length }}
          </Badge>
        </div>
        
        <!-- Loading State -->
        <div v-if="loading" class="flex items-center justify-center py-8">
          <div class="text-gray-500 text-sm">Loading conversations...</div>
        </div>

        <!-- Handshakes List -->
        <ScrollArea v-else class="flex-1">
          <div class="divide-y divide-gray-100">
            <button
              v-for="handshake in handshakeStore.handshakes"
              :key="handshake.id"
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
                      handshake.status === 'PENDING' ? 'bg-amber-500' :
                      handshake.status === 'CONFIRMED' ? 'bg-green-500' :
                      handshake.status === 'COMPLETED' ? 'bg-blue-500' : 'bg-gray-400'
                    ]"
                  />
                </div>
                
                <div class="flex-1 min-w-0">
                  <div class="flex items-center justify-between mb-1">
                    <span class="text-sm font-medium text-gray-900 truncate">
                      {{ getOtherUser(handshake).name }}
                    </span>
                    <Badge :variant="getStatusVariant(handshake.status)" class="text-xs">
                      {{ handshake.status }}
                    </Badge>
                  </div>
                  
                  <p class="text-xs text-gray-600 truncate mb-1">{{ handshake.offerTitle }}</p>
                  
                  <div class="flex items-center gap-2 text-xs text-gray-500">
                    <HandshakeIcon class="w-3 h-3" />
                    <span>{{ handshake.durationHours }}h</span>
                    <span>•</span>
                    <span>{{ formatDate(handshake.createdAt) }}</span>
                  </div>
                </div>
              </div>
            </button>
          </div>

          <!-- Empty State in List -->
          <div v-if="!loading && handshakeStore.handshakes.length === 0" class="px-4 py-8 text-center">
            <MessageSquare class="w-12 h-12 text-gray-300 mx-auto mb-3" />
            <p class="text-sm text-gray-600">No conversations yet</p>
            <p class="text-xs text-gray-500 mt-1">Accept an offer to start chatting</p>
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
import { ref, onMounted, computed } from 'vue';
import { storeToRefs } from 'pinia';
import { MessageSquare, ArrowLeft, RefreshCw, Handshake as HandshakeIcon } from 'lucide-vue-next';
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

async function refreshHandshakes() {
  loading.value = true;
  try {
    await handshakeStore.loadHandshakes();
    // Sync selectedHandshake with updated store data
    if (selectedHandshake.value) {
      const updatedHandshake = handshakeStore.handshakes.find(
        (h) => h.id === selectedHandshake.value?.id
      );
      if (updatedHandshake) {
        selectedHandshake.value = updatedHandshake;
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
  if (!appStore.currentUser) return handshake.provider;
  
  const currentUserId = appStore.currentUser.id.toString();
  const isSeeker = handshake.seeker.id.toString() === currentUserId;
  
  return isSeeker ? handshake.provider : handshake.seeker;
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

onMounted(() => {
  refreshHandshakes();
});
</script>

