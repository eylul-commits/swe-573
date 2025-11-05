<template>
  <div class="flex-1 overflow-y-auto bg-gray-50 p-8">
    <div class="max-w-6xl mx-auto">
      <h1 class="text-gray-900 mb-6">Profile</h1>
      <Card class="p-6">
        <div class="flex items-center gap-4 mb-6">
          <img :src="appStore.currentUser.avatar" :alt="appStore.currentUser.name" class="w-20 h-20 rounded-full" />
          <div>
            <div class="text-gray-900 text-xl">{{ appStore.currentUser.name }}</div>
            <div class="text-gray-600">{{ appStore.currentUser.location }}</div>
          </div>
        </div>
        <div class="grid grid-cols-3 gap-4">
          <div>
            <div class="text-2xl text-gray-900">{{ appStore.currentUser.timebankBalance }}h</div>
            <div class="text-sm text-gray-600">Balance</div>
          </div>
          <div>
            <div class="text-2xl text-gray-900">{{ appStore.currentUser.hoursGiven }}h</div>
            <div class="text-sm text-gray-600">Given</div>
          </div>
          <div>
            <div class="text-2xl text-gray-900">{{ appStore.currentUser.hoursReceived }}h</div>
            <div class="text-sm text-gray-600">Received</div>
          </div>
        </div>
      </Card>
    </div>
  </div>
</template>

<script setup lang="ts">
import Card from '../ui/Card.vue'
import { useAppStore } from '../../stores/appStore'

const appStore = useAppStore()
</script>

