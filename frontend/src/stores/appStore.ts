/**
 * App Store (Pinia)
 * 
 * Provides global state and data access throughout the application.
 * This replaces the React Context from the original application.
 */

import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { User, Notification, Conversation } from '../types'
import {
  getCurrentUser,
  getAllNotifications,
  getUnreadNotificationsCount,
  getAllConversations,
  getUnreadMessageCount,
} from '../services/dataService'

export const useAppStore = defineStore('app', () => {
  // State
  const currentUser = ref<User>(getCurrentUser())
  const notifications = ref<Notification[]>([])
  const unreadNotificationsCount = ref(0)
  const conversations = ref<Conversation[]>([])
  const unreadMessagesCount = ref(0)
  const currentPage = ref('home')
  const selectedServiceId = ref<string | null>(null)
  const selectedThreadId = ref<number | null>(null)

  // Actions
  const refreshNotifications = () => {
    notifications.value = getAllNotifications()
    unreadNotificationsCount.value = getUnreadNotificationsCount()
  }

  const refreshConversations = () => {
    conversations.value = getAllConversations()
    unreadMessagesCount.value = getUnreadMessageCount()
  }

  const setCurrentPage = (page: string) => {
    currentPage.value = page
  }

  const setSelectedServiceId = (id: string | null) => {
    selectedServiceId.value = id
  }

  const setSelectedThreadId = (id: number | null) => {
    selectedThreadId.value = id
  }

  // Initialize data on store creation
  refreshNotifications()
  refreshConversations()

  return {
    // State
    currentUser,
    notifications,
    unreadNotificationsCount,
    conversations,
    unreadMessagesCount,
    currentPage,
    selectedServiceId,
    selectedThreadId,
    
    // Actions
    refreshNotifications,
    refreshConversations,
    setCurrentPage,
    setSelectedServiceId,
    setSelectedThreadId,
  }
})

