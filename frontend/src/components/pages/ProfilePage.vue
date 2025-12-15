<template>
  <div class="flex-1 overflow-y-auto bg-gray-50 p-8">
    <div class="max-w-6xl mx-auto">
      <div class="flex items-center justify-between mb-6">
        <h1 class="text-gray-900">Profile</h1>
        <Button 
          @click="isEditing = !isEditing"
          :variant="isEditing ? 'outline' : 'default'"
        >
          {{ isEditing ? 'Cancel' : 'Edit Profile' }}
        </Button>
      </div>
      
      <!-- User Info Card -->
      <Card class="p-6 mb-6">
        <div class="flex items-center gap-6 mb-6">
          <ProfilePictureUpload
            v-model="profilePictureUrl"
            :alt-text="appStore.currentUser?.name || 'User'"
            @error="handleUploadError"
          />
          <div class="flex-1">
            <!-- Editing Mode -->
            <div v-if="isEditing" class="space-y-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Name</label>
                <Input 
                  v-model="editForm.name" 
                  placeholder="Enter your name"
                  class="w-full"
                />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Bio</label>
                <Textarea 
                  v-model="editForm.bio" 
                  placeholder="Tell us about yourself..."
                  class="w-full min-h-[80px]"
                />
              </div>
              <div class="grid grid-cols-2 gap-4">
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">Province</label>
                  <Input 
                    v-model="editForm.province" 
                    placeholder="e.g., Istanbul"
                    class="w-full"
                  />
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 mb-1">District</label>
                  <Input 
                    v-model="editForm.district" 
                    placeholder="e.g., Kadıköy"
                    class="w-full"
                  />
                </div>
              </div>
              <div class="flex gap-2">
                <Button 
                  @click="handleSaveProfile" 
                  :disabled="isSaving"
                  class="flex-1"
                >
                  {{ isSaving ? 'Saving...' : 'Save Changes' }}
                </Button>
                <Button 
                  @click="cancelEdit" 
                  variant="outline"
                  :disabled="isSaving"
                >
                  Cancel
                </Button>
              </div>
            </div>
            
            <!-- Display Mode -->
            <div v-else>
              <div class="text-gray-900 text-xl">{{ appStore.currentUser?.name || 'User' }}</div>
              <div class="text-gray-600">
                {{ displayLocation }}
              </div>
              <div v-if="appStore.currentUser?.bio" class="text-gray-700 text-sm mt-2">
                {{ appStore.currentUser.bio }}
              </div>
              <div v-if="currentUserBadge" class="mt-2">
                <BadgeDisplay :badge="currentUserBadge" />
              </div>
            </div>
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
                class="border border-gray-200 rounded-lg"
              >
                <div
                  @click="viewService(service.id)"
                  class="p-4 cursor-pointer transition-colors hover:bg-gray-50"
                >
                  <div class="flex gap-4">
                    <!-- Service Image -->
                    <div v-if="service.imageUrls && service.imageUrls.length > 0" class="flex-shrink-0">
                      <div class="w-40 h-40 rounded-lg overflow-hidden border border-gray-200">
                        <ImageWithFallback
                          :src="service.imageUrls[0]"
                          :alt="service.title"
                          className="w-full h-full object-cover"
                        />
                      </div>
                    </div>
                    
                    <!-- Service Content -->
                    <div class="flex-1 min-w-0">
                      <div class="flex items-start justify-between mb-2">
                        <div class="flex-1 min-w-0">
                          <h3 class="font-medium text-gray-900 mb-1">{{ service.title }}</h3>
                          <p class="text-sm text-gray-600">{{ service.location }}</p>
                        </div>
                        <div class="flex items-center gap-2">
                          <Badge 
                            :class="service.type === 'OFFER' 
                              ? 'bg-emerald-100 text-emerald-700 hover:bg-emerald-100' 
                              : 'bg-blue-100 text-blue-700 hover:bg-blue-100'"
                          >
                            {{ service.type }}
                          </Badge>
                          <!-- Deactivate Button -->
                          <Button
                            v-if="service.status === 'active'"
                            @click.stop="handleDeactivateService(service)"
                            variant="outline"
                            size="sm"
                            :disabled="deactivatingServices.has(service.id)"
                            class="text-red-600 hover:text-red-700 hover:bg-red-50 border-red-200 text-xs px-3 py-1"
                          >
                            {{ deactivatingServices.has(service.id) ? 'Deactivating...' : 'Deactivate' }}
                          </Button>
                        </div>
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
                </div>
                
                <!-- Handshakes Section -->
                <div 
                  v-if="getHandshakesForService(service.id).length > 0"
                  class="border-t border-gray-200"
                >
                  <button
                    @click.stop="toggleServiceExpanded(service.id)"
                    class="w-full px-4 py-2 flex items-center justify-between text-sm text-gray-700 hover:bg-gray-50 transition-colors"
                  >
                    <span class="font-medium">
                      Handshakes ({{ getHandshakesForService(service.id).length }})
                    </span>
                    <ChevronDown 
                      v-if="!expandedServices.has(service.id)" 
                      class="w-4 h-4"
                    />
                    <ChevronUp 
                      v-else
                      class="w-4 h-4"
                    />
                  </button>
                  
                  <div 
                    v-if="expandedServices.has(service.id)"
                    class="px-4 pb-4 space-y-2"
                  >
                    <div
                      v-for="handshake in getHandshakesForService(service.id)"
                      :key="handshake.id"
                      class="p-3 bg-gray-50 rounded-lg border border-gray-200"
                    >
                      <div class="flex items-start justify-between mb-2">
                        <div class="flex items-center gap-2">
                          <img
                            :src="getAvatarUrl(handshake.seeker.avatar, handshake.seeker.name)"
                            :alt="handshake.seeker.name"
                            class="w-8 h-8 rounded-full object-cover"
                          />
                          <div>
                            <div class="text-sm font-medium text-gray-900">
                              {{ handshake.seeker.name }}
                            </div>
                            <div class="text-xs text-gray-500">
                              Seeker
                            </div>
                          </div>
                          <span class="text-gray-400">↔</span>
                          <img
                            :src="getAvatarUrl(handshake.provider.avatar, handshake.provider.name)"
                            :alt="handshake.provider.name"
                            class="w-8 h-8 rounded-full object-cover"
                          />
                          <div>
                            <div class="text-sm font-medium text-gray-900">
                              {{ handshake.provider.name }}
                            </div>
                            <div class="text-xs text-gray-500">
                              Provider
                            </div>
                          </div>
                        </div>
                        <Badge 
                          :class="getHandshakeStatusColor(handshake.status)"
                        >
                          {{ handshake.status }}
                        </Badge>
                      </div>
                      
                      <div class="flex items-center gap-4 text-xs text-gray-600">
                        <div>
                          <span class="font-medium">Duration:</span> {{ handshake.durationHours }}h
                        </div>
                        <div v-if="handshake.agreedDate">
                          <span class="font-medium">Agreed Date:</span> 
                          {{ new Date(handshake.agreedDate).toLocaleDateString() }}
                        </div>
                        <div>
                          <span class="font-medium">Created:</span> 
                          {{ new Date(handshake.createdAt).toLocaleDateString() }}
                        </div>
                      </div>
                      
                      <div class="flex items-center gap-2 mt-2 text-xs">
                        <div 
                          :class="handshake.seekerConfirmed ? 'text-green-600' : 'text-gray-400'"
                          class="flex items-center gap-1"
                        >
                          <span>{{ handshake.seekerConfirmed ? '✓' : '○' }}</span>
                          <span>Seeker confirmed</span>
                        </div>
                        <div 
                          :class="handshake.providerConfirmed ? 'text-green-600' : 'text-gray-400'"
                          class="flex items-center gap-1"
                        >
                          <span>{{ handshake.providerConfirmed ? '✓' : '○' }}</span>
                          <span>Provider confirmed</span>
                        </div>
                      </div>
                    </div>
                  </div>
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
            />
          </TabsContent>
        </Tabs>
      </Card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { Clock, MapPin, ChevronDown, ChevronUp } from 'lucide-vue-next'
import Card from '../ui/Card.vue'
import Badge from '../ui/Badge.vue'
import Button from '../ui/Button.vue'
import Input from '../ui/Input.vue'
import Textarea from '../ui/Textarea.vue'
import Tabs from '../ui/Tabs.vue'
import TabsContent from '../ui/TabsContent.vue'
import TabsList from '../ui/TabsList.vue'
import TabsTrigger from '../ui/TabsTrigger.vue'
import HandshakeList from '../HandshakeList.vue'
import ProfilePictureUpload from '../ui/ProfilePictureUpload.vue'
import ImageWithFallback from '../ui/ImageWithFallback.vue'
import BadgeDisplay from '../BadgeDisplay.vue'
import { useAppStore } from '../../stores/appStore'
import { useHandshakeStore } from '../../stores/handshakeStore'
import { getUserServices, deactivateService } from '../../services/marketplaceService'
import { updateProfile } from '../../services/authService'
import { getHandshakesByOfferId, getHandshakesByRequestId } from '../../services/handshakeService'
import { getAvatarUrl } from '../../utils/avatarUtils'
import type { Service, Handshake } from '../../types'

const appStore = useAppStore()
const handshakeStore = useHandshakeStore()

const userServices = ref<Service[]>([])
const loadingServices = ref(false)
const loadingHandshakes = ref(false)
const profilePictureUrl = ref<string | undefined>(appStore.currentUser?.avatarUrl)
const serviceHandshakes = ref<Map<string, Handshake[]>>(new Map())
const expandedServices = ref<Set<string>>(new Set())
const isEditing = ref(false)
const isSaving = ref(false)
const deactivatingServices = ref<Set<string>>(new Set())

const editForm = ref({
  name: '',
  bio: '',
  province: '',
  district: ''
})

const userHandshakes = computed(() => handshakeStore.handshakes)

const displayLocation = computed(() => {
  const district = appStore.currentUser?.district
  const province = appStore.currentUser?.province
  
  if (district && province) {
    return `${district}, ${province}`
  } else if (province) {
    return province
  } else if (district) {
    return district
  }
  return 'Location not set'
})

// Get the user's current badge (only one badge now)
const currentUserBadge = computed(() => {
  if (appStore.currentUser?.badges && appStore.currentUser.badges.length > 0) {
    return appStore.currentUser.badges[0]
  }

  return null
})

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

// Watch for editing mode changes to initialize form
watch(isEditing, (editing) => {
  if (editing) {
    // Initialize form with current user data
    editForm.value = {
      name: appStore.currentUser?.name || '',
      bio: appStore.currentUser?.bio || '',
      province: appStore.currentUser?.province || '',
      district: appStore.currentUser?.district || ''
    }
  }
})

function handleUploadError(error: string) {
  console.error('Upload error:', error)
}

async function handleSaveProfile() {
  if (!appStore.currentUser) return
  
  isSaving.value = true
  try {
    const updatedUser = await updateProfile({
      name: editForm.value.name || undefined,
      bio: editForm.value.bio || undefined,
      province: editForm.value.province || undefined,
      district: editForm.value.district || undefined
    })
    
    // Update the app store with the new user data
    if (appStore.currentUser) {
      appStore.currentUser.name = updatedUser.name
      appStore.currentUser.bio = updatedUser.bio
      appStore.currentUser.province = updatedUser.province
      appStore.currentUser.district = updatedUser.district
      
      // Update location field
      if (updatedUser.district && updatedUser.province) {
        appStore.currentUser.location = `${updatedUser.district}, ${updatedUser.province}`
      } else if (updatedUser.province) {
        appStore.currentUser.location = updatedUser.province
      } else if (updatedUser.district) {
        appStore.currentUser.location = updatedUser.district
      }
    }
    
    isEditing.value = false
  } catch (error: any) {
    console.error('Failed to update profile:', error)
    alert(error.message || 'Failed to update profile. Please try again.')
  } finally {
    isSaving.value = false
  }
}

function cancelEdit() {
  isEditing.value = false
  // Reset form to current user data
  editForm.value = {
    name: appStore.currentUser?.name || '',
    bio: appStore.currentUser?.bio || '',
    province: appStore.currentUser?.province || '',
    district: appStore.currentUser?.district || ''
  }
}


// Load user's services
onMounted(async () => {
  if (appStore.currentUser) {
    // Load services
    loadingServices.value = true
    try {
      userServices.value = await getUserServices(String(appStore.currentUser.id))
      
      // Load handshakes for each service
      for (const service of userServices.value) {
        await loadHandshakesForService(service.id, service.type)
      }
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

const loadHandshakesForService = async (serviceId: string, serviceType: 'OFFER' | 'REQUEST') => {
  try {
    let handshakes: Handshake[]
    if (serviceType === 'OFFER') {
      handshakes = await getHandshakesByOfferId(Number(serviceId))
    } else {
      handshakes = await getHandshakesByRequestId(Number(serviceId))
    }
    serviceHandshakes.value.set(serviceId, handshakes)
  } catch (error) {
    console.error(`Failed to load handshakes for service ${serviceId}:`, error)
    serviceHandshakes.value.set(serviceId, [])
  }
}

const toggleServiceExpanded = (serviceId: string) => {
  if (expandedServices.value.has(serviceId)) {
    expandedServices.value.delete(serviceId)
  } else {
    expandedServices.value.add(serviceId)
  }
}

const getHandshakesForService = (serviceId: string): Handshake[] => {
  return serviceHandshakes.value.get(serviceId) || []
}

const getHandshakeStatusColor = (status: string): string => {
  switch (status) {
    case 'PENDING':
      return 'bg-yellow-100 text-yellow-700'
    case 'CONFIRMED':
      return 'bg-blue-100 text-blue-700'
    case 'COMPLETED':
      return 'bg-green-100 text-green-700'
    case 'CANCELLED':
      return 'bg-gray-100 text-gray-700'
    default:
      return 'bg-gray-100 text-gray-700'
  }
}

const viewService = (serviceId: string) => {
  appStore.setSelectedServiceId(serviceId)
}

async function handleDeactivateService(service: Service) {
  // Check if service can be deactivated
  const handshakes = getHandshakesForService(service.id)
  const hasActiveHandshakes = handshakes.some(h => 
    h.status !== 'COMPLETED' && h.status !== 'CANCELLED'
  )
  
  if (hasActiveHandshakes) {
    alert('Cannot deactivate service with pending or confirmed handshakes. Please wait until all handshakes are completed or cancelled.')
    return
  }
  
  const confirmMessage = handshakes.length > 0
    ? `Are you sure you want to deactivate "${service.title}"? All handshakes for this service are completed or cancelled.`
    : `Are you sure you want to deactivate "${service.title}"? This service has no handshakes.`
  
  if (!confirm(confirmMessage)) {
    return
  }
  
  deactivatingServices.value.add(service.id)
  
  try {
    await deactivateService(service.id, service.type)
    
    // Update the service status in the local list
    const serviceIndex = userServices.value.findIndex(s => s.id === service.id)
    if (serviceIndex !== -1) {
      userServices.value[serviceIndex].status = 'archived'
    }
    
    alert('Service deactivated successfully!')
  } catch (error: any) {
    console.error('Failed to deactivate service:', error)
    alert(error.message || 'Failed to deactivate service. Please try again.')
  } finally {
    deactivatingServices.value.delete(service.id)
  }
}
</script>

