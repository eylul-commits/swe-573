<template>
  <img
    :src="src"
    :alt="alt"
    class="aspect-square h-full w-full object-cover"
  />
</template>

<script setup lang="ts">
defineProps<{
  src?: string
  alt?: string
}>()
</script>

