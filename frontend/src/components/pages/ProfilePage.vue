<template>
  <div class="flex-1 overflow-y-auto bg-gray-50 p-8">
    <div class="max-w-6xl mx-auto">
      <h1 class="text-gray-900 mb-6">Profile</h1>
      
      <!-- User Info Card -->
      <Card class="p-6 mb-6">
        <div class="flex items-center gap-6 mb-6">
          <ProfilePictureUpload
            v-model="profilePictureUrl"
            :alt-text="appStore.currentUser?.name || 'User'"
            @error="handleUploadError"
          />
          <div>
            <div class="text-gray-900 text-xl">{{ appStore.currentUser?.name || 'User' }}</div>
            <div class="text-gray-600">{{ appStore.currentUser?.location || 'Location not set' }}</div>
          </div>
        </div>
        <div class="grid grid-cols-3 gap-4">
          <div>
            <div class="text-2xl text-gray-900">{{ appStore.currentUser?.timebankBalance ?? 0 }}h</div>
            <div class="text-sm text-gray-600">Balance</div>
          </div>
          <div>
            <div class="text-2xl text-gray-900">{{ appStore.currentUser?.hoursGiven ?? 0 }}h</div>
            <div class="text-sm text-gray-600">Given</div>
          </div>
          <div>
            <div class="text-2xl text-gray-900">{{ appStore.currentUser?.hoursReceived ?? 0 }}h</div>
            <div class="text-sm text-gray-600">Received</div>
          </div>
        </div>
      </Card>

      <!-- Tabs for Services and Handshakes -->
      <Card class="p-6">
        <Tabs default-value="services" class="w-full">
          <TabsList class="w-full grid grid-cols-2 bg-gray-100 p-1 mb-6">
            <TabsTrigger
              value="services"
              class="data-[state=active]:bg-gray-900 data-[state=active]:text-white rounded-lg"
            >
              My Services
            </TabsTrigger>
            <TabsTrigger
              value="handshakes"
              class="data-[state=active]:bg-gray-900 data-[state=active]:text-white rounded-lg"
            >
              My Handshakes
            </TabsTrigger>
          </TabsList>

          <!-- Services Tab -->
          <TabsContent value="services">
            <div v-if="loadingServices" class="text-center py-8">
              <div class="text-gray-500">Loading services...</div>
            </div>
            <div v-else-if="userServices.length === 0" class="text-center py-12">
              <div class="text-gray-400 text-5xl mb-4">📦</div>
              <h3 class="text-lg font-semibold text-gray-900 mb-2">No services yet</h3>
              <p class="text-gray-600">Create your first offer or request to get started</p>
            </div>
            <div v-else class="space-y-3">
              <div
                v-for="service in userServices"
                :key="service.id"
                @click="viewService(service.id)"
                class="p-4 border border-gray-200 rounded-lg cursor-pointer transition-colors hover:bg-gray-50"
              >
                <div class="flex items-start justify-between mb-2">
                  <div class="flex-1">
                    <h3 class="font-medium text-gray-900 mb-1">{{ service.title }}</h3>
                    <p class="text-sm text-gray-600">{{ service.location }}</p>
                  </div>
                  <Badge 
                    :class="service.type === 'OFFER' 
                      ? 'bg-emerald-100 text-emerald-700 hover:bg-emerald-100' 
                      : 'bg-blue-100 text-blue-700 hover:bg-blue-100'"
                  >
                    {{ service.type }}
                  </Badge>
                </div>
                
                <p class="text-sm text-gray-600 mb-2 line-clamp-2">
                  {{ service.description }}
                </p>
                
                <div class="flex items-center gap-4 text-xs text-gray-500">
                  <div class="flex items-center gap-1">
                    <MapPin class="w-3 h-3" />
                    <span>{{ service.location }}</span>
                  </div>
                  <div class="flex items-center gap-1">
                    <Clock class="w-3 h-3" />
                    <span>{{ service.timebank }}</span>
                  </div>
                  <div class="flex items-center gap-1">
                    <span :class="service.status === 'active' ? 'text-green-600' : 'text-gray-500'">
                      {{ service.status }}
                    </span>
                  </div>
                </div>
                
                <div v-if="service.tags && service.tags.length > 0" class="flex flex-wrap gap-1 mt-2">
                  <Badge
                    v-for="tag in service.tags.slice(0, 3)"
                    :key="tag"
                    variant="outline"
                    class="text-xs"
                  >
                    {{ tag }}
                  </Badge>
                </div>
              </div>
            </div>
          </TabsContent>

          <!-- Handshakes Tab -->
          <TabsContent value="handshakes">
            <div v-if="loadingHandshakes" class="text-center py-8">
              <div class="text-gray-500">Loading handshakes...</div>
            </div>
            <div v-else-if="userHandshakes.length === 0" class="text-center py-12">
              <div class="text-gray-400 text-5xl mb-4">🤝</div>
              <h3 class="text-lg font-semibold text-gray-900 mb-2">No handshakes yet</h3>
              <p class="text-gray-600">Handshakes will appear here when you accept service offers</p>
            </div>
            <HandshakeList
              v-else
              :handshakes="userHandshakes"
              @openChat="handleOpenChat"
              @openConfirm="handleOpenConfirm"
              @openRating="handleOpenRating"
              @cancelled="handleCancelled"
            />
          </TabsContent>
        </Tabs>
      </Card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { Clock, MapPin } from 'lucide-vue-next'
import Card from '../ui/Card.vue'
import Badge from '../ui/Badge.vue'
import Tabs from '../ui/Tabs.vue'
import TabsContent from '../ui/TabsContent.vue'
import TabsList from '../ui/TabsList.vue'
import TabsTrigger from '../ui/TabsTrigger.vue'
import HandshakeList from '../HandshakeList.vue'
import ProfilePictureUpload from '../ui/ProfilePictureUpload.vue'
import { useAppStore } from '../../stores/appStore'
import { useHandshakeStore } from '../../stores/handshakeStore'
import { getUserServices } from '../../services/marketplaceService'
import { updateProfile } from '../../services/authService'
import type { Service, Handshake } from '../../types'

const appStore = useAppStore()
const handshakeStore = useHandshakeStore()

const userServices = ref<Service[]>([])
const loadingServices = ref(false)
const loadingHandshakes = ref(false)
const profilePictureUrl = ref<string | undefined>(appStore.currentUser?.avatarUrl)

const userHandshakes = computed(() => handshakeStore.handshakes)

// Watch for changes in currentUser avatarUrl and update profilePictureUrl
watch(() => appStore.currentUser?.avatarUrl, (newAvatar) => {
  if (newAvatar && profilePictureUrl.value !== newAvatar) {
    profilePictureUrl.value = newAvatar
  }
}, { immediate: true })

// Watch for profile picture changes and update backend
watch(profilePictureUrl, async (newUrl: string | undefined) => {
  const currentAvatar = appStore.currentUser?.avatarUrl
  if (newUrl !== currentAvatar) {
    try {
      const updatedUser = await updateProfile({ avatarUrl: newUrl })
      // Update the app store with the new user data
      if (appStore.currentUser) {
        appStore.currentUser.avatarUrl = updatedUser.avatarUrl
      }
    } catch (error) {
      console.error('Failed to update profile picture:', error)
    }
  }
})

function handleUploadError(error: string) {
  console.error('Upload error:', error)
}


// Load user's services
onMounted(async () => {
  if (appStore.currentUser) {
    // Load services
    loadingServices.value = true
    try {
      userServices.value = await getUserServices(String(appStore.currentUser.id))
    } catch (error) {
      console.error('Failed to load user services:', error)
    } finally {
      loadingServices.value = false
    }

    // Load handshakes
    loadingHandshakes.value = true
    try {
      await handshakeStore.loadHandshakes()
    } catch (error) {
      console.error('Failed to load handshakes:', error)
    } finally {
      loadingHandshakes.value = false
    }
  }
})

const viewService = (serviceId: string) => {
  appStore.setSelectedServiceId(serviceId)
}

const handleOpenChat = (handshake: Handshake) => {
  // Navigate to messaging page with this handshake
  console.log('Open chat for handshake:', handshake)
  // TODO: Implement chat navigation
}

const handleOpenConfirm = (handshake: Handshake) => {
  // Open confirm modal
  console.log('Open confirm modal for handshake:', handshake)
  // TODO: Implement confirm modal
}

const handleOpenRating = (handshake: Handshake) => {
  // Open rating modal
  console.log('Open rating modal for handshake:', handshake)
  // TODO: Implement rating modal
}

const handleCancelled = async () => {
  // Refresh handshakes after cancellation
  try {
    await handshakeStore.loadHandshakes()
  } catch (error) {
    console.error('Failed to refresh handshakes after cancellation:', error)
  }
}
</script>

