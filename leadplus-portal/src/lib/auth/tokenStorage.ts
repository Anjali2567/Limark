const TOKEN_KEY = "token";

export const setRefreshToken = (token: string, tokenKey?: string): void => {
  if (typeof window === "undefined") return;
  localStorage.setItem(tokenKey ?? TOKEN_KEY, token);
};

export const getRefreshToken = (tokenKey?: string): string | null => {
  if (typeof window === "undefined") return null;
  return localStorage.getItem(tokenKey ?? TOKEN_KEY);
};

export const removeRefreshToken = (tokenKey?: string): void => {
  if (typeof window === "undefined") return;
  localStorage.removeItem(tokenKey ?? TOKEN_KEY);
};
