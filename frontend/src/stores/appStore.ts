import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { User } from '../services/authService'

export const useAppStore = defineStore('app', () => {
  // Restore currentUser from localStorage on initialization
  const restoreUserFromStorage = (): User | null => {
    try {
      const userStr = localStorage.getItem('currentUser')
      if (userStr) {
        return JSON.parse(userStr) as User
      }
    } catch (error) {
      console.error('Failed to restore user from localStorage:', error)
    }
    return null
  }

  const currentUser = ref<User | null>(restoreUserFromStorage())
  const authToken = ref<string | null>(localStorage.getItem('authToken'))
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

  const clearAuthState = () => {
    currentUser.value = null
    authToken.value = null
    localStorage.removeItem('authToken')
    localStorage.removeItem('currentUser')
    localStorage.removeItem('streamChatToken')
    streamChatReady.value = false
  }

  const logout = async () => {
    // Disconnect from Stream Chat
    try {
      const { disconnectStreamChat } = await import('../clients/streamChatClient')
      await disconnectStreamChat()
    } catch (error) {
      console.error('Failed to disconnect from Stream Chat:', error)
    }

    clearAuthState()
    currentPage.value = 'home'
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

  // Initialize app state from localStorage
  const initializeFromStorage = async () => {
    // if we have user and token try to restore Stream Chat connection
    if (authToken.value && currentUser.value) {
      const streamChatToken = localStorage.getItem('streamChatToken')
      if (streamChatToken) {
        try {
          const { initializeStreamChat } = await import('../clients/streamChatClient')
          await initializeStreamChat(
            currentUser.value.id.toString(),
            currentUser.value.name || currentUser.value.email,
            streamChatToken
          )
          console.log('✓ Stream Chat restored from localStorage')
          streamChatReady.value = true
        } catch (error) {
          console.error('✗ Failed to restore Stream Chat:', error)
          streamChatReady.value = false
        }
      }
    }
  }

  // Listen for auth cleared events (when tokens are cleared due to 401/403)
  if (typeof window !== 'undefined') {
    window.addEventListener('auth:cleared', () => {
      clearAuthState()
    })
  }

  return {
    // State
    currentUser,
    authToken,
    streamChatReady,
    currentPage,
    selectedServiceId,
    selectedThreadId,
    
    // Computed
    isAuthenticated,
    
    // Actions
    setCurrentUser,
    logout,
    clearAuthState,
    setCurrentPage,
    setSelectedServiceId,
    setSelectedThreadId,
    initializeFromStorage,
  }
})

