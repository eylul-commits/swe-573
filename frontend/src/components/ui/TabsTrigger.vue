<template>
  <button
    :class="cn(
      'inline-flex items-center justify-center whitespace-nowrap rounded-sm px-3 py-1.5 text-sm font-medium ring-offset-background transition-all focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50',
      isActive ? 'bg-white text-gray-900 shadow-sm' : 'text-gray-600 hover:text-gray-900',
      $attrs.class as string
    )"
    @click="handleClick"
  >
    <slot />
  </button>
</template>

<script setup lang="ts">
import { inject, computed } from 'vue'
import { cn } from '../../utils/cn'

const props = defineProps<{
  value: string
}>()

const tabs = inject<any>('tabs')

const isActive = computed(() => tabs?.activeTab.value === props.value)

const handleClick = () => {
  tabs?.setActiveTab(props.value)
}
</script>

