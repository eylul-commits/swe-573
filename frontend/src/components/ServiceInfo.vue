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
            <span>{{ service.location }}, Istanbul</span>
          </div>
          <div class="flex items-center gap-2 text-sm text-gray-600">
            <Clock class="w-4 h-4 text-amber-600" />
            <span class="text-amber-600">{{ service.timebank }}</span>
          </div>
          <div class="flex items-center gap-2 text-sm text-gray-600">
            <Calendar class="w-4 h-4" />
            <span>
              {{ getStartDate(serviceId) }}
            </span>
          </div>
          <div class="text-sm text-gray-600">
            {{ getSchedule(serviceId) }}
          </div>
          <div class="text-sm text-gray-500 col-span-2">
            Posted {{ getPostedTime(serviceId) }}
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
                <span class="text-emerald-600">{{ service.poster.hoursGiven }}h given</span>
                <span>•</span>
                <span class="text-blue-600">{{ service.poster.hoursReceived }}h received</span>
              </div>
              <p class="text-sm text-gray-600 mt-2 max-w-lg">
                {{ getPosterDescription(serviceId) }}
              </p>
            </div>
          </div>
          <div class="space-y-2">
            <Button class="bg-gray-900 hover:bg-gray-800 text-white w-full">
              {{ service.type === 'OFFER' ? 'Accept Offer' : 'Offer Help' }}
            </Button>
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
                :disabled="!questionText.trim()"
              >
                <Send class="w-4 h-4 mr-2" />
                Send Question
              </Button>
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
                    
                    <p class="text-sm text-gray-600">{{ review.text }}</p>
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
import { getServiceById } from '../services/dataService'
import type { Service } from '../types'

interface ServiceInfoProps {
  serviceId: string
  onViewFullDetails?: (serviceId: string) => void
}

const props = defineProps<ServiceInfoProps>()

const service = ref<Service | undefined>()

watch(() => props.serviceId, async (newId: string) => {
  if (newId) {
    try {
      const fetchedService = await getServiceById(newId)
      service.value = fetchedService || undefined
    } catch (error) {
      console.error(`Error fetching service ${newId}:`, error)
      service.value = undefined
    }
  } else {
    service.value = undefined
  }
}, { immediate: true })

const questionText = ref("")

// Mock reviews data - in real app this would come from a database
const allReviews: Record<string, any[]> = {
  "2": [
    {
      id: "1",
      author: "Mehmet Demir",
      avatar: "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=100&h=100&fit=crop",
      date: "2 weeks ago",
      ratings: {
        showedUp: 5,
        friendly: 5,
        communicative: 5,
        prepared: 5,
      },
      text: "Wonderful experience! Highly professional and exactly what I was looking for.",
    },
    {
      id: "2",
      author: "Zeynep Kaya",
      avatar: "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=100&h=100&fit=crop",
      date: "1 month ago",
      ratings: {
        showedUp: 5,
        friendly: 5,
        communicative: 4,
        prepared: 5,
      },
      text: "Such a warm and patient person. Would definitely exchange services again!",
    },
    {
      id: "3",
      author: "Can Özdemir",
      avatar: "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=100&h=100&fit=crop",
      date: "2 months ago",
      ratings: {
        showedUp: 5,
        friendly: 5,
        communicative: 3,
        prepared: 4,
      },
      text: "Great experience! Very punctual and reliable. A true community gem!",
    },
  ],
  "1": [
    {
      id: "4",
      author: "Ali Yılmaz",
      avatar: "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=100&h=100&fit=crop",
      date: "1 week ago",
      ratings: {
        showedUp: 4,
        friendly: 5,
        communicative: 4,
        prepared: 4,
      },
      text: "Very helpful and patient. Took time to explain everything clearly.",
    },
  ],
  "9": [], // New service with no reviews yet
}

// Mock questions data
const allQuestions: Record<string, any[]> = {
  "2": [
    {
      id: "q1",
      author: "Murat Demir",
      avatar: "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=100&h=100&fit=crop",
      date: "3 days ago",
      question: "Do you have experience reading stories to children aged 5-7?",
      answer: "Yes! I have been reading to children in this age group for over 3 years. I have a collection of age-appropriate books and love interactive storytelling.",
      answeredBy: "Ayşe Yılmaz",
      answeredDate: "3 days ago",
    },
    {
      id: "q2",
      author: "Ebru Kaya",
      avatar: "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=100&h=100&fit=crop",
      date: "1 week ago",
      question: "What languages can you read stories in?",
      answer: "I can read stories in both Turkish and English fluently. I also know some basic German if needed!",
      answeredBy: "Ayşe Yılmaz",
      answeredDate: "1 week ago",
    },
  ],
  "9": [
    {
      id: "q3",
      author: "Ahmet Yılmaz",
      avatar: "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=100&h=100&fit=crop",
      date: "2 days ago",
      question: "Do you have any portfolio of your previous garden designs?",
      answer: null, // Unanswered question
    },
    {
      id: "q4",
      author: "Elif Demir",
      avatar: "https://images.unsplash.com/photo-1487412720507-e7ab37603c6f?w=100&h=100&fit=crop",
      date: "5 hours ago",
      question: "Can you help with small balcony spaces? Mine is only about 3 square meters.",
      answer: null,
    },
  ],
}

const reviews = computed(() => allReviews[props.serviceId] || [])
const questions = computed(() => allQuestions[props.serviceId] || [])
const totalReviews = computed(() => reviews.value.length)

// Calculate average ratings for each criteria only if there are reviews
const averageRatings = computed(() => {
  if (totalReviews.value === 0) return null
  
  return {
    showedUp: reviews.value.reduce((sum: number, r: any) => sum + r.ratings.showedUp, 0) / totalReviews.value,
    friendly: reviews.value.reduce((sum: number, r: any) => sum + r.ratings.friendly, 0) / totalReviews.value,
    communicative: reviews.value.reduce((sum: number, r: any) => sum + r.ratings.communicative, 0) / totalReviews.value,
    prepared: reviews.value.reduce((sum: number, r: any) => sum + r.ratings.prepared, 0) / totalReviews.value,
  }
})

const getStartDate = (serviceId: string) => {
  if (serviceId === "9") return "Starting Nov 1, 2025"
  if (serviceId === "2") return "Starting Oct 25, 2025"
  return "Starting Oct 28, 2025"
}

const getSchedule = (serviceId: string) => {
  if (serviceId === "9") return "One-time consultation"
  if (serviceId === "2") return "Weekly, Saturdays"
  return "Flexible scheduling"
}

const getPostedTime = (serviceId: string) => {
  return serviceId === "9" ? "3 hours ago" : "2 hours ago"
}

const getPosterDescription = (serviceId: string) => {
  return serviceId === "9" 
    ? "New to The Hive and excited to share skills and connect with the community!"
    : "Active community member sharing skills and building connections through The Hive."
}

const handleAskQuestion = () => {
  if (questionText.value.trim()) {
    // In a real app, this would send the question to the backend
    console.log("Asking question:", questionText.value)
    questionText.value = ""
  }
}
</script>

