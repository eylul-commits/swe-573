import { api } from '../config/api';
import type {
  Report,
  ReportStatus,
  AdminStatistics,
  AdminUser,
  UserManagementRequest,
  ResolveReportRequest,
  UserAction,
} from '../types';

export async function getAdminStatistics(): Promise<AdminStatistics> {
  try {
    return await api.get<AdminStatistics>('/admin/statistics');
  } catch (error) {
    console.error('Failed to fetch admin statistics:', error);
    throw error;
  }
}

export async function getAllUsers(): Promise<AdminUser[]> {
  try {
    return await api.get<AdminUser[]>('/admin/users');
  } catch (error) {
    console.error('Failed to fetch users:', error);
    throw error;
  }
}

export async function manageUser(request: UserManagementRequest): Promise<AdminUser> {
  try {
    return await api.post<AdminUser>('/admin/users/manage', request);
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

export async function getUserActions(userId: number): Promise<UserAction[]> {
  try {
    return await api.get<UserAction[]>(`/admin/users/${userId}/actions`);
  } catch (error) {
    console.error(`Failed to fetch user actions for user ${userId}:`, error);
    throw error;
  }
}

