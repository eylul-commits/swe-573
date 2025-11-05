<template>
  <div class="flex-1 overflow-y-auto bg-gray-50 p-8">
    <div class="max-w-2xl mx-auto">
      <Button @click="appStore.setCurrentPage('home')" class="mb-4">
        ← Back
      </Button>
      
      <h1 class="text-gray-900 mb-6">Create New Service</h1>
      
      <Card class="p-6">
        <div class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">Title</label>
            <Input placeholder="Enter service title" />
          </div>
          
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">Description</label>
            <textarea 
              class="flex w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
              rows="4"
              placeholder="Describe your service"
            />
          </div>
          
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">Time Required</label>
            <Input placeholder="e.g., 1 hour" />
          </div>
          
          <Button class="w-full bg-emerald-600 hover:bg-emerald-700 text-white">
            Create Service
          </Button>
        </div>
      </Card>
    </div>
  </div>
</template>

<script setup lang="ts">
import Card from '../ui/Card.vue'
import Button from '../ui/Button.vue'
import Input from '../ui/Input.vue'
import { useAppStore } from '../../stores/appStore'

const appStore = useAppStore()
</script>

