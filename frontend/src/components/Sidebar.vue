<template>
  <div class="flex flex-col h-full items-center py-6">
    <!-- Logo - Bee Icon -->
    <div class="w-10 h-10 bg-amber-400 rounded-full flex items-center justify-center mb-8 cursor-pointer hover:bg-amber-500 transition-colors">
      <Bug class="w-5 h-5 text-gray-900" />
    </div>

    <!-- Navigation Icons -->
    <div class="flex-1 flex flex-col gap-4">
      <Button 
        v-for="item in navItems"
        :key="item.id"
        variant="ghost" 
        size="icon" 
        :class="[
          'text-white hover:bg-gray-800 hover:text-white transition-colors',
          currentPage === item.id ? 'bg-gray-800' : ''
        ]"
        :title="item.label"
        @click="$emit('navigate', item.id)"
      >
        <component :is="item.icon" class="w-5 h-5" />
      </Button>
    </div>

    <!-- Bottom Icons -->
    <div class="flex flex-col gap-4">
      <Button 
        variant="ghost" 
        size="icon" 
        class="text-white hover:bg-gray-800 hover:text-white"
        :title="'Logout'"
        @click="$emit('logout')"
      >
        <LogOut class="w-5 h-5" />
      </Button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import {
  Bug,
  Home,
  Compass,
  MessageSquare,
  User,
  LogOut,
  Users,
  Shield,
} from 'lucide-vue-next'
import Button from './ui/Button.vue'
import { useAppStore } from '../stores/appStore'

const appStore = useAppStore()

defineProps<{
  currentPage: string
}>()

defineEmits<{
  navigate: [page: string]
  logout: []
}>()

const isAdmin = computed(() => {
  return appStore.currentUser?.role === 'ADMIN'
})

const navItems = computed(() => {
  const items = [
    { id: 'home', icon: Home, label: 'Home' },
    { id: 'explore', icon: Compass, label: 'Explore' },
    { id: 'messages', icon: MessageSquare, label: 'Messages' },
    { id: 'commons', icon: Users, label: 'The Commons' },
    { id: 'profile', icon: User, label: 'Profile' },
  ]
  
  if (isAdmin.value) {
    items.push({ id: 'admin', icon: Shield, label: 'Admin Panel' })
  }
  
  return items
})
</script>

