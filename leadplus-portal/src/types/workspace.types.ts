import { WorkspaceUserRole } from '@/constants/user.constants';
import { BasicParams } from './user.types';

export type Recipient = {
  email: string;
  name?: string;
};

export type Workspace = {
  id: string;
  name: string;
  ownerId: string;
  tenantId: string;
  ccRecipients: Recipient[] | null;
  bccRecipients: Recipient[] | null;
  dailySendLimit: number;
  createdBy: string;
  createdAt: string;
  updatedBy: string;
  updatedAt: string;
};

type WorkspaceDetails = {
  name: string;
  ccRecipients: Recipient[];
  bccRecipients: Recipient[];
  dailySendLimit: number;
};

export type WorkspaceRequest = BasicParams & {
  payload: WorkspaceDetails;
};

export type WorkspaceCreateRequest = {
  tenantId: string;
  payload: WorkspaceDetails;
};

export type WorkspaceUserRoleUpdateRequest = BasicParams & {
  userId: string;
  payload: {
    role: WorkspaceUserRole;
  };
};
