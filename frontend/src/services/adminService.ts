import { api } from '../config/api';
import type { Report, ReportStatus } from './reportService';

export interface AdminStatistics {
  totalUsers: number;
  activeUsers: number;
  deactivatedUsers: number;
  totalOffers: number;
  activeOffers: number;
  totalRequests: number;
  activeRequests: number;
  totalReports: number;
  openReports: number;
  inReviewReports: number;
  resolvedReports: number;
  totalHandshakes: number;
  totalMessages: number;
}

export interface User {
  id: number;
  email: string;
  name: string;
  bio?: string;
  avatarUrl?: string;
  province?: string;
  district?: string;
  geohash?: string;
  role: 'USER' | 'ADMIN';
  accountStatus: 'ACTIVE' | 'DEACTIVATED' | 'WARNED';
  warningCount: number;
  balanceHours: number;
  hoursGiven?: number;
  hoursReceived?: number;
}

export interface UserManagementRequest {
  userId: number;
  action: 'WARN' | 'DEACTIVATE' | 'ACTIVATE';
  reason?: string;
}

export interface ResolveReportRequest {
  status: ReportStatus;
  adminNotes?: string;
  userId?: number;
  action?: 'WARN' | 'DEACTIVATE' | 'NO_ACTION';
}

export async function getAdminStatistics(): Promise<AdminStatistics> {
  try {
    return await api.get<AdminStatistics>('/admin/statistics');
  } catch (error) {
    console.error('Failed to fetch admin statistics:', error);
    throw error;
  }
}

export async function getAllUsers(): Promise<User[]> {
  try {
    return await api.get<User[]>('/admin/users');
  } catch (error) {
    console.error('Failed to fetch users:', error);
    throw error;
  }
}

export async function manageUser(request: UserManagementRequest): Promise<User> {
  try {
    return await api.post<User>('/admin/users/manage', request);
  } catch (error) {
    console.error('Failed to manage user:', error);
    throw error;
  }
}

export async function getAllReports(status?: ReportStatus): Promise<Report[]> {
  try {
    const url = status ? `/admin/reports?status=${status}` : '/admin/reports';
    return await api.get<Report[]>(url);
  } catch (error) {
    console.error('Failed to fetch reports:', error);
    throw error;
  }
}

export async function getReportById(id: number): Promise<Report> {
  try {
    return await api.get<Report>(`/admin/reports/${id}`);
  } catch (error) {
    console.error(`Failed to fetch report ${id}:`, error);
    throw error;
  }
}

export async function resolveReport(id: number, request: ResolveReportRequest): Promise<Report> {
  try {
    return await api.post<Report>(`/admin/reports/${id}/resolve`, request);
  } catch (error) {
    console.error(`Failed to resolve report ${id}:`, error);
    throw error;
  }
}

