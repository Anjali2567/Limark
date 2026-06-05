import apiClient from "./client";
import { apiEndpoints } from "@/config/endpoints";
import { handleAxiosError } from "../utils/apiErrorHandler";
import {
  PromptSpecificationParams,
  PromptSpecificationRequest,
} from "@/types/prompt-specification.type";

export const getLastPromptSpecificationByType = async (
  params: PromptSpecificationParams
) => {
  try {
    const response = await apiClient.get(apiEndpoints.promptSpecification.get, {
      params,
    });
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const updatePromptSpecification = async (
  payload: PromptSpecificationRequest
) => {
  try {
    const response = await apiClient.put(
      apiEndpoints.promptSpecification.update,
      payload
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};
