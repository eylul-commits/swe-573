<template>
  <Teleport to="body">
    <Transition name="dialog">
      <div
        v-if="modelValue"
        class="fixed inset-0 z-50 flex items-center justify-center"
        @click.self="onClose"
      >
        <!-- Backdrop -->
        <div class="absolute inset-0 bg-black/50" />
        
        <!-- Dialog Content -->
        <div class="relative">
          <slot />
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
defineProps<{
  modelValue: boolean
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
}>()

const onClose = () => {
  emit('update:modelValue', false)
}
</script>

<style scoped>
.dialog-enter-active,
.dialog-leave-active {
  transition: opacity 0.2s ease;
}

.dialog-enter-from,
.dialog-leave-to {
  opacity: 0;
}
</style>

