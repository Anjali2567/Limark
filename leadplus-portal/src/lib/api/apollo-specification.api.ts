import { apiEndpoints } from "@/config/endpoints";
import apiClient from "./client";
import { handleAxiosError } from "../utils/apiErrorHandler";
import {
  ApolloSpecification,
  ApolloSpecificationRequest,
} from "@/types/apollo-specification.types";

export const getLatestApolloSpecification =
  async (): Promise<ApolloSpecification> => {
    try {
      const response = await apiClient.get<ApolloSpecification>(
        apiEndpoints.apolloSpecification.collection
      );
      return response.data;
    } catch (error) {
      throw handleAxiosError(error);
    }
  };

export const updateApolloSpecification = async (
  payload: ApolloSpecificationRequest
): Promise<ApolloSpecification> => {
  try {
    const response = await apiClient.put<ApolloSpecification>(
      apiEndpoints.apolloSpecification.collection,
      payload
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};
