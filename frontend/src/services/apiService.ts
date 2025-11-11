/**
 * API Service Layer
 * 
 * This service handles all HTTP requests to the backend API.
 */

import { api } from '../config/api';
import type { Service, User } from '../types';

// Backend DTOs matching the Java backend structure
interface BackendAuthorDTO {
  id: number;
  name: string;
  avatar: string | null;
  badge: string;
}

interface BackendServiceDTO {
  id: number;
  type: 'OFFER' | 'REQUEST';
  title: string;
  description: string;
  timebank: number; // hours
  startDate: string | null;
  endDate: string | null;
  location: string;
  province: string | null;
  district: string | null;
  geohash: string | null;
  status: string;
  createdAt: string;
  updatedAt: string;
  poster: BackendAuthorDTO;
  tags: string[];
  distance?: string;
}

// Convert backend DTO to frontend type
function convertBackendServiceToFrontend(backendService: BackendServiceDTO): Service {
  // Validate that we have the required data
  if (!backendService || !backendService.poster) {
    throw new Error('Invalid service data received from backend');
  }

  const user: User = {
    id: backendService.poster.id.toString(),
    name: backendService.poster.name,
    avatar: backendService.poster.avatar || '/default-avatar.png',
    hoursGiven: 0, // This data is not in the basic service DTO
    hoursReceived: 0,
    timebankBalance: 0,
    badge: mapBadgeType(backendService.poster.badge),
  };

  return {
    id: backendService.id.toString(),
    type: backendService.type,
    title: backendService.title,
    description: backendService.description,
    location: backendService.location,
    distance: backendService.distance,
    tags: backendService.tags || [],
    timebank: `${backendService.timebank} hour${backendService.timebank !== 1 ? 's' : ''}`,
    poster: user,
    createdAt: backendService.createdAt,
    status: mapStatus(backendService.status),
  };
}

// Map backend badge names to frontend badge types
function mapBadgeType(backendBadge: string): 'top-contributor' | 'active' | 'newcomer' | 'balanced' {
  const badge = backendBadge.toLowerCase();
  if (badge.includes('top') || badge.includes('contributor')) return 'top-contributor';
  if (badge.includes('active')) return 'active';
  if (badge.includes('balanced')) return 'balanced';
  return 'newcomer';
}

// Map backend status to frontend status
function mapStatus(backendStatus: string): 'active' | 'completed' | 'cancelled' {
  const status = backendStatus.toLowerCase();
  if (status === 'completed') return 'completed';
  if (status === 'cancelled') return 'cancelled';
  return 'active';
}

/**
 * Fetch all services (offers and requests) from the backend
 */
export async function fetchAllServices(): Promise<Service[]> {
  try {
    const backendServices = await api.get<BackendServiceDTO[]>('/marketplace/services');
    return backendServices.map(convertBackendServiceToFrontend);
  } catch (error) {
    console.error('Failed to fetch services:', error);
    throw error;
  }
}

/**
 * Fetch active services only
 */
export async function fetchActiveServices(): Promise<Service[]> {
  try {
    const backendServices = await api.get<BackendServiceDTO[]>('/marketplace/services/active');
    return backendServices.map(convertBackendServiceToFrontend);
  } catch (error) {
    console.error('Failed to fetch active services:', error);
    throw error;
  }
}

/**
 * Fetch a single service by ID
 */
export async function fetchServiceById(id: string): Promise<Service | null> {
  try {
    // Use the unified services endpoint
    const backendService = await api.get<BackendServiceDTO>(`/marketplace/services/${id}`);
    return convertBackendServiceToFrontend(backendService);
  } catch (error) {
    console.error(`Failed to fetch service with id ${id}:`, error);
    return null;
  }
}

/**
 * Fetch nearby services for a user
 */
export async function fetchNearbyServices(limit: number = 6): Promise<Service[]> {
  try {
    const backendServices = await api.get<BackendServiceDTO[]>(
      `/marketplace/services/nearby?limit=${limit}`
    );
    return backendServices.map(convertBackendServiceToFrontend);
  } catch (error) {
    console.error('Failed to fetch nearby services:', error);
    throw error;
  }
}

/**
 * Fetch recommended services for a user
 */
export async function fetchRecommendedServices(limit: number = 3): Promise<Service[]> {
  try {
    const backendServices = await api.get<BackendServiceDTO[]>(
      `/marketplace/services/recommended?limit=${limit}`
    );
    return backendServices.map(convertBackendServiceToFrontend);
  } catch (error) {
    console.error('Failed to fetch recommended services:', error);
    throw error;
  }
}

/**
 * Fetch all unique tags from services
 */
export async function fetchAllTags(): Promise<string[]> {
  try {
    const services = await fetchAllServices();
    const tagsSet = new Set<string>();
    services.forEach(service => {
      service.tags.forEach(tag => tagsSet.add(tag));
    });
    return Array.from(tagsSet).sort();
  } catch (error) {
    console.error('Failed to fetch tags:', error);
    return [];
  }
}

