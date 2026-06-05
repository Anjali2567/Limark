import { queryKeys } from '@/config/queryKeys';
import {
  createRequestForQuote,
  deleteRequestForQuote,
  deleteRequestForQuoteFile,
  getAllRequestForQuotes,
  getRequestForQuoteById,
  updateRequestForQuote,
  uploadRequestForQuoteFile,
} from '@/lib/api/request-for-quote.api';
import { PaginatedResponse, PaginationParams } from '@/types/paginated-params';
import {
  RequestForQuote,
  RequestForQuoteFile,
  RequestForQuoteRequest,
  RequestForQuoteWithQuoteCount,
} from '@/types/request-for-quote.types';
import {
  useMutation,
  UseMutationResult,
  useQuery,
  useQueryClient,
  UseQueryResult,
} from '@tanstack/react-query';
import { AxiosError } from 'axios';

export const useGetAllRequestForQuotes = (
  params: PaginationParams
): UseQueryResult<PaginatedResponse<RequestForQuoteWithQuoteCount>, Error> => {
  return useQuery({
    queryKey: queryKeys.customer.requestForQuotes.search(params),
    queryFn: () => getAllRequestForQuotes(params),
  });
};

export const useGetRequestForQuoteById = (
  rfqId: string
): UseQueryResult<RequestForQuoteWithQuoteCount, Error> => {
  return useQuery({
    enabled: !!rfqId,
    queryKey: queryKeys.customer.requestForQuotes.byId(rfqId),
    queryFn: () => getRequestForQuoteById(rfqId),
  });
};

export const useDeleteRequestForQuote = (): UseMutationResult<void, AxiosError, string> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => deleteRequestForQuote(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.customer.requestForQuotes.all });
    },
  });
};

export const useUpsertRequestForQuote = (): UseMutationResult<
  RequestForQuote,
  AxiosError,
  RequestForQuoteRequest
> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async ({ id, payload, files }) => {
      let response: RequestForQuote;
      if (id) {
        response = await updateRequestForQuote(id, payload);
      } else {
        response = await createRequestForQuote(payload);
      }
      if (files && files.length > 0) {
        const uploadResults = await Promise.all(
          files.map((file) => uploadRequestForQuoteFile(response.id, file))
        );
        response = uploadResults[uploadResults.length - 1] ?? response;
      }
      return response;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.customer.requestForQuotes.all });
    },
  });
};

export const useDeleteRequestForQuoteAttachment = (): UseMutationResult<
  RequestForQuote,
  AxiosError,
  RequestForQuoteFile
> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (request: RequestForQuoteFile) => deleteRequestForQuoteFile(request),
    onSuccess: (_data, request) => {
      queryClient.setQueryData<RequestForQuoteWithQuoteCount>(
        queryKeys.customer.requestForQuotes.byId(request.requestForQuoteId),
        (oldData) => {
          if (!oldData) return undefined;
          return {
            ...oldData,
            attachments: oldData.attachments.filter((a) => a.id !== request.attachmentId),
          };
        }
      );
    },
  });
};
