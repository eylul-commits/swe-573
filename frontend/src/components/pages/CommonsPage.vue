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
            <h3 class="font-semibold text-gray-900 mb-3">Categories</h3>
            <div class="space-y-1">
              <button
                v-for="category in categories"
                :key="category.name"
                @click="selectedCategory = category.name"
                :class="[
                  'w-full text-left px-3 py-2 rounded-lg transition-colors flex items-center justify-between',
                  selectedCategory === category.name
                    ? 'bg-amber-50 text-amber-700'
                    : 'hover:bg-gray-50 text-gray-700',
                ]"
              >
                <span>{{ category.name }}</span>
                <Badge variant="secondary" class="text-xs">
                  {{ category.count }}
                </Badge>
              </button>
            </div>

            <!-- Quick Stats -->
            <div class="mt-6 pt-6 border-t border-gray-200">
              <h3 class="font-semibold text-gray-900 mb-3">Forum Stats</h3>
              <div class="space-y-2 text-sm">
                <div class="flex justify-between">
                  <span class="text-gray-600">Total Threads</span>
                  <span class="text-gray-900">47</span>
                </div>
                <div class="flex justify-between">
                  <span class="text-gray-600">Active Today</span>
                  <span class="text-gray-900">23</span>
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
          <Tabs default-value="recent" class="w-full">
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
                v-for="thread in filteredThreads"
                :key="thread.id"
                :thread="thread"
              />
              <EmptyState v-if="filteredThreads.length === 0" message="No threads found" />
            </TabsContent>

            <!-- Popular Tab -->
            <TabsContent value="popular" class="space-y-3">
              <ThreadCard
                v-for="thread in popularThreads"
                :key="thread.id"
                :thread="thread"
              />
            </TabsContent>

            <!-- Unanswered Tab -->
            <TabsContent value="unanswered" class="space-y-3">
              <ThreadCard
                v-for="thread in unansweredThreads"
                :key="thread.id"
                :thread="thread"
              />
              <EmptyState
                v-if="unansweredThreads.length === 0"
                message="All threads have responses!"
              />
            </TabsContent>
          </Tabs>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
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
import { forumThreads, categories, filterThreads } from '../../data/mockForumThreads'

const searchQuery = ref('')
const selectedCategory = ref('All')

const filteredThreads = computed(() => {
  return filterThreads(forumThreads, searchQuery.value, selectedCategory.value)
})

const popularThreads = computed(() => {
  return [...filteredThreads.value].sort((a, b) => b.likes - a.likes)
})

const unansweredThreads = computed(() => {
  return filteredThreads.value.filter((thread) => thread.replies < 5)
})
</script>
