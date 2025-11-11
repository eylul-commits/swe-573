import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { Notification, Conversation } from '../types'
import type { User } from '../services/authService'
import {
  getAllNotifications,
  getUnreadNotificationsCount,
  getAllConversations,
  getUnreadMessageCount,
} from '../services/dataService'

export const useAppStore = defineStore('app', () => {
  // State
  const currentUser = ref<User | null>(null)
  const authToken = ref<string | null>(localStorage.getItem('authToken'))
  const notifications = ref<Notification[]>([])
  const unreadNotificationsCount = ref(0)
  const conversations = ref<Conversation[]>([])
  const unreadMessagesCount = ref(0)
  const currentPage = ref('home')
  const selectedServiceId = ref<string | null>(null)
  const selectedThreadId = ref<number | null>(null)

  // Computed
  const isAuthenticated = computed(() => !!authToken.value && !!currentUser.value)

  // Actions
  const setCurrentUser = (user: User, token: string) => {
    // map backend properties to frontend expected properties
    const mappedUser: User = {
      ...user,
      timebankBalance: user.balanceHours,
      hoursGiven: user.hoursGiven || 0,
      hoursReceived: user.hoursReceived || 0,
      location: user.district && user.province ? `${user.district}, ${user.province}` : undefined,
    }
    currentUser.value = mappedUser
    authToken.value = token
    localStorage.setItem('authToken', token)
    localStorage.setItem('currentUser', JSON.stringify(mappedUser))
  }

  const logout = () => {
    currentUser.value = null
    authToken.value = null
    localStorage.removeItem('authToken')
    localStorage.removeItem('currentUser')
    currentPage.value = 'home'
  }

  const loadUserFromStorage = () => {
    const token = localStorage.getItem('authToken')
    const userStr = localStorage.getItem('currentUser')
    
    if (token && userStr) {
      try {
        currentUser.value = JSON.parse(userStr)
        authToken.value = token
      } catch (e) {
        logout()
      }
    }
  }

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
  loadUserFromStorage()
  if (isAuthenticated.value) {
    refreshNotifications()
    refreshConversations()
  }

  return {
    // State
    currentUser,
    authToken,
    notifications,
    unreadNotificationsCount,
    conversations,
    unreadMessagesCount,
    currentPage,
    selectedServiceId,
    selectedThreadId,
    
    // Computed
    isAuthenticated,
    
    // Actions
    setCurrentUser,
    logout,
    refreshNotifications,
    refreshConversations,
    setCurrentPage,
    setSelectedServiceId,
    setSelectedThreadId,
  }
})

