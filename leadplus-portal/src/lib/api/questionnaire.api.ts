import { Questionnaire, QuestionnaireParams } from '@/types/questionnaire.types';
import { handleAxiosError } from '../utils/apiErrorHandler';
import apiClient from './client';
import { apiEndpoints } from '@/config/endpoints';

export const getQuestionnaireForIndustry = async ({ industryIds }: QuestionnaireParams) => {
  try {
    const response = await apiClient.get<Questionnaire>(apiEndpoints.industries.questionnaires, {
      params: { industryIds },
    });
    return response.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};
