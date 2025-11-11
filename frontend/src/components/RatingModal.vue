<template>
  <Dialog v-model="isOpen">
    <DialogContent class="sm:max-w-[600px]">
      <DialogHeader>
        <DialogTitle>Rate Service Exchange</DialogTitle>
        <DialogDescription>
          Share your experience with {{ otherUser?.name }}
        </DialogDescription>
      </DialogHeader>

      <div class="space-y-6 py-4">
        <!-- User Info -->
        <div v-if="otherUser" class="flex items-center gap-3 bg-gray-50 p-4 rounded-lg">
          <Avatar class="w-12 h-12">
            <AvatarImage :src="otherUser.avatar" :alt="otherUser.name" />
            <AvatarFallback>{{ otherUser.name.charAt(0) }}</AvatarFallback>
          </Avatar>
          <div>
            <h3 class="font-semibold text-gray-900">{{ otherUser.name }}</h3>
            <p v-if="handshake" class="text-sm text-gray-600">{{ handshake.offerTitle }}</p>
          </div>
        </div>

        <!-- Rating Categories -->
        <div class="space-y-4">
          <!-- Punctuality -->
          <div>
            <label class="text-sm font-medium text-gray-900 mb-2 block">
              Punctuality
            </label>
            <div class="flex gap-2">
              <button
                v-for="star in 5"
                :key="`punctuality-${star}`"
                @click="ratings.punctuality = star"
                class="text-2xl transition-colors"
                :class="star <= ratings.punctuality ? 'text-amber-500' : 'text-gray-300'"
              >
                ★
              </button>
            </div>
          </div>

          <!-- Friendliness -->
          <div>
            <label class="text-sm font-medium text-gray-900 mb-2 block">
              Friendliness
            </label>
            <div class="flex gap-2">
              <button
                v-for="star in 5"
                :key="`friendliness-${star}`"
                @click="ratings.friendliness = star"
                class="text-2xl transition-colors"
                :class="star <= ratings.friendliness ? 'text-amber-500' : 'text-gray-300'"
              >
                ★
              </button>
            </div>
          </div>

          <!-- Communicative -->
          <div>
            <label class="text-sm font-medium text-gray-900 mb-2 block">
              Communication
            </label>
            <div class="flex gap-2">
              <button
                v-for="star in 5"
                :key="`communicative-${star}`"
                @click="ratings.communicative = star"
                class="text-2xl transition-colors"
                :class="star <= ratings.communicative ? 'text-amber-500' : 'text-gray-300'"
              >
                ★
              </button>
            </div>
          </div>

          <!-- Preparedness -->
          <div>
            <label class="text-sm font-medium text-gray-900 mb-2 block">
              Preparedness
            </label>
            <div class="flex gap-2">
              <button
                v-for="star in 5"
                :key="`preparedness-${star}`"
                @click="ratings.preparedness = star"
                class="text-2xl transition-colors"
                :class="star <= ratings.preparedness ? 'text-amber-500' : 'text-gray-300'"
              >
                ★
              </button>
            </div>
          </div>
        </div>

        <!-- Comment -->
        <div class="space-y-2">
          <label for="comment" class="text-sm font-medium text-gray-900">
            Comment (Optional)
          </label>
          <Textarea
            id="comment"
            v-model="ratings.comment"
            placeholder="Share your experience..."
            rows="4"
            class="resize-none"
          />
        </div>

        <!-- Error Message -->
        <div v-if="error" class="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded text-sm">
          {{ error }}
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
          @click="onSubmit"
          :disabled="loading || !isFormValid"
          class="bg-amber-500 hover:bg-amber-600"
        >
          {{ loading ? 'Submitting...' : 'Submit Rating' }}
        </Button>
      </DialogFooter>
    </DialogContent>
  </Dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import Dialog from './ui/Dialog.vue';
import DialogContent from './ui/DialogContent.vue';
import DialogHeader from './ui/DialogHeader.vue';
import DialogTitle from './ui/DialogTitle.vue';
import DialogDescription from './ui/DialogDescription.vue';
import DialogFooter from './ui/DialogFooter.vue';
import Button from './ui/Button.vue';
import Textarea from './ui/Textarea.vue';
import Avatar from './ui/Avatar.vue';
import AvatarImage from './ui/AvatarImage.vue';
import AvatarFallback from './ui/AvatarFallback.vue';
import type { Handshake, AuthorSummary } from '../types';
import { useHandshakeStore } from '../stores/handshakeStore';
import { useAppStore } from '../stores/appStore';

const props = defineProps<{
  modelValue: boolean;
  handshake: Handshake | null;
}>();

const emit = defineEmits<{
  'update:modelValue': [value: boolean];
  'rated': [handshake: Handshake];
}>();

const handshakeStore = useHandshakeStore();
const appStore = useAppStore();

const isOpen = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value),
});

const ratings = ref({
  punctuality: 0,
  friendliness: 0,
  communicative: 0,
  preparedness: 0,
  comment: '',
});

const loading = ref(false);
const error = ref<string | null>(null);

// Determine who to rate (the other user)
const otherUser = computed<AuthorSummary | null>(() => {
  if (!props.handshake || !appStore.currentUser) return null;
  
  const currentUserId = appStore.currentUser.id.toString();
  const isSeeker = props.handshake.seeker.id.toString() === currentUserId;
  
  return isSeeker ? props.handshake.provider : props.handshake.seeker;
});

// Check if form is valid (all ratings must be > 0)
const isFormValid = computed(() => {
  return ratings.value.punctuality > 0 &&
         ratings.value.friendliness > 0 &&
         ratings.value.communicative > 0 &&
         ratings.value.preparedness > 0;
});

// Reset form when modal opens
watch(() => props.modelValue, (newValue) => {
  if (newValue) {
    ratings.value = {
      punctuality: 0,
      friendliness: 0,
      communicative: 0,
      preparedness: 0,
      comment: '',
    };
    error.value = null;
    loading.value = false;
  }
});

async function onSubmit() {
  if (!props.handshake || !otherUser.value || !isFormValid.value) return;

  loading.value = true;
  error.value = null;

  try {
    const updatedHandshake = await handshakeStore.createRating({
      handshakeId: props.handshake.id,
      rateeId: Number(otherUser.value.id),
      punctuality: ratings.value.punctuality,
      friendliness: ratings.value.friendliness,
      communicative: ratings.value.communicative,
      preparedness: ratings.value.preparedness,
      comment: ratings.value.comment || undefined,
    });

    emit('rated', updatedHandshake);
    isOpen.value = false;
  } catch (e) {
    error.value = e instanceof Error ? e.message : 'Failed to submit rating';
  } finally {
    loading.value = false;
  }
}

function onCancel() {
  isOpen.value = false;
}
</script>

<style scoped>
button:focus-visible {
  outline: 2px solid #f59e0b;
  outline-offset: 2px;
}
</style>

