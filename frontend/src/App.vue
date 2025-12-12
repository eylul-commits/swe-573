<template>
  <!-- Show login page if not authenticated -->
  <LoginPage v-if="!appStore.isAuthenticated" />
  
  <!-- Show main app if authenticated -->
  <div v-else class="flex h-screen bg-gray-50">
    <!-- Icon Sidebar - Very narrow -->
    <div class="w-16 bg-gray-900 flex-shrink-0">
      <Sidebar 
        :current-page="appStore.currentPage" 
        @navigate="handleNavigate"
        @logout="handleLogout"
      />
    </div>

    <!-- Main Content -->
    <component :is="currentPageComponent" />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useAppStore } from './stores/appStore'
import Sidebar from './components/Sidebar.vue'
import LoginPage from './components/pages/LoginPage.vue'
import HomePage from './components/pages/HomePage.vue'
import ExplorePage from './components/pages/ExplorePage.vue'
import CommonsPage from './components/pages/CommonsPage.vue'
import MessagingPage from './components/pages/MessagingPage.vue'
import RequestsPage from './components/pages/RequestsPage.vue'
import ServiceDetailsPage from './components/pages/ServiceDetailsPage.vue'
import ThreadDetailsPage from './components/pages/ThreadDetailsPage.vue'
import CreateServicePage from './components/pages/CreateServicePage.vue'
import ProfilePage from './components/pages/ProfilePage.vue'
import SettingsPage from './components/pages/SettingsPage.vue'
import AdminPanelPage from './components/pages/AdminPanelPage.vue'

const appStore = useAppStore()

// Initialize app state from localStorage on mount
onMounted(() => {
  appStore.initializeFromStorage()
})

const handleNavigate = (page: string) => {
  appStore.setCurrentPage(page)
  appStore.setSelectedServiceId(null)
  appStore.setSelectedThreadId(null)
}

const handleLogout = () => {
  appStore.logout()
}

const currentPageComponent = computed(() => {
  // If a service is selected, show the service details page
  if (appStore.selectedServiceId) {
    return ServiceDetailsPage
  }

  // If a thread is selected, show the thread details page
  if (appStore.selectedThreadId) {
    return ThreadDetailsPage
  }

  switch (appStore.currentPage) {
    case 'home':
      return HomePage
    case 'explore':
      return ExplorePage
    case 'create-offer':
      return CreateServicePage
    case 'requests':
      return RequestsPage
    case 'messages':
      return MessagingPage
    case 'commons':
      return CommonsPage
    case 'profile':
      return ProfilePage
    case 'settings':
      return SettingsPage
    case 'admin':
      return AdminPanelPage
    default:
      return HomePage
  }
})
</script>

