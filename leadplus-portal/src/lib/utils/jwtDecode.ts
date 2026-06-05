import { jwtDecode } from 'jwt-decode';

interface JwtPayload {
  userId: string;
  workspaceId: string;
  tenantId: string;
  name: string;
  email: string;
  roles: string[];
  active: boolean;
  verified: boolean;
  sub: string;
  iat: number;
  exp: number;
}

export const jwtDecoder = (token: string): JwtPayload | null => {
  if (!token) return null;

  try {
    const decoded = jwtDecode<JwtPayload>(token);

    // Check if token has expired by comparing exp claim with current timestamp
    if (decoded.exp && decoded.exp < Date.now() / 1000) return null;

    return decoded;
  } catch {
    return null;
  }
};
