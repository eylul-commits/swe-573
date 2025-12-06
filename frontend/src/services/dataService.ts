import {
  Service,
  ServiceFilters,
  ServiceRatingsResponse,
  ServiceQuestion,
} from "../types";
import {
  fetchActiveServices,
  fetchServiceById as fetchServiceByIdAPI,
  fetchAllTags,
  fetchServiceRatings,
  fetchServiceQuestions,
} from "./apiService";

// Cache for services to avoid repeated API calls
let servicesCache: Service[] | null = null;
let cacheTimestamp: number | null = null;
const CACHE_DURATION = 60000; // 1 minute

// Helper to check if cache is valid
function isCacheValid(): boolean {
  return servicesCache !== null && 
         cacheTimestamp !== null && 
         Date.now() - cacheTimestamp < CACHE_DURATION;
}

// =============================================================================
// SERVICE OPERATIONS (using backend API)
// =============================================================================

export const getAllServices = async (): Promise<Service[]> => {
  if (isCacheValid() && servicesCache) {
    return servicesCache;
  }
  
  const services = await fetchActiveServices();
  servicesCache = services;
  cacheTimestamp = Date.now();
  return services;
};

export const getServiceById = async (id: string): Promise<Service | undefined> => {
  const service = await fetchServiceByIdAPI(id);
  return service || undefined;
};

export const filterServices = async (filters: ServiceFilters): Promise<Service[]> => {
  const allServices = await getAllServices();
  return filterServicesLocally(allServices, filters);
};

//TO-DO: Semantic tags filtering logic will be added here
const filterServicesLocally = (services: Service[], filters: ServiceFilters): Service[] => {
  let filtered = services;

  if (filters.searchQuery) {
    const query = filters.searchQuery.toLowerCase();
    filtered = filtered.filter(
      (service) =>
        service.title.toLowerCase().includes(query) ||
        service.description.toLowerCase().includes(query) ||
        service.tags.some((tag) => tag.toLowerCase().includes(query))
    );
  }

  if (filters.type && filters.type !== "all") {
    filtered = filtered.filter((service) => service.type === filters.type);
  }

  if (filters.tags && filters.tags.length > 0) {
    filtered = filtered.filter((service) =>
      filters.tags!.some((tag) => service.tags.includes(tag))
    );
  }

  if (filters.badge && filters.badge !== "all") {
    filtered = filtered.filter((service) => {
      const userBadge = getUserBadge(
        service.poster.hoursGiven,
        service.poster.hoursReceived,
        service.poster.timebankBalance
      );
      return userBadge === filters.badge;
    });
  }

  if (filters.location) {
    const locationQuery = filters.location.toLowerCase();
    filtered = filtered.filter((service) =>
      service.location.toLowerCase().includes(locationQuery)
    );
  }

  return filtered;
};

export const getAllTags = async (): Promise<string[]> => {
  return await fetchAllTags();
};

// Helper to determine user badge
export const getUserBadge = (
  hoursGiven: number,
  _hoursReceived: number,
  balance: number
): string => {
  if (hoursGiven >= 40) return "top-contributor";
  if (hoursGiven >= 20) return "active";
  if (Math.abs(balance) <= 5 && hoursGiven >= 10) return "balanced";
  return "newcomer";
};

// =============================================================================
// REVIEW OPERATIONS
// =============================================================================

export const getServiceRatings = async (
  serviceId: string
): Promise<ServiceRatingsResponse> => {
  try {
    return await fetchServiceRatings(serviceId);
  } catch (error) {
    console.error(`Failed to load ratings for service ${serviceId}:`, error);
    return {
      ratings: [],
      summary: {
        punctuality: 0,
        friendliness: 0,
        communicative: 0,
        preparedness: 0,
        totalReviews: 0,
      },
    };
  }
};

export const getServiceQuestions = async (
  serviceId: string
): Promise<ServiceQuestion[]> => {
  try {
    return await fetchServiceQuestions(serviceId);
  } catch (error) {
    console.error(`Failed to load questions for service ${serviceId}:`, error);
    return [];
  }
};