import { Message, Conversation } from "../types";
import { mockUsers } from "./mockUsers";

export const mockMessages: Message[] = [
  {
    id: "msg-1",
    conversationId: "conv-1",
    senderId: "user-3",
    senderName: "Melisa Demir",
    senderAvatar: "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=100&h=100&fit=crop",
    content: "Hi! I'm really interested in your photography walk. What camera do you recommend?",
    timestamp: "2025-11-05T09:30:00Z",
    read: false,
  },
  {
    id: "msg-2",
    conversationId: "conv-2",
    senderId: "user-1",
    senderName: "Selin Karaca",
    senderAvatar: "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=100&h=100&fit=crop",
    content: "Thanks for joining the meditation session! See you Saturday at 7 AM",
    timestamp: "2025-11-04T18:20:00Z",
    read: true,
  },
  {
    id: "msg-3",
    conversationId: "conv-3",
    senderId: "user-4",
    senderName: "Ahmet Can",
    senderAvatar: "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=100&h=100&fit=crop",
    content: "Would you be available for English practice tomorrow afternoon?",
    timestamp: "2025-11-04T16:45:00Z",
    read: false,
  },
  {
    id: "msg-4",
    conversationId: "conv-4",
    senderId: "user-7",
    senderName: "Ece Şahin",
    senderAvatar: "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=100&h=100&fit=crop",
    content: "Don't forget to bring your yoga mat tomorrow! 😊",
    timestamp: "2025-11-04T15:10:00Z",
    read: true,
  },
  {
    id: "msg-5",
    conversationId: "conv-5",
    senderId: "user-8",
    senderName: "Fadime Teyze",
    senderAvatar: "https://images.unsplash.com/photo-1487412720507-e7ab37603c6f?w=100&h=100&fit=crop",
    content: "I have extra ingredients this week. Would you like to learn how to make gözleme?",
    timestamp: "2025-11-03T14:30:00Z",
    read: true,
  },
];

export const mockConversations: Conversation[] = [
  {
    id: "conv-1",
    participants: [mockUsers[9], mockUsers[2]], // Current user and Melisa
    lastMessage: mockMessages[0],
    unreadCount: 1,
    relatedServiceId: "3",
  },
  {
    id: "conv-2",
    participants: [mockUsers[9], mockUsers[0]], // Current user and Selin
    lastMessage: mockMessages[1],
    unreadCount: 0,
    relatedServiceId: "1",
  },
  {
    id: "conv-3",
    participants: [mockUsers[9], mockUsers[3]], // Current user and Ahmet
    lastMessage: mockMessages[2],
    unreadCount: 1,
    relatedServiceId: "4",
  },
  {
    id: "conv-4",
    participants: [mockUsers[9], mockUsers[6]], // Current user and Ece
    lastMessage: mockMessages[3],
    unreadCount: 0,
    relatedServiceId: "7",
  },
  {
    id: "conv-5",
    participants: [mockUsers[9], mockUsers[7]], // Current user and Fadime Teyze
    lastMessage: mockMessages[4],
    unreadCount: 0,
    relatedServiceId: "8",
  },
];

// Get messages for a conversation
export const getMessagesByConversationId = (conversationId: string): Message[] => {
  return mockMessages.filter((msg) => msg.conversationId === conversationId);
};

// Get total unread message count
export const getUnreadMessageCount = (): number => {
  return mockConversations.reduce((sum, conv) => sum + conv.unreadCount, 0);
};

// Get conversation by ID
export const getConversationById = (id: string): Conversation | undefined => {
  return mockConversations.find((conv) => conv.id === id);
};

