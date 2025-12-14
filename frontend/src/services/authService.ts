import { api, ApiError } from '../config/api'

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
  accountStatus?: 'ACTIVE' | 'DEACTIVATED' | 'WARNED'
  warningCount?: number
  balanceHours: number
  timebankBalance?: number
  hoursGiven?: number
  hoursReceived?: number
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
    if (error instanceof ApiError) {
      if (error.status === 401) {
        throw new Error('Invalid email or password')
      }
      throw new Error(error.message || 'Login failed')
    }
    throw new Error('Login failed')
  }
}

export async function register(data: RegisterRequest): Promise<AuthResponse> {
  try {
    const response = await api.post<AuthResponse>('/auth/register', data)
    return response
  } catch (error: any) {
    if (error instanceof ApiError) {
      if (error.status === 400) {
        throw new Error(error.data?.message || 'Email already registered')
      }
      throw new Error(error.message || 'Registration failed')
    }
    throw new Error('Registration failed')
  }
}

export async function getCurrentUser(): Promise<User> {
  try {
    const response = await api.get<User>('/auth/me')
    return response
  } catch (error: any) {
    if (error instanceof ApiError && error.status === 401) {
      throw new Error('Authentication required. Please login again.')
    }
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
    if (error instanceof ApiError) {
      throw new Error(error.message || 'Failed to update profile')
    }
    throw new Error('Failed to update profile')
  }
}

export async function getUserById(userId: number): Promise<User> {
  try {
    const response = await api.get<User>(`/auth/users/${userId}`)
    return response
  } catch (error: any) {
    if (error instanceof ApiError) {
      throw new Error(error.message || 'Failed to fetch user')
    }
    throw new Error('Failed to fetch user')
  }
}

