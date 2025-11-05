<template>
  <div class="flex-1 overflow-y-auto bg-gray-50 p-8">
    <div class="max-w-4xl mx-auto">
      <div class="flex items-center justify-between mb-6">
        <h1 class="text-gray-900">Notifications</h1>
        <Button variant="outline" size="sm">Mark all as read</Button>
      </div>
      
      <div class="space-y-2">
        <Card 
          v-for="notification in appStore.notifications"
          :key="notification.id"
          :class="['p-4 hover:bg-gray-50 cursor-pointer', !notification.read ? 'bg-blue-50/30' : '']"
        >
          <div class="flex items-start justify-between">
            <div class="flex-1">
              <div class="text-sm text-gray-900 mb-1">{{ notification.title }}</div>
              <div class="text-xs text-gray-600">{{ notification.message }}</div>
              <div class="text-xs text-gray-500 mt-1">{{ notification.timestamp }}</div>
            </div>
            <div v-if="!notification.read" class="w-2 h-2 bg-blue-600 rounded-full flex-shrink-0 mt-1" />
          </div>
        </Card>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import Card from '../ui/Card.vue'
import Button from '../ui/Button.vue'
import { useAppStore } from '../../stores/appStore'

const appStore = useAppStore()
</script>

