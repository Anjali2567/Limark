import { queryKeys } from '@/config/queryKeys';
import { getQuestionnaireForIndustry } from '@/lib/api/questionnaire.api';
import { Questionnaire, QuestionnaireParams } from '@/types/questionnaire.types';
import { useQuery, UseQueryResult } from '@tanstack/react-query';
import { AxiosError } from 'axios';

export const useGetQuestionnaireIndustry = (
  params: QuestionnaireParams
): UseQueryResult<Questionnaire, AxiosError> => {
  return useQuery({
    enabled: params.industryIds.length > 0,
    queryKey: queryKeys.industries.checklist(params.industryIds),
    queryFn: () => getQuestionnaireForIndustry(params),
  });
};
