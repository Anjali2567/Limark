import { AxiosError } from 'axios';

import { queryKeys } from '@/config/queryKeys';
import { unsubscribeContact } from '@/lib/api/unsubscribe.api';
import { useQuery, UseQueryResult } from '@tanstack/react-query';

export const useUnsubscribeContact = (token: string): UseQueryResult<string, AxiosError> => {
  return useQuery({
    enabled: !!token,
    queryKey: queryKeys.unsubscribe.all,
    queryFn: () => unsubscribeContact(token),
  });
};
