// Core type definitions for The Hive application

export type ServiceType = "OFFER" | "REQUEST";

export type BadgeType = "top-contributor" | "active" | "newcomer" | "balanced";

export interface User {
  id: string;
  name: string;
  avatar: string;
  hoursGiven: number;
  hoursReceived: number;
  timebankBalance: number;
  badge?: BadgeType;
  bio?: string;
  joinedDate?: string;
  location?: string;
  skills?: string[];
  province?: string;
  district?: string;
  geohash?: string; // Geohash for user location
}

export interface Service {
  id: string;
  type: ServiceType;
  title: string;
  description: string;
  location: string;
  distance?: string;
  tags: string[];
  timebank: string;
  poster: User;
  createdAt?: string;
  updatedAt?: string;
  status?: "active" | "completed" | "cancelled";
  participants?: string[]; // User IDs
  maxParticipants?: number;
  startDate?: string;
  endDate?: string;
  province?: string;
  district?: string;
  geohash?: string; // Geohash for location
  imageUrls?: string[]; // Array of image URLs
  schedule?: {
    date?: string;
    time?: string;
    recurring?: boolean;
  };
}

export interface Message {
  id: string;
  conversationId: string;
  senderId: string;
  senderName: string;
  senderAvatar: string;
  content: string;
  timestamp: string;
  read: boolean;
}

export interface Conversation {
  id: string;
  participants: User[];
  lastMessage: Message;
  unreadCount: number;
  relatedServiceId?: string;
}

export interface Request {
  id: string;
  serviceId: string;
  requesterId: string;
  requesterName: string;
  requesterAvatar: string;
  status: "pending" | "accepted" | "declined" | "completed";
  message?: string;
  createdAt: string;
}

export interface CommunityStats {
  activeMembers: number;
  hoursExchanged: number;
  activeServices: number;
  completedThisMonth: number;
}

export interface AuthorSummary {
  id: string;
  name: string;
  avatar: string;
  badge?: BadgeType | string;
  bio?: string;
  province?: string;
  district?: string;
  timebankBalance?: number;
}

export interface ServiceAnswer {
  id: string;
  content: string;
  createdAt: string;
  responder: AuthorSummary;
}

export interface ServiceQuestion {
  id: string;
  content: string;
  createdAt: string;
  author: AuthorSummary;
  answer?: ServiceAnswer;
}

export interface ServiceRating {
  id: string;
  comment?: string;
  createdAt: string;
  rater: AuthorSummary;
  punctuality: number;
  friendliness: number;
  communicative: number;
  preparedness: number;
  serviceId?: number;
  serviceTitle?: string;
  rateeRole?: 'PROVIDER' | 'SEEKER';
}

export interface ServiceRatingSummary {
  punctuality: number;
  friendliness: number;
  communicative: number;
  preparedness: number;
  totalReviews: number;
}

export interface ServiceRatingsResponse {
  ratings: ServiceRating[];
  summary: ServiceRatingSummary;
}

export interface UserProfile extends User {
  email?: string;
  phone?: string;
  servicesOffered: Service[];
  servicesRequested: Service[];
}

// Filter types
export interface ServiceFilters {
  searchQuery?: string;
  tags?: string[];
  location?: string;
  badge?: BadgeType | "all";
  type?: ServiceType | "all";
  distance?: number;
}

// Handshake types
export type HandshakeStatus = "PENDING" | "CONFIRMED" | "COMPLETED" | "CANCELLED";

export interface Handshake {
  id: number;
  offerId?: number; // null if this is for a request
  requestId?: number; // null if this is for an offer
  offerTitle: string; // title of the service (either offer or request)
  seeker: AuthorSummary;
  provider: AuthorSummary;
  status: HandshakeStatus;
  durationHours: number;
  seekerConfirmed: boolean;
  providerConfirmed: boolean;
  createdAt: string;
  agreedDate: string | null;
  canRate: boolean;
}

export interface CreateHandshakeRequest {
  offerId?: number; // For offers
  requestId?: number; // For requests (either offerId or requestId must be provided)
  providerId: number;
}

export interface ConfirmHandshakeRequest {
  agreedDate: string;
}

export interface CreateRatingRequest {
  handshakeId: number;
  rateeId: number;
  punctuality: number;
  friendliness: number;
  communicative: number;
  preparedness: number;
  comment?: string;
}

export interface ServiceRequest {
  id: string;
  serviceId: string;
  service: Service;
  requester: Omit<User, 'badge'> & {
    badge: { emoji: string; label: string };
  };
  proposedDate: string;
  proposedTime: string;
  timestamp: string;
  status: "pending" | "accepted" | "declined";
  message?: string;
}

// =============================================================================
// AUTH SERVICE TYPES
// =============================================================================

export interface AuthUser {
  id: number;
  email: string;
  name: string;
  bio?: string;
  avatarUrl?: string;
  province?: string;
  district?: string;
  geohash?: string;
  role: string;
  accountStatus?: 'ACTIVE' | 'DEACTIVATED' | 'WARNED';
  warningCount?: number;
  balanceHours: number;
  timebankBalance?: number;
  hoursGiven?: number;
  hoursReceived?: number;
  location?: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  name: string;
  email: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  user: AuthUser;
  streamChatToken?: string;
}

export interface UpdateProfileRequest {
  name?: string;
  bio?: string;
  avatarUrl?: string;
  province?: string;
  district?: string;
  geohash?: string;
}

// =============================================================================
// ADMIN SERVICE TYPES
// =============================================================================

export interface AdminStatistics {
  totalUsers: number;
  activeUsers: number;
  deactivatedUsers: number;
  totalOffers: number;
  activeOffers: number;
  totalRequests: number;
  activeRequests: number;
  totalReports: number;
  openReports: number;
  resolvedReports: number;
  totalHandshakes: number;
  totalMessages: number;
}

export interface AdminUser {
  id: number;
  email: string;
  name: string;
  bio?: string;
  avatarUrl?: string;
  province?: string;
  district?: string;
  geohash?: string;
  role: 'USER' | 'ADMIN';
  accountStatus: 'ACTIVE' | 'DEACTIVATED' | 'WARNED';
  warningCount: number;
  balanceHours: number;
  hoursGiven?: number;
  hoursReceived?: number;
}

export interface UserManagementRequest {
  userId: number;
  action: 'WARN' | 'DEACTIVATE' | 'ACTIVATE';
  reason?: string;
}

export interface ResolveReportRequest {
  status: ReportStatus;
  adminNotes?: string;
  userId?: number;
  action?: 'WARN' | 'DEACTIVATE' | 'NO_ACTION';
}

export interface UserAction {
  id: number;
  userId: number;
  adminId: number;
  adminName: string;
  adminEmail: string;
  actionType: 'WARN' | 'DEACTIVATE' | 'ACTIVATE';
  reason?: string;
  reportId?: number;
  createdAt: string;
}

// =============================================================================
// REPORT SERVICE TYPES
// =============================================================================

export type ReportType = 'USER' | 'OFFER' | 'REQUEST' | 'FORUM_POST' | 'FORUM_TOPIC';
export type ReportStatus = 'OPEN' | 'RESOLVED';

export interface Report {
  id: number;
  reporterId: number;
  reporterName: string;
  reporterEmail: string;
  reportedUserId: number;
  reportedUserName: string;
  reportedUserEmail: string;
  reportedUserRole?: 'USER' | 'ADMIN';
  reportType: ReportType;
  reportedOfferId?: number;
  reportedOfferTitle?: string;
  reportedRequestId?: number;
  reportedRequestTitle?: string;
  reportedForumPostId?: number;
  reportedForumTopicId?: number;
  message: string;
  adminNotes?: string;
  status: ReportStatus;
  createdAt: string;
  resolvedAt?: string;
  resolvedById?: number;
  resolvedByName?: string;
}

export interface CreateReportRequest {
  reportType: ReportType;
  reportedUserId: number;
  reportedOfferId?: number;
  reportedRequestId?: number;
  reportedForumPostId?: number;
  reportedForumTopicId?: number;
  message: string;
}

// =============================================================================
// MARKETPLACE SERVICE TYPES
// =============================================================================

export interface CreateOfferPayload {
  title: string;
  description: string;
  durationHours: number;
  startDate: string;
  endDate: string;
  province: string;
  district: string;
  geohash: string;
  tags: string[];
  imageUrls?: string[];
}

export interface CreateRequestPayload {
  title: string;
  description: string;
  durationHours: number;
  startDate: string;
  endDate: string;
  province: string;
  district: string;
  geohash: string;
  tags: string[];
  imageUrls?: string[];
}

// =============================================================================
// FORUM SERVICE TYPES
// =============================================================================

export interface ForumAuthor {
  id: number;
  name: string;
  avatar: string | null;
  badge: string;
}

export interface ForumTopic {
  id: number;
  title: string;
  author: ForumAuthor;
  postCount: number;
  views: number;
  likes: number;
  createdAt: string;
  updatedAt: string;
  lastActivity: string;
  excerpt: string | null;
  isPinned: boolean;
}

export interface ForumPost {
  id: number;
  topicId: number;
  author: ForumAuthor;
  content: string;
  createdAt: string;
}

export interface CreateForumTopicRequest {
  title: string;
  initialPostContent: string;
}

export interface CreateForumPostRequest {
  content: string;
}

// =============================================================================
// BACKEND DTO TYPES (for API Service)
// =============================================================================

export interface BackendAuthorDTO {
  id: number;
  name: string;
  avatar: string | null;
  badge: string | null;
  bio?: string | null;
  province?: string | null;
  district?: string | null;
  balanceHours?: number | null;
}

export interface BackendServiceDTO {
  id: number;
  type: 'OFFER' | 'REQUEST';
  title: string;
  description: string;
  timebank: number;
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

export interface BackendServiceRatingDTO {
  id: number;
  comment: string | null;
  createdAt: string;
  rater: BackendAuthorDTO;
  punctuality: number | null;
  friendliness: number | null;
  communicative: number | null;
  preparedness: number | null;
}

export interface BackendServiceRatingSummaryDTO {
  punctuality: number;
  friendliness: number;
  communicative: number;
  preparedness: number;
  totalReviews: number;
}

export interface BackendServiceRatingsResponseDTO {
  ratings: BackendServiceRatingDTO[];
  summary: BackendServiceRatingSummaryDTO;
}

export interface BackendServiceAnswerDTO {
  id: number;
  content: string;
  createdAt: string;
  responder: BackendAuthorDTO;
}

export interface BackendServiceQuestionDTO {
  id: number;
  content: string;
  createdAt: string;
  author: BackendAuthorDTO;
  answer?: BackendServiceAnswerDTO | null;
}

