/**
 * Handshake Service
 * 
 * Handles all API calls related to handshakes (service exchanges)
 */

import { api } from '../config/api';
import type {
  Handshake,
  CreateHandshakeRequest,
  ConfirmHandshakeRequest,
  CreateRatingRequest,
  ServiceRating,
} from '../types';

/**
 * Create a new handshake (accept an offer)
 */
export async function createHandshake(request: CreateHandshakeRequest): Promise<Handshake> {
  return api.post<Handshake>('/handshakes', request);
}

/**
 * Confirm a handshake with completion date
 */
export async function confirmHandshake(
  handshakeId: number,
  request: ConfirmHandshakeRequest
): Promise<Handshake> {
  return api.post<Handshake>(`/handshakes/${handshakeId}/confirm`, request);
}

/**
 * Create a rating for a completed handshake
 */
export async function createRating(request: CreateRatingRequest): Promise<Handshake> {
  return api.post<Handshake>('/handshakes/rate', request);
}

/**
 * Get all handshakes for current user
 */
export async function getUserHandshakes(): Promise<Handshake[]> {
  return api.get<Handshake[]>('/handshakes');
}

/**
 * Get pending handshakes (waiting for confirmation)
 */
export async function getPendingHandshakes(): Promise<Handshake[]> {
  return api.get<Handshake[]>('/handshakes/pending');
}

/**
 * Get confirmed handshakes
 */
export async function getConfirmedHandshakes(): Promise<Handshake[]> {
  return api.get<Handshake[]>('/handshakes/confirmed');
}

/**
 * Cancel a handshake (only if pending and not both confirmed)
 */
export async function cancelHandshake(handshakeId: number): Promise<Handshake> {
  return api.post<Handshake>(`/handshakes/${handshakeId}/cancel`);
}

/**
 * Get a specific handshake by ID
 */
export async function getHandshakeById(handshakeId: number): Promise<Handshake> {
  return api.get<Handshake>(`/handshakes/${handshakeId}`);
}

/**
 * Get all ratings for a user
 */
export async function getUserRatings(userId: number): Promise<ServiceRating[]> {
  return api.get<ServiceRating[]>(`/handshakes/users/${userId}/ratings`);
}

/**
 * Get handshakes for a specific offer
 */
export async function getHandshakesByOfferId(offerId: number): Promise<Handshake[]> {
  return api.get<Handshake[]>(`/handshakes/offers/${offerId}`);
}

/**
 * Get handshakes for a specific request
 */
export async function getHandshakesByRequestId(requestId: number): Promise<Handshake[]> {
  return api.get<Handshake[]>(`/handshakes/requests/${requestId}`);
}

