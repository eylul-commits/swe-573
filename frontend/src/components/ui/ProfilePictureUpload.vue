<template>
  <div class="relative inline-block">
    <!-- Current Profile Picture -->
    <div class="relative group">
      <img 
        :src="displayImage"
        :alt="altText"
        class="w-32 h-32 rounded-full border-4 border-white shadow-lg object-cover bg-gray-100"
      />
      
      <!-- Upload Button Overlay -->
      <label 
        class="absolute inset-0 flex items-center justify-center bg-black bg-opacity-50 rounded-full opacity-0 group-hover:opacity-100 transition-opacity cursor-pointer"
      >
        <div class="text-white text-center">
          <svg class="w-8 h-8 mx-auto mb-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 9a2 2 0 012-2h.93a2 2 0 001.664-.89l.812-1.22A2 2 0 0110.07 4h3.86a2 2 0 011.664.89l.812 1.22A2 2 0 0018.07 7H19a2 2 0 012 2v9a2 2 0 01-2 2H5a2 2 0 01-2-2V9z" />
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 13a3 3 0 11-6 0 3 3 0 016 0z" />
          </svg>
          <span class="text-xs font-medium">Change</span>
        </div>
        <input 
          type="file"
          @change="handleImageUpload"
          accept="image/*"
          class="hidden"
        />
      </label>
      
      <!-- Loading Overlay -->
      <div 
        v-if="uploading"
        class="absolute inset-0 flex items-center justify-center bg-black bg-opacity-70 rounded-full"
      >
        <div class="text-white text-center">
          <svg class="animate-spin h-8 w-8 mx-auto mb-1" fill="none" viewBox="0 0 24 24">
            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
            <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
          </svg>
          <span class="text-xs">Uploading...</span>
        </div>
      </div>
    </div>
    
    <!-- Error Message -->
    <p v-if="errorMessage" class="text-sm text-red-500 mt-2 text-center max-w-xs">
      {{ errorMessage }}
    </p>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

// Props
interface Props {
  modelValue?: string
  altText?: string
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: undefined,
  altText: 'Profile picture'
})

// Emits
const emit = defineEmits<{
  'update:modelValue': [value: string]
  'error': [error: string]
}>()

// Cloudinary config from environment variables
const CLOUDINARY_CLOUD_NAME = import.meta.env.VITE_CLOUDINARY_CLOUD_NAME || 'demo'
const CLOUDINARY_UPLOAD_PRESET = import.meta.env.VITE_CLOUDINARY_UPLOAD_PRESET || 'ml_default'

// State
const uploading = ref(false)
const errorMessage = ref('')

// Computed display image - fallback to placeholder if no avatar
const displayImage = computed(() => {
  if (props.modelValue && props.modelValue.trim().length > 0) {
    return props.modelValue
  }
  // Generate a placeholder based on alt text
  const encoded = encodeURIComponent(props.altText)
  return `https://ui-avatars.com/api/?name=${encoded}&background=E2E8F0&color=1F2937&size=256`
})

// Image upload handler
async function handleImageUpload(event: Event) {
  const file = (event.target as HTMLInputElement).files?.[0]
  if (!file) return
  
  // Validate file type
  if (!file.type.startsWith('image/')) {
    errorMessage.value = 'Please select an image file'
    emit('error', errorMessage.value)
    return
  }
  
  // Validate file size (max 5MB)
  if (file.size > 5 * 1024 * 1024) {
    errorMessage.value = 'Image must be smaller than 5MB'
    emit('error', errorMessage.value)
    return
  }
  
  uploading.value = true
  errorMessage.value = ''
  
  try {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('upload_preset', CLOUDINARY_UPLOAD_PRESET)
    
    const response = await fetch(
      `https://api.cloudinary.com/v1_1/${CLOUDINARY_CLOUD_NAME}/image/upload`,
      {
        method: 'POST',
        body: formData
      }
    )
    
    if (!response.ok) throw new Error('Upload failed')
    
    const data = await response.json()
    emit('update:modelValue', data.secure_url)
    
    // Clear the file input
    ;(event.target as HTMLInputElement).value = ''
    
  } catch (error) {
    console.error('Upload failed:', error)
    errorMessage.value = 'Failed to upload image. Please try again.'
    emit('error', errorMessage.value)
  } finally {
    uploading.value = false
  }
}
</script>

