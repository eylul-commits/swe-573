<template>
  <div v-if="service" class="h-full overflow-y-auto">
    <div class="p-6 space-y-6">
      <!-- Service Header -->
      <div>
        <div class="flex items-start justify-between mb-4">
          <div class="flex-1">
            <h2 class="text-gray-900 mb-2">{{ service.title }}</h2>
            <p class="text-gray-600">
              {{ service.description }}
            </p>
          </div>
          <Badge :class="service.type === 'OFFER' ? 'bg-emerald-100 text-emerald-700 hover:bg-emerald-100' : 'bg-blue-100 text-blue-700 hover:bg-blue-100'">
            {{ service.type }}
          </Badge>
        </div>

        <!-- Tags -->
        <div class="flex gap-2 mb-4">
          <Badge v-for="(tag, index) in service.tags" :key="index" variant="outline" class="text-xs">{{ tag }}</Badge>
        </div>

        <!-- Service Details -->
        <div class="grid grid-cols-2 gap-4 mb-4">
          <div class="flex items-center gap-2 text-sm text-gray-600">
            <MapPin class="w-4 h-4" />
            <span>{{ service.location }}</span>
          </div>
          <div class="flex items-center gap-2 text-sm text-gray-600">
            <Clock class="w-4 h-4 text-amber-600" />
            <span class="text-amber-600">{{ service.timebank }}</span>
          </div>
          <div class="flex items-center gap-2 text-sm text-gray-600">
            <Calendar class="w-4 h-4" />
            <span>
              {{ formattedStartDate }}
            </span>
          </div>
          <div class="text-sm text-gray-600">
            {{ scheduleText }}
          </div>
          <div class="text-sm text-gray-500 col-span-2">
            Posted {{ postedTime }}
          </div>
        </div>
      </div>

      <Separator />

      <!-- Service Ratings -->
      <div v-if="averageRatings">
        <h3 class="text-gray-900 mb-4">Service Ratings ({{ totalReviews }} reviews)</h3>
        <div class="grid grid-cols-2 gap-4">
          <div class="space-y-1">
            <div class="text-sm text-gray-700">Showed Up</div>
            <div class="flex items-center gap-2">
              <div class="flex items-center gap-0.5">
                <Star v-for="star in 5" :key="star" :class="['w-4 h-4', star <= Math.round(averageRatings.showedUp) ? 'fill-amber-400 text-amber-400' : 'fill-gray-200 text-gray-200']" />
              </div>
              <span class="text-sm text-gray-600">{{ averageRatings.showedUp.toFixed(1) }}</span>
            </div>
          </div>
          <div class="space-y-1">
            <div class="text-sm text-gray-700">Friendly</div>
            <div class="flex items-center gap-2">
              <div class="flex items-center gap-0.5">
                <Star v-for="star in 5" :key="star" :class="['w-4 h-4', star <= Math.round(averageRatings.friendly) ? 'fill-amber-400 text-amber-400' : 'fill-gray-200 text-gray-200']" />
              </div>
              <span class="text-sm text-gray-600">{{ averageRatings.friendly.toFixed(1) }}</span>
            </div>
          </div>
          <div class="space-y-1">
            <div class="text-sm text-gray-700">Communicative</div>
            <div class="flex items-center gap-2">
              <div class="flex items-center gap-0.5">
                <Star v-for="star in 5" :key="star" :class="['w-4 h-4', star <= Math.round(averageRatings.communicative) ? 'fill-amber-400 text-amber-400' : 'fill-gray-200 text-gray-200']" />
              </div>
              <span class="text-sm text-gray-600">{{ averageRatings.communicative.toFixed(1) }}</span>
            </div>
          </div>
          <div class="space-y-1">
            <div class="text-sm text-gray-700">Prepared</div>
            <div class="flex items-center gap-2">
              <div class="flex items-center gap-0.5">
                <Star v-for="star in 5" :key="star" :class="['w-4 h-4', star <= Math.round(averageRatings.prepared) ? 'fill-amber-400 text-amber-400' : 'fill-gray-200 text-gray-200']" />
              </div>
              <span class="text-sm text-gray-600">{{ averageRatings.prepared.toFixed(1) }}</span>
            </div>
          </div>
        </div>
      </div>
      <div v-else class="bg-gray-50 border border-gray-200 rounded-lg p-6 text-center">
        <Star class="w-12 h-12 text-gray-300 mx-auto mb-3" />
        <h3 class="text-gray-900 mb-2">No ratings yet</h3>
        <p class="text-sm text-gray-600">
          This is a new service. Be the first to exchange and leave a rating!
        </p>
      </div>

      <Separator />

      <!-- Provider Info -->
      <div>
        <h3 class="text-gray-900 mb-3">{{ service.type === 'OFFER' ? 'Offered by' : 'Requested by' }}</h3>
        <div class="flex items-start justify-between">
          <div class="flex items-start gap-3">
            <Avatar class="w-12 h-12">
              <AvatarImage :src="service.poster.avatar" :alt="service.poster.name" />
            </Avatar>
            <div>
              <div class="text-gray-900">{{ service.poster.name }}</div>
              <div class="flex items-center gap-3 text-sm text-gray-500 mt-1">
                <span class="text-emerald-600">{{ posterBalance }}h balance</span>
                <span>•</span>
                <span class="text-blue-600">{{ posterBadgeLabel }}</span>
              </div>
              <p class="text-sm text-gray-600 mt-2 max-w-lg">
                {{ posterDescription }}
              </p>
            </div>
          </div>
          <div class="space-y-2">
            <Button 
              @click="handleAcceptService"
              :disabled="isAccepting || service.poster.id === appStore.currentUser?.id.toString()"
              class="bg-gray-900 hover:bg-gray-800 text-white w-full disabled:opacity-50 disabled:cursor-not-allowed"
            >
              <span v-if="isAccepting">{{ service.type === 'OFFER' ? 'Accepting...' : 'Sending...' }}</span>
              <span v-else-if="service.poster.id === appStore.currentUser?.id.toString()">Your Service</span>
              <span v-else>{{ service.type === 'OFFER' ? 'Accept Offer' : 'Offer Help' }}</span>
            </Button>
            <p v-if="acceptError" class="text-xs text-red-600">{{ acceptError }}</p>
            <p v-if="acceptSuccess" class="text-xs text-emerald-600">{{ acceptSuccess }}</p>
            <Button v-if="onViewFullDetails" variant="outline" class="w-full" @click="() => onViewFullDetails?.(serviceId)">
              View Full Details
            </Button>
          </div>
        </div>
      </div>

      <Separator />

      <!-- Tabs for Reviews, Questions, and Details -->
      <div>
        <Tabs default-value="questions" class="w-full">
          <TabsList class="w-full justify-start bg-transparent border-b rounded-none h-auto p-0">
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
              Reviews ({{ reviews.length }})
            </TabsTrigger>
          </TabsList>

          <TabsContent value="questions" class="space-y-4 mt-4">
            <!-- Ask a Question Form -->
            <div class="bg-gray-50 border border-gray-200 rounded-lg p-4">
              <div class="text-sm text-gray-700 mb-2">Ask a Question</div>
              <Textarea
                v-model="questionText"
                placeholder="Type your question here..."
                class="mb-3 min-h-[80px]"
              />
              <Button 
                @click="handleAskQuestion"
                class="bg-gray-900 hover:bg-gray-800 text-white w-full"
                :disabled="isLoading || !questionText.trim() || isAskingQuestion"
              >
                <Send class="w-4 h-4 mr-2" />
                <span v-if="isAskingQuestion">Sending...</span>
                <span v-else>Send Question</span>
              </Button>
              <p v-if="questionError" class="text-xs text-red-600 mt-1">{{ questionError }}</p>
            </div>

            <!-- Existing Questions -->
            <div v-if="questions.length > 0" class="space-y-4">
              <div v-for="q in questions" :key="q.id" class="space-y-3 bg-white border border-gray-200 rounded-lg p-4">
                <div class="flex items-start gap-3">
                  <Avatar class="w-8 h-8">
                    <AvatarImage :src="q.avatar" :alt="q.author" />
                  </Avatar>
                  <div class="flex-1">
                    <div class="flex items-center gap-2 mb-1">
                      <div class="text-sm text-gray-900">{{ q.author }}</div>
                      <span class="text-xs text-gray-400">•</span>
                      <div class="text-xs text-gray-500">{{ q.date }}</div>
                    </div>
                    <p class="text-sm text-gray-700">{{ q.question }}</p>
                    
                    <div v-if="q.answer" class="mt-3 pl-4 border-l-2 border-emerald-200 bg-emerald-50 p-3 rounded">
                      <div class="flex items-center gap-2 mb-1">
                        <div class="text-xs text-emerald-700">Answer from {{ q.answeredBy }}</div>
                        <span class="text-xs text-gray-400">•</span>
                        <div class="text-xs text-gray-500">{{ q.answeredDate }}</div>
                      </div>
                      <p class="text-sm text-gray-700">{{ q.answer }}</p>
                    </div>
                    <div v-else-if="service?.poster.id === appStore.currentUser?.id.toString()" class="mt-3 pl-4 border-l-2 border-gray-200 bg-gray-50 p-3 rounded">
                      <div class="text-xs text-gray-700 mb-2">Answer this question:</div>
                      <Textarea
                        v-model="answerTexts[q.id]"
                        placeholder="Type your answer here..."
                        class="mb-2 min-h-[80px]"
                      />
                      <Button 
                        @click="handleAnswerQuestion(q.id)"
                        class="bg-emerald-600 hover:bg-emerald-700 text-white"
                        :disabled="!answerTexts[q.id]?.trim() || isAnswering[q.id]"
                      >
                        <span v-if="isAnswering[q.id]">Sending...</span>
                        <span v-else>Send Answer</span>
                      </Button>
                      <p v-if="answerErrors[q.id]" class="text-xs text-red-600 mt-1">{{ answerErrors[q.id] }}</p>
                    </div>
                    <div v-else class="mt-2 text-xs text-gray-500 italic">
                      Waiting for answer...
                    </div>
                  </div>
                </div>
              </div>
            </div>
            <div v-else class="text-center text-gray-500 text-sm py-6">
              No questions yet. Be the first to ask!
            </div>
          </TabsContent>

          <TabsContent value="reviews" class="space-y-4 mt-4">
            <div v-if="reviews.length > 0" class="space-y-4">
              <div v-for="review in reviews" :key="review.id" class="space-y-3">
                <div class="flex items-start gap-3">
                  <Avatar class="w-10 h-10">
                    <AvatarImage :src="review.avatar" :alt="review.author" />
                  </Avatar>
                  <div class="flex-1">
                    <div class="flex items-center justify-between mb-2">
                      <div>
                        <div class="text-gray-900 text-sm">{{ review.author }}</div>
                        <div class="text-xs text-gray-500 mt-0.5">{{ review.date }}</div>
                      </div>
                    </div>
                    
                    <!-- Individual ratings -->
                    <div class="grid grid-cols-2 gap-2 mb-3">
                      <div class="flex items-center gap-2">
                        <span class="text-xs text-gray-600 w-24">Showed Up:</span>
                        <div class="flex items-center gap-0.5">
                          <Star v-for="star in 5" :key="star" :class="['w-3 h-3', star <= review.ratings.showedUp ? 'fill-amber-400 text-amber-400' : 'fill-gray-200 text-gray-200']" />
                        </div>
                      </div>
                      <div class="flex items-center gap-2">
                        <span class="text-xs text-gray-600 w-24">Friendly:</span>
                        <div class="flex items-center gap-0.5">
                          <Star v-for="star in 5" :key="star" :class="['w-3 h-3', star <= review.ratings.friendly ? 'fill-amber-400 text-amber-400' : 'fill-gray-200 text-gray-200']" />
                        </div>
                      </div>
                      <div class="flex items-center gap-2">
                        <span class="text-xs text-gray-600 w-24">Communicative:</span>
                        <div class="flex items-center gap-0.5">
                          <Star v-for="star in 5" :key="star" :class="['w-3 h-3', star <= review.ratings.communicative ? 'fill-amber-400 text-amber-400' : 'fill-gray-200 text-gray-200']" />
                        </div>
                      </div>
                      <div class="flex items-center gap-2">
                        <span class="text-xs text-gray-600 w-24">Prepared:</span>
                        <div class="flex items-center gap-0.5">
                          <Star v-for="star in 5" :key="star" :class="['w-3 h-3', star <= review.ratings.prepared ? 'fill-amber-400 text-amber-400' : 'fill-gray-200 text-gray-200']" />
                        </div>
                      </div>
                    </div>
                    
                    <p class="text-sm text-gray-600">{{ review.comment }}</p>
                  </div>
                </div>
                <Separator />
              </div>
            </div>
            <div v-else class="text-center py-8">
              <Star class="w-12 h-12 text-gray-300 mx-auto mb-3" />
              <p class="text-gray-600">No reviews yet</p>
              <p class="text-sm text-gray-500 mt-1">
                Complete a service exchange to leave the first review!
              </p>
            </div>
          </TabsContent>
        </Tabs>
      </div>
    </div>
  </div>
  
  <div v-else class="flex items-center justify-center h-full text-gray-500">
    <div class="text-center">
      <MapPin class="w-12 h-12 mx-auto mb-3 text-gray-400" />
      <p>Select a service to view details</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { MapPin, Clock, Calendar, Star, Send, MessageCircle } from 'lucide-vue-next'
import Avatar from './ui/Avatar.vue'
import AvatarImage from './ui/AvatarImage.vue'
import Badge from './ui/Badge.vue'
import Button from './ui/Button.vue'
import Separator from './ui/Separator.vue'
import Tabs from './ui/Tabs.vue'
import TabsContent from './ui/TabsContent.vue'
import TabsList from './ui/TabsList.vue'
import TabsTrigger from './ui/TabsTrigger.vue'
import Textarea from './ui/Textarea.vue'
import { getServiceById, getServiceRatings, getServiceQuestions, askServiceQuestion, answerQuestion } from '../services/dataService'
import { createHandshake } from '../services/handshakeService'
import { createHandshakeChannel, isStreamChatInitialized } from '../clients/streamChatClient'
import { useAppStore } from '../stores/appStore'
import { formatDistanceToNow, formatDate } from '../utils/dateUtils'
import type { Service, ServiceRatingsResponse, ServiceQuestion as ServiceQuestionDTO } from '../types'

const appStore = useAppStore()

interface ServiceInfoProps {
  serviceId: string
  onViewFullDetails?: (serviceId: string) => void
}

interface AverageRatings {
  showedUp: number
  friendly: number
  communicative: number
  prepared: number
}

interface UiReview {
  id: string
  author: string
  avatar: string
  date: string
  comment: string
  ratings: {
    showedUp: number
    friendly: number
    communicative: number
    prepared: number
  }
}

interface UiQuestion {
  id: string
  author: string
  avatar: string
  date: string
  question: string
  answer?: string
  answeredBy?: string
  answeredDate?: string
}

const props = defineProps<ServiceInfoProps>()

const service = ref<Service | undefined>()
const questionText = ref("")
const reviews = ref<UiReview[]>([])
const questions = ref<UiQuestion[]>([])
const averageRatings = ref<AverageRatings | null>(null)
const totalReviews = ref(0)
const isLoading = ref(false)
const isAccepting = ref(false)
const acceptError = ref('')
const acceptSuccess = ref('')
const isAskingQuestion = ref(false)
const questionError = ref('')
const answerTexts = ref<Record<string, string>>({})
const isAnswering = ref<Record<string, boolean>>({})
const answerErrors = ref<Record<string, string>>({})

let activeRequestId = 0

const avatarFallback = (name: string) => {
  const encoded = encodeURIComponent(name || 'Community Member')
  return `https://ui-avatars.com/api/?name=${encoded}&background=E2E8F0&color=1F2937`
}

const resetData = () => {
  service.value = undefined
  reviews.value = []
  questions.value = []
  averageRatings.value = null
  totalReviews.value = 0
}

const mapRatingsResponse = (response: ServiceRatingsResponse | null) => {
  if (!response) {
    averageRatings.value = null
    totalReviews.value = 0
    reviews.value = []
    return
  }

  totalReviews.value = response.summary.totalReviews

  if (response.summary.totalReviews > 0) {
    averageRatings.value = {
      showedUp: response.summary.punctuality,
      friendly: response.summary.friendliness,
      communicative: response.summary.communicative,
      prepared: response.summary.preparedness,
    }
  } else {
    averageRatings.value = null
  }

  reviews.value = response.ratings.map((rating) => ({
    id: rating.id,
    author: rating.rater.name,
    avatar: rating.rater.avatar || avatarFallback(rating.rater.name),
    date: formatDistanceToNow(rating.createdAt),
    comment: rating.comment ?? '',
    ratings: {
      showedUp: rating.punctuality,
      friendly: rating.friendliness,
      communicative: rating.communicative,
      prepared: rating.preparedness,
    },
  }))
}

const mapQuestionsResponse = (response: ServiceQuestionDTO[]) => {
  questions.value = response.map((question) => ({
    id: question.id,
    author: question.author.name,
    avatar: question.author.avatar || avatarFallback(question.author.name),
    date: formatDistanceToNow(question.createdAt),
    question: question.content,
    answer: question.answer?.content ?? undefined,
    answeredBy: question.answer?.responder.name ?? undefined,
    answeredDate: question.answer ? formatDistanceToNow(question.answer.createdAt) : undefined,
  }))
}

const loadServiceData = async (serviceId: string) => {
  const currentRequest = ++activeRequestId

  if (!serviceId) {
    resetData()
    return
  }

  isLoading.value = true

  try {
    const [fetchedService, ratingsResponse, questionsResponse] = await Promise.all([
      getServiceById(serviceId),
      getServiceRatings(serviceId),
      getServiceQuestions(serviceId),
    ])

    if (currentRequest !== activeRequestId) {
      return
    }

    if (!fetchedService) {
      resetData()
      return
    }

    service.value = fetchedService
    mapRatingsResponse(ratingsResponse)
    mapQuestionsResponse(questionsResponse)
  } catch (error) {
    if (currentRequest === activeRequestId) {
      console.error(`Error loading service info for ${serviceId}:`, error)
      resetData()
    }
  } finally {
    if (currentRequest === activeRequestId) {
      isLoading.value = false
    }
  }
}

watch(
  () => props.serviceId,
  (newId: string) => {
    void loadServiceData(newId)
  },
  { immediate: true }
)

const formattedStartDate = computed(() => {
  if (!service.value?.startDate) {
    return 'Start date not specified'
  }
  return `Starts ${formatDate(service.value.startDate)}`
})

const scheduleText = computed(() => {
  const start = service.value?.startDate
  const end = service.value?.endDate

  if (start && end) {
    if (start === end) {
      return `Scheduled for ${formatDate(start)}`
    }
    return `${formatDate(start)} - ${formatDate(end)}`
  }

  if (start) {
    return `Starts ${formatDate(start)}`
  }

  if (end) {
    return `Available until ${formatDate(end)}`
  }

  return 'Schedule not specified'

})

const postedTime = computed(() => {
  if (!service.value?.createdAt) {
    return 'some time ago'
  }
  return formatDistanceToNow(service.value.createdAt)
})

const posterDescription = computed(() => {
  if (!service.value?.poster?.bio) {
    return 'Community member sharing skills through The Hive.'
  }
  return service.value.poster.bio
})

const posterBalance = computed(() => service.value?.poster?.timebankBalance ?? 0)

const posterBadgeLabel = computed(() => {
  const badge = service.value?.poster?.badge
  if (!badge) return 'Newcomer'

  switch (badge) {
    case 'top-contributor':
      return 'Top Contributor'
    case 'active':
      return 'Active Member'
    case 'balanced':
      return 'Balanced Exchanger'
    default:
      return 'Newcomer'
  }
})

const handleAskQuestion = async () => {
  if (!questionText.value.trim() || !props.serviceId) {
    return
  }

  if (!appStore.currentUser) {
    questionError.value = 'Please log in to ask a question'
    return
  }

  isAskingQuestion.value = true
  questionError.value = ''

  try {
    await askServiceQuestion(props.serviceId, questionText.value.trim())
    // Reload questions to get the updated list with proper formatting
    await loadServiceData(props.serviceId)
    questionText.value = ''
  } catch (error: any) {
    console.error('Failed to ask question:', error)
    questionError.value = error.message || 'Failed to ask question. Please try again.'
  } finally {
    isAskingQuestion.value = false
  }
}

const handleAnswerQuestion = async (questionId: string) => {
  const answerText = answerTexts.value[questionId]
  if (!answerText?.trim()) {
    return
  }

  if (!appStore.currentUser) {
    answerErrors.value[questionId] = 'Please log in to answer questions'
    return
  }

  isAnswering.value[questionId] = true
  answerErrors.value[questionId] = ''

  try {
    await answerQuestion(questionId, answerText.trim())
    // Reload questions to get the updated list with the answer
    await loadServiceData(props.serviceId)
    answerTexts.value[questionId] = ''
  } catch (error: any) {
    console.error('Failed to answer question:', error)
    answerErrors.value[questionId] = error.message || 'Failed to answer question. Please try again.'
  } finally {
    isAnswering.value[questionId] = false
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
    // Parse the timebank hours
    const agreedHours = parseInt(service.value.timebank.replace(/[^\d]/g, '')) || 1

    // Create the handshake (pass either offerId or requestId based on type)
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
    
    // Create Stream Chat channel
    if (isStreamChatInitialized()) {
      try {
        const channel = await createHandshakeChannel(handshake)
        console.log('Stream Chat channel created:', channel.id)
      } catch (chatError) {
        console.error('Failed to create Stream Chat channel:', chatError)
      }
    }
    
    acceptSuccess.value = '✅ Success! Chat created.'
    
    setTimeout(() => {
      appStore.setCurrentPage('messages')
    }, 1500)

  } catch (error: any) {
    console.error('Failed to accept service:', error)
    acceptError.value = error.response?.data?.message || 'Failed to accept. Try again.'
  } finally {
    isAccepting.value = false
  }
}
</script>

