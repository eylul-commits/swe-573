import { mockServices, getUserBadge } from "./mockServices";
import { mockUsers } from "./mockUsers";
import type { Service, User } from "../types";

export interface ServiceRequest {
  id: string;
  serviceId: string;
  service: Service;
  requester: User & {
    badge: { emoji: string; label: string };
  };
  proposedDate: string;
  proposedTime: string;
  timestamp: string;
  status: "pending" | "accepted" | "declined";
  message?: string;
}

const getBadgeDisplay = (
  hoursGiven: number,
  hoursReceived: number,
  balance: number
): { emoji: string; label: string } => {
  if (hoursGiven >= 40) return { emoji: "🏆", label: "Top Contributor" };
  if (hoursGiven >= 20) return { emoji: "⭐", label: "Active Member" };
  if (Math.abs(balance) <= 5 && hoursGiven >= 10)
    return { emoji: "⚖️", label: "Balanced Exchanger" };
  return { emoji: "🌱", label: "Newcomer" };
};

// Mock service requests for the current user's services
export const mockRequests: ServiceRequest[] = [
  {
    id: "req-1",
    serviceId: "8",
    service: mockServices.find((s) => s.id === "8")!,
    requester: {
      ...mockUsers[2], // Melisa Demir
      badge: getBadgeDisplay(
        mockUsers[2].hoursGiven,
        mockUsers[2].hoursReceived,
        mockUsers[2].timebankBalance
      ),
    },
    proposedDate: "Sunday, Nov 10",
    proposedTime: "10:00 AM - 12:00 PM",
    timestamp: "5 minutes ago",
    status: "pending",
    message: "I'd love to learn your family recipes! I'm free this weekend.",
  },
  {
    id: "req-2",
    serviceId: "1",
    service: mockServices.find((s) => s.id === "1")!,
    requester: {
      ...mockUsers[3], // Ahmet Can
      badge: getBadgeDisplay(
        mockUsers[3].hoursGiven,
        mockUsers[3].hoursReceived,
        mockUsers[3].timebankBalance
      ),
    },
    proposedDate: "Saturday, Nov 9",
    proposedTime: "7:00 AM - 8:00 AM",
    timestamp: "2 hours ago",
    status: "pending",
    message: "Looking forward to some peaceful morning meditation!",
  },
  {
    id: "req-3",
    serviceId: "7",
    service: mockServices.find((s) => s.id === "7")!,
    requester: {
      ...mockUsers[5], // Ali Rıza
      badge: getBadgeDisplay(
        mockUsers[5].hoursGiven,
        mockUsers[5].hoursReceived,
        mockUsers[5].timebankBalance
      ),
    },
    proposedDate: "Tuesday, Nov 12",
    proposedTime: "6:00 PM - 7:00 PM",
    timestamp: "1 day ago",
    status: "accepted",
    message: "Can't wait for the yoga session!",
  },
  {
    id: "req-4",
    serviceId: "3",
    service: mockServices.find((s) => s.id === "3")!,
    requester: {
      ...mockUsers[1], // Cem Öztürk
      badge: getBadgeDisplay(
        mockUsers[1].hoursGiven,
        mockUsers[1].hoursReceived,
        mockUsers[1].timebankBalance
      ),
    },
    proposedDate: "Sunday, Nov 10",
    proposedTime: "10:00 AM - 12:00 PM",
    timestamp: "2 days ago",
    status: "accepted",
    message: "Excited to learn photography with you!",
  },
  {
    id: "req-5",
    serviceId: "9",
    service: mockServices.find((s) => s.id === "9")!,
    requester: {
      ...mockUsers[6], // Ece Şahin
      badge: getBadgeDisplay(
        mockUsers[6].hoursGiven,
        mockUsers[6].hoursReceived,
        mockUsers[6].timebankBalance
      ),
    },
    proposedDate: "Saturday, Nov 2",
    proposedTime: "3:00 PM - 4:00 PM",
    timestamp: "3 days ago",
    status: "declined",
    message: "Would love to join the book swap!",
  },
];

// Helper to get requests by status
export const getRequestsByStatus = (
  status: "pending" | "accepted" | "declined"
): ServiceRequest[] => {
  return mockRequests.filter((req) => req.status === status);
};

