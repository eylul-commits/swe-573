<template>
  <div
    :class="cn(
      'shrink-0 bg-gray-200',
      orientation === 'horizontal' ? 'h-[1px] w-full' : 'h-full w-[1px]',
      $attrs.class as string
    )"
  />
</template>

<script setup lang="ts">
import { cn } from '../../utils/cn'

withDefaults(
  defineProps<{
    orientation?: 'horizontal' | 'vertical'
  }>(),
  {
    orientation: 'horizontal',
  }
)
</script>

