import { PaginatedResponse } from '@/types/paginated-params';
import { handleAxiosError } from '../utils/apiErrorHandler';
import apiClient from './client';
import { Agreement, AgreementParams, AgreementPayload } from '@/types/agreements.types';
import { apiEndpoints } from '@/config/endpoints';
import { AgreementType } from '@/constants/agreement.constant';

export const getAllAgreements = async (params: AgreementParams) => {
  try {
    const response = await apiClient.get<PaginatedResponse<Agreement>>(
      apiEndpoints.admin.agreements.collection,
      { params }
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const getAgreementById = async (agreementId: string) => {
  try {
    const response = await apiClient.get<Agreement>(
      apiEndpoints.admin.agreements.item(agreementId)
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const updateAgreement = async (agreementType: AgreementType, payload: AgreementPayload) => {
  try {
    const response = await apiClient.put<Agreement>(
      apiEndpoints.admin.agreements.collection,
      payload,
      { params: { agreementType } }
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};
