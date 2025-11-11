/**
 * Handshake Store
 * 
 * Manages state for handshakes (service exchanges)
 */

import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import type { Handshake } from '../types';
import {
  getUserHandshakes,
  getPendingHandshakes,
  getConfirmedHandshakes,
  createHandshake as createHandshakeAPI,
  confirmHandshake as confirmHandshakeAPI,
  createRating as createRatingAPI,
  getHandshakeById,
} from '../services/handshakeService';
import type {
  CreateHandshakeRequest,
  ConfirmHandshakeRequest,
  CreateRatingRequest,
} from '../types';

export const useHandshakeStore = defineStore('handshake', () => {
  // State
  const handshakes = ref<Handshake[]>([]);
  const selectedHandshake = ref<Handshake | null>(null);
  const loading = ref(false);
  const error = ref<string | null>(null);

  // Computed
  const pendingHandshakes = computed(() =>
    handshakes.value.filter((h) => h.status === 'PENDING')
  );

  const confirmedHandshakes = computed(() =>
    handshakes.value.filter((h) => h.status === 'CONFIRMED')
  );

  const completedHandshakes = computed(() =>
    handshakes.value.filter((h) => h.status === 'COMPLETED')
  );

  const ratableHandshakes = computed(() =>
    handshakes.value.filter((h) => h.canRate)
  );

  // Actions
  async function loadHandshakes() {
    loading.value = true;
    error.value = null;
    try {
      handshakes.value = await getUserHandshakes();
    } catch (e) {
      error.value = 'Failed to load handshakes';
      console.error('Error loading handshakes:', e);
    } finally {
      loading.value = false;
    }
  }

  async function loadPendingHandshakes() {
    loading.value = true;
    error.value = null;
    try {
      const pending = await getPendingHandshakes();
      // Update handshakes array with pending ones
      pending.forEach((h) => {
        const index = handshakes.value.findIndex((existing) => existing.id === h.id);
        if (index !== -1) {
          handshakes.value[index] = h;
        } else {
          handshakes.value.push(h);
        }
      });
    } catch (e) {
      error.value = 'Failed to load pending handshakes';
      console.error('Error loading pending handshakes:', e);
    } finally {
      loading.value = false;
    }
  }

  async function loadConfirmedHandshakes() {
    loading.value = true;
    error.value = null;
    try {
      const confirmed = await getConfirmedHandshakes();
      confirmed.forEach((h) => {
        const index = handshakes.value.findIndex((existing) => existing.id === h.id);
        if (index !== -1) {
          handshakes.value[index] = h;
        } else {
          handshakes.value.push(h);
        }
      });
    } catch (e) {
      error.value = 'Failed to load confirmed handshakes';
      console.error('Error loading confirmed handshakes:', e);
    } finally {
      loading.value = false;
    }
  }

  async function createHandshake(request: CreateHandshakeRequest): Promise<Handshake> {
    loading.value = true;
    error.value = null;
    try {
      const handshake = await createHandshakeAPI(request);
      handshakes.value.push(handshake);

      // Auto-create Stream Chat channel
      try {
        const { isStreamChatInitialized, createHandshakeChannel } = await import('../services/streamChatService');
        if (isStreamChatInitialized()) {
          await createHandshakeChannel(handshake);
          console.log('Stream Chat channel created for handshake:', handshake.id);
        }
      } catch (chatError) {
        console.error('Failed to create Stream Chat channel:', chatError);
        // Don't fail the handshake creation if chat fails
      }

      return handshake;
    } catch (e) {
      error.value = 'Failed to create handshake';
      console.error('Error creating handshake:', e);
      throw e;
    } finally {
      loading.value = false;
    }
  }

  async function confirmHandshake(
    handshakeId: number,
    request: ConfirmHandshakeRequest
  ): Promise<Handshake> {
    loading.value = true;
    error.value = null;
    try {
      const handshake = await confirmHandshakeAPI(handshakeId, request);
      // Update in store
      const index = handshakes.value.findIndex((h) => h.id === handshakeId);
      if (index !== -1) {
        handshakes.value[index] = handshake;
      }
      if (selectedHandshake.value?.id === handshakeId) {
        selectedHandshake.value = handshake;
      }

      // Update Stream Chat channel status
      try {
        const { isStreamChatInitialized, updateChannelStatus } = await import('../services/streamChatService');
        if (isStreamChatInitialized() && handshake.status === 'CONFIRMED') {
          await updateChannelStatus(handshakeId, 'CONFIRMED');
        }
      } catch (chatError) {
        console.error('Failed to update Stream Chat channel:', chatError);
      }

      return handshake;
    } catch (e) {
      error.value = 'Failed to confirm handshake';
      console.error('Error confirming handshake:', e);
      throw e;
    } finally {
      loading.value = false;
    }
  }

  async function createRating(request: CreateRatingRequest): Promise<Handshake> {
    loading.value = true;
    error.value = null;
    try {
      const handshake = await createRatingAPI(request);
      // Update in store
      const index = handshakes.value.findIndex((h) => h.id === request.handshakeId);
      if (index !== -1) {
        handshakes.value[index] = handshake;
      }
      if (selectedHandshake.value?.id === request.handshakeId) {
        selectedHandshake.value = handshake;
      }
      return handshake;
    } catch (e) {
      error.value = 'Failed to submit rating';
      console.error('Error creating rating:', e);
      throw e;
    } finally {
      loading.value = false;
    }
  }

  async function loadHandshakeById(handshakeId: number) {
    loading.value = true;
    error.value = null;
    try {
      const handshake = await getHandshakeById(handshakeId);
      selectedHandshake.value = handshake;
      // Update in store if exists
      const index = handshakes.value.findIndex((h) => h.id === handshakeId);
      if (index !== -1) {
        handshakes.value[index] = handshake;
      } else {
        handshakes.value.push(handshake);
      }
      return handshake;
    } catch (e) {
      error.value = 'Failed to load handshake';
      console.error('Error loading handshake:', e);
      throw e;
    } finally {
      loading.value = false;
    }
  }

  function setSelectedHandshake(handshake: Handshake | null) {
    selectedHandshake.value = handshake;
  }

  function clearError() {
    error.value = null;
  }

  return {
    // State
    handshakes,
    selectedHandshake,
    loading,
    error,

    // Computed
    pendingHandshakes,
    confirmedHandshakes,
    completedHandshakes,
    ratableHandshakes,

    // Actions
    loadHandshakes,
    loadPendingHandshakes,
    loadConfirmedHandshakes,
    createHandshake,
    confirmHandshake,
    createRating,
    loadHandshakeById,
    setSelectedHandshake,
    clearError,
  };
});

