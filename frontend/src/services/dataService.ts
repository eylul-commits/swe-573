/**
 * Data Service Layer
 * 
 * This service provides a centralized interface for all data operations.
 * Currently uses mock data, but can be easily replaced with API calls.
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

// =============================================================================
// SERVICE OPERATIONS
// =============================================================================

export const getAllServices = (): Service[] => {
  return mockServices;
};

export const getServiceById = (id: string): Service | undefined => {
  return getServiceByIdMock(id);
};

export const filterServices = (filters: ServiceFilters): Service[] => {
  return filterServicesMock(mockServices, filters);
};

export const getAllTags = (): string[] => {
  return getAllTagsMock();
};

export const getUserBadge = (
  hoursGiven: number,
  hoursReceived: number,
  balance: number
): string => {
  return getUserBadgeMock(hoursGiven, hoursReceived, balance);
};

export const createService = (service: Omit<Service, "id">): Service => {
  const newService: Service = {
    ...service,
    id: `service-${Date.now()}`,
  };
  mockServices.push(newService);
  return newService;
};

export const updateService = (
  id: string,
  updates: Partial<Service>
): Service | undefined => {
  const index = mockServices.findIndex((s) => s.id === id);
  if (index === -1) return undefined;
  mockServices[index] = { ...mockServices[index], ...updates };
  return mockServices[index];
};

export const deleteService = (id: string): boolean => {
  const index = mockServices.findIndex((s) => s.id === id);
  if (index === -1) return false;
  mockServices.splice(index, 1);
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

