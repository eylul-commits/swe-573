<template>
  <div class="flex-1 overflow-y-auto bg-gray-50">
    <!-- Header -->
    <div class="bg-white border-b border-gray-200">
      <div class="max-w-6xl mx-auto px-6 py-6">
        <div class="flex items-center justify-between mb-4">
          <div>
            <h1 class="text-2xl font-semibold text-gray-900 mb-2">The Commons</h1>
            <p class="text-gray-600">
              Community discussions, questions, and shared experiences
            </p>
          </div>
          <Button class="bg-amber-500 hover:bg-amber-600 text-white">
            <Plus class="w-4 h-4 mr-2" />
            New Thread
          </Button>
        </div>

        <!-- Search -->
        <div class="relative">
          <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
          <Input
            v-model="searchQuery"
            type="text"
            placeholder="Search discussions..."
            class="pl-10"
          />
        </div>
      </div>
    </div>

    <div class="max-w-6xl mx-auto px-6 py-6">
      <div class="grid grid-cols-1 lg:grid-cols-4 gap-6">
        <!-- Sidebar -->
        <div class="lg:col-span-1">
          <Card class="p-4">
            <!-- Quick Stats -->
            <div>
              <h3 class="font-semibold text-gray-900 mb-3">Forum Stats</h3>
              <div class="space-y-2 text-sm">
                <div class="flex justify-between">
                  <span class="text-gray-600">Total Topics</span>
                  <span class="text-gray-900">{{ forumTopics.length }}</span>
                </div>
                <div class="flex justify-between">
                  <span class="text-gray-600">Active Today</span>
                  <span class="text-gray-900">{{ forumTopics.length }}</span>
                </div>
                <div class="flex justify-between">
                  <span class="text-gray-600">Members</span>
                  <span class="text-gray-900">142</span>
                </div>
              </div>
            </div>
          </Card>
        </div>

        <!-- Main Content -->
        <div class="lg:col-span-3">
          <!-- Loading State -->
          <Card v-if="loading" class="p-8 text-center">
            <div class="text-gray-600">Loading forum topics...</div>
          </Card>

          <!-- Error State -->
          <Card v-else-if="error" class="p-8 text-center">
            <div class="text-red-600 mb-4">{{ error }}</div>
            <Button @click="loadForumTopics" variant="outline">
              Try Again
            </Button>
          </Card>

          <!-- Threads Content -->
          <Tabs v-else default-value="recent" class="w-full">
            <TabsList class="w-full justify-start mb-4">
              <TabsTrigger value="recent" class="flex items-center gap-2">
                <Clock class="w-4 h-4" />
                Recent
              </TabsTrigger>
              <TabsTrigger value="popular" class="flex items-center gap-2">
                <TrendingUp class="w-4 h-4" />
                Popular
              </TabsTrigger>
              <TabsTrigger value="unanswered" class="flex items-center gap-2">
                <MessageCircle class="w-4 h-4" />
                Unanswered
              </TabsTrigger>
            </TabsList>

            <!-- Recent Tab -->
            <TabsContent value="recent" class="space-y-3">
              <ThreadCard
                v-for="topic in filteredTopics"
                :key="topic.id"
                :topic="topic"
              />
              <EmptyState v-if="filteredTopics.length === 0" message="No topics found" />
            </TabsContent>

            <!-- Popular Tab -->
            <TabsContent value="popular" class="space-y-3">
              <ThreadCard
                v-for="topic in popularTopics"
                :key="topic.id"
                :topic="topic"
              />
            </TabsContent>

            <!-- Unanswered Tab -->
            <TabsContent value="unanswered" class="space-y-3">
              <ThreadCard
                v-for="topic in unansweredTopics"
                :key="topic.id"
                :topic="topic"
              />
              <EmptyState
                v-if="unansweredTopics.length === 0"
                message="All topics have responses!"
              />
            </TabsContent>
          </Tabs>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import {
  Plus,
  Search,
  Clock,
  TrendingUp,
  MessageCircle,
} from 'lucide-vue-next'
import Button from '../ui/Button.vue'
import Input from '../ui/Input.vue'
import Badge from '../ui/Badge.vue'
import Card from '../ui/Card.vue'
import Tabs from '../ui/Tabs.vue'
import TabsList from '../ui/TabsList.vue'
import TabsTrigger from '../ui/TabsTrigger.vue'
import TabsContent from '../ui/TabsContent.vue'
import ThreadCard from '../ThreadCard.vue'
import EmptyState from '../EmptyState.vue'
import { getAllForumTopics, filterTopics } from '../../services/forumService'
import { formatDistanceToNow } from '../../utils/dateUtils'
import type { ForumTopic } from '../../types/forum'

const searchQuery = ref('')
const forumTopics = ref<ForumTopic[]>([])
const loading = ref(true)
const error = ref<string | null>(null)

// Load forum topics from API
const loadForumTopics = async () => {
  try {
    loading.value = true
    error.value = null
    const topics = await getAllForumTopics()
    forumTopics.value = topics
  } catch (err) {
    error.value = err instanceof Error ? err.message : 'Failed to load forum topics'
    console.error('Error loading forum topics:', err)
  } finally {
    loading.value = false
  }
}

// Load on mount
onMounted(() => {
  loadForumTopics()
})

const filteredTopics = computed(() => {
  return filterTopics(forumTopics.value, searchQuery.value)
})

const popularTopics = computed(() => {
  return [...filteredTopics.value].sort((a, b) => b.likes - a.likes)
})

const unansweredTopics = computed(() => {
  return filteredTopics.value.filter((topic) => topic.postCount < 5)
})
</script>
