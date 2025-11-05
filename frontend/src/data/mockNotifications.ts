import { Notification } from "../types";

export const mockNotifications: Notification[] = [
  {
    id: "notif-1",
    type: "request",
    title: "New service request",
    message: "Melisa Demir wants to join your photography walk",
    timestamp: "2025-11-05T10:30:00Z",
    read: false,
    relatedUserId: "user-3",
    relatedServiceId: "3",
  },
  {
    id: "notif-2",
    type: "message",
    title: "New message",
    message: "Ahmet Can sent you a message about English practice",
    timestamp: "2025-11-05T09:15:00Z",
    read: false,
    relatedUserId: "user-4",
  },
  {
    id: "notif-3",
    type: "review",
    title: "New review received",
    message: "Selin Karaca left you a 5-star review!",
    timestamp: "2025-11-04T16:45:00Z",
    read: false,
    relatedUserId: "user-1",
  },
  {
    id: "notif-4",
    type: "reminder",
    title: "Upcoming session",
    message: "Your yoga session with Ece Şahin is tomorrow at 6:00 PM",
    timestamp: "2025-11-04T14:00:00Z",
    read: true,
    relatedServiceId: "7",
    relatedUserId: "user-7",
  },
  {
    id: "notif-5",
    type: "achievement",
    title: "Milestone reached!",
    message: "You've given 38 hours to the community. Keep up the great work!",
    timestamp: "2025-11-03T12:00:00Z",
    read: true,
  },
  {
    id: "notif-6",
    type: "offer",
    title: "New service nearby",
    message: "Mert Acar is hosting a book-swap meetup 0.8 km away",
    timestamp: "2025-11-03T11:20:00Z",
    read: true,
    relatedServiceId: "9",
    relatedUserId: "user-9",
  },
  {
    id: "notif-7",
    type: "request",
    title: "Request accepted",
    message: "Zeynep Arslan accepted your request for the meal exchange",
    timestamp: "2025-11-02T18:30:00Z",
    read: true,
    relatedUserId: "user-5",
    relatedServiceId: "5",
  },
];

// Get unread count
export const getUnreadNotificationsCount = (): number => {
  return mockNotifications.filter((n) => !n.read).length;
};

// Mark notification as read
export const markNotificationAsRead = (id: string): void => {
  const notification = mockNotifications.find((n) => n.id === id);
  if (notification) {
    notification.read = true;
  }
};

// Mark all notifications as read
export const markAllNotificationsAsRead = (): void => {
  mockNotifications.forEach((n) => (n.read = true));
};

