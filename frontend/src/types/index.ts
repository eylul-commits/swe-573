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
  status?: "active" | "completed" | "cancelled";
  participants?: string[]; // User IDs
  maxParticipants?: number;
  schedule?: {
    date?: string;
    time?: string;
    recurring?: boolean;
  };
}

export interface Review {
  id: string;
  serviceId: string;
  reviewerId: string;
  reviewerName: string;
  reviewerAvatar: string;
  rating: number;
  comment: string;
  createdAt: string;
}

export interface Notification {
  id: string;
  type: "request" | "offer" | "message" | "review" | "reminder" | "achievement";
  title: string;
  message: string;
  timestamp: string;
  read: boolean;
  actionUrl?: string;
  relatedUserId?: string;
  relatedServiceId?: string;
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

export interface UserProfile extends User {
  email?: string;
  phone?: string;
  preferences?: {
    emailNotifications: boolean;
    pushNotifications: boolean;
    showLocation: boolean;
  };
  servicesOffered: Service[];
  servicesRequested: Service[];
  reviews: Review[];
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

