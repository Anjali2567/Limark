import { LeadType } from '@/constants/leadSearch.constants';
import { BasicParams } from './user.types';
import { PaginationParams } from './paginated-params';

export type LeadList = {
  id: string;
  tenantId: string;
  workspaceId: string;
  name: string;
  type: LeadType;
  sourceIds: string[];
  active: boolean;
  createdBy: string;
  createdAt: string;
  updatedBy: string;
  updatedAt: string;
};

export type LeadListSearch = LeadList & {
  sourceCount: number;
  username: string;
};

export type LeadListParams = BasicParams & {
  listId: string;
};

export type LeadListPayload = {
  name: string;
  type: LeadType;
  sourceIds: string[];
};

export type LeadListSearchParams = {
  page: PaginationParams;
  params: BasicParams;
  type: LeadType;
};

export type LeadListRequest = {
  params: LeadListParams;
  payload: LeadListPayload;
};
