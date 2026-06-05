import axios, { AxiosError } from 'axios';

export function handleAxiosError(error: unknown) {
  if (axios.isAxiosError(error)) {
    const statusCode = error.response?.status ?? 500;
    const errorMessage = error.response?.data?.message || 'An error occurred';
    throw new AxiosError(errorMessage, String(statusCode));
  }
  throw error;
}
