import axios, {
  AxiosError,
  AxiosInstance,
  AxiosRequestConfig,
  InternalAxiosRequestConfig,
} from 'axios';
import { getAccessTokenInMemory, setAccessTokenInMemory } from '../auth/tokenManager';
import { getRefreshToken, removeRefreshToken } from '../auth/tokenStorage';
import { AuthToken } from '@/constants/Auth';
import { refreshToken } from './auth.api';
import { appRoutes } from '@/config/routes';

const REFRESH_TOKEN_ENDPOINT = '/v1/auth/refresh';
const LOGIN_ENDPOINT = '/v1/auth/login';

const PUBLIC_ENDPOINTS = [REFRESH_TOKEN_ENDPOINT, LOGIN_ENDPOINT];

const apiBaseURL = `${process.env.NEXT_PUBLIC_API_URL}/api`;

export const apiClient: AxiosInstance = axios.create({
  baseURL: apiBaseURL,
});

apiClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    if (PUBLIC_ENDPOINTS.some((endpoint) => config.url === endpoint)) {
      return config;
    }

    const token = getAccessTokenInMemory();
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    const timeZone = Intl.DateTimeFormat().resolvedOptions().timeZone;
    config.headers['X-Timezone'] = timeZone;
    return config;
  },
  (error) => Promise.reject(error)
);

let isRefreshing = false;
let failedQueue: Array<{
  resolve: (token: string) => void;
  reject: (err: unknown) => void;
}> = [];

const processQueue = (token: string | null, error: unknown = null) => {
  failedQueue.forEach(({ resolve, reject }) => {
    if (token) {
      resolve(token);
    } else {
      reject(error);
    }
  });
  failedQueue = [];
};

apiClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as AxiosRequestConfig & {
      _retry?: boolean;
    };

    if (originalRequest.url === REFRESH_TOKEN_ENDPOINT && error.response) {
      removeRefreshToken(AuthToken.REFRESH_TOKEN);
      if (typeof window !== 'undefined') {
        const loginPath = appRoutes.leadgen.auth.login;
        if (window.location.pathname !== loginPath) {
          window.location.href = loginPath;
        }
      }
      return Promise.reject(error);
    }

    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({
            resolve: (token: string) => {
              if (originalRequest.headers) {
                originalRequest.headers['Authorization'] = `Bearer ${token}`;
              }
              resolve(apiClient(originalRequest));
            },
            reject,
          });
        });
      }

      isRefreshing = true;
      const refreshTokenFromStorage = getRefreshToken(AuthToken.REFRESH_TOKEN);
      try {
        if (!refreshTokenFromStorage) {
          return Promise.reject(new Error('No refresh token available'));
        }
        const newToken = await refreshToken(refreshTokenFromStorage)
          .then((data) => {
            setAccessTokenInMemory(data.accessToken!);
            return data.accessToken;
          })
          .catch((err) => {
            removeRefreshToken(AuthToken.REFRESH_TOKEN);
            if (typeof window !== 'undefined') {
              const loginPath = appRoutes.leadgen.auth.login;
              if (window.location.pathname !== loginPath) {
                window.location.href = loginPath;
              }
            }
            throw err;
          });

        processQueue(newToken!);

        if (newToken && originalRequest.headers) {
          originalRequest.headers['Authorization'] = `Bearer ${newToken}`;
          return apiClient(originalRequest);
        }
      } catch (err) {
        processQueue(null, err);
        return Promise.reject(err);
      } finally {
        isRefreshing = false;
      }
    }

    return Promise.reject(error);
  }
);

export default apiClient;
