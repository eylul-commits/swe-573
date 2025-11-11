<template>
  <div class="relative w-full h-full bg-gray-100">
    <!-- Map placeholder with visual styling -->
    <div class="absolute inset-0 flex items-center justify-center">
      <!-- Grid background to simulate map -->
      <div class="absolute inset-0 opacity-10">
        <svg class="w-full h-full" xmlns="http://www.w3.org/2000/svg">
          <defs>
            <pattern id="grid" width="40" height="40" patternUnits="userSpaceOnUse">
              <path d="M 40 0 L 0 0 0 40" fill="none" stroke="gray" stroke-width="1"/>
            </pattern>
          </defs>
          <rect width="100%" height="100%" fill="url(#grid)" />
        </svg>
      </div>
      
      <!-- Map markers simulation -->
      <div class="relative z-10 w-full h-full p-8">
        <div class="relative w-full h-full">
          <!-- Sample markers at different positions -->
          <div
            v-for="(marker, index) in markers"
            :key="index"
            :style="{
              position: 'absolute',
              left: marker.x + '%',
              top: marker.y + '%',
              transform: 'translate(-50%, -50%)'
            }"
            class="group cursor-pointer"
          >
            <div class="relative">
              <!-- Marker pin -->
              <div :class="[
                'w-8 h-8 rounded-full shadow-lg flex items-center justify-center transition-transform group-hover:scale-110',
                marker.type === 'OFFER' ? 'bg-emerald-500' : 'bg-blue-500'
              ]">
                <MapPin class="w-4 h-4 text-white" />
              </div>
              
              <!-- Tooltip on hover -->
              <div class="absolute bottom-full left-1/2 transform -translate-x-1/2 mb-2 opacity-0 group-hover:opacity-100 transition-opacity pointer-events-none">
                <div class="bg-gray-900 text-white text-xs rounded px-2 py-1 whitespace-nowrap">
                  {{ marker.title }}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Map Controls (zoom, etc.) -->
      <div class="absolute bottom-4 right-4 flex flex-col gap-2 z-20">
        <Button variant="outline" size="sm" class="bg-white shadow-md">
          <Plus class="w-4 h-4" />
        </Button>
        <Button variant="outline" size="sm" class="bg-white shadow-md">
          <Minus class="w-4 h-4" />
        </Button>
        <Button variant="outline" size="sm" class="bg-white shadow-md">
          <Locate class="w-4 h-4" />
        </Button>
      </div>

      <!-- Legend -->
      <div class="absolute top-4 right-4 bg-white rounded-lg shadow-md p-3 text-xs z-20">
        <div class="font-semibold mb-2">Legend</div>
        <div class="flex items-center gap-2 mb-1">
          <div class="w-3 h-3 rounded-full bg-emerald-500"></div>
          <span>Offers</span>
        </div>
        <div class="flex items-center gap-2">
          <div class="w-3 h-3 rounded-full bg-blue-500"></div>
          <span>Requests</span>
        </div>
      </div>

      <!-- Location label -->
      <div class="absolute top-4 left-4 bg-white rounded-lg shadow-md px-3 py-2 text-sm font-medium z-20">
        <div class="flex items-center gap-2">
          <MapPin class="w-4 h-4 text-gray-600" />
          <span>Your Community</span>
        </div>
      </div>
    </div>

    <!-- Map attribution/note -->
    <div class="absolute bottom-2 left-2 text-xs text-gray-500 z-10">
      Interactive map view
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, onMounted } from 'vue'
import { MapPin, Plus, Minus, Locate } from 'lucide-vue-next'
import Button from './ui/Button.vue'
import { getAllServices } from '../services/dataService'
import type { Service } from '../types'

// Generate marker positions from services
const services = ref<Service[]>([])

onMounted(async () => {
  services.value = await getAllServices()
})

const markers = computed(() => {
  return services.value.slice(0, 12).map((service, index) => ({
    id: service.id,
    title: service.title,
    type: service.type,
    // Distribute markers across the map
    x: 20 + (index % 4) * 20 + Math.random() * 10,
    y: 20 + Math.floor(index / 4) * 20 + Math.random() * 10
  }))
})
</script>

