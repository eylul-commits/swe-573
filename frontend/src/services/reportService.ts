import { api } from '../config/api';
import type {
  Report,
  CreateReportRequest,
} from '../types';

export async function createReport(request: CreateReportRequest): Promise<Report> {
  try {
    return await api.post<Report>('/reports', request);
  } catch (error) {
    console.error('Failed to create report:', error);
    throw error;
  }
}

export async function getMyReports(): Promise<Report[]> {
  try {
    return await api.get<Report[]>('/reports/my-reports');
  } catch (error) {
    console.error('Failed to fetch reports:', error);
    throw error;
  }
}

export async function getReportById(id: number): Promise<Report> {
  try {
    return await api.get<Report>(`/reports/${id}`);
  } catch (error) {
    console.error(`Failed to fetch report ${id}:`, error);
    throw error;
  }
}
