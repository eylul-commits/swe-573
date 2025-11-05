<template>
  <Teleport to="body">
    <Transition
      enter-active-class="transition ease-out duration-100"
      enter-from-class="opacity-0"
      enter-to-class="opacity-100"
      leave-active-class="transition ease-in duration-75"
      leave-from-class="opacity-100"
      leave-to-class="opacity-0"
    >
      <div
        v-if="select?.isOpen?.value"
        class="fixed inset-0 z-50"
        @click="select?.close()"
      >
        <div
          ref="contentEl"
          :class="cn(
            'absolute z-50 min-w-[8rem] max-h-[300px] overflow-auto rounded-md border bg-white p-1 shadow-md',
            $attrs.class as string
          )"
          :style="position"
          @click.stop
        >
          <slot />
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
import { inject, ref, watch } from 'vue'
import { cn } from '../../utils/cn'

const select = inject<any>('select')
const contentEl = ref<HTMLElement | null>(null)
const position = ref({
  top: '0px',
  left: '0px',
  width: 'auto',
})

watch(() => select?.isOpen.value, (isOpen) => {
  if (isOpen) {
    setTimeout(() => calculatePosition(), 0)
  }
})

const calculatePosition = () => {
  if (!select?.triggerRef?.value) return
  
  const triggerRect = select.triggerRef.value.getBoundingClientRect()
  const contentHeight = contentEl.value?.offsetHeight || 0
  const viewportHeight = window.innerHeight
  
  // Determine if there's enough space below
  const spaceBelow = viewportHeight - triggerRect.bottom
  const spaceAbove = triggerRect.top
  
  let top = triggerRect.bottom + 4
  
  // If not enough space below but more space above, position above
  if (spaceBelow < contentHeight && spaceAbove > spaceBelow) {
    top = triggerRect.top - contentHeight - 4
  }
  
  position.value = {
    top: `${top}px`,
    left: `${triggerRect.left}px`,
    width: `${triggerRect.width}px`,
  }
}
</script>

