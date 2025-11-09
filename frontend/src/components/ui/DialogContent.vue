<template>
  <div
    :class="cn(
      'bg-white rounded-lg shadow-xl max-w-lg w-full mx-4 max-h-[90vh] overflow-y-auto',
      $attrs.class as string
    )"
  >
    <slot />
  </div>
</template>

<script setup lang="ts">
import { cn } from '../../utils/cn'
</script>

