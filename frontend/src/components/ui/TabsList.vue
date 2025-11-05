<template>
  <div :class="cn('inline-flex h-10 items-center justify-center rounded-md bg-gray-100 p-1 text-gray-600', $attrs.class as string)">
    <slot />
  </div>
</template>

<script setup lang="ts">
import { cn } from '../../utils/cn'
</script>

