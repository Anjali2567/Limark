export type TokenResponse = {
  accessToken?: string | null;
  refreshToken?: string | null;
};

export type LoggedInUser = {
  userId?: string | null;
  workspaceId?: string | null;
  tenantId?: string | null;
  name?: string | null;
  email?: string | null;
  roles?: string[] | null;
  active?: boolean | null;
  verified?: boolean | null;
};

export type GoogleAuthRequest = {
  token: string;
};
