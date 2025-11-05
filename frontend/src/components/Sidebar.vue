<template>
  <div class="flex flex-col h-full items-center py-6">
    <!-- Logo - Bee Icon -->
    <div class="w-10 h-10 bg-amber-400 rounded-full flex items-center justify-center mb-8 cursor-pointer hover:bg-amber-500 transition-colors">
      <Bug class="w-5 h-5 text-gray-900" />
    </div>

    <!-- Navigation Icons -->
    <div class="flex-1 flex flex-col gap-4">
      <div
        v-for="item in navItems"
        :key="item.id"
        class="relative"
        :title="item.label"
      >
        <Button 
          variant="ghost" 
          size="icon" 
          :class="[
            'text-white hover:bg-gray-800 hover:text-white transition-colors',
            currentPage === item.id ? 'bg-gray-800' : ''
          ]"
          @click="$emit('navigate', item.id)"
        >
          <component :is="item.icon" class="w-5 h-5" />
        </Button>
        <Badge 
          v-if="item.badge && item.badge > 0"
          class="absolute -top-1 -right-1 h-5 w-5 flex items-center justify-center p-0 bg-red-600 hover:bg-red-600 text-white border-2 border-gray-900"
        >
          {{ item.badge }}
        </Badge>
      </div>
    </div>

    <!-- Bottom Icons -->
    <div class="flex flex-col gap-4">
      <!-- Notification Bell -->
      <div class="relative" :title="'Notifications'">
        <Button 
          variant="ghost" 
          size="icon" 
          class="text-white hover:bg-gray-800 hover:text-white relative"
          @click="$emit('view-notifications')"
        >
          <Bell class="w-5 h-5" />
          <Badge 
            v-if="unreadCount > 0"
            class="absolute -top-1 -right-1 h-5 w-5 flex items-center justify-center p-0 bg-red-600 hover:bg-red-600 text-white border-2 border-gray-900"
          >
            {{ unreadCount }}
          </Badge>
        </Button>
      </div>

      <Button 
        variant="ghost" 
        size="icon" 
        class="text-white hover:bg-gray-800 hover:text-white"
        @click="$emit('navigate', 'settings')"
        :title="'Settings'"
      >
        <Settings class="w-5 h-5" />
      </Button>
      
      <Button 
        variant="ghost" 
        size="icon" 
        class="text-white hover:bg-gray-800 hover:text-white"
        :title="'Logout'"
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
  Settings,
  LogOut,
  Users,
  ClipboardList,
  Bell,
} from 'lucide-vue-next'
import Button from './ui/Button.vue'
import Badge from './ui/Badge.vue'
import { useAppStore } from '../stores/appStore'

defineProps<{
  currentPage: string
}>()

defineEmits<{
  navigate: [page: string]
  'view-notifications': []
}>()

const appStore = useAppStore()

const navItems = [
  { id: 'home', icon: Home, label: 'Home' },
  { id: 'explore', icon: Compass, label: 'Explore' },
  { id: 'requests', icon: ClipboardList, label: 'Requests', badge: 3 },
  { id: 'messages', icon: MessageSquare, label: 'Messages' },
  { id: 'commons', icon: Users, label: 'The Commons' },
  { id: 'profile', icon: User, label: 'Profile' },
]

const unreadCount = computed(() => appStore.unreadNotificationsCount)
</script>

