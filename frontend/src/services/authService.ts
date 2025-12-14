import { api, ApiError } from '../config/api'
import type {
  LoginRequest,
  RegisterRequest,
  AuthResponse,
  UpdateProfileRequest,
  AuthUser,
} from '../types'

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

export async function getCurrentUser(): Promise<AuthUser> {
  try {
    const response = await api.get<AuthUser>('/auth/me')
    return response
  } catch (error: any) {
    if (error instanceof ApiError && error.status === 401) {
      throw new Error('Authentication required. Please login again.')
    }
    throw new Error('Failed to fetch user data')
  }
}

export async function updateProfile(data: UpdateProfileRequest): Promise<AuthUser> {
  try {
    const response = await api.put<AuthUser>('/auth/me', data)
    return response
  } catch (error: any) {
    if (error instanceof ApiError) {
      throw new Error(error.message || 'Failed to update profile')
    }
    throw new Error('Failed to update profile')
  }
}

export async function getUserById(userId: number): Promise<AuthUser> {
  try {
    const response = await api.get<AuthUser>(`/auth/users/${userId}`)
    return response
  } catch (error: any) {
    if (error instanceof ApiError) {
      throw new Error(error.message || 'Failed to fetch user')
    }
    throw new Error('Failed to fetch user')
  }
}

