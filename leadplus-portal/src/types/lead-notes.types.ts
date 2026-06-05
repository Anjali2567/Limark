import { LeadType } from '@/constants/leadSearch.constants';
import { BasicParams, UserIdentity } from './user.types';
import { PaginationParams } from './paginated-params';

export type LeadNote = {
  id: string;
  tenantId: string;
  workspaceId: string;
  note: string;
  type: LeadType;
  sourceId: string;
  active: boolean;
  createdBy: UserIdentity | null;
  createdAt: string;
  updatedBy: string;
  updatedAt: string;
};

export type LeadNotePayload = {
  note: string;
  type: LeadType;
  sourceId: string;
};

export type CreateLeadNoteRequest = {
  params: BasicParams;
  payload: LeadNotePayload;
};

export type LeadNoteParams = BasicParams & {
  leadNoteId: string;
};

export type LeadNoteSourceParams = BasicParams & {
  sourceId: string;
  params: PaginationParams;
};

export type UpdateLeadNoteRequest = {
  params: LeadNoteParams;
  note: string;
  sourceId: string;
};
