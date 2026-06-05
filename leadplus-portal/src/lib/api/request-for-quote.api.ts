import { PaginatedResponse, PaginationParams } from '@/types/paginated-params';
import {
  RequestForQuote,
  RequestForQuoteFile,
  RequestForQuotePayload,
  RequestForQuoteWithQuoteCount,
} from '@/types/request-for-quote.types';
import apiClient from './client';
import { apiEndpoints } from '@/config/endpoints';
import { handleAxiosError } from '../utils/apiErrorHandler';

export const getAllRequestForQuotes = async (
  params: PaginationParams
): Promise<PaginatedResponse<RequestForQuoteWithQuoteCount>> => {
  try {
    const response = await apiClient.get(apiEndpoints.customer.requestForQuotes.collection, {
      params,
    });
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const getRequestForQuoteById = async (
  rfqId: string
): Promise<RequestForQuoteWithQuoteCount> => {
  try {
    const response = await apiClient.get(apiEndpoints.customer.requestForQuotes.item(rfqId));
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const createRequestForQuote = async (
  payload: RequestForQuotePayload
): Promise<RequestForQuote> => {
  try {
    const response = await apiClient.post(
      apiEndpoints.customer.requestForQuotes.collection,
      payload
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const updateRequestForQuote = async (
  id: string,
  payload: RequestForQuotePayload
): Promise<RequestForQuote> => {
  try {
    const response = await apiClient.put(apiEndpoints.customer.requestForQuotes.item(id), payload);
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const deleteRequestForQuote = async (rfqId: string): Promise<void> => {
  try {
    await apiClient.delete(apiEndpoints.customer.requestForQuotes.item(rfqId));
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const uploadRequestForQuoteFile = async (
  id: string,
  file: File
): Promise<RequestForQuote> => {
  try {
    const formData = new FormData();
    formData.append('file', file);
    const response = await apiClient.put(
      apiEndpoints.customer.requestForQuotes.attachments(id),
      formData,
      {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      }
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const deleteRequestForQuoteFile = async (
  request: RequestForQuoteFile
): Promise<RequestForQuote> => {
  try {
    const response = await apiClient.delete(
      apiEndpoints.customer.requestForQuotes.attachmentById(
        request.requestForQuoteId,
        request.attachmentId
      )
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};
