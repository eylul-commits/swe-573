<template>
  <button
    ref="triggerEl"
    type="button"
    :class="cn(
      'flex h-10 w-full items-center justify-between rounded-md border border-gray-300 bg-gray-50 px-3 py-2 text-sm ring-offset-background placeholder:text-gray-400 focus:outline-none focus:ring-2 focus:ring-amber-500 focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50 hover:bg-white transition-colors',
      $attrs.class as string
    )"
    @click="select?.toggle()"
  >
    <span class="flex items-center flex-1 text-left">
      <slot />
    </span>
    <ChevronDown 
      class="w-4 h-4 text-gray-500 transition-transform flex-shrink-0"
      :class="select?.isOpen?.value && 'rotate-180'"
    />
  </button>
</template>

<script setup lang="ts">
import { inject, ref, onMounted } from 'vue'
import { ChevronDown } from 'lucide-vue-next'
import { cn } from '../../utils/cn'

const select = inject<any>('select')
const triggerEl = ref<HTMLElement | null>(null)

onMounted(() => {
  if (triggerEl.value) {
    select?.setTriggerRef(triggerEl.value)
  }
})
</script>

