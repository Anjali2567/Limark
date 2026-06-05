import {
  VendorAgreement,
  VendorAgreementParams,
  VendorAgreementVerifyOtpRequest,
} from '@/types/vendor-agreements.types';
import apiClient from './client';
import { apiEndpoints } from '@/config/endpoints';
import { handleAxiosError } from '../utils/apiErrorHandler';

export const getVendorAgreementByType = async ({
  vendorId,
  agreementType,
}: VendorAgreementParams): Promise<VendorAgreement> => {
  try {
    const response = await apiClient.get<VendorAgreement>(
      apiEndpoints.vendor.agreements.collection(vendorId),
      { params: { agreementType } }
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const sendVendorAgreementOtp = async ({
  vendorId,
  agreementType,
}: VendorAgreementParams): Promise<void> => {
  try {
    await apiClient.post(
      apiEndpoints.vendor.agreements.sendOtp(),
      {},
      { params: { vendorId, agreementType } }
    );
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const verifyVendorAgreementOtp = async (
  payload: VendorAgreementVerifyOtpRequest
): Promise<void> => {
  try {
    await apiClient.post(apiEndpoints.vendor.agreements.verifyOtp(), payload);
  } catch (error) {
    throw handleAxiosError(error);
  }
};
