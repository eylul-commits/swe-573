<template>
  <Card class="p-4 hover:shadow-md transition-shadow cursor-pointer">
    <div class="flex gap-4">
      <!-- Avatar -->
      <Avatar class="w-10 h-10 flex-shrink-0">
        <AvatarImage :src="thread.author.avatar" />
      </Avatar>

      <!-- Content -->
      <div class="flex-1 min-w-0">
        <div class="flex items-start justify-between gap-2 mb-2">
          <div class="flex items-center gap-2 flex-wrap">
            <Pin v-if="thread.isPinned" class="w-4 h-4 text-amber-500 flex-shrink-0" />
            <h3 class="text-gray-900 hover:text-amber-600 transition-colors font-medium">
              {{ thread.title }}
            </h3>
          </div>
          <Badge variant="outline" class="flex-shrink-0 text-xs">
            {{ thread.category }}
          </Badge>
        </div>

        <p class="text-gray-600 text-sm mb-3 line-clamp-2">
          {{ thread.excerpt }}
        </p>

        <div class="flex items-center gap-4 text-sm text-gray-500">
          <div class="flex items-center gap-1">
            <span>{{ thread.author.name }}</span>
            <span class="text-xs">{{ thread.author.badge }}</span>
          </div>
          <div class="flex items-center gap-1">
            <Eye class="w-4 h-4" />
            {{ thread.views }}
          </div>
          <div class="flex items-center gap-1">
            <MessageCircle class="w-4 h-4" />
            {{ thread.replies }}
          </div>
          <div class="flex items-center gap-1">
            <ThumbsUp class="w-4 h-4" />
            {{ thread.likes }}
          </div>
          <div class="ml-auto text-xs">
            {{ thread.lastActivity }}
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
import Badge from './ui/Badge.vue'
import Card from './ui/Card.vue'
import type { ForumThread } from '../data/mockForumThreads'

defineProps<{
  thread: ForumThread
}>()
</script>

<style scoped>
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>

