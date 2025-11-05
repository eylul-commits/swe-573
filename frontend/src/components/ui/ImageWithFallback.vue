<template>
  <img
    :src="currentSrc"
    :alt="alt"
    :class="className"
    @error="handleError"
  />
</template>

<script setup lang="ts">
import { ref } from 'vue'

const props = withDefaults(
  defineProps<{
    src: string
    alt: string
    className?: string
    fallback?: string
  }>(),
  {
    fallback: 'https://images.unsplash.com/photo-1582213782179-e0d53f98f2ca?w=400&h=300&fit=crop',
  }
)

const currentSrc = ref(props.src)

const handleError = () => {
  if (currentSrc.value !== props.fallback) {
    currentSrc.value = props.fallback
  }
}
</script>

