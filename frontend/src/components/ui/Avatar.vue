<template>
  <div :class="cn('relative flex shrink-0 overflow-hidden rounded-full', $attrs.class as string)">
    <slot />
  </div>
</template>

<script setup lang="ts">
import { cn } from '../../utils/cn'
</script>

