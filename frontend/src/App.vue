<template>
  <div class="flex h-screen bg-gray-50">
    <!-- Icon Sidebar - Very narrow -->
    <div class="w-16 bg-gray-900 flex-shrink-0">
      <Sidebar 
        :current-page="appStore.currentPage" 
        @navigate="handleNavigate"
        @view-notifications="() => appStore.setCurrentPage('notifications')"
      />
    </div>

    <!-- Main Content -->
    <component :is="currentPageComponent" />
    
    <!-- Toast Notifications (simplified, would need a toast library) -->
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useAppStore } from './stores/appStore'
import Sidebar from './components/Sidebar.vue'
import HomePage from './components/pages/HomePage.vue'
import ExplorePage from './components/pages/ExplorePage.vue'
import CommonsPage from './components/pages/CommonsPage.vue'
import MessagingPage from './components/pages/MessagingPage.vue'
import NotificationsPage from './components/pages/NotificationsPage.vue'
import RequestsPage from './components/pages/RequestsPage.vue'
import ServiceDetailsPage from './components/pages/ServiceDetailsPage.vue'
import CreateOfferPage from './components/pages/CreateOfferPage.vue'
import ProfilePage from './components/pages/ProfilePage.vue'
import SettingsPage from './components/pages/SettingsPage.vue'

const appStore = useAppStore()

const handleNavigate = (page: string) => {
  appStore.setCurrentPage(page)
  appStore.setSelectedServiceId(null)
}

const currentPageComponent = computed(() => {
  // If a service is selected, show the details page
  if (appStore.selectedServiceId) {
    return ServiceDetailsPage
  }

  switch (appStore.currentPage) {
    case 'home':
      return HomePage
    case 'explore':
      return ExplorePage
    case 'create-offer':
      return CreateOfferPage
    case 'notifications':
      return NotificationsPage
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
    default:
      return HomePage
  }
})
</script>

