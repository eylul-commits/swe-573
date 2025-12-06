<template>
  <div class="flex-1 bg-gray-50 overflow-y-auto">
    <div class="max-w-4xl mx-auto">
      <!-- Back Button -->
      <div class="bg-white border-b border-gray-200 sticky top-0 z-10">
        <div class="p-6">
          <Button 
            variant="ghost" 
            @click="appStore.setSelectedServiceId(null)"
            class="hover:bg-gray-100"
          >
            <ArrowLeft class="w-4 h-4 mr-2" />
            Back to Services
          </Button>
        </div>
      </div>

      <!-- Service not found -->
      <div v-if="!service" class="flex-1 flex items-center justify-center bg-gray-50 min-h-[400px]">
        <div class="text-center">
          <h2 class="text-gray-900 mb-2">Service not found</h2>
          <Button @click="appStore.setSelectedServiceId(null)" class="bg-gray-900 hover:bg-gray-800 text-white">
            <ArrowLeft class="w-4 h-4 mr-2" />
            Go Back
          </Button>
        </div>
      </div>

      <!-- Main Content -->
      <div v-else class="p-6 space-y-6">
        <!-- Service Header Card -->
        <Card class="p-6">
          <!-- Featured Image -->
          <div v-if="serviceImages[serviceId]" class="mb-6 -mt-6 -mx-6 overflow-hidden rounded-t-lg">
            <ImageWithFallback 
              :src="serviceImages[serviceId]"
              :alt="service.title"
              class="w-full h-64 object-cover"
            />
          </div>
          
          <div class="flex items-start justify-between mb-4">
            <div class="flex-1">
              <h1 class="text-gray-900 mb-3">{{ service.title }}</h1>
              <p class="text-gray-600">
                {{ service.description }}
              </p>
            </div>
            <Badge :class="service.type === 'OFFER' ? 'bg-emerald-100 text-emerald-700 hover:bg-emerald-100 ml-4' : 'bg-blue-100 text-blue-700 hover:bg-blue-100 ml-4'">
              {{ service.type }}
            </Badge>
          </div>

          <!-- Tags -->
          <div class="flex gap-2 mb-6">
            <Badge v-for="(tag, index) in service.tags" :key="index" variant="outline">{{ tag }}</Badge>
          </div>

          <!-- Service Details Grid -->
          <div class="grid grid-cols-2 gap-6 mb-6 pb-6 border-b border-gray-200">
            <div class="space-y-1">
              <div class="text-sm text-gray-500">Location</div>
              <div class="flex items-center gap-2 text-gray-900">
                <MapPin class="w-4 h-4 text-gray-400" />
                <span>{{ service.location }}, Istanbul</span>
              </div>
              <div class="text-sm text-gray-500">{{ service.distance }} away</div>
            </div>
            <div class="space-y-1">
              <div class="text-sm text-gray-500">TimeBank Hours</div>
              <div class="flex items-center gap-2 text-amber-600">
                <Clock class="w-4 h-4" />
                <span>{{ service.timebank }}</span>
              </div>
            </div>
            <div class="space-y-1">
              <div class="text-sm text-gray-500">Availability</div>
              <div class="flex items-center gap-2 text-gray-900">
                <Calendar class="w-4 h-4 text-gray-400" />
                <span>
                  {{ getAvailability(serviceId) }}
                </span>
              </div>
            </div>
            <div class="space-y-1">
              <div class="text-sm text-gray-500">Posted</div>
              <div class="text-gray-900">
                {{ serviceId === '9' ? '3 hours ago' : '2 days ago' }}
              </div>
            </div>
          </div>

          <!-- Call to Action -->
          <Button 
            @click="handleAcceptService"
            :disabled="isAccepting || service.poster.id === appStore.currentUser?.id.toString()"
            class="w-full bg-gray-900 hover:bg-gray-800 text-white h-12 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            <span v-if="isAccepting">{{ service.type === 'OFFER' ? 'Accepting...' : 'Sending Request...' }}</span>
            <span v-else-if="service.poster.id === appStore.currentUser?.id.toString()">Your Own Service</span>
            <span v-else>{{ service.type === 'OFFER' ? 'Accept This Offer' : 'Offer Your Help' }}</span>
          </Button>
          <p v-if="acceptError" class="text-sm text-red-600 mt-2">{{ acceptError }}</p>
          <p v-if="acceptSuccess" class="text-sm text-emerald-600 mt-2">{{ acceptSuccess }}</p>
        </Card>

        <!-- Service Ratings Card -->
        <Card v-if="reviewCount > 0" class="p-6">
          <h2 class="text-gray-900 mb-4">Service Ratings ({{ reviewCount }} {{ reviewCount === 1 ? 'review' : 'reviews' }})</h2>
          <div class="grid grid-cols-2 gap-4">
            <div class="space-y-1">
              <div class="text-sm text-gray-700">Punctuality</div>
              <div class="flex items-center gap-2">
                <div class="flex items-center gap-0.5">
                  <Star v-for="star in 5" :key="star" :class="['w-4 h-4', star <= Math.round(ratings?.summary.punctuality || 0) ? 'fill-amber-400 text-amber-400' : 'fill-gray-200 text-gray-200']" />
                </div>
                <span class="text-sm text-gray-600">{{ ratings?.summary.punctuality.toFixed(1) }}</span>
              </div>
            </div>
            <div class="space-y-1">
              <div class="text-sm text-gray-700">Friendly</div>
              <div class="flex items-center gap-2">
                <div class="flex items-center gap-0.5">
                  <Star v-for="star in 5" :key="star" :class="['w-4 h-4', star <= Math.round(ratings?.summary.friendliness || 0) ? 'fill-amber-400 text-amber-400' : 'fill-gray-200 text-gray-200']" />
                </div>
                <span class="text-sm text-gray-600">{{ ratings?.summary.friendliness.toFixed(1) }}</span>
              </div>
            </div>
            <div class="space-y-1">
              <div class="text-sm text-gray-700">Communicative</div>
              <div class="flex items-center gap-2">
                <div class="flex items-center gap-0.5">
                  <Star v-for="star in 5" :key="star" :class="['w-4 h-4', star <= Math.round(ratings?.summary.communicative || 0) ? 'fill-amber-400 text-amber-400' : 'fill-gray-200 text-gray-200']" />
                </div>
                <span class="text-sm text-gray-600">{{ ratings?.summary.communicative.toFixed(1) }}</span>
              </div>
            </div>
            <div class="space-y-1">
              <div class="text-sm text-gray-700">Prepared</div>
              <div class="flex items-center gap-2">
                <div class="flex items-center gap-0.5">
                  <Star v-for="star in 5" :key="star" :class="['w-4 h-4', star <= Math.round(ratings?.summary.preparedness || 0) ? 'fill-amber-400 text-amber-400' : 'fill-gray-200 text-gray-200']" />
                </div>
                <span class="text-sm text-gray-600">{{ ratings?.summary.preparedness.toFixed(1) }}</span>
              </div>
            </div>
          </div>
        </Card>

        <Card v-else class="p-8">
          <div class="text-center">
            <Star class="w-16 h-16 text-gray-300 mx-auto mb-4" />
            <h3 class="text-gray-900 mb-2">No ratings yet</h3>
            <p class="text-sm text-gray-600">
              This is a new service. Be the first to exchange and leave a rating!
            </p>
          </div>
        </Card>

        <!-- Provider Info Card -->
        <Card class="p-6">
          <h2 class="text-gray-900 mb-4">{{ service.type === 'OFFER' ? 'Offered by' : 'Requested by' }}</h2>
          <div class="flex items-start gap-4">
            <Avatar class="w-16 h-16">
              <AvatarImage :src="service.poster.avatar" :alt="service.poster.name" />
            </Avatar>
            <div class="flex-1">
              <div class="flex items-center gap-2 mb-2">
                <div class="text-gray-900">{{ service.poster.name }}</div>
                <div class="flex items-center gap-1 text-sm">
                  <span>{{ badge.emoji }}</span>
                  <span class="text-gray-600">{{ badge.label }}</span>
                </div>
              </div>
              <div class="flex items-center gap-4 text-sm mb-3">
                <span class="text-emerald-600">{{ service.poster.hoursGiven }}h given</span>
                <span class="text-gray-300">•</span>
                <span class="text-blue-600">{{ service.poster.hoursReceived }}h received</span>
              </div>
              <p class="text-sm text-gray-600">
                {{ serviceId === '9' 
                  ? 'New to The Hive and excited to share skills and connect with the community!'
                  : 'Active community member sharing skills and building connections through The Hive.'
                }}
              </p>
            </div>
          </div>
        </Card>

        <!-- Tabs for Questions and Reviews -->
        <Card class="p-6">
          <Tabs default-value="questions" class="w-full">
            <TabsList class="w-full justify-start bg-transparent border-b rounded-none h-auto p-0 mb-6">
              <TabsTrigger 
                value="questions" 
                class="rounded-none border-b-2 border-transparent data-[state=active]:border-gray-900 data-[state=active]:bg-transparent"
              >
                <MessageCircle class="w-4 h-4 mr-2" />
                Questions ({{ questions.length }})
              </TabsTrigger>
              <TabsTrigger 
                value="reviews" 
                class="rounded-none border-b-2 border-transparent data-[state=active]:border-gray-900 data-[state=active]:bg-transparent"
              >
                <Star class="w-4 h-4 mr-2" />
                Reviews ({{ serviceRatings.length }})
              </TabsTrigger>
            </TabsList>

            <TabsContent value="questions" class="space-y-4">
              <!-- Ask a Question Form -->
              <div class="bg-gray-50 border border-gray-200 rounded-lg p-4">
                <div class="text-sm text-gray-700 mb-3">Ask a Question</div>
                <Textarea
                  placeholder="Type your question here..."
                  v-model="questionText"
                  class="mb-3 min-h-[100px]"
                />
                <Button 
                  @click="handleAskQuestion"
                  class="bg-gray-900 hover:bg-gray-800 text-white w-full"
                  :disabled="!questionText.trim()"
                >
                  <Send class="w-4 h-4 mr-2" />
                  Send Question
                </Button>
              </div>

              <!-- Existing Questions -->
              <div v-if="questions.length > 0">
                <div v-for="q in questions" :key="q.id" class="space-y-3 bg-white border border-gray-200 rounded-lg p-4">
                  <div class="flex items-start gap-3">
                    <Avatar class="w-8 h-8">
                      <AvatarImage :src="q.author.avatar" :alt="q.author.name" />
                    </Avatar>
                    <div class="flex-1">
                      <div class="flex items-center gap-2 mb-1">
                        <div class="text-sm text-gray-900">{{ q.author.name }}</div>
                        <span class="text-xs text-gray-400">•</span>
                        <div class="text-xs text-gray-500">{{ q.createdAt }}</div>
                      </div>
                      <p class="text-sm text-gray-700">{{ q.content }}</p>
                      
                      <div v-if="q.answer" class="mt-3 pl-4 border-l-2 border-emerald-200 bg-emerald-50 p-3 rounded">
                        <div class="flex items-center gap-2 mb-1">
                          <div class="text-xs text-emerald-700">Answer from {{ q.answer.responder.name }}</div>
                          <span class="text-xs text-gray-400">•</span>
                          <div class="text-xs text-gray-500">{{ q.answer.createdAt }}</div>
                        </div>
                        <p class="text-sm text-gray-700">{{ q.answer.content }}</p>
                      </div>

                      <div v-else class="mt-2 text-xs text-gray-500 italic">
                        Waiting for answer...
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <div v-else class="text-center text-gray-500 text-sm py-8">
                No questions yet. Be the first to ask!
              </div>
            </TabsContent>

            <TabsContent value="reviews" class="space-y-4">
              <div v-if="serviceRatings.length > 0">
                <div v-for="rating in serviceRatings" :key="rating.id" class="space-y-3 pb-4 border-b border-gray-200 last:border-0 last:pb-0">
                  <div class="flex items-start gap-3">
                    <Avatar class="w-10 h-10">
                      <AvatarImage :src="rating.rater.avatar" :alt="rating.rater.name" />
                    </Avatar>
                    <div class="flex-1">
                      <div class="flex items-center gap-2 mb-2">
                        <div class="text-gray-900 text-sm">{{ rating.rater.name }}</div>
                        <span class="text-xs text-gray-400">•</span>
                        <div class="text-xs text-gray-500">{{ rating.createdAt }}</div>
                      </div>
                      
                      <!-- Individual ratings -->
                      <div class="grid grid-cols-2 gap-2 mb-3">
                        <div class="flex items-center gap-2">
                          <span class="text-xs text-gray-600 w-24">Punctuality:</span>
                          <div class="flex items-center gap-0.5">
                            <Star v-for="star in 5" :key="star" :class="['w-3 h-3', star <= rating.punctuality ? 'fill-amber-400 text-amber-400' : 'fill-gray-200 text-gray-200']" />
                          </div>
                        </div>
                        <div class="flex items-center gap-2">
                          <span class="text-xs text-gray-600 w-24">Friendly:</span>
                          <div class="flex items-center gap-0.5">
                            <Star v-for="star in 5" :key="star" :class="['w-3 h-3', star <= rating.friendliness ? 'fill-amber-400 text-amber-400' : 'fill-gray-200 text-gray-200']" />
                          </div>
                        </div>
                        <div class="flex items-center gap-2">
                          <span class="text-xs text-gray-600 w-24">Communicative:</span>
                          <div class="flex items-center gap-0.5">
                            <Star v-for="star in 5" :key="star" :class="['w-3 h-3', star <= rating.communicative ? 'fill-amber-400 text-amber-400' : 'fill-gray-200 text-gray-200']" />
                          </div>
                        </div>
                        <div class="flex items-center gap-2">
                          <span class="text-xs text-gray-600 w-24">Prepared:</span>
                          <div class="flex items-center gap-0.5">
                            <Star v-for="star in 5" :key="star" :class="['w-3 h-3', star <= rating.preparedness ? 'fill-amber-400 text-amber-400' : 'fill-gray-200 text-gray-200']" />
                          </div>
                        </div>
                      </div>
                      
                      <p class="text-sm text-gray-600">{{ rating.comment }}</p>
                    </div>
                  </div>
                </div>
              </div>

              <div v-else class="text-center py-8">
                <Star class="w-16 h-16 text-gray-300 mx-auto mb-4" />
                <p class="text-gray-600">No reviews yet</p>
                <p class="text-sm text-gray-500 mt-1">
                  Complete a service exchange to leave the first review!
                </p>
              </div>
            </TabsContent>
          </Tabs>
        </Card>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { MapPin, Clock, Calendar, Star, Send, MessageCircle, ArrowLeft } from 'lucide-vue-next'
import Card from '../ui/Card.vue'
import Badge from '../ui/Badge.vue'
import Button from '../ui/Button.vue'
import Avatar from '../ui/Avatar.vue'
import AvatarImage from '../ui/AvatarImage.vue'
import Tabs from '../ui/Tabs.vue'
import TabsContent from '../ui/TabsContent.vue'
import TabsList from '../ui/TabsList.vue'
import TabsTrigger from '../ui/TabsTrigger.vue'
import Textarea from '../ui/Textarea.vue'
import ImageWithFallback from '../ui/ImageWithFallback.vue'
import { getServiceById, getServiceRatings, getServiceQuestions  } from '../../services/dataService'
import { createHandshake } from '../../services/handshakeService'
import { createHandshakeChannel, isStreamChatInitialized } from '../../clients/streamChatClient'
import { useAppStore } from '../../stores/appStore'
import type { Service, ServiceQuestion, ServiceRatingsResponse } from '../../types'

const appStore = useAppStore()

const serviceId = computed(() => appStore.selectedServiceId || '')
const service = ref<Service | null>(null)
const isAccepting = ref(false)
const acceptError = ref('')
const acceptSuccess = ref('')
const ratings = ref<ServiceRatingsResponse | null>(null)
const questions = ref<ServiceQuestion[]>([])

watch(() => appStore.selectedServiceId, async (newId) => {
  if (newId) {
    const [fetchedService, ratingsResponse, questionsResponse] = await Promise.all([
      getServiceById(newId),
      getServiceRatings(newId),
      getServiceQuestions(newId),
    ])
    
    service.value = fetchedService || null
    ratings.value = ratingsResponse
    questions.value = questionsResponse
  } else {
    service.value = null
    ratings.value = null
    questions.value = []
  }
}, { immediate: true })

const reviewCount = computed(() => ratings.value?.summary.totalReviews || 0)
const serviceRatings = computed(() => ratings.value?.ratings || [])

const questionText = ref('')

// Service images mapping
const serviceImages: Record<string, string> = {
  "1": "https://images.unsplash.com/photo-1506126613408-eca07ce68773?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxtZWRpdGF0aW9uJTIwYm9zcGhvcnVzfGVufDF8fHx8MTc2MTA1MjQ0N3ww&ixlib=rb-4.1.0&q=80&w=1080",
  "2": "https://images.unsplash.com/photo-1416879595882-3373a0480b5b?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxiYWxjb255JTIwZ2FyZGVuJTIwcGxhbnRzfGVufDF8fHx8MTc2MTA1MjQ0N3ww&ixlib=rb-4.1.0&q=80&w=1080",
  "3": "https://images.unsplash.com/photo-1452587925148-ce544e77e70d?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxzdHJlZXQlMjBwaG90b2dyYXBoeSUyMGNpdHl8ZW58MXx8fHwxNzYxMDUyNDQ3fDA&ixlib=rb-4.1.0&q=80&w=1080",
  "4": "https://images.unsplash.com/photo-1503676260728-1c00da094a0b?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxlbmdsaXNoJTIwY29udmVyc2F0aW9ufGVufDF8fHx8MTc2MTA1MjQ0N3ww&ixlib=rb-4.1.0&q=80&w=1080",
  "5": "https://images.unsplash.com/photo-1600001793718-012b51a11a78?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHx0dXJraXNoJTIwY29va2luZyUyMGtpdGNoZW58ZW58MXx8fHwxNzYxMDUyNDQ4fDA&ixlib=rb-4.1.0&q=80&w=1080",
  "6": "https://images.unsplash.com/photo-1612515809491-1e0a8b3697e8?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxzaWduJTIwbGFuZ3VhZ2V8ZW58MXx8fHwxNzYxMDUyNDQ3fDA&ixlib=rb-4.1.0&q=80&w=1080",
  "7": "https://images.unsplash.com/photo-1544367567-0f2fcb009e0b?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHx5b2dhJTIwc2VhfGVufDF8fHx8MTc2MTA1MjQ0N3ww&ixlib=rb-4.1.0&q=80&w=1080",
  "8": "https://images.unsplash.com/photo-1556910096-6f5e72db6803?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHx0dXJraXNoJTIwaG9tZSUyMGNvb2tpbmd8ZW58MXx8fHwxNzYxMDUyNDQ3fDA&ixlib=rb-4.1.0&q=80&w=1080",
  "9": "https://images.unsplash.com/photo-1524995997946-a1c2e315a42f?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxib29rcyUyMGNvbW11bml0eXxlbnwxfHx8fDE3NjEwNTI0NDd8MA&ixlib=rb-4.1.0&q=80&w=1080",
}

// Helper function to determine user badge
const getUserBadge = (hoursGiven: number, _hoursReceived: number, balance: number) => {
  if (hoursGiven >= 40) return { emoji: "🏆", label: "Top Contributor" }
  if (hoursGiven >= 20) return { emoji: "⭐", label: "Active Member" }
  if (Math.abs(balance) <= 5 && hoursGiven >= 10) return { emoji: "⚖️", label: "Balanced Exchanger" }
  return { emoji: "🌱", label: "Newcomer" }
}

const badge = computed(() => {
  if (!service.value) return { emoji: "🌱", label: "Newcomer" }
  return getUserBadge(
    service.value.poster.hoursGiven,
    service.value.poster.hoursReceived,
    service.value.poster.timebankBalance
  )
})

const getAvailability = (id: string) => {
  if (id === "9") return "Starting Nov 1, 2025"
  if (id === "2") return "Weekday evenings"
  return "Flexible scheduling"
}

const handleAskQuestion = () => {
  if (questionText.value.trim()) {
    // TODO: Implement backend API call to create a question
    console.log("Asking question:", questionText.value)
    questionText.value = ""
  }
}

const handleAcceptService = async () => {
  if (!service.value || !appStore.currentUser) {
    acceptError.value = 'Please log in to accept services'
    return
  }

  // Don't allow users to accept their own services
  if (service.value.poster.id === appStore.currentUser.id.toString()) {
    acceptError.value = 'You cannot accept your own service'
    return
  }

  isAccepting.value = true
  acceptError.value = ''
  acceptSuccess.value = ''

  try {
    // Parse the timebank hours (e.g., "2h" -> 2)
    const agreedHours = parseInt(service.value.timebank.replace(/[^\d]/g, '')) || 1

    // Create the handshake request (pass either offerId or requestId based on type)
    const handshakeRequest: any = {
      providerId: parseInt(service.value.poster.id),
      agreedHours: agreedHours
    }
    
    if (service.value.type === 'OFFER') {
      handshakeRequest.offerId = parseInt(service.value.id)
    } else {
      handshakeRequest.requestId = parseInt(service.value.id)
    }
    
    const handshake = await createHandshake(handshakeRequest)

    console.log('Handshake created:', handshake)
    
    // Create Stream Chat channel for this handshake
    if (isStreamChatInitialized()) {
      try {
        const channel = await createHandshakeChannel(handshake)
        console.log('Stream Chat channel created:', channel.id)
        
        // Send initial system message
        await channel.sendMessage({
          text: `🤝 ${service.value.type === 'OFFER' ? 'Offer accepted' : 'Help offered'}! You can now coordinate the details for "${handshake.offerTitle}". Duration: ${handshake.agreedHours} hours.`,
          type: 'system',
        })
      } catch (chatError) {
        console.error('Failed to create Stream Chat channel:', chatError)
        // Don't fail the handshake if chat fails
      }
    } else {
      console.warn('Stream Chat not initialized. Channel will be created later.')
    }
    
    // Show success message
    acceptSuccess.value = service.value.type === 'OFFER' 
      ? '✅ Offer accepted! A chat has been created to coordinate the details.'
      : '✅ Help offered! A chat has been created to coordinate the details.'

    // Navigate to messages page to show the new chat
    setTimeout(() => {
      appStore.setCurrentPage('messages')
    }, 2000)

  } catch (error: any) {
    console.error('Failed to accept service:', error)
    acceptError.value = error.response?.data?.message || 'Failed to accept service. Please try again.'
  } finally {
    isAccepting.value = false
  }
}
</script>

