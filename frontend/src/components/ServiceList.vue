<template>
  <div class="flex flex-col h-full">
    <!-- Header -->
    <div class="p-6 border-b border-gray-200">
      <div class="flex items-center justify-between mb-4">
        <h1 class="text-gray-900">The Hive</h1>
        <Button variant="ghost" size="icon">
          <Settings class="w-5 h-5 text-gray-600" />
        </Button>
      </div>
      
      <!-- TimeBank Balance -->
      <div class="bg-gradient-to-r from-amber-50 to-orange-50 border border-amber-200 rounded-lg p-4 mb-4">
        <div class="flex items-center justify-between">
          <div>
            <div class="text-gray-600 text-sm">Your TimeBank Balance</div>
            <div class="flex items-center gap-2 mt-1">
              <Clock class="w-5 h-5 text-amber-600" />
              <span class="text-gray-900">{{ currentUser.timebankBalance }} hours</span>
            </div>
          </div>
          <div class="text-right">
            <div class="text-xs text-gray-500">Given: {{ currentUser.hoursGiven }}h</div>
            <div class="text-xs text-gray-500">Received: {{ currentUser.hoursReceived }}h</div>
          </div>
        </div>
      </div>

      <!-- Search Bar -->
      <div class="relative mb-3">
        <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
        <Input
          v-model="searchQuery"
          type="text"
          placeholder="Search services or tags..."
          class="pl-10 pr-10"
        />
        <button
          v-if="searchQuery"
          @click="searchQuery = ''"
          class="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
        >
          <X class="w-4 h-4" />
        </button>
      </div>

      <!-- Badge Filter -->
      <div class="mb-3">
        <Select v-model="selectedBadge">
          <SelectTrigger class="w-full">
            <Award class="w-4 h-4 mr-2 text-gray-400" />
            <SelectValue placeholder="Filter by badge" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="all">All badges</SelectItem>
            <SelectItem value="top-contributor">🏆 Top Contributor</SelectItem>
            <SelectItem value="active">⭐ Active Member</SelectItem>
            <SelectItem value="newcomer">🌱 Newcomer</SelectItem>
            <SelectItem value="balanced">⚖️ Balanced Exchanger</SelectItem>
          </SelectContent>
        </Select>
      </div>

      <!-- Popular Tags -->
      <div class="mb-2">
        <div class="text-xs text-gray-600 mb-2">Filter by tags:</div>
        <div class="flex flex-wrap gap-2">
          <Badge
            v-for="tag in allTags.slice(0, 8)"
            :key="tag"
            :variant="selectedTags.includes(tag) ? 'default' : 'outline'"
            :class="[
              'cursor-pointer transition-colors',
              selectedTags.includes(tag)
                ? 'bg-gray-900 hover:bg-gray-800'
                : 'hover:bg-gray-100'
            ]"
            @click="toggleTag(tag)"
          >
            {{ tag }}
          </Badge>
        </div>
      </div>

      <!-- Active Filters -->
      <div v-if="searchQuery || selectedTags.length > 0 || selectedBadge !== 'all'" class="flex items-center gap-2 mt-3">
        <span class="text-xs text-gray-600">
          {{ filteredServices.length }} service{{ filteredServices.length !== 1 ? 's' : '' }} found
        </span>
        <Button
          variant="ghost"
          size="sm"
          @click="clearFilters"
          class="h-6 px-2 text-xs"
        >
          Clear filters
        </Button>
      </div>
    </div>

    <!-- Tabs -->
    <div class="px-6 pt-6">
      <Tabs default-value="all" class="w-full">
        <TabsList class="w-full grid grid-cols-3 bg-gray-100 p-1">
          <TabsTrigger
            value="all"
            class="data-[state=active]:bg-gray-900 data-[state=active]:text-white rounded-lg"
          >
            All
          </TabsTrigger>
          <TabsTrigger
            value="offers"
            class="data-[state=active]:bg-gray-900 data-[state=active]:text-white rounded-lg"
          >
            Offers
          </TabsTrigger>
          <TabsTrigger
            value="requests"
            class="data-[state=active]:bg-gray-900 data-[state=active]:text-white rounded-lg"
          >
            Requests
          </TabsTrigger>
        </TabsList>

        <TabsContent value="all" class="mt-4 space-y-3 pb-6">
          <div v-if="filteredServices.length > 0">
            <div
              v-for="service in filteredServices"
              :key="service.id"
              @click="$emit('selectService', service.id)"
              :class="[
                'p-4 border border-gray-200 rounded-lg cursor-pointer transition-colors',
                selectedServiceId === service.id 
                  ? 'bg-amber-50 border-amber-300' 
                  : 'hover:bg-gray-50'
              ]"
            >
              <div class="flex items-start justify-between mb-2">
                <div class="flex-1">
                  <h3 class="font-medium text-gray-900 mb-1">{{ service.title }}</h3>
                  <p class="text-sm text-gray-600">{{ service.poster.name }}</p>
                </div>
                <Badge 
                  :class="service.type === 'OFFER' 
                    ? 'bg-emerald-100 text-emerald-700 hover:bg-emerald-100' 
                    : 'bg-blue-100 text-blue-700 hover:bg-blue-100'"
                >
                  {{ service.type }}
                </Badge>
              </div>
              
              <p class="text-sm text-gray-600 mb-2 line-clamp-2">
                {{ service.description }}
              </p>
              
              <div class="flex items-center gap-4 text-xs text-gray-500">
                <div class="flex items-center gap-1">
                  <MapPin class="w-3 h-3" />
                  <span>{{ service.location }}</span>
                </div>
                <div class="flex items-center gap-1">
                  <Clock class="w-3 h-3" />
                  <span>{{ service.timebank }}</span>
                </div>
              </div>
              
              <div class="flex flex-wrap gap-1 mt-2">
                <Badge
                  v-for="tag in service.tags.slice(0, 3)"
                  :key="tag"
                  variant="outline"
                  class="text-xs"
                >
                  {{ tag }}
                </Badge>
              </div>
            </div>
          </div>
          <div v-else class="text-center py-8 text-gray-500">
            No services found. Try adjusting your filters.
          </div>
        </TabsContent>

        <TabsContent value="offers" class="mt-4 space-y-3 pb-6">
          <div v-if="offers.length > 0">
            <div
              v-for="service in offers"
              :key="service.id"
              @click="$emit('selectService', service.id)"
              :class="[
                'p-4 border border-gray-200 rounded-lg cursor-pointer transition-colors',
                selectedServiceId === service.id 
                  ? 'bg-amber-50 border-amber-300' 
                  : 'hover:bg-gray-50'
              ]"
            >
              <div class="flex items-start justify-between mb-2">
                <div class="flex-1">
                  <h3 class="font-medium text-gray-900 mb-1">{{ service.title }}</h3>
                  <p class="text-sm text-gray-600">{{ service.poster.name }}</p>
                </div>
                <Badge class="bg-emerald-100 text-emerald-700 hover:bg-emerald-100">
                  {{ service.type }}
                </Badge>
              </div>
              
              <p class="text-sm text-gray-600 mb-2 line-clamp-2">
                {{ service.description }}
              </p>
              
              <div class="flex items-center gap-4 text-xs text-gray-500">
                <div class="flex items-center gap-1">
                  <MapPin class="w-3 h-3" />
                  <span>{{ service.location }}</span>
                </div>
                <div class="flex items-center gap-1">
                  <Clock class="w-3 h-3" />
                  <span>{{ service.timebank }}</span>
                </div>
              </div>
              
              <div class="flex flex-wrap gap-1 mt-2">
                <Badge
                  v-for="tag in service.tags.slice(0, 3)"
                  :key="tag"
                  variant="outline"
                  class="text-xs"
                >
                  {{ tag }}
                </Badge>
              </div>
            </div>
          </div>
          <div v-else class="text-center py-8 text-gray-500">
            No offers found. Try adjusting your filters.
          </div>
        </TabsContent>

        <TabsContent value="requests" class="mt-4 space-y-3 pb-6">
          <div v-if="requests.length > 0">
            <div
              v-for="service in requests"
              :key="service.id"
              @click="$emit('selectService', service.id)"
              :class="[
                'p-4 border border-gray-200 rounded-lg cursor-pointer transition-colors',
                selectedServiceId === service.id 
                  ? 'bg-amber-50 border-amber-300' 
                  : 'hover:bg-gray-50'
              ]"
            >
              <div class="flex items-start justify-between mb-2">
                <div class="flex-1">
                  <h3 class="font-medium text-gray-900 mb-1">{{ service.title }}</h3>
                  <p class="text-sm text-gray-600">{{ service.poster.name }}</p>
                </div>
                <Badge class="bg-blue-100 text-blue-700 hover:bg-blue-100">
                  {{ service.type }}
                </Badge>
              </div>
              
              <p class="text-sm text-gray-600 mb-2 line-clamp-2">
                {{ service.description }}
              </p>
              
              <div class="flex items-center gap-4 text-xs text-gray-500">
                <div class="flex items-center gap-1">
                  <MapPin class="w-3 h-3" />
                  <span>{{ service.location }}</span>
                </div>
                <div class="flex items-center gap-1">
                  <Clock class="w-3 h-3" />
                  <span>{{ service.timebank }}</span>
                </div>
              </div>
              
              <div class="flex flex-wrap gap-1 mt-2">
                <Badge
                  v-for="tag in service.tags.slice(0, 3)"
                  :key="tag"
                  variant="outline"
                  class="text-xs"
                >
                  {{ tag }}
                </Badge>
              </div>
            </div>
          </div>
          <div v-else class="text-center py-8 text-gray-500">
            No requests found. Try adjusting your filters.
          </div>
        </TabsContent>
      </Tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { Settings, Clock, Search, X, Award, MapPin } from 'lucide-vue-next'
import Badge from './ui/Badge.vue'
import Button from './ui/Button.vue'
import Input from './ui/Input.vue'
import Select from './ui/Select.vue'
import SelectContent from './ui/SelectContent.vue'
import SelectItem from './ui/SelectItem.vue'
import SelectTrigger from './ui/SelectTrigger.vue'
import SelectValue from './ui/SelectValue.vue'
import Tabs from './ui/Tabs.vue'
import TabsContent from './ui/TabsContent.vue'
import TabsList from './ui/TabsList.vue'
import TabsTrigger from './ui/TabsTrigger.vue'
import { filterServices, getAllTags } from '../services/dataService'
import { useAppStore } from '../stores/appStore'

interface Props {
  selectedServiceId?: string | null
}

defineProps<Props>()

defineEmits<{
  selectService: [serviceId: string]
}>()

const appStore = useAppStore()
const currentUser = computed(() => appStore.currentUser)

// Filter state
const searchQuery = ref('')
const selectedTags = ref<string[]>([])
const selectedBadge = ref('all')

// Get all unique tags
const allTags = getAllTags()

// Filter services based on search, selected tags, and badge
const filteredServices = computed(() => {
  return filterServices({
    searchQuery: searchQuery.value,
    tags: selectedTags.value,
    badge: selectedBadge.value as any,
  })
})

const offers = computed(() => filteredServices.value.filter((s) => s.type === 'OFFER'))
const requests = computed(() => filteredServices.value.filter((s) => s.type === 'REQUEST'))

const toggleTag = (tag: string) => {
  const index = selectedTags.value.indexOf(tag)
  if (index > -1) {
    selectedTags.value.splice(index, 1)
  } else {
    selectedTags.value.push(tag)
  }
}

const clearFilters = () => {
  searchQuery.value = ''
  selectedTags.value = []
  selectedBadge.value = 'all'
}
</script>

