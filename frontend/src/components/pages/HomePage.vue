<template>
  <div class="flex-1 overflow-y-auto bg-gray-50">
    <!-- Hero Section with Search -->
    <div class="relative h-[500px] bg-gray-900">
      <ImageWithFallback 
        src="https://images.unsplash.com/photo-1759752394757-323a0adc0d62?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxjb21tdW5pdHklMjBjb2xsYWJvcmF0aW9uJTIwcGVvcGxlfGVufDF8fHx8MTc2MTA0NjQ0MXww&ixlib=rb-4.1.0&q=80&w=1080&utm_source=figma&utm_medium=referral"
        alt="Community collaboration"
        class-name="w-full h-full object-cover"
      />
      <div class="absolute inset-0 bg-black/50" />
      
      <!-- Hero Content -->
      <div class="absolute inset-0 flex flex-col items-center justify-center px-8">
        <div class="max-w-3xl w-full text-center">
          <h1 class="text-white mb-4">Share Skills, Build Community</h1>
          <p class="text-white/90 text-xl mb-8">
            Exchange services with your neighbors using time instead of money
          </p>
          
          <!-- Search Box -->
          <div class="space-y-4">
            <div class="relative">
              <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400 z-10" />
              <Input
                v-model="heroSearch"
                type="text"
                placeholder="Search for services, tags, or locations..."
                class="pl-12 bg-white h-14"
                @keyup.enter="performHeroSearch"
              />
            </div>

            <Button 
              variant="custom"
              class="w-full bg-amber-500 hover:bg-amber-600 text-white h-14"
              @click="performHeroSearch"
            >
              <Search class="w-5 h-5 mr-2" />
              Search Services
            </Button>
          </div>
        </div>
      </div>
    </div>

    <!-- Content Below Hero -->
    <div class="max-w-5xl mx-auto p-8">
      <!-- TimeBank Summary Card -->
      <Card class="mb-8 p-6 bg-gradient-to-r from-amber-50 to-orange-50 border-amber-200">
        <div class="flex items-center justify-between">
          <div class="flex-1">
            <div class="text-gray-600 mb-1">Your TimeBank Balance</div>
            <div class="flex items-center gap-3">
              <Clock class="w-6 h-6 text-amber-600" />
              <span class="text-gray-900 text-2xl">{{ appStore.currentUser?.timebankBalance ?? 0 }} hours</span>
            </div>
          </div>
          <div class="text-right mr-6">
            <div class="text-sm text-gray-600 mb-1">This month</div>
            <div class="flex items-center gap-4">
              <div>
                <div class="text-xs text-gray-500">Given</div>
                <div class="text-emerald-600">+{{ appStore.currentUser?.hoursGiven ?? 0 }}h</div>
              </div>
              <div>
                <div class="text-xs text-gray-500">Received</div>
                <div class="text-blue-600">-{{ appStore.currentUser?.hoursReceived ?? 0 }}h</div>
              </div>
            </div>
          </div>
          <Button 
            variant="custom"
            @click="appStore.setCurrentPage('create-offer')"
            class="bg-emerald-600 hover:bg-emerald-700 text-white"
            size="lg"
          >
            + New Service
          </Button>
        </div>
      </Card>

      <!-- Popular Tags -->
      <div class="mb-8">
        <h3 class="text-gray-900 mb-4">Popular Tags</h3>
        <div class="flex flex-wrap gap-2">
          <Badge 
            v-for="(tag, index) in popularTags"
            :key="index"
            variant="outline" 
            class="cursor-pointer hover:bg-gray-100 hover:border-gray-400 transition-colors"
            @click="filterByTag(tag)"
          >
            <Tag class="w-3 h-3 mr-1" />
            {{ tag }}
          </Badge>
        </div>
      </div>

      <!-- Filter Bar -->
      <Card class="p-4 mb-6 bg-white shadow-sm">
        <div class="flex flex-col gap-3">
          <div class="grid grid-cols-1 md:grid-cols-4 gap-3">
          <div class="relative">
            <Tag class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400 z-10" />
            <Input
              v-model="filters.searchQuery"
              type="text"
              placeholder="Tags or services"
              class="pl-10"
            />
          </div>
          <div class="relative">
            <MapPin class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400 z-10" />
            <Input
              v-model="filters.location"
              type="text"
              placeholder="Location"
              class="pl-10"
            />
          </div>
          <div class="relative">
            <Award class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400 z-10 pointer-events-none" />
            <Select v-model="filters.badge">
              <SelectTrigger class="pl-10">
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
            <Button 
              v-if="hasActiveFilters" 
              variant="outline" 
              @click="clearFilters"
              class="border-gray-300 hover:bg-gray-50"
            >
              Clear Filters
            </Button>
            <Button 
              v-else
              variant="custom"
              class="bg-gray-100 text-gray-400 cursor-not-allowed"
              disabled
            >
              <Search class="w-4 h-4 mr-2" />
              No Filters
            </Button>
          </div>
        </div>
      </Card>

      <!-- Active Filters Indicator -->
      <div v-if="hasActiveFilters" class="mb-4 p-3 bg-blue-50 border border-blue-200 rounded-md">
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-2 text-sm text-blue-900">
            <Search class="w-4 h-4" />
            <span>Filters active - showing {{ filteredServices.length }} result(s)</span>
          </div>
          <button 
            @click="clearFilters"
            class="text-blue-600 hover:text-blue-800 text-sm font-medium"
          >
            Clear all
          </button>
        </div>
      </div>

      <!-- Services in Your Area -->
      <div class="mb-8">
        <h3 class="text-gray-900 mb-4">Services in Your Area</h3>
        <div v-if="loading" class="text-center py-12 text-gray-500">
          <Clock class="w-12 h-12 mx-auto mb-3 text-gray-400 animate-spin" />
          <p class="text-lg">Loading services...</p>
        </div>
        <div v-else-if="nearbyServices.length === 0" class="text-center py-12 text-gray-500">
          <MapPin class="w-12 h-12 mx-auto mb-3 text-gray-400" />
          <p class="text-lg">No services found in your area</p>
          <p class="text-sm">Try adjusting your filters or search query</p>
        </div>
        <div v-else class="grid grid-cols-1 md:grid-cols-3 gap-4">
          <Card 
            v-for="service in nearbyServices"
            :key="service.id"
            class="p-4 hover:border-gray-300 transition-colors cursor-pointer" 
            @click="appStore.setSelectedServiceId(service.id)"
          >
            <div class="flex items-start justify-between mb-2">
              <div class="flex-1">
                <div class="text-gray-900 mb-1">{{ service.title }}</div>
                <div class="text-sm text-gray-600">{{ service.poster.name }}</div>
              </div>
              <Badge 
                variant="custom"
                :class="service.type === 'OFFER' ? 'bg-emerald-100 text-emerald-700 hover:bg-emerald-100' : 'bg-blue-100 text-blue-700 hover:bg-blue-100'"
              >
                {{ service.type }}
              </Badge>
            </div>
            <div class="flex items-center gap-4 text-sm text-gray-500">
              <div class="flex items-center gap-1">
                <MapPin class="w-3 h-3" />
                <span>{{ service.location }}</span>
              </div>
              <div class="flex items-center gap-1 text-amber-600">
                <Clock class="w-3 h-3" />
                <span>{{ service.timebank }}</span>
              </div>
            </div>
            <div class="flex gap-1 mt-2">
              <Badge
                v-for="(tag, idx) in service.tags.slice(0, 2)"
                :key="idx"
                variant="outline"
                class="text-xs"
              >
                {{ tag }}
              </Badge>
              <Badge
                v-if="service.distance"
                variant="secondary"
                class="text-xs ml-auto"
              >
                {{ service.distance }}
              </Badge>
            </div>
          </Card>
        </div>
      </div>

      <!-- Services You Might Be Interested In -->
      <div v-if="!hasActiveFilters" class="mb-8">
        <h3 class="text-gray-900 mb-4">Services You Might Be Interested In</h3>
        <div v-if="loading" class="text-center py-12 text-gray-500">
          <Clock class="w-12 h-12 mx-auto mb-3 text-gray-400 animate-spin" />
          <p class="text-lg">Loading services...</p>
        </div>
        <div v-else-if="recommendedServices.length === 0" class="text-center py-12 text-gray-500">
          <Tag class="w-12 h-12 mx-auto mb-3 text-gray-400" />
          <p class="text-lg">No recommended services available</p>
        </div>
        <div v-else class="grid grid-cols-1 md:grid-cols-3 gap-4">
          <Card 
            v-for="service in recommendedServices"
            :key="service.id"
            class="p-4 hover:border-gray-300 transition-colors cursor-pointer" 
            @click="appStore.setSelectedServiceId(service.id)"
          >
            <div class="flex items-start justify-between mb-2">
              <div class="flex-1">
                <div class="text-gray-900 mb-1">{{ service.title }}</div>
                <div class="text-sm text-gray-600">{{ service.poster.name }}</div>
              </div>
              <Badge 
                variant="custom"
                :class="service.type === 'OFFER' ? 'bg-emerald-100 text-emerald-700 hover:bg-emerald-100' : 'bg-blue-100 text-blue-700 hover:bg-blue-100'"
              >
                {{ service.type }}
              </Badge>
            </div>
            <div class="flex items-center gap-4 text-sm text-gray-500">
              <div class="flex items-center gap-1">
                <MapPin class="w-3 h-3" />
                <span>{{ service.location }}</span>
              </div>
              <div class="flex items-center gap-1 text-amber-600">
                <Clock class="w-3 h-3" />
                <span>{{ service.timebank }}</span>
              </div>
            </div>
            <div class="flex gap-1 mt-2">
              <Badge
                v-for="(tag, idx) in service.tags.slice(0, 2)"
                :key="idx"
                variant="outline"
                class="text-xs"
              >
                {{ tag }}
              </Badge>
            </div>
          </Card>
        </div>
      </div>

      <!-- Community Stats -->
      <Card class="p-6 mt-8 bg-gray-50">
        <h3 class="text-gray-900 mb-4">Community Stats</h3>
        <div v-if="loading" class="text-center py-8 text-gray-500">
          <Clock class="w-8 h-8 mx-auto mb-2 text-gray-400 animate-spin" />
          <p>Loading stats...</p>
        </div>
        <div v-else class="grid grid-cols-2 md:grid-cols-4 gap-6">
          <div>
            <div class="text-3xl text-gray-900">{{ communityStats.activeMembers }}</div>
            <div class="text-sm text-gray-600">Active Members</div>
          </div>
          <div>
            <div class="text-3xl text-gray-900">{{ communityStats.hoursExchanged.toLocaleString() }}</div>
            <div class="text-sm text-gray-600">Hours Exchanged</div>
          </div>
          <div>
            <div class="text-3xl text-gray-900">{{ communityStats.activeServices }}</div>
            <div class="text-sm text-gray-600">Active Services</div>
          </div>
          <div>
            <div class="text-3xl text-gray-900">{{ communityStats.completedThisMonth }}</div>
            <div class="text-sm text-gray-600">Completed This Month</div>
          </div>
        </div>
      </Card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref, onMounted } from 'vue'
import { Search, MapPin, Clock, Tag, Award } from 'lucide-vue-next'
import Input from '../ui/Input.vue'
import Button from '../ui/Button.vue'
import Badge from '../ui/Badge.vue'
import Card from '../ui/Card.vue'
import ImageWithFallback from '../ui/ImageWithFallback.vue'
import Select from '../ui/Select.vue'
import SelectTrigger from '../ui/SelectTrigger.vue'
import SelectValue from '../ui/SelectValue.vue'
import SelectContent from '../ui/SelectContent.vue'
import SelectItem from '../ui/SelectItem.vue'
import { getActiveServices, getAllTags, getCommunityStats, filterServices, getNearbyServices, getRecommendedServices } from '../../services/marketplaceService'
import { useAppStore } from '../../stores/appStore'
import type { BadgeType, Service, CommunityStats } from '../../types'

const appStore = useAppStore()
const allServices = ref<Service[]>([])
const allTags = ref<string[]>([])
const nearbyServicesData = ref<Service[]>([])
const recommendedServicesData = ref<Service[]>([])
const communityStats = ref<CommunityStats>({
  activeMembers: 0,
  hoursExchanged: 0,
  activeServices: 0,
  completedThisMonth: 0
})

// Hero search state
const heroSearch = ref('')

// Loading state
const loading = ref(true)

// Filter state
const filters = reactive<{
  searchQuery: string
  location: string
  badge: BadgeType | 'all' | ''
}>({
  searchQuery: '',
  location: '',
  badge: ''
})

// Load data on component mount
onMounted(async () => {
  loading.value = true
  try {
    const [services, tags, stats, nearby, recommended] = await Promise.all([
      getActiveServices(),
      getAllTags(),
      getCommunityStats(),
      getNearbyServices(6),
      getRecommendedServices(3)
    ])
    allServices.value = services
    allTags.value = tags
    communityStats.value = stats
    nearbyServicesData.value = nearby
    recommendedServicesData.value = recommended
  } catch (error) {
    console.error('Failed to load data:', error)
  } finally {
    loading.value = false
  }
})

// Get popular tags (top 10 most used)
const popularTags = computed(() => allTags.value.slice(0, 10))

// Check if any filters are active
const hasActiveFilters = computed(() => {
  return !!(filters.searchQuery || filters.location || (filters.badge && filters.badge !== 'all'))
})

// Apply filters to services
const filteredServices = computed(() => {
  if (!hasActiveFilters.value) {
    return allServices.value
  }

  return filterServices(allServices.value, {
    searchQuery: filters.searchQuery || undefined,
    location: filters.location || undefined,
    badge: filters.badge && filters.badge !== 'all' ? filters.badge : undefined
  })
})

// Sort services by distance and get nearby ones
const nearbyServices = computed(() => {
  // If filters are active, use filtered results
  if (hasActiveFilters.value) {
    return [...filteredServices.value]
      .filter(s => s.distance)
      .sort((a, b) => parseFloat(a.distance!) - parseFloat(b.distance!))
      .slice(0, 6)
  }
  // Otherwise use the smart nearby recommendations
  return nearbyServicesData.value
})

// Get recommended services (different criteria)
const recommendedServices = computed(() => {
  // Don't show recommendations when filters are active
  if (hasActiveFilters.value) {
    return []
  }
  return recommendedServicesData.value
})

// Hero search
const performHeroSearch = () => {
  filters.searchQuery = heroSearch.value
}

// Filter by tag click
const filterByTag = (tag: string) => {
  filters.searchQuery = tag
}

// Clear filters
const clearFilters = () => {
  filters.searchQuery = ''
  filters.location = ''
  filters.badge = ''
  heroSearch.value = ''
}
</script>

