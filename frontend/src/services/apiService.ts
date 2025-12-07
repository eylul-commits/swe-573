/**
 * API Service Layer
 * 
 * This service handles all HTTP requests to the backend API.
 */

import { api } from '../config/api';
import type {
  Service,
  User,
  AuthorSummary,
  ServiceRatingsResponse,
  ServiceQuestion,
} from '../types';

// Backend DTOs matching the Java backend structure
interface BackendAuthorDTO {
  id: number;
  name: string;
  avatar: string | null;
  badge: string | null;
  bio?: string | null;
  province?: string | null;
  district?: string | null;
  balanceHours?: number | null;
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
  imageUrls?: string[];
}

interface BackendServiceRatingDTO {
  id: number;
  comment: string | null;
  createdAt: string;
  rater: BackendAuthorDTO;
  punctuality: number | null;
  friendliness: number | null;
  communicative: number | null;
  preparedness: number | null;
}

interface BackendServiceRatingSummaryDTO {
  punctuality: number;
  friendliness: number;
  communicative: number;
  preparedness: number;
  totalReviews: number;
}

interface BackendServiceRatingsResponseDTO {
  ratings: BackendServiceRatingDTO[];
  summary: BackendServiceRatingSummaryDTO;
}

interface BackendServiceAnswerDTO {
  id: number;
  content: string;
  createdAt: string;
  responder: BackendAuthorDTO;
}

interface BackendServiceQuestionDTO {
  id: number;
  content: string;
  createdAt: string;
  author: BackendAuthorDTO;
  answer?: BackendServiceAnswerDTO | null;
}

function createAvatarUrl(name: string | null, avatar?: string | null): string {
  if (avatar && avatar.trim().length > 0) {
    return avatar;
  }

  const baseName = name && name.trim().length > 0 ? name : 'Community Member';
  const encoded = encodeURIComponent(baseName);
  return `https://ui-avatars.com/api/?name=${encoded}&background=E2E8F0&color=1F2937`;
}

function convertBackendAuthorToUser(backendAuthor: BackendAuthorDTO): User {
  return {
    id: backendAuthor.id.toString(),
    name: backendAuthor.name,
    avatar: createAvatarUrl(backendAuthor.name, backendAuthor.avatar),
    hoursGiven: 0,
    hoursReceived: 0,
    timebankBalance: backendAuthor.balanceHours ?? 0,
    badge: mapBadgeType(backendAuthor.badge),
    bio: backendAuthor.bio ?? undefined,
    province: backendAuthor.province ?? undefined,
    district: backendAuthor.district ?? undefined,
    location: backendAuthor.district
      ? `${backendAuthor.district}${backendAuthor.province ? `, ${backendAuthor.province}` : ''}`
      : backendAuthor.province ?? undefined,
  };
}

function convertBackendAuthorToSummary(backendAuthor: BackendAuthorDTO): AuthorSummary {
  return {
    id: backendAuthor.id.toString(),
    name: backendAuthor.name,
    avatar: createAvatarUrl(backendAuthor.name, backendAuthor.avatar),
    badge: mapBadgeType(backendAuthor.badge),
    bio: backendAuthor.bio ?? undefined,
    province: backendAuthor.province ?? undefined,
    district: backendAuthor.district ?? undefined,
    timebankBalance: backendAuthor.balanceHours ?? undefined,
  };
}

// Convert backend DTO to frontend type
function convertBackendServiceToFrontend(backendService: BackendServiceDTO): Service {
  // Validate that we have the required data
  if (!backendService || !backendService.poster) {
    throw new Error('Invalid service data received from backend');
  }

  const user = convertBackendAuthorToUser(backendService.poster);

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
    updatedAt: backendService.updatedAt,
    status: mapStatus(backendService.status),
    startDate: backendService.startDate ?? undefined,
    endDate: backendService.endDate ?? undefined,
    province: backendService.province ?? undefined,
    district: backendService.district ?? undefined,
    geohash: backendService.geohash ?? undefined,
    imageUrls: backendService.imageUrls || [],
  };
}

// Map backend badge names to frontend badge types
function mapBadgeType(backendBadge?: string | null): 'top-contributor' | 'active' | 'newcomer' | 'balanced' {
  if (!backendBadge) {
    return 'newcomer';
  }
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

export async function fetchServiceRatings(id: string): Promise<ServiceRatingsResponse> {
  try {
    const response = await api.get<BackendServiceRatingsResponseDTO>(
      `/marketplace/services/${id}/ratings`
    );

    return {
      ratings: response.ratings.map((rating) => ({
        id: rating.id.toString(),
        comment: rating.comment ?? undefined,
        createdAt: rating.createdAt,
        rater: convertBackendAuthorToSummary(rating.rater),
        punctuality: rating.punctuality ?? 0,
        friendliness: rating.friendliness ?? 0,
        communicative: rating.communicative ?? 0,
        preparedness: rating.preparedness ?? 0,
      })),
      summary: {
        punctuality: response.summary.punctuality,
        friendliness: response.summary.friendliness,
        communicative: response.summary.communicative,
        preparedness: response.summary.preparedness,
        totalReviews: response.summary.totalReviews,
      },
    };
  } catch (error) {
    console.error(`Failed to fetch ratings for service ${id}:`, error);
    throw error;
  }
}

export async function fetchServiceQuestions(id: string): Promise<ServiceQuestion[]> {
  try {
    const response = await api.get<BackendServiceQuestionDTO[]>(
      `/marketplace/services/${id}/questions`
    );

    return response.map((question) => ({
      id: question.id.toString(),
      content: question.content,
      createdAt: question.createdAt,
      author: convertBackendAuthorToSummary(question.author),
      answer: question.answer
        ? {
            id: question.answer.id.toString(),
            content: question.answer.content,
            createdAt: question.answer.createdAt,
            responder: convertBackendAuthorToSummary(question.answer.responder),
          }
        : undefined,
    }));
  } catch (error) {
    console.error(`Failed to fetch questions for service ${id}:`, error);
    throw error;
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

