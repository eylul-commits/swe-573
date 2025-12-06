<template>
  <Card class="p-6">
    <div class="flex items-start gap-4 mb-4">
      <Avatar class="w-14 h-14">
        <AvatarImage :src="request.requester.avatar" :alt="request.requester.name" />
      </Avatar>

      <div class="flex-1">
        <div class="flex items-start justify-between mb-1">
          <div>
            <div class="flex items-center gap-2 mb-1">
              <span class="font-medium text-gray-900">{{ request.requester.name }}</span>
              <div class="flex items-center gap-1 text-xs">
                <span>{{ request.requester.badge.emoji }}</span>
                <span class="text-gray-600">{{ request.requester.badge.label }}</span>
              </div>
            </div>
            <div class="flex items-center gap-3 text-sm">
              <span class="text-emerald-600">{{ request.requester.hoursGiven }}h given</span>
              <span class="text-gray-300">•</span>
              <span class="text-blue-600">{{ request.requester.hoursReceived }}h received</span>
              <span class="text-gray-300">•</span>
              <span class="text-gray-500">{{ request.timestamp }}</span>
            </div>
          </div>
          <Badge
            variant="custom"
            :class="
              request.service.type === 'OFFER'
                ? 'bg-emerald-100 text-emerald-700 hover:bg-emerald-100'
                : 'bg-blue-100 text-blue-700 hover:bg-blue-100'
            "
          >
            {{ request.service.type }}
          </Badge>
        </div>
      </div>
    </div>

    <div class="mb-4">
      <h3 class="text-sm font-medium text-gray-900">
        Request for: {{ request.service.title }}
      </h3>
      <p v-if="request.message" class="text-sm text-gray-600 mt-1">
        "{{ request.message }}"
      </p>
    </div>

    <div class="grid grid-cols-2 gap-4 mb-4 pb-4 border-b border-gray-200">
      <div class="space-y-1">
        <div class="text-xs text-gray-500">Proposed Date & Time</div>
        <div class="flex items-center gap-2 text-sm text-gray-900">
          <Calendar class="w-4 h-4 text-gray-400" />
          <span>{{ request.proposedDate }}</span>
        </div>
        <div class="text-sm text-gray-600 ml-6">{{ request.proposedTime }}</div>
      </div>
      <div class="space-y-1">
        <div class="text-xs text-gray-500">Location</div>
        <div class="flex items-center gap-2 text-sm text-gray-900">
          <MapPin class="w-4 h-4 text-gray-400" />
          <span>{{ request.service.location }}</span>
        </div>
      </div>
      <div class="space-y-1">
        <div class="text-xs text-gray-500">TimeBank Hours</div>
        <div class="flex items-center gap-2 text-sm text-amber-600">
          <Clock class="w-4 h-4" />
          <span>{{ request.service.timebank }}</span>
        </div>
      </div>
    </div>

    <!-- Actions for pending requests -->
    <div v-if="request.status === 'pending'" class="flex gap-3">
      <Button
        @click="emit('accept', request.id)"
        variant="custom"
        class="flex-1 bg-emerald-600 hover:bg-emerald-700 text-white"
      >
        <Check class="w-4 h-4 mr-2" />
        Accept Request
      </Button>
      <Button
        @click="emit('decline', request.id)"
        variant="outline"
        class="flex-1 border-red-200 text-red-600 hover:bg-red-50 hover:text-red-700"
      >
        <X class="w-4 h-4 mr-2" />
        Decline
      </Button>
    </div>

    <!-- Status for accepted requests -->
    <div
      v-if="request.status === 'accepted'"
      class="bg-emerald-50 border border-emerald-200 rounded-lg p-3 flex items-center gap-2"
    >
      <Check class="w-4 h-4 text-emerald-600" />
      <span class="text-sm text-emerald-700">
        Request accepted - You can now message {{ request.requester.name }}
      </span>
    </div>

    <!-- Status for declined requests -->
    <div
      v-if="request.status === 'declined'"
      class="bg-gray-50 border border-gray-200 rounded-lg p-3 flex items-center gap-2"
    >
      <X class="w-4 h-4 text-gray-600" />
      <span class="text-sm text-gray-600">Request declined</span>
    </div>
  </Card>
</template>

<script setup lang="ts">
import { Calendar, MapPin, Clock, Check, X } from 'lucide-vue-next'
import Avatar from './ui/Avatar.vue'
import AvatarImage from './ui/AvatarImage.vue'
import Badge from './ui/Badge.vue'
import Card from './ui/Card.vue'
import Button from './ui/Button.vue'
import type { ServiceRequest } from '../types'

defineProps<{
  request: ServiceRequest
}>()

const emit = defineEmits<{
  accept: [requestId: string]
  decline: [requestId: string]
}>()
</script>

