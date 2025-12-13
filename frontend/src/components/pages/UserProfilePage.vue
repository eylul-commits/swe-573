<template>
  <div class="flex-1 overflow-y-auto bg-gray-50 p-8">
    <div class="max-w-6xl mx-auto">
      <h1 class="text-gray-900 mb-6">User Profile</h1>
      
      <div v-if="loading" class="text-center py-12">
        <div class="text-gray-500">Loading profile...</div>
      </div>

      <div v-else-if="error" class="text-center py-12">
        <div class="text-red-500 mb-4">{{ error }}</div>
        <Button @click="loadProfile" variant="outline">Retry</Button>
      </div>

      <div v-else-if="userProfile">
        <!-- User Info Card -->
        <Card class="p-6 mb-6">
          <div class="flex items-center gap-6 mb-6">
            <Avatar class="w-20 h-20">
              <AvatarImage 
                :src="getAvatarUrl(userProfile.avatar, userProfile.name)" 
                :alt="userProfile.name" 
              />
              <AvatarFallback>{{ userProfile.name.charAt(0) }}</AvatarFallback>
            </Avatar>
            <div class="flex-1">
              <div class="text-gray-900 text-xl mb-1">{{ userProfile.name }}</div>
              <div class="text-gray-600 mb-2">{{ userProfile.location || 'Location not set' }}</div>
              <div v-if="userProfile.bio" class="text-gray-700 text-sm mt-3">
                {{ userProfile.bio }}
              </div>
            </div>
          </div>
          <div class="grid grid-cols-3 gap-4">
            <div>
              <div class="text-2xl text-gray-900">{{ userProfile.timebankBalance ?? 0 }}h</div>
              <div class="text-sm text-gray-600">Balance</div>
            </div>
            <div>
              <div class="text-2xl text-gray-900">{{ userProfile.hoursGiven ?? 0 }}h</div>
              <div class="text-sm text-gray-600">Given</div>
            </div>
            <div>
              <div class="text-2xl text-gray-900">{{ userProfile.hoursReceived ?? 0 }}h</div>
              <div class="text-sm text-gray-600">Received</div>
            </div>
          </div>
        </Card>

        <!-- Tabs for Services and Reviews -->
        <Card class="p-6">
          <Tabs default-value="services" class="w-full">
            <TabsList class="w-full grid grid-cols-2 bg-gray-100 p-1 mb-6">
              <TabsTrigger
                value="services"
                class="data-[state=active]:bg-gray-900 data-[state=active]:text-white rounded-lg"
              >
                Services
              </TabsTrigger>
              <TabsTrigger
                value="reviews"
                class="data-[state=active]:bg-gray-900 data-[state=active]:text-white rounded-lg"
              >
                Reviews
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
                <p class="text-gray-600">This user hasn't created any offers or requests</p>
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

            <!-- Reviews Tab -->
            <TabsContent value="reviews">
              <div v-if="loadingReviews" class="text-center py-8">
                <div class="text-gray-500">Loading reviews...</div>
              </div>
              <div v-else-if="userRatings.length === 0" class="text-center py-12">
                <div class="text-gray-400 text-5xl mb-4">⭐</div>
                <h3 class="text-lg font-semibold text-gray-900 mb-2">No reviews yet</h3>
                <p class="text-gray-600">This user hasn't received any reviews</p>
              </div>
              <div v-else class="space-y-4">
                <div
                  v-for="rating in userRatings"
                  :key="rating.id"
                  class="p-4 border border-gray-200 rounded-lg"
                >
                  <div class="flex items-start gap-4 mb-3">
                    <Avatar 
                      class="w-10 h-10 cursor-pointer hover:opacity-80 transition-opacity"
                      @click="viewUserProfile(rating.rater.id)"
                    >
                      <AvatarImage 
                        :src="getAvatarUrl(rating.rater.avatar, rating.rater.name)" 
                        :alt="rating.rater.name" 
                      />
                      <AvatarFallback>{{ rating.rater.name.charAt(0) }}</AvatarFallback>
                    </Avatar>
                    <div class="flex-1">
                      <div 
                        class="font-medium text-gray-900 cursor-pointer hover:text-gray-700 transition-colors"
                        @click="viewUserProfile(rating.rater.id)"
                      >
                        {{ rating.rater.name }}
                      </div>
                      <div class="text-xs text-gray-500">{{ formatDate(rating.createdAt) }}</div>
                    </div>
                  </div>
                  
                  <div class="grid grid-cols-2 gap-4 mb-3">
                    <div>
                      <div class="text-xs text-gray-600 mb-1">Punctuality</div>
                      <div class="flex items-center gap-1">
                        <span class="text-sm font-medium">{{ rating.punctuality }}/5</span>
                        <div class="flex">
                          <span v-for="i in 5" :key="i" class="text-yellow-400">
                            {{ i <= rating.punctuality ? '★' : '☆' }}
                          </span>
                        </div>
                      </div>
                    </div>
                    <div>
                      <div class="text-xs text-gray-600 mb-1">Friendliness</div>
                      <div class="flex items-center gap-1">
                        <span class="text-sm font-medium">{{ rating.friendliness }}/5</span>
                        <div class="flex">
                          <span v-for="i in 5" :key="i" class="text-yellow-400">
                            {{ i <= rating.friendliness ? '★' : '☆' }}
                          </span>
                        </div>
                      </div>
                    </div>
                    <div>
                      <div class="text-xs text-gray-600 mb-1">Communicative</div>
                      <div class="flex items-center gap-1">
                        <span class="text-sm font-medium">{{ rating.communicative }}/5</span>
                        <div class="flex">
                          <span v-for="i in 5" :key="i" class="text-yellow-400">
                            {{ i <= rating.communicative ? '★' : '☆' }}
                          </span>
                        </div>
                      </div>
                    </div>
                    <div>
                      <div class="text-xs text-gray-600 mb-1">Preparedness</div>
                      <div class="flex items-center gap-1">
                        <span class="text-sm font-medium">{{ rating.preparedness }}/5</span>
                        <div class="flex">
                          <span v-for="i in 5" :key="i" class="text-yellow-400">
                            {{ i <= rating.preparedness ? '★' : '☆' }}
                          </span>
                        </div>
                      </div>
                    </div>
                  </div>
                  
                  <div v-if="rating.comment" class="text-sm text-gray-700 mt-3 pt-3 border-t border-gray-200">
                    {{ rating.comment }}
                  </div>
                </div>
              </div>
            </TabsContent>
          </Tabs>
        </Card>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Clock, MapPin } from 'lucide-vue-next'
import Card from '../ui/Card.vue'
import Badge from '../ui/Badge.vue'
import Button from '../ui/Button.vue'
import Tabs from '../ui/Tabs.vue'
import TabsContent from '../ui/TabsContent.vue'
import TabsList from '../ui/TabsList.vue'
import TabsTrigger from '../ui/TabsTrigger.vue'
import Avatar from '../ui/Avatar.vue'
import AvatarImage from '../ui/AvatarImage.vue'
import AvatarFallback from '../ui/AvatarFallback.vue'
import { useAppStore } from '../../stores/appStore'
import { getUserById } from '../../services/authService'
import { getUserServices } from '../../services/marketplaceService'
import { getUserRatings } from '../../services/handshakeService'
import { getAvatarUrl } from '../../utils/avatarUtils'
import type { User, Service, ServiceRating } from '../../types'

const props = defineProps<{
  userId: string
}>()

const appStore = useAppStore()

const userProfile = ref<User | null>(null)
const userServices = ref<Service[]>([])
const userRatings = ref<ServiceRating[]>([])
const loading = ref(false)
const loadingServices = ref(false)
const loadingReviews = ref(false)
const error = ref<string | null>(null)

function formatDate(dateString: string): string {
  const date = new Date(dateString)
  return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })
}

async function loadProfile() {
  if (!props.userId) {
    error.value = 'User ID is required'
    return
  }

  loading.value = true
  error.value = null

  try {
    const userIdNum = parseInt(props.userId, 10)
    if (isNaN(userIdNum)) {
      throw new Error('Invalid user ID')
    }

    // Load user profile
    const user = await getUserById(userIdNum)
    
    // Convert to User type (matching frontend User interface)
    userProfile.value = {
      id: user.id.toString(),
      name: user.name,
      avatar: user.avatarUrl || '',
      hoursGiven: user.hoursGiven || 0,
      hoursReceived: user.hoursReceived || 0,
      timebankBalance: user.timebankBalance || user.balanceHours || 0,
      bio: user.bio,
      location: user.location || (user.district ? `${user.district}${user.province ? `, ${user.province}` : ''}` : undefined),
      province: user.province,
      district: user.district,
      geohash: user.geohash,
    }

    // Load services
    loadingServices.value = true
    try {
      userServices.value = await getUserServices(props.userId)
    } catch (err) {
      console.error('Failed to load user services:', err)
    } finally {
      loadingServices.value = false
    }

    // Load reviews
    loadingReviews.value = true
    try {
      const ratings = await getUserRatings(userIdNum)
      // Convert ratings to match ServiceRating type
      userRatings.value = ratings.map((rating: any) => ({
        id: rating.id.toString(),
        comment: rating.comment,
        createdAt: rating.createdAt,
        rater: {
          id: rating.rater.id.toString(),
          name: rating.rater.name,
          avatar: rating.rater.avatar || '',
          badge: rating.rater.badge,
        },
        punctuality: rating.punctuality || 0,
        friendliness: rating.friendliness || 0,
        communicative: rating.communicative || 0,
        preparedness: rating.preparedness || 0,
      }))
    } catch (err) {
      console.error('Failed to load user ratings:', err)
    } finally {
      loadingReviews.value = false
    }
  } catch (err: any) {
    console.error('Failed to load user profile:', err)
    error.value = err.message || 'Failed to load user profile'
  } finally {
    loading.value = false
  }
}

const viewService = (serviceId: string) => {
  appStore.setSelectedServiceId(serviceId)
}

const viewUserProfile = (userId: string) => {
  appStore.setSelectedUserId(userId)
}

onMounted(() => {
  loadProfile()
})
</script>

