import {
  AddAttachmentRequestParams,
  Attachment,
  DeleteAttachmentParams,
  GetAttachmentParams,
  RenameAttachmentParams,
  ReplaceAttachmentParams,
} from "@/types/attachment.types";
import apiClient from "./client";
import { handleAxiosError } from "@/lib/utils/apiErrorHandler";
import { apiEndpoints } from "@/config/endpoints";
import { PaginatedResponse } from "@/types/paginated-params";

export const getAttachmentLibraries = async ({
  tenantId,
  workspaceId,
  params,
}: GetAttachmentParams) => {
  try {
    const response = await apiClient.get<PaginatedResponse<Attachment>>(
      apiEndpoints.attachments.collection(tenantId, workspaceId),
      { params }
    );
    return response.data;
  } catch (error) {
    handleAxiosError(error);
    throw error;
  }
};

export const addAttachmentLibrary = async ({
  tenantId,
  workspaceId,
  file,
}: AddAttachmentRequestParams) => {
  try {
    const formData = new FormData();
    formData.append("file", file);
    const response = await apiClient.post<Attachment>(
      apiEndpoints.attachments.collection(tenantId, workspaceId),
      formData,
      {
        headers: {
          "Content-Type": "multipart/form-data",
        },
      }
    );
    return response.data;
  } catch (error) {
    handleAxiosError(error);
    throw error;
  }
};

export const deleteAttachmentLibrary = async ({
  tenantId,
  workspaceId,
  attachmentId,
}: DeleteAttachmentParams) => {
  try {
    const response = await apiClient.delete<void>(
      apiEndpoints.attachments.item(tenantId, workspaceId, attachmentId)
    );
    return response.data;
  } catch (error) {
    handleAxiosError(error);
    throw error;
  }
};

export const renameAttachmentLibrary = async ({
  tenantId,
  workspaceId,
  attachmentId,
  requestBody,
}: RenameAttachmentParams) => {
  try {
    const response = await apiClient.put<Attachment>(
      apiEndpoints.attachments.rename(tenantId, workspaceId, attachmentId),
      requestBody
    );
    return response.data;
  } catch (error) {
    handleAxiosError(error);
    throw error;
  }
};

export const replaceAttachmentLibrary = async ({
  tenantId,
  workspaceId,
  attachmentId,
  file,
}: ReplaceAttachmentParams) => {
  try {
    const formData = new FormData();
    formData.append("file", file);
    const response = await apiClient.put<Attachment>(
      apiEndpoints.attachments.replace(tenantId, workspaceId, attachmentId),
      formData,
      {
        headers: {
          "Content-Type": "multipart/form-data",
        },
      }
    );
    return response.data;
  } catch (error) {
    handleAxiosError(error);
    throw error;
  }
};
