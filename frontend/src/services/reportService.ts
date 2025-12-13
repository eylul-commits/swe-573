import { api } from '../config/api';

export type ReportType = 'USER' | 'OFFER' | 'REQUEST' | 'FORUM_POST' | 'FORUM_TOPIC';
export type ReportStatus = 'OPEN' | 'RESOLVED';

export interface Report {
  id: number;
  reporterId: number;
  reporterName: string;
  reporterEmail: string;
  reportedUserId: number;
  reportedUserName: string;
  reportedUserEmail: string;
  reportedUserRole?: 'USER' | 'ADMIN';
  reportType: ReportType;
  reportedOfferId?: number;
  reportedOfferTitle?: string;
  reportedRequestId?: number;
  reportedRequestTitle?: string;
  reportedForumPostId?: number;
  reportedForumTopicId?: number;
  message: string;
  adminNotes?: string;
  status: ReportStatus;
  createdAt: string;
  resolvedAt?: string;
  resolvedById?: number;
  resolvedByName?: string;
}

export interface CreateReportRequest {
  reportType: ReportType;
  reportedUserId: number;
  reportedOfferId?: number;
  reportedRequestId?: number;
  reportedForumPostId?: number;
  reportedForumTopicId?: number;
  message: string;
}

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

