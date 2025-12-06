<template>
  <div class="flex-1 overflow-y-auto bg-gray-50 p-8">
    <div class="max-w-4xl mx-auto">
      <Button @click="appStore.setCurrentPage('home')" class="mb-4">
        ← Back
      </Button>
      
      <h1 class="text-3xl font-bold text-gray-900 mb-6">Create New Service</h1>
      
      <Card class="p-6">
        <form @submit.prevent="handleSubmit" class="space-y-6">
          <!-- Service Type Selection -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-3">
              What would you like to do? <span class="text-red-500">*</span>
            </label>
            <div class="grid grid-cols-2 gap-4">
              <button
                type="button"
                @click="serviceType = 'OFFER'"
                :class="[
                  'p-4 rounded-lg border-2 transition-all',
                  serviceType === 'OFFER'
                    ? 'border-emerald-500 bg-emerald-50'
                    : 'border-gray-200 hover:border-gray-300'
                ]"
              >
                <div class="flex flex-col items-center gap-2">
                  <div 
                    :class="[
                      'w-12 h-12 rounded-full flex items-center justify-center',
                      serviceType === 'OFFER' ? 'bg-emerald-500' : 'bg-gray-200'
                    ]"
                  >
                    <span class="text-2xl">🎁</span>
                  </div>
                  <div>
                    <div 
                      :class="[
                        'font-semibold',
                        serviceType === 'OFFER' ? 'text-emerald-700' : 'text-gray-700'
                      ]"
                    >
                      Offer a Service
                    </div>
                    <div class="text-xs text-gray-500 mt-1">
                      Provide help to others
                    </div>
                  </div>
                </div>
              </button>
              
              <button
                type="button"
                @click="serviceType = 'REQUEST'"
                :class="[
                  'p-4 rounded-lg border-2 transition-all',
                  serviceType === 'REQUEST'
                    ? 'border-blue-500 bg-blue-50'
                    : 'border-gray-200 hover:border-gray-300'
                ]"
              >
                <div class="flex flex-col items-center gap-2">
                  <div 
                    :class="[
                      'w-12 h-12 rounded-full flex items-center justify-center',
                      serviceType === 'REQUEST' ? 'bg-blue-500' : 'bg-gray-200'
                    ]"
                  >
                    <span class="text-2xl">🙋</span>
                  </div>
                  <div>
                    <div 
                      :class="[
                        'font-semibold',
                        serviceType === 'REQUEST' ? 'text-blue-700' : 'text-gray-700'
                      ]"
                    >
                      Request a Service
                    </div>
                    <div class="text-xs text-gray-500 mt-1">
                      Ask for help from others
                    </div>
                  </div>
                </div>
              </button>
            </div>
          </div>

          <!-- Title -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">
              Title <span class="text-red-500">*</span>
            </label>
            <Input 
              v-model="formData.title"
              placeholder="Enter service title"
              required
            />
          </div>
          
          <!-- Description -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">
              Description <span class="text-red-500">*</span>
            </label>
            <Textarea 
              v-model="formData.description"
              placeholder="Describe your service in detail"
              rows="5"
              required
            />
          </div>
          
          <!-- Duration -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">
              Duration (hours) <span class="text-red-500">*</span>
            </label>
            <Input 
              v-model.number="formData.durationHours"
              type="number"
              min="1"
              placeholder="e.g., 2"
              required
            />
          </div>
          
          <!-- Dates -->
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">
                Start Date <span class="text-red-500">*</span>
              </label>
              <Input 
                v-model="formData.startDate"
                type="date"
                required
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">
                End Date <span class="text-red-500">*</span>
              </label>
              <Input 
                v-model="formData.endDate"
                type="date"
                required
              />
            </div>
          </div>
          
          <!-- Province & District -->
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">
                Province <span class="text-red-500">*</span>
              </label>
              <select 
                v-model="formData.province"
                class="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                required
                @change="onProvinceChange"
              >
                <option value="">Select province</option>
                <option v-for="province in provinces" :key="province" :value="province">
                  {{ province }}
                </option>
              </select>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">
                District <span class="text-red-500">*</span>
              </label>
              <Input 
                v-model="formData.district"
                placeholder="Enter district"
                required
              />
            </div>
          </div>
          
          <!-- Tags -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">
              Tags
            </label>
            <div class="flex gap-2 mb-2">
              <Input 
                v-model="tagInput"
                placeholder="Add a tag"
                @keypress.enter.prevent="addTag"
              />
              <Button type="button" @click="addTag" variant="outline">
                Add Tag
              </Button>
            </div>
            <div class="flex flex-wrap gap-2">
              <Badge 
                v-for="(tag, index) in formData.tags" 
                :key="index"
                class="bg-emerald-100 text-emerald-800 cursor-pointer hover:bg-emerald-200"
                @click="removeTag(index)"
              >
                {{ tag }} ×
              </Badge>
            </div>
          </div>
          
          <!-- Map Section -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">
              Location <span class="text-red-500">*</span>
            </label>
            <p class="text-sm text-gray-500 mb-2">
              Search for a location or click on the map to select your service location
            </p>
            
            <!-- Location Search -->
            <div class="mb-3 relative">
              <div class="flex gap-2">
                <Input 
                  v-model="searchQuery"
                  placeholder="Search for a place (e.g., Ankara, Turkey)"
                  @keypress.enter.prevent="searchLocation"
                  class="flex-1"
                />
                <Button 
                  type="button" 
                  @click="searchLocation"
                  :disabled="!searchQuery || isSearching"
                  variant="outline"
                >
                  <span v-if="isSearching">Searching...</span>
                  <span v-else>🔍 Search</span>
                </Button>
              </div>
              
              <!-- Search Results Dropdown -->
              <div 
                v-if="searchResults.length > 0" 
                class="absolute z-10 w-full mt-1 bg-white border border-gray-300 rounded-md shadow-lg max-h-60 overflow-y-auto"
              >
                <button
                  v-for="(result, index) in searchResults"
                  :key="index"
                  type="button"
                  @click="selectSearchResult(result)"
                  class="w-full text-left px-4 py-2 hover:bg-gray-100 border-b border-gray-100 last:border-b-0"
                >
                  <div class="font-medium text-sm">{{ result.display_name }}</div>
                </button>
              </div>
            </div>
            
            <!-- Map wrapper with dynamic border (doesn't affect map container) -->
            <div 
              class="rounded-lg border-2 transition-colors"
              :class="formData.geohash ? 'border-emerald-500' : 'border-gray-300'"
            >
              <div 
                ref="mapContainer" 
                class="map-container rounded-lg"
              ></div>
            </div>
            <p v-if="formData.geohash" class="text-sm text-emerald-600 mt-2">
              Location selected: {{ selectedLocation.lat.toFixed(6) }}, {{ selectedLocation.lng.toFixed(6) }}
            </p>
            <p v-else class="text-sm text-red-500 mt-2">
              Please select a location on the map
            </p>
          </div>
          
          <!-- Submit Button -->
          <div class="flex gap-4">
            <Button 
              type="submit" 
              :class="[
                'flex-1 text-white',
                serviceType === 'OFFER' 
                  ? 'bg-emerald-600 hover:bg-emerald-700' 
                  : 'bg-blue-600 hover:bg-blue-700'
              ]"
              :disabled="isSubmitting || !formData.geohash"
            >
              <span v-if="isSubmitting">Creating...</span>
              <span v-else>
                {{ serviceType === 'OFFER' ? 'Create Offer' : 'Create Request' }}
              </span>
            </Button>
            <Button 
              type="button"
              variant="outline"
              @click="appStore.setCurrentPage('home')"
            >
              Cancel
            </Button>
          </div>
          
          <!-- Error Message -->
          <div v-if="errorMessage" class="p-4 bg-red-50 border border-red-200 rounded-md">
            <p class="text-sm text-red-600">{{ errorMessage }}</p>
          </div>
        </form>
      </Card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'
import { encode } from 'ngeohash'
import Card from '../ui/Card.vue'
import Button from '../ui/Button.vue'
import Input from '../ui/Input.vue'
import Textarea from '../ui/Textarea.vue'
import Badge from '../ui/Badge.vue'
import { useAppStore } from '../../stores/appStore'
import { createOffer, createRequest, type CreateOfferPayload, type CreateRequestPayload } from '../../services/marketplaceService'

// Fix Leaflet default icon issue with bundlers
delete (L.Icon.Default.prototype as any)._getIconUrl
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png',
  iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
  shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
})

const appStore = useAppStore()

// Service type selection
const serviceType = ref<'OFFER' | 'REQUEST'>('OFFER')

// Form data
const formData = ref<CreateOfferPayload | CreateRequestPayload>({
  title: '',
  description: '',
  durationHours: 1,
  startDate: '',
  endDate: '',
  province: '',
  district: '',
  geohash: '',
  tags: []
})

const tagInput = ref('')
const isSubmitting = ref(false)
const errorMessage = ref('')

// Map
const mapContainer = ref<HTMLElement | null>(null)
let map: L.Map | null = null
let marker: L.Marker | null = null

const selectedLocation = ref({ lat: 39.9334, lng: 32.8597 }) // Default to Ankara

// Search
const searchQuery = ref('')
const searchResults = ref<any[]>([])
const isSearching = ref(false)

// Turkish provinces
const provinces = ref([
  'Adana', 'Adıyaman', 'Afyonkarahisar', 'Ağrı', 'Aksaray', 'Amasya', 'Ankara', 'Antalya',
  'Ardahan', 'Artvin', 'Aydın', 'Balıkesir', 'Bartın', 'Batman', 'Bayburt', 'Bilecik',
  'Bingöl', 'Bitlis', 'Bolu', 'Burdur', 'Bursa', 'Çanakkale', 'Çankırı', 'Çorum',
  'Denizli', 'Diyarbakır', 'Düzce', 'Edirne', 'Elazığ', 'Erzincan', 'Erzurum', 'Eskişehir',
  'Gaziantep', 'Giresun', 'Gümüşhane', 'Hakkari', 'Hatay', 'Iğdır', 'Isparta', 'İstanbul',
  'İzmir', 'Kahramanmaraş', 'Karabük', 'Karaman', 'Kars', 'Kastamonu', 'Kayseri', 'Kilis',
  'Kırıkkale', 'Kırklareli', 'Kırşehir', 'Kocaeli', 'Konya', 'Kütahya', 'Malatya', 'Manisa',
  'Mardin', 'Mersin', 'Muğla', 'Muş', 'Nevşehir', 'Niğde', 'Ordu', 'Osmaniye',
  'Rize', 'Sakarya', 'Samsun', 'Şanlıurfa', 'Siirt', 'Sinop', 'Şırnak', 'Sivas',
  'Tekirdağ', 'Tokat', 'Trabzon', 'Tunceli', 'Uşak', 'Van', 'Yalova', 'Yozgat', 'Zonguldak'
])

// Initialize map
onMounted(() => {
  // Use setTimeout to ensure the container is fully rendered
  setTimeout(() => {
    if (mapContainer.value) {
      try {
        // Create map centered on Turkey
        map = L.map(mapContainer.value, {
          center: [39.9334, 32.8597],
          zoom: 6,
          zoomControl: true,
          scrollWheelZoom: true,
          attributionControl: true
        })
        
        // Add OpenStreetMap tiles
        const tileLayer = L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
          attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
          maxZoom: 19,
          minZoom: 3
        })
        
        tileLayer.addTo(map)
        
        tileLayer.on('tileerror', (error) => {
          console.error('Tile loading error:', error)
        })
        
        // Invalidate size after a short delay to ensure proper rendering
        setTimeout(() => {
          if (map) {
            map.invalidateSize()
          }
        }, 200)
        
        // Add click event to map
        map.on('click', (e: L.LeafletMouseEvent) => {
          const { lat, lng } = e.latlng
          placeMarker(lat, lng)
        })
      } catch (error) {
        console.error('Failed to initialize map:', error)
        errorMessage.value = 'Failed to load map. Please refresh the page.'
      }
    } else {
      console.error('Map container not found')
      errorMessage.value = 'Map container not available. Please refresh the page.'
    }
  }, 250)
})

onUnmounted(() => {
  if (map) {
    map.remove()
    map = null
  }
})

function onProvinceChange() {
  // Clear district when province changes
  // You could add district auto-completion here based on province
}

function addTag() {
  const tag = tagInput.value.trim()
  if (tag && !formData.value.tags.includes(tag)) {
    formData.value.tags.push(tag)
    tagInput.value = ''
  }
}

function removeTag(index: number) {
  formData.value.tags.splice(index, 1)
}

// Search for location using Nominatim
async function searchLocation() {
  if (!searchQuery.value.trim()) return
  
  isSearching.value = true
  searchResults.value = []
  
  try {
    const response = await fetch(
      `https://nominatim.openstreetmap.org/search?` +
      `q=${encodeURIComponent(searchQuery.value)}&` +
      `format=json&` +
      `limit=5&` +
      `countrycodes=tr&` +
      `addressdetails=1`
    )
    
    if (response.ok) {
      const results = await response.json()
      searchResults.value = results
    } else {
      errorMessage.value = 'Failed to search location. Please try again.'
    }
  } catch (error) {
    console.error('Search error:', error)
    errorMessage.value = 'Failed to search location. Please check your connection.'
  } finally {
    isSearching.value = false
  }
}

// Select a search result
function selectSearchResult(result: any) {
  const lat = parseFloat(result.lat)
  const lng = parseFloat(result.lon)
  
  // Close search results
  searchResults.value = []
  searchQuery.value = result.display_name
  
  // Update map center and zoom
  if (map) {
    map.setView([lat, lng], 15)
    
    // Place marker at the location
    placeMarker(lat, lng)
  }
}

// Helper function to place marker
function placeMarker(lat: number, lng: number) {
  if (!map) return
  
  try {
    // Remove existing marker if any
    if (marker) {
      map.removeLayer(marker)
      marker = null
    }
    
    // Add new marker
    marker = L.marker([lat, lng], {
      draggable: false,
      icon: L.icon({
        iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
        iconRetinaUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png',
        shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
        iconSize: [25, 41],
        iconAnchor: [12, 41],
        popupAnchor: [1, -34],
        shadowSize: [41, 41]
      })
    })
    
    marker.addTo(map)
    marker.bindPopup(`Selected location<br>Lat: ${lat.toFixed(6)}<br>Lng: ${lng.toFixed(6)}`).openPopup()
    
    // Update selected location
    selectedLocation.value = { lat, lng }
    
    // Generate geohash
    formData.value.geohash = encode(lat, lng, 9)
    
    // Force map to refresh and ensure tiles are visible
    setTimeout(() => {
      if (map) {
        map.invalidateSize()
      }
    }, 10)
  } catch (error) {
    console.error('Error placing marker:', error)
  }
}

async function handleSubmit() {
  // Validate dates
  if (new Date(formData.value.startDate) > new Date(formData.value.endDate)) {
    errorMessage.value = 'End date must be after start date'
    return
  }
  
  if (!formData.value.geohash) {
    errorMessage.value = 'Please select a location on the map'
    return
  }
  
  isSubmitting.value = true
  errorMessage.value = ''
  
  try {
    if (serviceType.value === 'OFFER') {
      await createOffer(formData.value as CreateOfferPayload)
    } else {
      await createRequest(formData.value as CreateRequestPayload)
    }
    // Success! Navigate to home or services page
    appStore.setCurrentPage('home')
  } catch (error: any) {
    errorMessage.value = error.response?.data?.message || 'Failed to create service. Please try again.'
  } finally {
    isSubmitting.value = false
  }
}
</script>

<style scoped>
.map-container {
  height: 400px;
  width: 100%;
  min-height: 400px;
  position: relative;
  z-index: 0;
  background-color: #f3f4f6;
  overflow: hidden;
}

/* Ensure Leaflet container fills the parent and is always visible */
:deep(.leaflet-container) {
  height: 100%;
  width: 100%;
  border-radius: 0.5rem;
  background-color: #e5e7eb;
  font-family: inherit;
  position: relative;
  z-index: 1;
}

/* Ensure Leaflet tiles and pane are visible and on top */
:deep(.leaflet-tile-pane) {
  z-index: 2 !important;
}

:deep(.leaflet-tile) {
  image-rendering: auto;
  opacity: 1 !important;
}

:deep(.leaflet-tile-container) {
  opacity: 1 !important;
}

/* Ensure Leaflet controls are visible */
:deep(.leaflet-control-zoom) {
  border: 2px solid rgba(0,0,0,0.2);
  border-radius: 4px;
  z-index: 10;
}

:deep(.leaflet-bar) {
  border: 2px solid rgba(0,0,0,0.2);
  box-shadow: 0 1px 5px rgba(0,0,0,0.65);
}

:deep(.leaflet-control-zoom a) {
  background-color: white;
  color: black;
  text-decoration: none;
}

:deep(.leaflet-control-attribution) {
  background-color: rgba(255, 255, 255, 0.7);
  font-size: 11px;
  z-index: 10;
}

/* Fix for marker icons */
:deep(.leaflet-marker-icon) {
  margin-left: -12px;
  margin-top: -41px;
}

:deep(.leaflet-marker-shadow) {
  opacity: 0.5;
}
</style>

