<template>
  <div :class="cn('overflow-auto', $attrs.class as string)">
    <slot />
  </div>
</template>

<script setup lang="ts">
import { cn } from '../../utils/cn'
</script>

