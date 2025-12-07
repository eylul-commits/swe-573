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
  preferences?: {
    showLocation: boolean;
  };
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
  agreedHours: number;
  seekerConfirmed: boolean;
  providerConfirmed: boolean;
  createdAt: string;
  completedAt: string | null;
  canRate: boolean;
}

export interface CreateHandshakeRequest {
  offerId?: number; // For offers
  requestId?: number; // For requests (either offerId or requestId must be provided)
  providerId: number;
  agreedHours: number;
}

export interface ConfirmHandshakeRequest {
  completedAt: string;
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

