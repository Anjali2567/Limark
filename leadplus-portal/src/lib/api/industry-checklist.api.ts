import {
  CheckListParams,
  CheckListQuestion,
  CheckListQuestionRequest,
  CheckListSection,
  CheckListSectionRequest,
} from '@/types/question.types';
import apiClient from './client';
import { handleAxiosError } from '../utils/apiErrorHandler';
import { apiEndpoints } from '@/config/endpoints';
import { PaginatedResponse } from '@/types/paginated-params';

// Sections

export const getAllChecklistSections = async (): Promise<CheckListSection[]> => {
  try {
    const response = await apiClient.get(apiEndpoints.admin.industryChecklistSection.collection);
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const createChecklistSection = async (
  payload: CheckListSectionRequest
): Promise<CheckListSection> => {
  try {
    const response = await apiClient.post(
      apiEndpoints.admin.industryChecklistSection.collection,
      payload
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const updateChecklistSection = async (
  sectionId: string,
  payload: CheckListSectionRequest
): Promise<CheckListSection> => {
  try {
    const response = await apiClient.put(
      apiEndpoints.admin.industryChecklistSection.item(sectionId),
      payload
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const bulkUpdateChecklistSections = async (
  payload: CheckListSection[]
): Promise<CheckListSection[]> => {
  try {
    const response = await apiClient.put(
      apiEndpoints.admin.industryChecklistSection.collection,
      payload
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const bulkUpdateChecklistQuestionInSection = async (
  sectionId: string,
  questions: CheckListQuestion[]
): Promise<CheckListQuestion[]> => {
  try {
    const response = await apiClient.put(
      apiEndpoints.admin.industryChecklistSection.bulkSaveQuestions(sectionId),
      questions
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

// Questions
export const getAllCheckListQuestions = async (
  params?: CheckListParams
): Promise<CheckListQuestion[]> => {
  try {
    const response = await apiClient.get<PaginatedResponse<CheckListQuestion>>(
      apiEndpoints.admin.industryChecklistQuestion.collection,
      { params }
    );
    return response.data.content;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const createCheckListQuestion = async (
  payload: CheckListQuestionRequest
): Promise<CheckListQuestion> => {
  try {
    const response = await apiClient.post(
      apiEndpoints.admin.industryChecklistQuestion.collection,
      payload
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const updateCheckListQuestion = async (
  questionId: string,
  payload: CheckListQuestionRequest
): Promise<CheckListQuestion> => {
  try {
    const response = await apiClient.put(
      apiEndpoints.admin.industryChecklistQuestion.item(questionId),
      payload
    );
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const deleteCheckListQuestion = async (questionId: string): Promise<void> => {
  try {
    await apiClient.delete(apiEndpoints.admin.industryChecklistQuestion.item(questionId));
  } catch (error) {
    throw handleAxiosError(error);
  }
};
