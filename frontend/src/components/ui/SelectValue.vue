<template>
  <span :class="cn('text-sm', !select?.selectedLabel?.value && 'text-gray-500')">
    {{ select?.selectedLabel?.value || placeholder }}
  </span>
</template>

<script setup lang="ts">
import { inject } from 'vue'
import { cn } from '../../utils/cn'

defineProps<{
  placeholder?: string
}>()

const select = inject<any>('select')
</script>

