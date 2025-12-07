import { api } from '../config/api'

export interface LoginRequest {
  email: string
  password: string
}

export interface RegisterRequest {
  name: string
  email: string
  password: string
}

export interface User {
  id: number
  email: string
  name: string
  bio?: string
  avatarUrl?: string
  province?: string
  district?: string
  geohash?: string
  role: string
  balanceHours: number
  timebankBalance?: number
  hoursGiven?: number
  hoursReceived?: number
  avatar?: string
  location?: string
}

export interface AuthResponse {
  token: string
  user: User
  streamChatToken?: string
}

export async function login(credentials: LoginRequest): Promise<AuthResponse> {
  try {
    const response = await api.post<AuthResponse>('/auth/login', credentials)
    return response
  } catch (error: any) {
    if (error.response?.status === 401) {
      throw new Error('Invalid email or password')
    }
    throw new Error(error.response?.data?.message || 'Login failed')
  }
}

export async function register(data: RegisterRequest): Promise<AuthResponse> {
  try {
    const response = await api.post<AuthResponse>('/auth/register', data)
    return response
  } catch (error: any) {
    if (error.response?.status === 400) {
      throw new Error(error.response?.data?.message || 'Email already registered')
    }
    throw new Error(error.response?.data?.message || 'Registration failed')
  }
}

export async function getCurrentUser(): Promise<User> {
  try {
    const response = await api.get<User>('/auth/me')
    return response
  } catch (error: any) {
    throw new Error('Failed to fetch user data')
  }
}

export interface UpdateProfileRequest {
  name?: string
  bio?: string
  avatarUrl?: string
  province?: string
  district?: string
  geohash?: string
}

export async function updateProfile(data: UpdateProfileRequest): Promise<User> {
  try {
    const response = await api.put<User>('/auth/me', data)
    return response
  } catch (error: any) {
    throw new Error(error.response?.data?.message || 'Failed to update profile')
  }
}

