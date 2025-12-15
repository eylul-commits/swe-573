import { api } from '../config/api';
import type {
  Service,
  User,
  CreateOfferPayload,
  CreateRequestPayload,
  BackendAuthorDTO,
  BackendServiceDTO,
  OfferDTO,
  RequestDTO
} from '../types';

function convertAuthorToUser(author: BackendAuthorDTO): User {
  return {
    id: author.id.toString(),
    name: author.name,
    avatar: author.avatar || '',
    timebankBalance: 0, // Not available in this DTO
    hoursGiven: 0,
    hoursReceived: 0,
    currentBadge: author.badge ?? undefined,
  };
}

function convertServiceDTOToService(dto: BackendServiceDTO): Service {
  return {
    id: dto.id.toString(),
    type: dto.type,
    title: dto.title,
    description: dto.description,
    location: dto.location,
    distance: dto.distance,
    tags: dto.tags || [],
    timebank: dto.timebank ? `${dto.timebank}h` : '0h',
    poster: convertAuthorToUser(dto.poster),
    createdAt: dto.createdAt,
    status: dto.status.toLowerCase() as any || 'active',
    province: dto.province ?? undefined,
    district: dto.district ?? undefined,
    geohash: dto.geohash ?? undefined,
    imageUrls: dto.imageUrls || [],
  };
}

export async function getAllServices(): Promise<Service[]> {
  try {
    const services = await api.get<BackendServiceDTO[]>('/marketplace/services');
    return services.map(convertServiceDTOToService);
  } catch (error) {
    console.error('Failed to fetch services:', error);
    return [];
  }
}

export async function getActiveServices(): Promise<Service[]> {
  try {
    const services = await api.get<BackendServiceDTO[]>('/marketplace/services/active');
    return services.map(convertServiceDTOToService);
  } catch (error) {
    console.error('Failed to fetch active services:', error);
    return [];
  }
}

export async function getAllOffers(): Promise<Service[]> {
  try {
    const offers = await api.get<OfferDTO[]>('/marketplace/offers');
    return offers.map(offer => ({
      id: offer.id.toString(),
      type: 'OFFER' as const,
      title: offer.title,
      description: offer.description,
      location: `${offer.district}, ${offer.province}`,
      tags: offer.tags || [],
      timebank: offer.durationHours ? `${offer.durationHours}h` : '0h',
      poster: convertAuthorToUser(offer.provider),
      createdAt: offer.createdAt,
      status: offer.status.toLowerCase() as any || 'active',
      province: offer.province,
      district: offer.district,
      geohash: offer.geohash,
    }));
  } catch (error) {
    console.error('Failed to fetch offers:', error);
    return [];
  }
}

export async function getActiveOffers(): Promise<Service[]> {
  try {
    const offers = await api.get<OfferDTO[]>('/marketplace/offers/active');
    return offers.map(offer => ({
      id: offer.id.toString(),
      type: 'OFFER' as const,
      title: offer.title,
      description: offer.description,
      location: `${offer.district}, ${offer.province}`,
      tags: offer.tags || [],
      timebank: offer.durationHours ? `${offer.durationHours}h` : '0h',
      poster: convertAuthorToUser(offer.provider),
      createdAt: offer.createdAt,
      status: offer.status.toLowerCase() as any || 'active',
      province: offer.province,
      district: offer.district,
      geohash: offer.geohash,
    }));
  } catch (error) {
    console.error('Failed to fetch active offers:', error);
    return [];
  }
}

export async function getAllRequests(): Promise<Service[]> {
  try {
    const requests = await api.get<RequestDTO[]>('/marketplace/requests');
    return requests.map(request => ({
      id: request.id.toString(),
      type: 'REQUEST' as const,
      title: request.title,
      description: request.description,
      location: `${request.district}, ${request.province}`,
      tags: request.tags || [],
      timebank: request.durationHours ? `${request.durationHours}h` : '0h',
      poster: convertAuthorToUser(request.seeker),
      createdAt: request.createdAt,
      status: request.status.toLowerCase() as any || 'active',
      province: request.province,
      district: request.district,
      geohash: request.geohash,
    }));
  } catch (error) {
    console.error('Failed to fetch requests:', error);
    return [];
  }
}

export async function getActiveRequests(): Promise<Service[]> {
  try {
    const requests = await api.get<RequestDTO[]>('/marketplace/requests/active');
    return requests.map(request => ({
      id: request.id.toString(),
      type: 'REQUEST' as const,
      title: request.title,
      description: request.description,
      location: `${request.district}, ${request.province}`,
      tags: request.tags || [],
      timebank: request.durationHours ? `${request.durationHours}h` : '0h',
      poster: convertAuthorToUser(request.seeker),
      createdAt: request.createdAt,
      status: request.status.toLowerCase() as any || 'active',
      province: request.province,
      district: request.district,
      geohash: request.geohash,
    }));
  } catch (error) {
    console.error('Failed to fetch active requests:', error);
    return [];
  }
}

export async function getAllTags(): Promise<string[]> {
  try {
    const services = await getAllServices();
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

export function filterServices(
  services: Service[],
  filters: {
    searchQuery?: string;
    location?: string;
    badge?: string;
    type?: string;
    tags?: string[];
  }
): Service[] {
  return services.filter(service => {
    // Search query filter (searches in title, description, and tags)
    if (filters.searchQuery) {
      const query = filters.searchQuery.toLowerCase();
      const matchesSearch =
        service.title.toLowerCase().includes(query) ||
        service.description.toLowerCase().includes(query) ||
        service.tags.some(tag => tag.toLowerCase().includes(query));
      if (!matchesSearch) return false;
    }

    // Location filter
    if (filters.location) {
      const locationQuery = filters.location.toLowerCase();
      const matchesLocation = service.location.toLowerCase().includes(locationQuery);
      if (!matchesLocation) return false;
    }

    // Badge filter
    if (filters.badge && filters.badge !== 'all') {
      const matchesBadge = service.poster.currentBadge?.name === filters.badge;
      if (!matchesBadge) return false;
    }

    // Type filter
    if (filters.type && filters.type !== 'all') {
      const matchesType = service.type === filters.type;
      if (!matchesType) return false;
    }

    return true;
  });
}

export async function getCommunityStats(): Promise<{
  activeMembers: number;
  hoursExchanged: number;
  activeServices: number;
  completedThisMonth: number;
}> {
  try {
    const services = await getAllServices();
    
    // Count unique posters
    const uniquePosters = new Set(services.map(s => s.poster.id));
    
    return {
      activeMembers: uniquePosters.size,
      hoursExchanged: services.reduce((sum, s) => {
        const hours = parseInt(s.timebank);
        return sum + (isNaN(hours) ? 0 : hours);
      }, 0),
      activeServices: services.filter(s => s.status === 'active').length,
      completedThisMonth: services.filter(s => s.status === 'completed').length,
    };
  } catch (error) {
    console.error('Failed to fetch community stats:', error);
    return {
      activeMembers: 0,
      hoursExchanged: 0,
      activeServices: 0,
      completedThisMonth: 0,
    };
  }
}

export async function getNearbyServices(limit: number = 6): Promise<Service[]> {
  try {
    const services = await api.get<BackendServiceDTO[]>(`/marketplace/services/nearby?limit=${limit}`);
    return services.map(convertServiceDTOToService);
  } catch (error) {
    console.error('Failed to fetch nearby services:', error);
    return [];
  }
}

export async function getRecommendedServices(limit: number = 3): Promise<Service[]> {
  try {
    const services = await api.get<BackendServiceDTO[]>(`/marketplace/services/recommended?limit=${limit}`);
    return services.map(convertServiceDTOToService);
  } catch (error) {
    console.error('Failed to fetch recommended services:', error);
    return [];
  }
}

export async function createOffer(payload: CreateOfferPayload): Promise<Service | null> {
  try {
    const offer = await api.post<OfferDTO>('/marketplace/offers', payload);
    return {
      id: offer.id.toString(),
      type: 'OFFER' as const,
      title: offer.title,
      description: offer.description,
      location: `${offer.district}, ${offer.province}`,
      tags: offer.tags || [],
      timebank: offer.durationHours ? `${offer.durationHours}h` : '0h',
      poster: convertAuthorToUser(offer.provider),
      createdAt: offer.createdAt,
      status: offer.status.toLowerCase() as any || 'active',
      province: offer.province,
      district: offer.district,
      geohash: offer.geohash,
    };
  } catch (error) {
    console.error('Failed to create offer:', error);
    throw error;
  }
}

export async function createRequest(payload: CreateRequestPayload): Promise<Service | null> {
  try {
    const request = await api.post<RequestDTO>('/marketplace/requests', payload);
    return {
      id: request.id.toString(),
      type: 'REQUEST' as const,
      title: request.title,
      description: request.description,
      location: `${request.district}, ${request.province}`,
      tags: request.tags || [],
      timebank: request.durationHours ? `${request.durationHours}h` : '0h',
      poster: convertAuthorToUser(request.seeker),
      createdAt: request.createdAt,
      status: request.status.toLowerCase() as any || 'active',
      province: request.province,
      district: request.district,
      geohash: request.geohash,
    };
  } catch (error) {
    console.error('Failed to create request:', error);
    throw error;
  }
}

export async function getUserOffers(userId: string): Promise<Service[]> {
  try {
    const offers = await api.get<OfferDTO[]>(`/marketplace/offers/user/${userId}`);
    return offers.map(offer => ({
      id: offer.id.toString(),
      type: 'OFFER' as const,
      title: offer.title,
      description: offer.description,
      location: `${offer.district}, ${offer.province}`,
      tags: offer.tags || [],
      timebank: offer.durationHours ? `${offer.durationHours}h` : '0h',
      poster: convertAuthorToUser(offer.provider),
      createdAt: offer.createdAt,
      status: offer.status.toLowerCase() as any || 'active',
      province: offer.province,
      district: offer.district,
      geohash: offer.geohash,
    }));
  } catch (error) {
    console.error('Failed to fetch user offers:', error);
    return [];
  }
}

export async function getUserRequests(userId: string): Promise<Service[]> {
  try {
    const requests = await api.get<RequestDTO[]>(`/marketplace/requests/user/${userId}`);
    return requests.map(request => ({
      id: request.id.toString(),
      type: 'REQUEST' as const,
      title: request.title,
      description: request.description,
      location: `${request.district}, ${request.province}`,
      tags: request.tags || [],
      timebank: request.durationHours ? `${request.durationHours}h` : '0h',
      poster: convertAuthorToUser(request.seeker),
      createdAt: request.createdAt,
      status: request.status.toLowerCase() as any || 'active',
      province: request.province,
      district: request.district,
      geohash: request.geohash,
    }));
  } catch (error) {
    console.error('Failed to fetch user requests:', error);
    return [];
  }
}

export async function getUserServices(userId: string): Promise<Service[]> {
  try {
    const services = await api.get<BackendServiceDTO[]>(`/marketplace/services/user/${userId}`);
    return services.map(convertServiceDTOToService);
  } catch (error) {
    console.error('Failed to fetch user services:', error);
    return [];
  }
}
