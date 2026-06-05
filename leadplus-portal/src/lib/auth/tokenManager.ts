/**
 * This file handles the management of the access token in memory.
 * It provides functions to get and set the access token.
 */
let accessToken: string | null = null;

/**
 * Get the current in-memory access token.
 */
export const getAccessTokenInMemory = (): string | null => {
  return accessToken;
};

/**
 * Set or clear the in-memory access token.
 * @param token The new access token or null to clear.
 */
export const setAccessTokenInMemory = (token: string | null): void => {
  accessToken = token;
};

/**
 * This temp token is used to store the access token temporarily
 * during the OAuth2 flow before it is finalized.
 **/
let tempToken: string | null = null;

export const getTempToken = (): string | null => {
  return tempToken;
};

export const setTempToken = (token: string | null): void => {
  tempToken = token;
};
