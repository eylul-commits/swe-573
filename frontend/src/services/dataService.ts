/**
 * Data Service Layer
 * 
 * This service provides a centralized interface for all data operations.
 * Uses real backend API calls for all data.
 */

import {
  Service,
  User,
  Notification,
  Review,
  Message,
  Conversation,
  CommunityStats,
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

// Local filtering logic (moved from mock data)
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

export const createService = (_service: Omit<Service, "id">): Service => {
  // TODO: Implement backend API call
  throw new Error("createService API not implemented yet - use marketplaceService.createOffer/createRequest instead");
};

export const updateService = (
  _id: string,
  _updates: Partial<Service>
): Service | undefined => {
  // TODO: Implement backend API call
  throw new Error("updateService API not implemented yet");
};

export const deleteService = (_id: string): boolean => {
  // TODO: Implement backend API call
  throw new Error("deleteService API not implemented yet");
};

// =============================================================================
// USER OPERATIONS
// =============================================================================

export const getAllUsers = (): User[] => {
  // TODO: Implement backend API call
  console.warn("getAllUsers API not implemented yet");
  return [];
};

export const getUserById = (_id: string): User | undefined => {
  // TODO: Implement backend API call
  console.warn("getUserById API not implemented yet");
  return undefined;
};

export const getCurrentUser = (): User => {
  // TODO: Implement backend API call
  throw new Error("getCurrentUser API not implemented yet");
};

export const updateCurrentUser = (_updates: Partial<User>): User => {
  // TODO: Implement backend API call
  throw new Error("updateCurrentUser API not implemented yet");
};

// =============================================================================
// NOTIFICATION OPERATIONS
// =============================================================================

export const getAllNotifications = (): Notification[] => {
  // TODO: Implement backend API call
  console.warn("getAllNotifications API not implemented yet");
  return [];
};

export const getUnreadNotificationsCount = (): number => {
  // TODO: Implement backend API call
  console.warn("getUnreadNotificationsCount API not implemented yet");
  return 0;
};

export const markNotificationAsRead = (_id: string): void => {
  // TODO: Implement backend API call
  console.warn("markNotificationAsRead API not implemented yet");
};

export const markAllNotificationsAsRead = (): void => {
  // TODO: Implement backend API call
  console.warn("markAllNotificationsAsRead API not implemented yet");
};

// =============================================================================
// REVIEW OPERATIONS
// =============================================================================

export const getAllReviews = (): Review[] => {
  // TODO: Implement backend API call
  console.warn("getAllReviews API not implemented yet");
  return [];
};

export const getReviewsByServiceId = (_serviceId: string): Review[] => {
  // TODO: Implement backend API call
  console.warn("getReviewsByServiceId API not implemented yet");
  return [];
};

export const getAverageRating = (_serviceId: string): number => {
  // TODO: Implement backend API call
  console.warn("getAverageRating API not implemented yet");
  return 0;
};

export const getReviewCount = (_serviceId: string): number => {
  // TODO: Implement backend API call
  console.warn("getReviewCount API not implemented yet");
  return 0;
};

export const createReview = (_review: Omit<Review, "id">): Review => {
  // TODO: Implement backend API call
  throw new Error("createReview API not implemented yet");
};

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

// =============================================================================
// MESSAGE OPERATIONS
// =============================================================================

export const getAllConversations = (): Conversation[] => {
  // TODO: Implement backend API call
  console.warn("getAllConversations API not implemented yet");
  return [];
};

export const getConversationById = (_id: string): Conversation | undefined => {
  // TODO: Implement backend API call
  console.warn("getConversationById API not implemented yet");
  return undefined;
};

export const getMessagesByConversationId = (_conversationId: string): Message[] => {
  // TODO: Implement backend API call
  console.warn("getMessagesByConversationId API not implemented yet");
  return [];
};

export const getUnreadMessageCount = (): number => {
  // TODO: Implement backend API call
  console.warn("getUnreadMessageCount API not implemented yet");
  return 0;
};

export const sendMessage = (_message: Omit<Message, "id">): Message => {
  // TODO: Implement backend API call
  throw new Error("sendMessage API not implemented yet");
};

// =============================================================================
// COMMUNITY STATS OPERATIONS
// =============================================================================

export const getCommunityStats = (): CommunityStats => {
  // TODO: Implement backend API call
  console.warn("getCommunityStats API not implemented yet");
  return {
    activeMembers: 0,
    hoursExchanged: 0,
    activeServices: 0,
    completedThisMonth: 0,
  };
};
