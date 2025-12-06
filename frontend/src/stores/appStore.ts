import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { Notification } from '../types'
import type { User } from '../services/authService'

export const useAppStore = defineStore('app', () => {
  // State
  const currentUser = ref<User | null>(null)
  const authToken = ref<string | null>(localStorage.getItem('authToken'))
  const notifications = ref<Notification[]>([])
  const unreadNotificationsCount = ref(0)
  const streamChatReady = ref(false)
  const currentPage = ref('home')
  const selectedServiceId = ref<string | null>(null)
  const selectedThreadId = ref<number | null>(null)

  // Computed
  const isAuthenticated = computed(() => !!authToken.value && !!currentUser.value)

  // Actions
  const setCurrentUser = async (user: User, token: string, streamChatToken?: string) => {
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

    if (streamChatToken) {
      localStorage.setItem('streamChatToken', streamChatToken)
    }

    // Initialize Stream Chat if token provided
    streamChatReady.value = false

    console.log('🔐 Stream Chat token received:', streamChatToken ? `Yes (${streamChatToken.substring(0, 20)}...)` : 'No')

    if (streamChatToken) {
      try {
        const { initializeStreamChat } = await import('../clients/streamChatClient')
        await initializeStreamChat(
          user.id.toString(),
          user.name || user.email,
          streamChatToken
        )
        console.log('✓ Stream Chat initialized successfully')
        streamChatReady.value = true
      } catch (error) {
        console.error('✗ Failed to initialize Stream Chat:', error)
        streamChatReady.value = false
      }
    } else {
      console.warn('⚠️ No Stream Chat token provided, chat will not be available')
      streamChatReady.value = false
    }
  }

  const logout = async () => {
    // Disconnect from Stream Chat
    try {
      const { disconnectStreamChat } = await import('../clients/streamChatClient')
      await disconnectStreamChat()
    } catch (error) {
      console.error('Failed to disconnect from Stream Chat:', error)
    }

    currentUser.value = null
    authToken.value = null
    localStorage.removeItem('authToken')
    localStorage.removeItem('currentUser')
    localStorage.removeItem('streamChatToken')
    currentPage.value = 'home'
    streamChatReady.value = false
  }

  const loadUserFromStorage = async () => {
    const token = localStorage.getItem('authToken')
    const userStr = localStorage.getItem('currentUser')
    const streamChatToken = localStorage.getItem('streamChatToken')
    
    if (token && userStr) {
      try {
        currentUser.value = JSON.parse(userStr)
        authToken.value = token

        if (streamChatToken && currentUser.value) {
          console.log('Reinitializing Stream Chat from storage...')
          streamChatReady.value = false
          try {
            const { initializeStreamChat } = await import('../clients/streamChatClient')
            await initializeStreamChat(
              currentUser.value.id.toString(),
              currentUser.value.name || currentUser.value.email,
              streamChatToken
            )
            console.log('Stream Chat reinitialized successfully')
            streamChatReady.value = true
          } catch (error) {
            // Token expired olmuşsa siliyorum
            console.error('Failed to reinitialize Stream Chat:', error)
            streamChatReady.value = false
            localStorage.removeItem('streamChatToken')
          }
        }

      } catch (e) {
        logout()
      }
    }
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


  return {
    // State
    currentUser,
    authToken,
    notifications,
    unreadNotificationsCount,
    streamChatReady,
    currentPage,
    selectedServiceId,
    selectedThreadId,
    
    // Computed
    isAuthenticated,
    
    // Actions
    setCurrentUser,
    logout,
    setCurrentPage,
    setSelectedServiceId,
    setSelectedThreadId,
  }
})

