<template>
  <div
    v-if="isActive"
    :class="cn('mt-2 ring-offset-background focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2', $attrs.class as string)"
  >
    <slot />
  </div>
</template>

<script setup lang="ts">
import { inject, computed } from 'vue'
import { cn } from '../../utils/cn'

const props = defineProps<{
  value: string
}>()

const tabs = inject<any>('tabs')

const isActive = computed(() => tabs?.activeTab.value === props.value)
</script>

