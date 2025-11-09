<template>
  <Card class="p-4 hover:shadow-md transition-shadow cursor-pointer" @click="handleClick">
    <div class="flex gap-4">
      <!-- Avatar -->
      <Avatar class="w-10 h-10 flex-shrink-0">
        <AvatarImage :src="topic.author.avatar || 'https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=100&h=100&fit=crop'" />
      </Avatar>

      <!-- Content -->
      <div class="flex-1 min-w-0">
        <div class="flex items-start justify-between gap-2 mb-2">
          <div class="flex items-center gap-2 flex-wrap">
            <Pin v-if="topic.isPinned" class="w-4 h-4 text-amber-500 flex-shrink-0" />
            <h3 class="text-gray-900 hover:text-amber-600 transition-colors font-medium">
              {{ topic.title }}
            </h3>
          </div>
        </div>

        <p class="text-gray-600 text-sm mb-3 line-clamp-2">
          {{ topic.excerpt }}
        </p>

        <div class="flex items-center gap-4 text-sm text-gray-500">
          <div class="flex items-center gap-1">
            <span>{{ topic.author.name }}</span>
            <span class="text-xs">{{ topic.author.badge }}</span>
          </div>
          <div class="flex items-center gap-1">
            <Eye class="w-4 h-4" />
            {{ topic.views }}
          </div>
          <div class="flex items-center gap-1">
            <MessageCircle class="w-4 h-4" />
            {{ topic.postCount }}
          </div>
          <div class="flex items-center gap-1">
            <ThumbsUp class="w-4 h-4" />
            {{ topic.likes }}
          </div>
          <div class="ml-auto text-xs">
            {{ formatDistanceToNow(topic.lastActivity) }}
          </div>
        </div>
      </div>
    </div>
  </Card>
</template>

<script setup lang="ts">
import { Pin, Eye, MessageCircle, ThumbsUp } from 'lucide-vue-next'
import Avatar from './ui/Avatar.vue'
import AvatarImage from './ui/AvatarImage.vue'
import Card from './ui/Card.vue'
import type { ForumTopic } from '../types/forum'
import { formatDistanceToNow } from '../utils/dateUtils'
import { useAppStore } from '../stores/appStore'

const props = defineProps<{
  topic: ForumTopic
}>()

const appStore = useAppStore()

const handleClick = () => {
  appStore.setSelectedThreadId(props.topic.id)
}
</script>

<style scoped>
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>

