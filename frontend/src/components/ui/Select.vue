<template>
  <div class="relative" ref="selectRef">
    <slot />
  </div>
</template>

<script setup lang="ts">
import { ref, provide } from 'vue'

const selectRef = ref<HTMLDivElement | null>(null)
const isOpen = ref(false)
const selectedValue = ref<string>('')
const selectedLabel = ref<string>('')
const triggerRef = ref<HTMLElement | null>(null)

const modelValue = defineModel<string>()

const toggle = () => {
  isOpen.value = !isOpen.value
}

const close = () => {
  isOpen.value = false
}

const selectItem = (value: string, label: string) => {
  selectedValue.value = value
  selectedLabel.value = label
  modelValue.value = value
  close()
}

const setTriggerRef = (el: HTMLElement | null) => {
  triggerRef.value = el
}

provide('select', {
  isOpen,
  selectedValue,
  selectedLabel,
  triggerRef,
  toggle,
  close,
  selectItem,
  setTriggerRef,
})
</script>

