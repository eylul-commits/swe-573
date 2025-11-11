<template>
  <div class="relative w-full h-full">
    <!-- Map container -->
    <div ref="mapContainer" class="w-full h-full"></div>
    
    <!-- Legend -->
    <div class="absolute top-4 right-4 bg-white rounded-lg shadow-md p-3 text-xs z-[1000]">
      <div class="font-semibold mb-2">Legend</div>
      <div class="flex items-center gap-2 mb-1">
        <div class="w-3 h-3 rounded-full bg-emerald-500"></div>
        <span>Offers ({{ offerCount }})</span>
      </div>
      <div class="flex items-center gap-2">
        <div class="w-3 h-3 rounded-full bg-blue-500"></div>
        <span>Requests ({{ requestCount }})</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed, watch } from 'vue'
import L from 'leaflet'
import geohash from 'ngeohash'
import 'leaflet/dist/leaflet.css'
import { getAllServices } from '../services/dataService'
import type { Service } from '../types'

const props = defineProps<{
  selectedServiceId?: string | null
}>()

const mapContainer = ref<HTMLElement | null>(null)
let map: L.Map | null = null
const services = ref<Service[]>([])
const markers = ref<L.Marker[]>([])
const markersById = new Map<string, L.Marker>()
let activeMarkerId: string | null = null

const offerCount = computed(() => services.value.filter((s: Service) => s.type === 'OFFER').length)
const requestCount = computed(() => services.value.filter((s: Service) => s.type === 'REQUEST').length)

// Create custom icons for offers and requests
const createCustomIcon = (type: 'OFFER' | 'REQUEST') => {
  const color = type === 'OFFER' ? '#10b981' : '#3b82f6'
  const svgIcon = `
    <svg width="32" height="32" viewBox="0 0 32 32" xmlns="http://www.w3.org/2000/svg">
      <path d="M16 2 C16 2, 8 8, 8 16 C8 20, 12 24, 16 30 C20 24, 24 20, 24 16 C24 8, 16 2, 16 2 Z" 
            fill="${color}" stroke="white" stroke-width="2"/>
      <circle cx="16" cy="14" r="5" fill="white"/>
    </svg>
  `
  
  return L.divIcon({
    html: svgIcon,
    className: 'custom-marker-icon',
    iconSize: [32, 32],
    iconAnchor: [16, 32],
    popupAnchor: [0, -32]
  })
}

const initMap = async () => {
  if (!mapContainer.value) return

  // Initialize the map - centered on Turkey/Istanbul by default
  map = L.map(mapContainer.value, {
    center: [41.0082, 28.9784], // Istanbul coordinates as default
    zoom: 10,
    zoomControl: true
  })

  // Add OpenStreetMap tile layer
  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '© <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
    maxZoom: 19
  }).addTo(map)

  // Load services and add markers
  try {
    services.value = await getAllServices()
    console.debug(`Loaded ${services.value.length} services for map`)
    if (services.value.length > 0) {
      console.debug('Sample service:', services.value[0])
    }
    addMarkers()
  } catch (error) {
    console.error('Error loading services:', error)
  }
}

const focusOnService = (serviceId: string) => {
  if (!map) return

  const service = services.value.find((s) => s.id === serviceId)
  if (!service) {
    console.warn(`Service ${serviceId} not found in map data`)
    return
  }

  if (!service.geohash) {
    console.warn(`Selected service ${service.id} (${service.title}) has no geohash`)
    return
  }

  const { latitude, longitude } = geohash.decode(service.geohash)
  const targetLatLng: L.LatLngExpression = [latitude, longitude]
  const currentZoom = map.getZoom()
  const targetZoom = currentZoom < 13 ? 13 : currentZoom

  map.whenReady(() => {
    map!.flyTo(targetLatLng, targetZoom, { duration: 0.8 })

    // Reset previous active marker styling
    if (activeMarkerId && markersById.has(activeMarkerId)) {
      markersById.get(activeMarkerId)!.setZIndexOffset(0)
    }

    const marker = markersById.get(serviceId)
    if (marker) {
      marker.openPopup()
      marker.setZIndexOffset(1000)
      activeMarkerId = serviceId
    } else {
      activeMarkerId = null
    }
  })
}

const addMarkers = () => {
  if (!map) return

  // Clear existing markers
  markers.value.forEach(marker => {
    marker.remove()
  })
  markers.value = []
  markersById.clear()

  const bounds: L.LatLngBoundsExpression = []

  services.value.forEach((service: Service) => {
    // Check if service has geohash data
    const serviceGeohash = service.geohash
    if (!serviceGeohash) {
      console.warn(`Service ${service.id} (${service.title}) has no geohash`)
      return
    }

    try {
      // Decode geohash to lat/lng
      const { latitude, longitude } = geohash.decode(serviceGeohash)
      console.debug(`Decoded ${service.title}: ${serviceGeohash} -> [${latitude}, ${longitude}]`)
      
      // Create marker with custom icon
      const marker = L.marker([latitude, longitude], {
        icon: createCustomIcon(service.type)
      })

      // Add popup with service information
      const popupContent = `
        <div class="p-2 min-w-[200px]">
          <h3 class="font-bold text-sm mb-1">${service.title}</h3>
          <p class="text-xs text-gray-600 mb-2">${service.description?.substring(0, 100) || ''}${service.description?.length > 100 ? '...' : ''}</p>
          <div class="flex items-center gap-2 text-xs">
            <span class="px-2 py-1 rounded ${service.type === 'OFFER' ? 'bg-emerald-100 text-emerald-700' : 'bg-blue-100 text-blue-700'}">
              ${service.type}
            </span>
            <span class="text-gray-500">${service.location || service.district || service.province || ''}</span>
          </div>
          ${service.poster ? `<div class="mt-2 text-xs text-gray-600">By: ${service.poster.name}</div>` : ''}
        </div>
      `
      
      marker.bindPopup(popupContent)
      marker.addTo(map!)
      markers.value.push(marker)
      markersById.set(service.id, marker)

      // Add to bounds for auto-fitting
      bounds.push([latitude, longitude])
    } catch (error) {
      console.error(`Error decoding geohash for service ${service.id}:`, error)
    }
  })

  // Fit map to show all markers
  if (bounds.length > 0 && map) {
    console.debug(`Added ${markers.value.length} markers to map`)
    if (props.selectedServiceId) {
      focusOnService(props.selectedServiceId)
    } else {
      map.fitBounds(bounds, { padding: [50, 50] })
    }
  } else {
    console.warn('No markers to display on map')
  }
}

watch(() => props.selectedServiceId, (newId, oldId) => {
  if (!newId) {
    if (oldId && markersById.has(oldId)) {
      markersById.get(oldId)!.setZIndexOffset(0)
    }
    activeMarkerId = null
    return
  }

  focusOnService(newId)
})

watch(services, () => {
  if (props.selectedServiceId) {
    focusOnService(props.selectedServiceId)
  }
})

onMounted(() => {
  initMap()
})

onUnmounted(() => {
  if (map) {
    map.remove()
    map = null
  }
})
</script>

<style scoped>
:deep(.leaflet-container) {
  width: 100%;
  height: 100%;
  border-radius: inherit;
}

:deep(.custom-marker-icon) {
  background: none;
  border: none;
}

:deep(.leaflet-popup-content-wrapper) {
  border-radius: 8px;
}

:deep(.leaflet-popup-content) {
  margin: 0;
}
</style>

