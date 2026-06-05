import { handleAxiosError } from '../utils/apiErrorHandler';
import apiClient from './client';

import { apiEndpoints } from '@/config/endpoints';
import { PaginatedResponse } from '@/types/paginated-params';
import {
  CustomerQuotationResponse,
  Quotation,
  QuotationIdentity,
  RFQQuotationRequest,
  VendorQuotationsRequest,
} from '@/types/quotation.types';

export const getAllVendorRFQQuotations = async (request: VendorQuotationsRequest) => {
  try {
    const response = await apiClient.get<PaginatedResponse<CustomerQuotationResponse>>(
      apiEndpoints.vendor.rfqQuotations(request.tenantId, request.workspaceId),
      {
        params: request.params,
      }
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const getQuotationById = async (
  tenantId: string,
  workspaceId: string,
  quotationId: string
) => {
  try {
    const response = await apiClient.get<CustomerQuotationResponse>(
      apiEndpoints.vendor.rfqQuotationById(tenantId, workspaceId, quotationId)
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const acceptRFQQuotation = async (identity: QuotationIdentity) => {
  try {
    const response = await apiClient.put<Quotation>(
      apiEndpoints.vendor.rfqQuotationAccept(
        identity.tenantId,
        identity.workspaceId,
        identity.quotationId
      )
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const rejectRFQQuotation = async (identity: QuotationIdentity) => {
  try {
    const response = await apiClient.put<Quotation>(
      apiEndpoints.vendor.rfqQuotationReject(
        identity.tenantId,
        identity.workspaceId,
        identity.quotationId
      )
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const updateRFQQuotation = async (req: RFQQuotationRequest) => {
  const { params: identity, payload: updatedData } = req;
  try {
    const response = await apiClient.put<Quotation>(
      apiEndpoints.vendor.rfqQuotationById(
        identity.tenantId,
        identity.workspaceId,
        identity.quotationId
      ),
      updatedData
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};
