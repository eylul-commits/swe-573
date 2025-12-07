<template>
  <div>
    <label class="block text-sm font-medium text-gray-700 mb-2">
      Images (Optional) - Upload up to 5 images
    </label>
    <input 
      type="file"
      @change="handleImageUpload"
      accept="image/*"
      multiple
      class="block w-full text-sm text-gray-500 
        file:mr-4 file:py-2 file:px-4 
        file:rounded-lg file:border-0 
        file:text-sm file:font-semibold 
        file:bg-emerald-50 file:text-emerald-700 
        hover:file:bg-emerald-100 cursor-pointer"
    />
    
    <!-- Image Previews -->
    <div v-if="modelValue.length > 0" class="mt-3 grid grid-cols-3 gap-2">
      <div 
        v-for="(image, index) in modelValue" 
        :key="index"
        class="relative group"
      >
        <img 
          :src="image" 
          alt="Preview" 
          class="w-full h-24 object-cover rounded-lg border-2 border-gray-200"
        />
        <button
          type="button"
          @click="removeImage(index)"
          class="absolute top-1 right-1 bg-red-500 text-white rounded-full w-6 h-6 
                 flex items-center justify-center opacity-0 group-hover:opacity-100 
                 transition-opacity"
        >
          ×
        </button>
      </div>
    </div>
    
    <p v-if="uploadingImages" class="text-sm text-emerald-600 mt-2">
      Uploading {{ uploadingImagesCount }} image(s)...
    </p>
    <p v-if="modelValue.length > 0" class="text-sm text-gray-500 mt-2">
      {{ modelValue.length }} / 5 images uploaded
    </p>
    
    <!-- Error Message -->
    <p v-if="errorMessage" class="text-sm text-red-500 mt-2">
      {{ errorMessage }}
    </p>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

// Cloudinary config from environment variables
const CLOUDINARY_CLOUD_NAME = import.meta.env.VITE_CLOUDINARY_CLOUD_NAME || 'demo'
const CLOUDINARY_UPLOAD_PRESET = import.meta.env.VITE_CLOUDINARY_UPLOAD_PRESET || 'ml_default'

// Props
interface Props {
  modelValue: string[]
  maxImages?: number
}

const props = withDefaults(defineProps<Props>(), {
  maxImages: 5
})

// Emits
const emit = defineEmits<{
  'update:modelValue': [value: string[]]
  'error': [error: string]
}>()

// State
const uploadingImages = ref(false)
const uploadingImagesCount = ref(0)
const errorMessage = ref('')

// Image upload handler
async function handleImageUpload(event: Event) {
  const files = (event.target as HTMLInputElement).files
  if (!files || files.length === 0) return
  
  // Limit to maxImages total
  const remainingSlots = props.maxImages - props.modelValue.length
  const filesToUpload = Array.from(files).slice(0, remainingSlots)
  
  if (filesToUpload.length === 0) {
    errorMessage.value = `Maximum ${props.maxImages} images allowed`
    emit('error', errorMessage.value)
    return
  }
  
  uploadingImages.value = true
  uploadingImagesCount.value = filesToUpload.length
  errorMessage.value = ''
  
  try {
    // Upload all images in parallel
    const uploadPromises = filesToUpload.map(async (file) => {
      const formData = new FormData()
      formData.append('file', file)
      formData.append('upload_preset', CLOUDINARY_UPLOAD_PRESET)
      console.log("cloudinary config:")
      console.log(CLOUDINARY_CLOUD_NAME, CLOUDINARY_UPLOAD_PRESET)
      
      const response = await fetch(
        `https://api.cloudinary.com/v1_1/${CLOUDINARY_CLOUD_NAME}/image/upload`,
        {
          method: 'POST',
          body: formData
        }
      )
      
      if (!response.ok) throw new Error('Upload failed')
      
      const data = await response.json()
      return data.secure_url
    })
    
    const newImageUrls = await Promise.all(uploadPromises)
    
    // Emit updated array
    emit('update:modelValue', [...props.modelValue, ...newImageUrls])
    
    // Clear the file input
    ;(event.target as HTMLInputElement).value = ''
    
  } catch (error) {
    console.error('Upload failed:', error)
    errorMessage.value = 'Failed to upload images. Please try again.'
    emit('error', errorMessage.value)
  } finally {
    uploadingImages.value = false
    uploadingImagesCount.value = 0
  }
}

// Remove image handler
function removeImage(index: number) {
  const newImages = [...props.modelValue]
  newImages.splice(index, 1)
  emit('update:modelValue', newImages)
}
</script>

