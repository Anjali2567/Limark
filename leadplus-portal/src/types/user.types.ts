import { IdentityProviderType } from '@/constants/Auth';
import {
  UserRoles,
  UserStatus,
  WorkspaceUserRole,
  WorkspaceUserStatus,
} from '@/constants/user.constants';
import { PaginationParams } from './paginated-params';

export type IdentityProvider = {
  type: IdentityProviderType;
  personId: string;
};

export type UserIdentity = {
  id: string;
  name: string;
};

export type User = {
  id: string;
  tenantId: string;
  workspaceId: string;
  name: string;
  email: string;
  identityProviders: IdentityProvider[];
  roles: UserRoles[];
  status: UserStatus;
  phoneNumber: string;
  company: string;
  createdAt: Date;
};

export type BasicParams = {
  tenantId: string;
  workspaceId: string;
};

export type SignupPayload = {
  name: string;
  email: string;
  password: string;
  phoneNumber?: string;
  company?: string;
};

export type UserListingParams = PaginationParams & {
  userRoles?: UserRoles[];
  userStatuses?: UserStatus[];
};

export type ForgotPasswordRequest = {
  email: string;
};

export type ResetPasswordRequest = {
  password: string;
  token: string;
};

export type UpdateUserProfileRequest = {
  name?: string | null;
  phoneNumber?: string | null;
  company?: string | null;
};

export type UserWorkspace = {
  id: string;
  tenantId: string;
  workspaceId: string;
  workspaceName: string;
  userId: string;
  role: WorkspaceUserRole;
};

export type CurrentWorkspace = {
  tenantId: string;
  workspaceId: string;
};

export type WorkspaceUsersResponse = {
  id: string;
  name: string;
  email: string;
  workspaceUserRole: WorkspaceUserRole;
  workspaceUserStatus: WorkspaceUserStatus;
  createdAt: Date;
  updatedAt: Date;
};

export type InviteUserRequest = BasicParams & {
  payload: {
    email: string;
    name?: string;
  };
};

export type UserBasicParams = {
  userId: string;
} & BasicParams;
