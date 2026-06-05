import { FeedbackStatus, FeedbackType } from '@/constants/user-feedback.constant';
import { BasicParams } from './user.types';
import { PaginationParams } from './paginated-params';

export type FeedbackPayload = {
  type: FeedbackType;
  message: string;
  rating: number;
};

export type FeedbackSearchParams = PaginationParams & {
  userId?: string;
  workspaceId?: string;
  status?: FeedbackStatus[];
  types?: FeedbackType[];
};

export type FeedbackReplyPayload = {
  message: string;
};

export type FeedbackReplyRequest = {
  feedbackId: string;
  payload: FeedbackReplyPayload;
};

export type FeedbackRequest = {
  params: BasicParams;
  payload: FeedbackPayload;
};

export type Feedback = {
  id: string;
  tenantId: string;
  workspaceId: string;
  userId: string;
  username: string;
  companyName: string;
  type: FeedbackType;
  message: string;
  rating: number;
  status: FeedbackStatus;
  feedbackDate: Date;
  createdBy: string;
  createdAt: Date;
  updatedBy: string;
  updatedAt: Date;
};
