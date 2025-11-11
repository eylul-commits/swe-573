<template>
  <div class="container mx-auto px-4 py-6 max-w-6xl">
    <div class="flex items-center justify-between mb-6">
      <h1 class="text-3xl font-bold text-gray-900">My Handshakes</h1>
      <Button @click="refreshHandshakes" :disabled="loading" variant="outline">
        <svg v-if="!loading" xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="mr-2"><path d="M21 12a9 9 0 1 1-9-9c2.52 0 4.93 1 6.74 2.74L21 8"></path><path d="M21 3v5h-5"></path></svg>
        {{ loading ? 'Loading...' : 'Refresh' }}
      </Button>
    </div>

    <!-- Tabs for filtering -->
    <Tabs v-model="activeTab" class="mb-6">
      <TabsList>
        <TabsTrigger value="all">
          All ({{ handshakeStore.handshakes.length }})
        </TabsTrigger>
        <TabsTrigger value="pending">
          Pending ({{ handshakeStore.pendingHandshakes.length }})
        </TabsTrigger>
        <TabsTrigger value="confirmed">
          Confirmed ({{ handshakeStore.confirmedHandshakes.length }})
        </TabsTrigger>
        <TabsTrigger value="ratable">
          Can Rate ({{ handshakeStore.ratableHandshakes.length }})
        </TabsTrigger>
        <TabsTrigger value="completed">
          Completed ({{ handshakeStore.completedHandshakes.length }})
        </TabsTrigger>
      </TabsList>

      <!-- All Handshakes -->
      <TabsContent value="all">
        <HandshakeList
          :handshakes="handshakeStore.handshakes"
          @open-chat="openChat"
          @open-confirm="openConfirmModal"
          @open-rating="openRatingModal"
        />
      </TabsContent>

      <!-- Pending -->
      <TabsContent value="pending">
        <HandshakeList
          :handshakes="handshakeStore.pendingHandshakes"
          @open-chat="openChat"
          @open-confirm="openConfirmModal"
          @open-rating="openRatingModal"
        />
      </TabsContent>

      <!-- Confirmed -->
      <TabsContent value="confirmed">
        <HandshakeList
          :handshakes="handshakeStore.confirmedHandshakes"
          @open-chat="openChat"
          @open-confirm="openConfirmModal"
          @open-rating="openRatingModal"
        />
      </TabsContent>

      <!-- Ratable -->
      <TabsContent value="ratable">
        <HandshakeList
          :handshakes="handshakeStore.ratableHandshakes"
          @open-chat="openChat"
          @open-confirm="openConfirmModal"
          @open-rating="openRatingModal"
        />
      </TabsContent>

      <!-- Completed -->
      <TabsContent value="completed">
        <HandshakeList
          :handshakes="handshakeStore.completedHandshakes"
          @open-chat="openChat"
          @open-confirm="openConfirmModal"
          @open-rating="openRatingModal"
        />
      </TabsContent>
    </Tabs>

    <!-- Chat Modal -->
    <Dialog v-model="chatModalOpen">
      <DialogContent class="sm:max-w-[800px] h-[600px] p-0">
        <ChatWindow
          :handshake="selectedHandshake"
          :on-close="() => chatModalOpen = false"
          :stream-chat-enabled="false"
          :show-stream-chat-info="true"
          @open-confirm-modal="openConfirmModalFromChat"
        />
      </DialogContent>
    </Dialog>

    <!-- Confirm Handshake Modal -->
    <ConfirmHandshakeModal
      v-model="confirmModalOpen"
      :handshake="selectedHandshake"
      @confirmed="onHandshakeConfirmed"
    />

    <!-- Rating Modal -->
    <RatingModal
      v-model="ratingModalOpen"
      :handshake="selectedHandshake"
      @rated="onRatingSubmitted"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useHandshakeStore } from '../../stores/handshakeStore';
import { useAppStore } from '../../stores/appStore';
import Button from '../ui/Button.vue';
import Tabs from '../ui/Tabs.vue';
import TabsList from '../ui/TabsList.vue';
import TabsTrigger from '../ui/TabsTrigger.vue';
import TabsContent from '../ui/TabsContent.vue';
import Dialog from '../ui/Dialog.vue';
import DialogContent from '../ui/DialogContent.vue';
import ChatWindow from '../ChatWindow.vue';
import ConfirmHandshakeModal from '../ConfirmHandshakeModal.vue';
import RatingModal from '../RatingModal.vue';
import HandshakeList from '../HandshakeList.vue';
import type { Handshake } from '../../types';

const handshakeStore = useHandshakeStore();
const appStore = useAppStore();

const activeTab = ref('all');
const loading = ref(false);
const selectedHandshake = ref<Handshake | null>(null);
const chatModalOpen = ref(false);
const confirmModalOpen = ref(false);
const ratingModalOpen = ref(false);

async function refreshHandshakes() {
  loading.value = true;
  try {
    await handshakeStore.loadHandshakes();
  } finally {
    loading.value = false;
  }
}

function openChat(handshake: Handshake) {
  selectedHandshake.value = handshake;
  chatModalOpen.value = true;
}

function openConfirmModal(handshake: Handshake) {
  selectedHandshake.value = handshake;
  confirmModalOpen.value = true;
}

function openConfirmModalFromChat() {
  chatModalOpen.value = false;
  confirmModalOpen.value = true;
}

function openRatingModal(handshake: Handshake) {
  selectedHandshake.value = handshake;
  ratingModalOpen.value = true;
}

function onHandshakeConfirmed(handshake: Handshake) {
  console.log('Handshake confirmed:', handshake);
  refreshHandshakes();
}

function onRatingSubmitted(handshake: Handshake) {
  console.log('Rating submitted:', handshake);
  refreshHandshakes();
}

onMounted(() => {
  refreshHandshakes();
});
</script>

