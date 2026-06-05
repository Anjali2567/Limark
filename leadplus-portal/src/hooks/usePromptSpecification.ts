import {
  useMutation,
  UseMutationResult,
  useQuery,
  useQueryClient,
} from "@tanstack/react-query";
import { AxiosError } from "axios";

import { queryKeys } from "@/config/queryKeys";
import {
  getLastPromptSpecificationByType,
  updatePromptSpecification,
} from "@/lib/api/prompt-specification.api";
import {
  PromptSpecification,
  PromptSpecificationParams,
  PromptSpecificationRequest,
} from "@/types/prompt-specification.type";

export const useGetPromptSpecification = (
  params: PromptSpecificationParams
) => {
  return useQuery({
    queryKey: queryKeys.promptSpecification.byType(params),
    queryFn: () => getLastPromptSpecificationByType(params),
  });
};

export const useUpdatePromptSpecification = (): UseMutationResult<
  PromptSpecification,
  AxiosError,
  PromptSpecificationRequest
> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (payload) => updatePromptSpecification(payload),
    onSuccess: (data) => {
      queryClient.setQueryData(
        queryKeys.promptSpecification.byType(data),
        data
      );
    },
  });
};
