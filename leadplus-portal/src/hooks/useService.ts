import { queryKeys } from '@/config/queryKeys';
import { getAllServiceCategories, getAllServices } from '@/lib/api/services.api';
import { Service, ServiceCategory, ServiceParams } from '@/types/services.types';
import { useQuery, UseQueryResult } from '@tanstack/react-query';
import { AxiosError } from 'axios';

export const useGetAllServices = (params: ServiceParams): UseQueryResult<Service[], AxiosError> => {
  return useQuery({
    queryKey: queryKeys.services.search(params),
    queryFn: () => getAllServices(params),
  });
};

export const useGetAllServiceCategories = (): UseQueryResult<ServiceCategory[], AxiosError> => {
  return useQuery({
    queryKey: queryKeys.serviceCategories.all,
    queryFn: () => getAllServiceCategories(),
  });
};
