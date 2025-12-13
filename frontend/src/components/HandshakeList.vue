<template>
  <div class="space-y-4">
    <!-- Empty State -->
    <div v-if="handshakes.length === 0" class="text-center py-12">
      <div class="text-gray-400 text-5xl mb-4">🤝</div>
      <h3 class="text-lg font-semibold text-gray-900 mb-2">No handshakes yet</h3>
      <p class="text-gray-600">Handshakes will appear here when you accept service offers</p>
    </div>

    <!-- Handshake Cards -->
    <Card
      v-for="handshake in handshakes"
      :key="handshake.id"
      class="hover:shadow-md transition-shadow"
    >
      <div class="p-6">
        <div class="flex items-start justify-between mb-4">
          <!-- Service Info -->
          <div class="flex-1">
            <h3 class="text-lg font-semibold text-gray-900 mb-2">
              {{ handshake.offerTitle }}
            </h3>
            <div class="flex items-center gap-4 text-sm text-gray-600">
              <span>{{ handshake.durationHours }} hours</span>
              <span>•</span>
              <span>{{ formatDate(handshake.createdAt) }}</span>
            </div>
          </div>

          <!-- Status Badge -->
          <Badge :variant="getStatusVariant(handshake.status)">
            {{ handshake.status }}
          </Badge>
        </div>

        <!-- Participants -->
        <div class="grid grid-cols-2 gap-4 mb-4">
          <!-- Provider -->
          <div class="flex items-center gap-3">
            <Avatar class="w-10 h-10">
              <AvatarImage :src="getAvatarUrl(handshake.provider.avatar, handshake.provider.name)" :alt="handshake.provider.name" />
              <AvatarFallback>{{ handshake.provider.name.charAt(0) }}</AvatarFallback>
            </Avatar>
            <div class="flex-1 min-w-0">
              <p class="text-sm font-medium text-gray-900 truncate">
                {{ handshake.provider.name }}
              </p>
              <p class="text-xs text-gray-500">Provider</p>
            </div>
            <div v-if="handshake.providerConfirmed" class="text-green-600 text-sm">✓</div>
          </div>

          <!-- Seeker -->
          <div class="flex items-center gap-3">
            <Avatar class="w-10 h-10">
              <AvatarImage :src="getAvatarUrl(handshake.seeker.avatar, handshake.seeker.name)" :alt="handshake.seeker.name" />
              <AvatarFallback>{{ handshake.seeker.name.charAt(0) }}</AvatarFallback>
            </Avatar>
            <div class="flex-1 min-w-0">
              <p class="text-sm font-medium text-gray-900 truncate">
                {{ handshake.seeker.name }}
              </p>
              <p class="text-xs text-gray-500">Seeker</p>
            </div>
            <div v-if="handshake.seekerConfirmed" class="text-green-600 text-sm">✓</div>
          </div>
        </div>

        <!-- Agreed Date -->
        <div v-if="handshake.agreedDate" class="bg-gray-50 rounded-lg p-3 mb-4">
          <p class="text-sm text-gray-700">
            <span class="font-medium">Agreed Date:</span>
            {{ formatDateTime(handshake.agreedDate) }}
          </p>
        </div>

        <!-- Action Buttons -->
        <div class="flex gap-2 flex-wrap">
          <!-- Chat Button -->
          <Button
            @click="$emit('openChat', handshake)"
            variant="outline"
            size="sm"
            class="flex-1 min-w-[100px]"
          >
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="mr-2"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path></svg>
            Chat
          </Button>

          <!-- Cancel Button (only if pending and not both confirmed) -->
          <Button
            v-if="isCancelable(handshake)"
            @click="handleCancel(handshake)"
            variant="outline"
            size="sm"
            :disabled="cancelingHandshakeId === handshake.id"
            class="flex-1 min-w-[100px] text-red-600 border-red-300 hover:bg-red-50 hover:text-red-700"
          >
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="mr-2"><line x1="18" y1="6" x2="6" y2="18"></line><line x1="6" y1="6" x2="18" y2="18"></line></svg>
            {{ cancelingHandshakeId === handshake.id ? 'Canceling...' : 'Cancel' }}
          </Button>

          <!-- Confirm Button (only if pending and not confirmed by current user) -->
          <Button
            v-if="handshake.status === 'PENDING' && !isCurrentUserConfirmed(handshake)"
            @click="$emit('openConfirm', handshake)"
            class="flex-1 min-w-[100px] bg-amber-500 hover:bg-amber-600"
            size="sm"
          >
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="mr-2"><polyline points="20 6 9 17 4 12"></polyline></svg>
            Confirm
          </Button>

          <!-- Rate Button (only if can rate) -->
          <Button
            v-if="handshake.canRate"
            @click="$emit('openRating', handshake)"
            class="flex-1 min-w-[100px] bg-green-600 hover:bg-green-700"
            size="sm"
          >
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="mr-2"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon></svg>
            Rate
          </Button>
        </div>
      </div>
    </Card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import Card from './ui/Card.vue';
import Badge from './ui/Badge.vue';
import Button from './ui/Button.vue';
import Avatar from './ui/Avatar.vue';
import AvatarImage from './ui/AvatarImage.vue';
import AvatarFallback from './ui/AvatarFallback.vue';
import type { Handshake } from '../types';
import { useAppStore } from '../stores/appStore';
import { useHandshakeStore } from '../stores/handshakeStore';
import { getAvatarUrl } from '../utils/avatarUtils';

defineProps<{
  handshakes: Handshake[];
}>();

const emit = defineEmits<{
  'openChat': [handshake: Handshake];
  'openConfirm': [handshake: Handshake];
  'openRating': [handshake: Handshake];
  'cancelled': [handshake: Handshake];
}>();

const appStore = useAppStore();
const handshakeStore = useHandshakeStore();

const cancelingHandshakeId = ref<number | null>(null);

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
  return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
}

function formatDateTime(dateString: string): string {
  const date = new Date(dateString);
  return date.toLocaleString('en-US', {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
}

function isCurrentUserConfirmed(handshake: Handshake): boolean {
  if (!appStore.currentUser) return false;
  
  const currentUserId = appStore.currentUser.id.toString();
  const isSeeker = handshake.seeker.id.toString() === currentUserId;
  
  return isSeeker ? handshake.seekerConfirmed : handshake.providerConfirmed;
}

function isCancelable(handshake: Handshake): boolean {
  // Can only cancel if PENDING and not both confirmed
  const isPending = handshake.status === 'PENDING';
  const bothConfirmed = handshake.seekerConfirmed && handshake.providerConfirmed;
  const cancelable = isPending && !bothConfirmed;
  
  // Debug logging
  console.log('Handshake cancelability check:', {
    id: handshake.id,
    status: handshake.status,
    isPending,
    seekerConfirmed: handshake.seekerConfirmed,
    providerConfirmed: handshake.providerConfirmed,
    bothConfirmed,
    cancelable
  });
  
  return cancelable;
}

async function handleCancel(handshake: Handshake) {
  if (!confirm('Are you sure you want to cancel this handshake? This action cannot be undone.')) {
    return;
  }

  cancelingHandshakeId.value = handshake.id;
  try {
    const cancelledHandshake = await handshakeStore.cancelHandshake(handshake.id);
    emit('cancelled', cancelledHandshake);
  } catch (error) {
    console.error('Failed to cancel handshake:', error);
    alert(error instanceof Error ? error.message : 'Failed to cancel handshake');
  } finally {
    cancelingHandshakeId.value = null;
  }
}
</script>

