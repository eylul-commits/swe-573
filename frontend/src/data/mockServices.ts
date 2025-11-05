import { Service } from "../types";
import { mockUsers } from "./mockUsers";

export const mockServices: Service[] = [
  {
    id: "1",
    type: "OFFER",
    title: "Guided morning meditation by the Bosphorus",
    description: "Join me for peaceful morning meditation sessions by the waterfront. Perfect for beginners and experienced practitioners alike.",
    location: "Arnavutköy",
    distance: "1.3 km",
    tags: ["mindfulness", "wellness", "community"],
    timebank: "1 hour",
    poster: mockUsers[0],
    createdAt: "2025-10-28",
    status: "active",
    maxParticipants: 10,
    schedule: {
      date: "Every Saturday",
      time: "7:00 AM",
      recurring: true,
    },
  },
  {
    id: "2",
    type: "REQUEST",
    title: "Help with balcony gardening",
    description: "Looking for guidance to start a small herb and vegetable garden on my balcony. I'm a complete beginner!",
    location: "Etiler",
    distance: "1.1 km",
    tags: ["gardening", "plants", "eco-living"],
    timebank: "1.5 hours",
    poster: mockUsers[1],
    createdAt: "2025-10-30",
    status: "active",
    maxParticipants: 1,
  },
  {
    id: "3",
    type: "OFFER",
    title: "Street photography walk",
    description: "Let's explore the neighborhood with our cameras and learn street photography techniques together.",
    location: "Beşiktaş",
    distance: "0.7 km",
    tags: ["photography", "art", "exploration"],
    timebank: "2 hours",
    poster: mockUsers[2],
    createdAt: "2025-11-01",
    status: "active",
    maxParticipants: 5,
    schedule: {
      date: "Every Sunday",
      time: "10:00 AM",
      recurring: true,
    },
  },
  {
    id: "4",
    type: "REQUEST",
    title: "Practicing conversational English",
    description: "I want to improve my spoken English for work. Looking for a conversation partner to practice with regularly.",
    location: "Levent",
    distance: "1.6 km",
    tags: ["language", "learning", "culture"],
    timebank: "1 hour",
    poster: mockUsers[3],
    createdAt: "2025-10-25",
    status: "active",
    maxParticipants: 1,
  },
  {
    id: "5",
    type: "OFFER",
    title: "Home-cooked meal exchange",
    description: "I love cooking traditional Turkish dishes. Let's share meals and recipes from our cultures!",
    location: "Ortaköy",
    distance: "0.9 km",
    tags: ["food", "community", "culture"],
    timebank: "1.5 hours",
    poster: mockUsers[4],
    createdAt: "2025-10-29",
    status: "active",
    maxParticipants: 4,
  },
  {
    id: "6",
    type: "REQUEST",
    title: "Learning basic sign language",
    description: "I want to learn sign language to better communicate with my deaf neighbor. Looking for a patient teacher.",
    location: "Nişantaşı",
    distance: "1.4 km",
    tags: ["language", "accessibility", "communication"],
    timebank: "1 hour",
    poster: mockUsers[5],
    createdAt: "2025-11-02",
    status: "active",
    maxParticipants: 1,
  },
  {
    id: "7",
    type: "OFFER",
    title: "Evening yoga by the sea",
    description: "Relaxing yoga sessions by the waterfront at sunset. All levels welcome, bring your own mat!",
    location: "Bebek",
    distance: "1.2 km",
    tags: ["yoga", "wellness", "community"],
    timebank: "1 hour",
    poster: mockUsers[6],
    createdAt: "2025-10-27",
    status: "active",
    maxParticipants: 15,
    schedule: {
      date: "Tuesdays & Thursdays",
      time: "6:00 PM",
      recurring: true,
    },
  },
  {
    id: "8",
    type: "OFFER",
    title: "Traditional home-cooking lesson with Fadime Teyze",
    description: "Learn authentic Turkish home cooking from a master. I'll teach you family recipes passed down for generations.",
    location: "Beşiktaş",
    distance: "1.0 km",
    tags: ["cooking", "tradition", "community"],
    timebank: "2 hours",
    poster: mockUsers[7],
    createdAt: "2025-10-26",
    status: "active",
    maxParticipants: 6,
    schedule: {
      date: "Every Wednesday",
      time: "2:00 PM",
      recurring: true,
    },
  },
  {
    id: "9",
    type: "OFFER",
    title: "Neighborhood book-swap meetup",
    description: "Monthly book exchange and discussion group. Bring books you've finished and discover new reads from neighbors!",
    location: "Ortaköy",
    distance: "0.8 km",
    tags: ["books", "community", "culture"],
    timebank: "1 hour",
    poster: mockUsers[8],
    createdAt: "2025-11-03",
    status: "active",
    maxParticipants: 20,
    schedule: {
      date: "First Saturday of each month",
      time: "3:00 PM",
      recurring: true,
    },
  },
];

// Helper to get service by ID
export const getServiceById = (id: string): Service | undefined => {
  return mockServices.find((service) => service.id === id);
};

// Helper to filter services
export const filterServices = (
  services: Service[],
  filters: {
    searchQuery?: string;
    tags?: string[];
    type?: "OFFER" | "REQUEST" | "all";
    badge?: string;
    location?: string;
  }
): Service[] => {
  let filtered = [...services];

  if (filters.searchQuery) {
    const query = filters.searchQuery.toLowerCase();
    filtered = filtered.filter(
      (service) =>
        service.title.toLowerCase().includes(query) ||
        service.description.toLowerCase().includes(query) ||
        service.tags.some((tag) => tag.toLowerCase().includes(query))
    );
  }

  if (filters.tags && filters.tags.length > 0) {
    filtered = filtered.filter((service) =>
      filters.tags!.some((tag) => service.tags.includes(tag))
    );
  }

  if (filters.type && filters.type !== "all") {
    filtered = filtered.filter((service) => service.type === filters.type);
  }

  if (filters.badge && filters.badge !== "all") {
    filtered = filtered.filter((service) => {
      const { hoursGiven, hoursReceived, timebankBalance } = service.poster;
      const userBadge = getUserBadge(hoursGiven, hoursReceived, timebankBalance);
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

// Helper to determine user badge
export const getUserBadge = (
  hoursGiven: number,
  hoursReceived: number,
  balance: number
): string => {
  if (hoursGiven >= 40) return "top-contributor";
  if (hoursGiven >= 20) return "active";
  if (Math.abs(balance) <= 5 && hoursGiven >= 10) return "balanced";
  return "newcomer";
};

// Get all unique tags
export const getAllTags = (): string[] => {
  const tags = new Set<string>();
  mockServices.forEach((service) => {
    service.tags.forEach((tag) => tags.add(tag));
  });
  return Array.from(tags).sort();
};

