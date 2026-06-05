import { AxiosError } from "axios";

import { queryKeys } from "@/config/queryKeys";
import {
  getLatestApolloSpecification,
  updateApolloSpecification,
} from "@/lib/api/apollo-specification.api";
import {
  ApolloSpecification,
  ApolloSpecificationRequest,
} from "@/types/apollo-specification.types";
import {
  useMutation,
  UseMutationResult,
  useQuery,
  useQueryClient,
} from "@tanstack/react-query";

export const useGetLatestApolloSpecification = () => {
  return useQuery({
    queryKey: queryKeys.apolloSpecification.latest,
    queryFn: () => getLatestApolloSpecification(),
  });
};

export const useUpdateApolloSpecification = (): UseMutationResult<
  ApolloSpecification,
  AxiosError,
  ApolloSpecificationRequest
> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (payload) => updateApolloSpecification(payload),
    onSuccess: (data) => {
      queryClient.setQueryData(queryKeys.apolloSpecification.latest, data);
    },
  });
};
