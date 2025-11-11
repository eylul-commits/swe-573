<template>
  <div class="flex flex-1 h-full bg-gray-50">
    <!-- Main Feed - Left Panel (Fixed width) -->
    <div class="w-[640px] bg-white border-r border-gray-200 overflow-y-auto flex-shrink-0">
      <ServiceList 
        :selected-service-id="selectedServiceId"
        @select-service="setSelectedServiceId"
      />
    </div>

    <!-- Right Content Area - Flexible -->
    <div class="flex-1 flex flex-col min-w-0">
      <!-- Map - Dynamic height based on selection -->
      <div 
        :class="[
          'relative border-b border-gray-200 transition-all',
          selectedServiceId ? 'h-[45%]' : 'h-full'
        ]"
      >
        <CommunityMap />
      </div>

      <!-- Service Description & Comments - Only show when selected -->
      <div 
        v-if="selectedServiceId"
        class="flex-1 bg-white overflow-y-auto"
      >
        <ServiceInfo 
          :service-id="selectedServiceId" 
          @view-full-details="handleViewFullDetails"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import ServiceList from '../ServiceList.vue'
import CommunityMap from '../CommunityMap.vue'
import ServiceInfo from '../ServiceInfo.vue'
import { useAppStore } from '../../stores/appStore'

const appStore = useAppStore()

// Local state for selected service in Explore page  
const selectedServiceId = ref<string | null>(null)

const setSelectedServiceId = (serviceId: string) => {
  selectedServiceId.value = serviceId
}

const handleViewFullDetails = () => {
  if (selectedServiceId.value) {
    appStore.setSelectedServiceId(selectedServiceId.value)
  }
}
</script>

