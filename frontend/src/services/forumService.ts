import { api } from '../config/api';
import type {
  ForumTopic,
  ForumPost,
  CreateForumTopicRequest,
  CreateForumPostRequest,
} from '../types/forum';
import { formatDistanceToNow } from '../utils/dateUtils';


 //Get all forum topics
export async function getAllForumTopics(): Promise<ForumTopic[]> {
  return api.get<ForumTopic[]>('/forum/topics');
}

 //Get a specific forum topic by ID
export async function getForumTopicById(id: number): Promise<ForumTopic> {
  return api.get<ForumTopic>(`/forum/topics/${id}`);
}

 //Create a new forum topic
export async function createForumTopic(
  request: CreateForumTopicRequest
): Promise<ForumTopic> {
  return api.post<ForumTopic>('/forum/topics', request);
}

 //Delete a forum topic
export async function deleteForumTopic(id: number): Promise<void> {
  return api.delete<void>(`/forum/topics/${id}`);
}

 //Get all posts for a specific topic
export async function getPostsByTopicId(topicId: number): Promise<ForumPost[]> {
  return api.get<ForumPost[]>(`/forum/topics/${topicId}/posts`);
}

 //Create a new post in a topic
export async function createForumPost(
  topicId: number,
  request: CreateForumPostRequest
): Promise<ForumPost> {
  return api.post<ForumPost>(`/forum/topics/${topicId}/posts`, request);
}

 //Delete a forum post
export async function deleteForumPost(id: number): Promise<void> {
  return api.delete<void>(`/forum/posts/${id}`);
}

 //Filter topics by search query
export function filterTopics(
  topics: ForumTopic[],
  searchQuery: string
): ForumTopic[] {
  return topics.filter((topic) => {
    const matchesSearch =
      searchQuery === '' ||
      topic.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
      (topic.excerpt && topic.excerpt.toLowerCase().includes(searchQuery.toLowerCase()));

    return matchesSearch;
  });
}


