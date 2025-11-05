<template>
  <div
    ref="itemEl"
    :class="cn(
      'relative flex w-full cursor-pointer select-none items-center rounded-sm py-1.5 px-2 text-sm outline-none hover:bg-gray-100 transition-colors',
      select?.selectedValue.value === value && 'bg-gray-100',
      $attrs.class as string
    )"
    @click="handleClick"
  >
    <slot />
  </div>
</template>

<script setup lang="ts">
import { inject, ref } from 'vue'
import { cn } from '../../utils/cn'

const props = defineProps<{
  value: string
}>()

const select = inject<any>('select')
const itemEl = ref<HTMLElement | null>(null)

const handleClick = () => {
  const label = itemEl.value?.innerText || props.value
  select?.selectItem(props.value, label)
}
</script>

