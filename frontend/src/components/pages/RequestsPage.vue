<template>
  <div class="flex-1 flex flex-col bg-gray-50">
    <!-- Header -->
    <div class="bg-white border-b border-gray-200 px-6 py-4">
      <h1 class="text-2xl font-semibold text-gray-900">Service Requests</h1>
      <p class="text-sm text-gray-600 mt-1">
        Manage requests for your services
      </p>
    </div>

    <!-- Tabs -->
    <Tabs default-value="pending" class="flex-1 flex flex-col">
      <div class="bg-white border-b border-gray-200 px-6">
        <TabsList class="bg-transparent border-b-0 h-auto p-0">
          <TabsTrigger
            value="pending"
            class="rounded-none border-b-2 border-transparent data-[state=active]:border-gray-900 data-[state=active]:bg-transparent data-[state=active]:shadow-none px-4 py-3"
          >
            Pending ({{ pendingRequests.length }})
          </TabsTrigger>
          <TabsTrigger
            value="accepted"
            class="rounded-none border-b-2 border-transparent data-[state=active]:border-gray-900 data-[state=active]:bg-transparent data-[state=active]:shadow-none px-4 py-3"
          >
            Accepted ({{ acceptedRequests.length }})
          </TabsTrigger>
          <TabsTrigger
            value="declined"
            class="rounded-none border-b-2 border-transparent data-[state=active]:border-gray-900 data-[state=active]:bg-transparent data-[state=active]:shadow-none px-4 py-3"
          >
            Declined ({{ declinedRequests.length }})
          </TabsTrigger>
        </TabsList>
      </div>

      <ScrollArea class="flex-1">
        <TabsContent value="pending" class="p-6 mt-0">
          <div class="max-w-3xl space-y-4">
            <RequestCard
              v-for="request in pendingRequests"
              :key="request.id"
              :request="request"
              @accept="handleAccept"
              @decline="handleDecline"
            />
            <div v-if="pendingRequests.length === 0" class="text-center py-12">
              <Clock class="w-16 h-16 text-gray-300 mx-auto mb-4" />
              <h3 class="text-lg font-medium text-gray-900 mb-2">No pending requests</h3>
              <p class="text-sm text-gray-600">
                When people request your services, they'll appear here
              </p>
            </div>
          </div>
        </TabsContent>

        <TabsContent value="accepted" class="p-6 mt-0">
          <div class="max-w-3xl space-y-4">
            <RequestCard
              v-for="request in acceptedRequests"
              :key="request.id"
              :request="request"
            />
            <div v-if="acceptedRequests.length === 0" class="text-center py-12">
              <Check class="w-16 h-16 text-gray-300 mx-auto mb-4" />
              <h3 class="text-lg font-medium text-gray-900 mb-2">No accepted requests</h3>
              <p class="text-sm text-gray-600">Accepted requests will appear here</p>
            </div>
          </div>
        </TabsContent>

        <TabsContent value="declined" class="p-6 mt-0">
          <div class="max-w-3xl space-y-4">
            <RequestCard
              v-for="request in declinedRequests"
              :key="request.id"
              :request="request"
            />
            <div v-if="declinedRequests.length === 0" class="text-center py-12">
              <X class="w-16 h-16 text-gray-300 mx-auto mb-4" />
              <h3 class="text-lg font-medium text-gray-900 mb-2">No declined requests</h3>
              <p class="text-sm text-gray-600">Declined requests will appear here</p>
            </div>
          </div>
        </TabsContent>
      </ScrollArea>
    </Tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { Clock, Check, X } from 'lucide-vue-next'
import Tabs from '../ui/Tabs.vue'
import TabsList from '../ui/TabsList.vue'
import TabsTrigger from '../ui/TabsTrigger.vue'
import TabsContent from '../ui/TabsContent.vue'
import ScrollArea from '../ui/ScrollArea.vue'
import RequestCard from '../RequestCard.vue'
import { mockRequests } from '../../data/mockRequests'
import type { ServiceRequest } from '../../data/mockRequests'

const requests = ref<ServiceRequest[]>([...mockRequests])

const pendingRequests = computed(() => requests.value.filter(r => r.status === 'pending'))
const acceptedRequests = computed(() => requests.value.filter(r => r.status === 'accepted'))
const declinedRequests = computed(() => requests.value.filter(r => r.status === 'declined'))

const handleAccept = (requestId: string) => {
  const request = requests.value.find(r => r.id === requestId)
  if (request) {
    request.status = 'accepted'
  }
}

const handleDecline = (requestId: string) => {
  const request = requests.value.find(r => r.id === requestId)
  if (request) {
    request.status = 'declined'
  }
}
</script>
