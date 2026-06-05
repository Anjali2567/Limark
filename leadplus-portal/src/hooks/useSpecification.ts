import { queryKeys } from '@/config/queryKeys';
import { getAllSpecifications } from '@/lib/api/specifications.api';
import { Specification, SpecificationParams } from '@/types/specifications.types';
import { useQuery, UseQueryResult } from '@tanstack/react-query';
import { AxiosError } from 'axios';

export const useGetAllSpecifications = (
  params: SpecificationParams
): UseQueryResult<Specification[], AxiosError> => {
  return useQuery({
    queryKey: queryKeys.specifications.search(params),
    queryFn: () => getAllSpecifications(params),
  });
};
