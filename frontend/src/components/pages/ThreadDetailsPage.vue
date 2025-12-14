<template>
  <div class="flex-1 flex flex-col overflow-hidden bg-gray-50">
    <!-- Header -->
    <div class="bg-white border-b border-gray-200 px-6 py-4">
      <div class="max-w-4xl mx-auto">
        <Button
          variant="ghost"
          class="text-gray-600 hover:text-gray-900 mb-4 -ml-2"
          @click="handleBack"
        >
          <ArrowLeft class="w-4 h-4 mr-2" />
          Back to Commons
        </Button>
      </div>
    </div>

    <!-- Content -->
    <div class="flex-1 overflow-y-auto">
      <div class="max-w-4xl mx-auto px-6 py-6">
        <!-- Loading State -->
        <Card v-if="loading" class="p-8 text-center">
          <div class="text-gray-600">Loading thread...</div>
        </Card>

        <!-- Error State -->
        <Card v-else-if="error" class="p-8 text-center">
          <div class="text-red-600 mb-4">{{ error }}</div>
          <Button @click="loadThread" variant="outline">
            Try Again
          </Button>
        </Card>

        <!-- Thread Content -->
        <div v-else-if="thread" class="space-y-6">
          <!-- Thread Header -->
          <Card class="p-6">
            <div class="flex items-start gap-4 mb-4">
              <Avatar class="w-12 h-12 flex-shrink-0">
                <AvatarImage :src="getAvatarUrl(thread.author.avatar, thread.author.name)" />
              </Avatar>
              <div class="flex-1">
                <div class="flex items-start justify-between mb-2">
                  <h1 class="text-2xl font-semibold text-gray-900">
                    {{ thread.title }}
                  </h1>
                  <Button 
                    v-if="thread.author.id !== appStore.currentUser?.id"
                    @click.stop="openReportTopicModal"
                    variant="outline"
                    size="sm"
                    class="text-red-600 hover:text-red-700 hover:bg-red-50"
                  >
                    <Flag class="w-4 h-4 mr-1" />
                    Report
                  </Button>
                </div>
                <div class="flex items-center gap-4 text-sm text-gray-500">
                  <div class="flex items-center gap-2">
                    <span class="font-medium text-gray-700">{{ thread.author.name }}</span>
                    <Badge variant="secondary" class="text-xs">{{ thread.author.badge }}</Badge>
                  </div>
                  <div class="flex items-center gap-1">
                    <Clock class="w-4 h-4" />
                    {{ formatDistanceToNow(thread.createdAt) }}
                  </div>
                  <div class="flex items-center gap-1">
                    <Eye class="w-4 h-4" />
                    {{ thread.views }} views
                  </div>
                  <div class="flex items-center gap-1">
                    <MessageCircle class="w-4 h-4" />
                    {{ thread.postCount }} replies
                  </div>
                </div>
              </div>
            </div>
          </Card>

          <!-- Posts -->
          <div class="space-y-4">
            <Card v-for="post in posts" :key="post.id" class="p-6">
              <div class="flex gap-4">
                <Avatar class="w-10 h-10 flex-shrink-0">
                  <AvatarImage :src="getAvatarUrl(post.author.avatar, post.author.name)" />
                </Avatar>
                <div class="flex-1">
                  <div class="flex items-center justify-between mb-2">
                    <div class="flex items-center gap-2">
                      <span class="font-medium text-gray-900">{{ post.author.name }}</span>
                      <Badge variant="secondary" class="text-xs">{{ post.author.badge }}</Badge>
                      <span class="text-sm text-gray-500">{{ formatDistanceToNow(post.createdAt) }}</span>
                    </div>
                    <Button 
                      v-if="post.author.id !== appStore.currentUser?.id"
                      @click.stop="openReportPostModal(post)"
                      variant="ghost"
                      size="sm"
                      class="text-red-600 hover:text-red-700 hover:bg-red-50"
                    >
                      <Flag class="w-3 h-3 mr-1" />
                      Report
                    </Button>
                  </div>
                  <div class="text-gray-700 whitespace-pre-wrap">{{ post.content }}</div>
                </div>
              </div>
            </Card>

            <!-- Empty State -->
            <Card v-if="posts.length === 0" class="p-8 text-center">
              <MessageCircle class="w-12 h-12 text-gray-400 mx-auto mb-3" />
              <p class="text-gray-600">No replies yet. Be the first to reply!</p>
            </Card>
          </div>

          <!-- Reply Form -->
          <Card class="p-6">
            <h3 class="text-lg font-semibold text-gray-900 mb-4">Add a Reply</h3>
            <form @submit.prevent="handleSubmitReply" class="space-y-4">
              <Textarea
                v-model="replyContent"
                placeholder="Write your reply..."
                rows="4"
                required
              />
              <div class="flex justify-end">
                <Button
                  type="submit"
                  :disabled="isSubmitting || !replyContent.trim()"
                  class="bg-amber-500 hover:bg-amber-600 text-white"
                >
                  {{ isSubmitting ? 'Posting...' : 'Post Reply' }}
                </Button>
              </div>
            </form>
          </Card>
        </div>
      </div>
    </div>

    <!-- Report Topic Modal -->
    <ReportContentModal
      v-if="thread"
      v-model="reportTopicModalOpen"
      :reported-user-id="thread.author.id"
      :reported-user-name="thread.author.name"
      :reported-forum-topic-id="thread.id"
      report-type="FORUM_TOPIC"
      @submitted="onReportSubmitted"
    />

    <!-- Report Post Modal -->
    <ReportContentModal
      v-if="selectedPost"
      v-model="reportPostModalOpen"
      :reported-user-id="selectedPost.author.id"
      :reported-user-name="selectedPost.author.name"
      :reported-forum-post-id="selectedPost.id"
      report-type="FORUM_POST"
      @submitted="onReportSubmitted"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { ArrowLeft, Clock, Eye, MessageCircle, Flag } from 'lucide-vue-next'
import Button from '../ui/Button.vue'
import Card from '../ui/Card.vue'
import Avatar from '../ui/Avatar.vue'
import AvatarImage from '../ui/AvatarImage.vue'
import Badge from '../ui/Badge.vue'
import Textarea from '../ui/Textarea.vue'
import { useAppStore } from '../../stores/appStore'
import ReportContentModal from '../ReportContentModal.vue'
import { formatDistanceToNow } from '../../utils/dateUtils'
import { getAvatarUrl } from '../../utils/avatarUtils'
import {
  getForumTopicById,
  getPostsByTopicId,
  createForumPost
} from '../../services/forumService'
import type { ForumTopic, ForumPost } from '../../types'

const appStore = useAppStore()

const thread = ref<ForumTopic | null>(null)
const posts = ref<ForumPost[]>([])
const loading = ref(true)
const error = ref<string | null>(null)
const replyContent = ref('')
const isSubmitting = ref(false)

// Report modal state
const reportTopicModalOpen = ref(false)
const reportPostModalOpen = ref(false)
const selectedPost = ref<ForumPost | null>(null)

const loadThread = async () => {
  if (!appStore.selectedThreadId) return

  try {
    loading.value = true
    error.value = null
    
    const [threadData, postsData] = await Promise.all([
      getForumTopicById(appStore.selectedThreadId),
      getPostsByTopicId(appStore.selectedThreadId)
    ])
    
    thread.value = threadData
    posts.value = postsData
  } catch (err) {
    error.value = err instanceof Error ? err.message : 'Failed to load thread'
    console.error('Error loading thread:', err)
  } finally {
    loading.value = false
  }
}

const openReportTopicModal = () => {
  reportTopicModalOpen.value = true
}

const openReportPostModal = (post: ForumPost) => {
  selectedPost.value = post
  reportPostModalOpen.value = true
}

const onReportSubmitted = () => {
  reportTopicModalOpen.value = false
  reportPostModalOpen.value = false
  selectedPost.value = null
}

const handleSubmitReply = async () => {
  if (!replyContent.value.trim() || !appStore.selectedThreadId) return

  try {
    isSubmitting.value = true
    const newPost = await createForumPost(appStore.selectedThreadId, {
      content: replyContent.value
    })
    
    // Add new post to the list
    posts.value.push(newPost)
    
    // Reset form
    replyContent.value = ''
    
    // Update post count
    if (thread.value) {
      thread.value.postCount++
    }
  } catch (err) {
    console.error('Error posting reply:', err)
    alert('Failed to post reply. Please try again.')
  } finally {
    isSubmitting.value = false
  }
}

const handleBack = () => {
  appStore.setSelectedThreadId(null)
}

// Load thread when component mounts
onMounted(() => {
  loadThread()
})

// Reload when thread ID changes
watch(() => appStore.selectedThreadId, () => {
  if (appStore.selectedThreadId) {
    loadThread()
  }
})
</script>

