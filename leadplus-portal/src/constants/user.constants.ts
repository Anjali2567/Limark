export enum UserRoles {
  ADMIN = 'ADMIN',
  USER = 'USER',
  GUEST = 'GUEST',
  TENANT_OWNER = 'TENANT_OWNER',
  CUSTOMER = 'CUSTOMER',
  VENDOR = 'VENDOR',
}

export enum UserStatus {
  PENDING = 'PENDING',
  APPROVED = 'APPROVED',
  REJECTED = 'REJECTED',
}

export const userStatusList = [
  { label: 'Active', value: UserStatus.APPROVED },
  { label: 'Pending', value: UserStatus.PENDING },
  { label: 'Inactive', value: UserStatus.REJECTED },
];

export enum WorkspaceUserRole {
  OWNER = 'OWNER',
  MEMBER = 'MEMBER',
  WORKSPACE_ADMIN = 'WORKSPACE_ADMIN',
}

export enum WorkspaceUserStatus {
  INVITED = 'INVITED',
  ACCEPTED = 'ACCEPTED',
  REVOKED = 'REVOKED',
}
