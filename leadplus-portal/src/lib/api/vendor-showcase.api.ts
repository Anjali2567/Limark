import { apiEndpoints } from '@/config/endpoints';
import apiClient from './client';
import {
  VendorShowcase,
  VendorShowcaseAttachmentDetails,
  VendorShowcaseAttachmentRequest,
  VendorShowcaseBasicDetails,
  VendorShowcaseDetails,
  VendorShowcaseRequest,
  VendorShowcaseUpdateRequest,
} from '@/types/vendor-showcase.types';
import { handleAxiosError } from '../utils/apiErrorHandler';

export const getAllVendorShowcases = async ({
  tenantId,
  vendorId,
}: VendorShowcaseBasicDetails): Promise<VendorShowcase[]> => {
  try {
    const response = await apiClient.get<VendorShowcase[]>(
      apiEndpoints.vendor.showcase.collection(tenantId, vendorId)
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const createVendorShowcase = async ({
  tenantId,
  vendorId,
  payload,
}: VendorShowcaseRequest): Promise<VendorShowcase> => {
  try {
    const response = await apiClient.post<VendorShowcase>(
      apiEndpoints.vendor.showcase.collection(tenantId, vendorId),
      payload
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const getVendorShowCaseById = async ({
  tenantId,
  vendorId,
  id,
}: VendorShowcaseDetails): Promise<VendorShowcase> => {
  try {
    const response = await apiClient.get<VendorShowcase>(
      apiEndpoints.vendor.showcase.item(tenantId, vendorId, id)
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const updateVendorShowcase = async ({
  tenantId,
  vendorId,
  id,
  payload,
}: VendorShowcaseUpdateRequest): Promise<VendorShowcase> => {
  try {
    const response = await apiClient.put<VendorShowcase>(
      apiEndpoints.vendor.showcase.item(tenantId, vendorId, id),
      payload
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const deleteVendorShowcase = async ({
  tenantId,
  vendorId,
  id,
}: VendorShowcaseDetails): Promise<void> => {
  try {
    await apiClient.delete(apiEndpoints.vendor.showcase.item(tenantId, vendorId, id));
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const addVendorShowcaseAttachment = async ({
  tenantId,
  vendorId,
  id,
  file,
}: VendorShowcaseAttachmentRequest): Promise<VendorShowcase> => {
  try {
    const formData = new FormData();
    formData.append('file', file);
    const response = await apiClient.put<VendorShowcase>(
      apiEndpoints.vendor.showcase.attachments.collection(tenantId, vendorId, id),
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

export const deleteVendorShowcaseAttachment = async ({
  tenantId,
  vendorId,
  id,
  attachmentId,
}: VendorShowcaseAttachmentDetails): Promise<void> => {
  try {
    await apiClient.delete(
      apiEndpoints.vendor.showcase.attachments.item(tenantId, vendorId, id, attachmentId)
    );
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const getAllVendorShowcasesByVendorId = async ({
  vendorId,
}: {
  vendorId: string;
}): Promise<VendorShowcase[]> => {
  try {
    const response = await apiClient.get<VendorShowcase[]>(
      apiEndpoints.admin.vendor.showcase(vendorId)
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};
