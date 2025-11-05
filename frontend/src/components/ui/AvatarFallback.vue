<template>
  <div :class="cn('flex h-full w-full items-center justify-center rounded-full bg-gray-200 text-gray-700', $attrs.class as string)">
    <slot />
  </div>
</template>

<script setup lang="ts">
import { cn } from '../../utils/cn'
</script>

