/**
 * Data Service Layer
 * 
 * This service provides a centralized interface for all data operations.
 * Now uses real backend API calls for services data.
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
  mockServices,
  getServiceById as getServiceByIdMock,
  filterServices as filterServicesMock,
  getAllTags as getAllTagsMock,
  getUserBadge as getUserBadgeMock,
  mockUsers,
  getUserById as getUserByIdMock,
  currentUser as currentUserMock,
  mockNotifications,
  getUnreadNotificationsCount as getUnreadNotificationsCountMock,
  markNotificationAsRead as markNotificationAsReadMock,
  markAllNotificationsAsRead as markAllNotificationsAsReadMock,
  mockReviews,
  getReviewsByServiceId as getReviewsByServiceIdMock,
  getAverageRating as getAverageRatingMock,
  getReviewCount as getReviewCountMock,
  mockMessages,
  mockConversations,
  getMessagesByConversationId as getMessagesByConversationIdMock,
  getUnreadMessageCount as getUnreadMessageCountMock,
  getConversationById as getConversationByIdMock,
  getCommunityStats as getCommunityStatsMock,
} from "../data";
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
  
  try {
    const services = await fetchActiveServices();
    servicesCache = services;
    cacheTimestamp = Date.now();
    return services;
  } catch (error) {
    console.error('Failed to fetch services, falling back to mock data:', error);
    return mockServices;
  }
};

export const getServiceById = async (id: string): Promise<Service | undefined> => {
  try {
    const service = await fetchServiceByIdAPI(id);
    return service || undefined;
  } catch (error) {
    console.error(`Failed to fetch service ${id}, falling back to mock data:`, error);
    return getServiceByIdMock(id);
  }
};

export const filterServices = async (filters: ServiceFilters): Promise<Service[]> => {
  try {
    const allServices = await getAllServices();
    return filterServicesMock(allServices, filters);
  } catch (error) {
    console.error('Failed to filter services, using mock data:', error);
    return filterServicesMock(mockServices, filters);
  }
};

export const getAllTags = async (): Promise<string[]> => {
  try {
    return await fetchAllTags();
  } catch (error) {
    console.error('Failed to fetch tags, falling back to mock data:', error);
    return getAllTagsMock();
  }
};

export const getUserBadge = (
  hoursGiven: number,
  hoursReceived: number,
  balance: number
): string => {
  return getUserBadgeMock(hoursGiven, hoursReceived, balance);
};

export const createService = (service: Omit<Service, "id">): Service => {
  // TODO: Implement backend API call
  const newService: Service = {
    ...service,
    id: `service-${Date.now()}`,
  };
  mockServices.push(newService);
  // Invalidate cache
  servicesCache = null;
  cacheTimestamp = null;
  return newService;
};

export const updateService = (
  id: string,
  updates: Partial<Service>
): Service | undefined => {
  // TODO: Implement backend API call
  const index = mockServices.findIndex((s) => s.id === id);
  if (index === -1) return undefined;
  mockServices[index] = { ...mockServices[index], ...updates };
  // Invalidate cache
  servicesCache = null;
  cacheTimestamp = null;
  return mockServices[index];
};

export const deleteService = (id: string): boolean => {
  // TODO: Implement backend API call
  const index = mockServices.findIndex((s) => s.id === id);
  if (index === -1) return false;
  mockServices.splice(index, 1);
  // Invalidate cache
  servicesCache = null;
  cacheTimestamp = null;
  return true;
};

// =============================================================================
// USER OPERATIONS
// =============================================================================

export const getAllUsers = (): User[] => {
  return mockUsers;
};

export const getUserById = (id: string): User | undefined => {
  return getUserByIdMock(id);
};

export const getCurrentUser = (): User => {
  return currentUserMock;
};

export const updateCurrentUser = (updates: Partial<User>): User => {
  Object.assign(currentUserMock, updates);
  return currentUserMock;
};

// =============================================================================
// NOTIFICATION OPERATIONS
// =============================================================================

export const getAllNotifications = (): Notification[] => {
  return mockNotifications;
};

export const getUnreadNotificationsCount = (): number => {
  return getUnreadNotificationsCountMock();
};

export const markNotificationAsRead = (id: string): void => {
  markNotificationAsReadMock(id);
};

export const markAllNotificationsAsRead = (): void => {
  markAllNotificationsAsReadMock();
};

// =============================================================================
// REVIEW OPERATIONS
// =============================================================================

export const getAllReviews = (): Review[] => {
  return mockReviews;
};

export const getReviewsByServiceId = (serviceId: string): Review[] => {
  return getReviewsByServiceIdMock(serviceId);
};

export const getAverageRating = (serviceId: string): number => {
  return getAverageRatingMock(serviceId);
};

export const getReviewCount = (serviceId: string): number => {
  return getReviewCountMock(serviceId);
};

export const createReview = (review: Omit<Review, "id">): Review => {
  const newReview: Review = {
    ...review,
    id: `review-${Date.now()}`,
  };
  mockReviews.push(newReview);
  return newReview;
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
  return mockConversations;
};

export const getConversationById = (id: string): Conversation | undefined => {
  return getConversationByIdMock(id);
};

export const getMessagesByConversationId = (conversationId: string): Message[] => {
  return getMessagesByConversationIdMock(conversationId);
};

export const getUnreadMessageCount = (): number => {
  return getUnreadMessageCountMock();
};

export const sendMessage = (message: Omit<Message, "id">): Message => {
  const newMessage: Message = {
    ...message,
    id: `msg-${Date.now()}`,
  };
  mockMessages.push(newMessage);
  return newMessage;
};

// =============================================================================
// COMMUNITY STATS OPERATIONS
// =============================================================================

export const getCommunityStats = (): CommunityStats => {
  return getCommunityStatsMock();
};

