<template>
  <div class="text-center py-12">
    <MessageSquare class="w-12 h-12 text-gray-300 mx-auto mb-3" />
    <p class="text-gray-500">{{ message }}</p>
  </div>
</template>

<script setup lang="ts">
import { MessageSquare } from 'lucide-vue-next'

withDefaults(
  defineProps<{
    message?: string
  }>(),
  {
    message: 'No items found',
  }
)
</script>

