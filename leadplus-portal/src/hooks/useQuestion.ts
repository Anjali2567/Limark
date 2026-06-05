import { queryKeys } from '@/config/queryKeys';
import {
  bulkUpdateChecklistQuestionInSection,
  bulkUpdateChecklistSections,
  createCheckListQuestion,
  createChecklistSection,
  deleteCheckListQuestion,
  getAllCheckListQuestions,
  getAllChecklistSections,
  updateCheckListQuestion,
  updateChecklistSection,
} from '@/lib/api/industry-checklist.api';
import {
  CheckListParams,
  CheckListQuestion,
  CheckListSection,
  UpsertCheckListSectionRequest,
  UpsertRFQCheckListQuestionRequest,
} from '@/types/question.types';
import {
  useMutation,
  UseMutationResult,
  useQuery,
  useQueryClient,
  UseQueryResult,
} from '@tanstack/react-query';
import { AxiosError } from 'axios';

// Sections

export const useGetAllChecklistSections = (): UseQueryResult<CheckListSection[], Error> => {
  return useQuery({
    queryKey: queryKeys.admin.industries.section.all,
    queryFn: getAllChecklistSections,
  });
};

export const useUpsertCheckListSection = (): UseMutationResult<
  CheckListSection,
  Error,
  UpsertCheckListSectionRequest
> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ sectionId, payload }) => {
      if (sectionId) {
        return updateChecklistSection(sectionId, payload);
      } else {
        return createChecklistSection(payload);
      }
    },
    onSuccess: (data, variables) => {
      queryClient.setQueryData<CheckListSection[]>(
        queryKeys.admin.industries.section.all,
        (oldData) => {
          if (!oldData) return [data];
          if (variables.sectionId) {
            return oldData.map((section) => (section.id === variables.sectionId ? data : section));
          }
          return [...oldData, data];
        }
      );
    },
  });
};

export const useBulkUpdateCheckListSections = (): UseMutationResult<
  CheckListSection[],
  Error,
  CheckListSection[]
> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (payload) => {
      return bulkUpdateChecklistSections(payload);
    },
    onSuccess: (data) => {
      queryClient.setQueryData<CheckListSection[]>(
        queryKeys.admin.industries.section.all,
        (oldData) => {
          if (!oldData) return data;
          return oldData.map((section) => {
            const updatedSection = data.find((updated) => updated.id === section.id);
            return updatedSection || section;
          });
        }
      );
    },
  });
};

// Questions

export const useGetAllCheckListQuestions = (
  params?: CheckListParams
): UseQueryResult<CheckListQuestion[], AxiosError> => {
  return useQuery({
    queryKey: queryKeys.admin.industries.questions.all,
    queryFn: () => getAllCheckListQuestions(params),
  });
};

export const useUpsertCheckListQuestion = (): UseMutationResult<
  CheckListQuestion,
  AxiosError,
  UpsertRFQCheckListQuestionRequest
> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ questionId, payload }) => {
      if (questionId) {
        return updateCheckListQuestion(questionId, payload);
      }
      return createCheckListQuestion(payload);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.industries.questions.all });
    },
  });
};

export const useDeleteCheckListQuestion = (): UseMutationResult<void, AxiosError, string> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (questionId) => {
      return deleteCheckListQuestion(questionId);
    },
    onSuccess: (_, variables) => {
      queryClient.setQueryData(
        queryKeys.admin.industries.questions.all,
        (oldData: CheckListQuestion[] | undefined) => {
          if (!oldData) return [];
          return oldData.filter((question) => question.id !== variables);
        }
      );
    },
  });
};

export const useBulkUpdateCheckListQuestionsInSection = (): UseMutationResult<
  CheckListQuestion[],
  Error,
  { sectionId: string; questions: CheckListQuestion[] }
> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ sectionId, questions }) =>
      bulkUpdateChecklistQuestionInSection(sectionId, questions),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.admin.industries.questions.all });
    },
  });
};
