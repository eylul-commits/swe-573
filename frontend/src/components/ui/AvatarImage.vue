<template>
  <img
    v-if="!hasError && src"
    :src="src"
    :alt="alt"
    class="aspect-square h-full w-full object-cover"
    @error="handleError"
    @load="handleLoad"
  />
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'

const props = defineProps<{
  src?: string
  alt?: string
}>()

const hasError = ref(false)

const handleError = () => {
  hasError.value = true
}

const handleLoad = () => {
  hasError.value = false
}

// Reset error state when src changes
watch(() => props.src, () => {
  hasError.value = false
})
</script>

