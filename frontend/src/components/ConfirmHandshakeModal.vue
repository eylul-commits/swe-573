<template>
  <Dialog v-model="isOpen">
    <DialogContent class="sm:max-w-[500px]">
      <DialogHeader>
        <DialogTitle>Confirm Handshake</DialogTitle>
        <DialogDescription>
          {{ isServiceCreator ? 'Set a completion date for this service exchange. The other party will confirm with the same date.' : (handshake?.agreedDate ? 'Confirm the completion date set by the service creator.' : 'The service creator will set the completion date. Please wait for them to select a date.') }}
        </DialogDescription>
      </DialogHeader>

      <div class="px-6">
        <div class="space-y-4 py-4">
        <!-- Handshake Info -->
        <div v-if="handshake" class="bg-gray-50 p-4 rounded-lg">
          <h3 class="font-semibold text-sm text-gray-900 mb-2">{{ handshake.offerTitle }}</h3>
          <div class="text-sm text-gray-600 space-y-1">
            <p><span class="font-medium">Provider:</span> {{ handshake.provider.name }}</p>
            <p><span class="font-medium">Seeker:</span> {{ handshake.seeker.name }}</p>
            <p><span class="font-medium">Duration Hours:</span> {{ handshake.durationHours }} hours</p>
          </div>
        </div>

        <!-- Confirmation Status -->
        <div class="bg-amber-50 border border-amber-200 rounded-lg p-3">
          <div class="flex items-center gap-2 text-sm">
            <div :class="handshake?.seekerConfirmed ? 'text-green-600' : 'text-gray-400'">
              {{ handshake?.seekerConfirmed ? '✓' : '○' }} Seeker confirmed
            </div>
            <div :class="handshake?.providerConfirmed ? 'text-green-600' : 'text-gray-400'">
              {{ handshake?.providerConfirmed ? '✓' : '○' }} Provider confirmed
            </div>
          </div>
        </div>

        <!-- Date Picker -->
        <div class="space-y-2">
          <label for="completion-date" class="text-sm font-medium text-gray-900">
            Completion Date & Time
          </label>
          <Input
            id="completion-date"
            v-model="completionDate"
            type="datetime-local"
            :min="minDate"
            :disabled="!isServiceCreator"
            required
          />
          <p v-if="isServiceCreator" class="text-xs text-gray-500">
            Choose when the service will be completed. You'll be able to rate each other after this date.
          </p>
          <p v-else-if="handshake?.agreedDate" class="text-xs text-gray-500">
            This date was set by the service creator. You can confirm the handshake with this date.
          </p>
          <p v-else class="text-xs text-gray-500">
            Only the service creator can select the completion date. Please wait for them to set it.
          </p>
        </div>

        <!-- Error Message -->
        <div v-if="error" class="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded text-sm">
          {{ error }}
        </div>
        </div>
      </div>

      <DialogFooter>
        <Button
          variant="outline"
          @click="onCancel"
          :disabled="loading"
        >
          Cancel
        </Button>
        <Button
          @click="onConfirm"
          :disabled="loading || !completionDate"
          class="bg-amber-500 hover:bg-amber-600"
        >
          {{ loading ? 'Confirming...' : 'Confirm Handshake' }}
        </Button>
      </DialogFooter>
    </DialogContent>
  </Dialog>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue';
import Dialog from './ui/Dialog.vue';
import DialogContent from './ui/DialogContent.vue';
import DialogHeader from './ui/DialogHeader.vue';
import DialogTitle from './ui/DialogTitle.vue';
import DialogDescription from './ui/DialogDescription.vue';
import DialogFooter from './ui/DialogFooter.vue';
import Button from './ui/Button.vue';
import Input from './ui/Input.vue';
import type { Handshake } from '../types';
import { useHandshakeStore } from '../stores/handshakeStore';
import { useAppStore } from '../stores/appStore';

const props = defineProps<{
  modelValue: boolean;
  handshake: Handshake | null;
}>();

const emit = defineEmits<{
  'update:modelValue': [value: boolean];
  'confirmed': [handshake: Handshake];
}>();

const handshakeStore = useHandshakeStore();
const appStore = useAppStore();

const isOpen = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value),
});

const completionDate = ref('');
const loading = ref(false);
const error = ref<string | null>(null);

// Check if current user is the service creator (provider)
const isServiceCreator = computed(() => {
  if (!props.handshake || !appStore.currentUser) return false;
  return props.handshake.provider.id == appStore.currentUser.id.toString();
});

// Calculate minimum date (today)
const minDate = computed(() => {
  const today = new Date();
  return today.toISOString().slice(0, 16);
});

// Reset form when modal opens
watch(() => props.modelValue, (newValue) => {
  if (newValue) {
    // If there's already an agreed date, use it (for both creator and non-creator)
    if (props.handshake?.agreedDate) {
      const date = new Date(props.handshake.agreedDate);
      completionDate.value = date.toISOString().slice(0, 16);
    } else {
      completionDate.value = '';
    }
    error.value = null;
    loading.value = false;
  }
});

async function onConfirm() {
  if (!props.handshake || !completionDate.value) return;

  loading.value = true;
  error.value = null;

  try {
    const dateString = completionDate.value.length === 16 
      ? `${completionDate.value}:00` 
      : completionDate.value;
    
    const updatedHandshake = await handshakeStore.confirmHandshake(props.handshake.id, {
      agreedDate: dateString,
    });

    emit('confirmed', updatedHandshake);
    isOpen.value = false;
  } catch (e) {
    error.value = e instanceof Error ? e.message : 'Failed to confirm handshake';
  } finally {
    loading.value = false;
  }
}

function onCancel() {
  isOpen.value = false;
}
</script>

