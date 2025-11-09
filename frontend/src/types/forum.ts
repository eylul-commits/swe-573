/**
 * Forum-related type definitions
 */

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


