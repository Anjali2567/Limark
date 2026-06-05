import {
  useMutation,
  UseMutationResult,
  useQuery,
  useQueryClient,
  UseQueryResult,
} from '@tanstack/react-query';
import { AxiosError } from 'axios';

import { queryKeys } from '@/config/queryKeys';
import {
  addAttachmentLibrary,
  deleteAttachmentLibrary,
  getAttachmentLibraries,
  renameAttachmentLibrary,
  replaceAttachmentLibrary,
} from '@/lib/api/attachment.api';
import {
  AddAttachmentRequestParams,
  AddMultipleAttachmentRequestParams,
  Attachment,
  DeleteAttachmentParams,
  GetAttachmentParams,
  RenameAttachmentParams,
  ReplaceAttachmentParams,
} from '@/types/attachment.types';
import { PaginatedResponse } from '@/types/paginated-params';

export const useGetAllAttachments = (
  params: GetAttachmentParams
): UseQueryResult<PaginatedResponse<Attachment>, AxiosError> => {
  return useQuery<PaginatedResponse<Attachment>, AxiosError>({
    enabled: !!params.workspaceId,
    queryKey: queryKeys.attachments.search(params),
    queryFn: () => getAttachmentLibraries(params),
  });
};

export const useAddAttachment = (): UseMutationResult<
  Attachment,
  AxiosError,
  AddAttachmentRequestParams
> => {
  return useMutation<Attachment, AxiosError, AddAttachmentRequestParams>({
    mutationFn: (params: AddAttachmentRequestParams) => addAttachmentLibrary(params),
  });
};

export const useMultipleAddAttachment = (): UseMutationResult<
  Attachment[],
  AxiosError,
  AddMultipleAttachmentRequestParams
> => {
  const queryClient = useQueryClient();
  return useMutation<Attachment[], AxiosError, AddMultipleAttachmentRequestParams>({
    mutationFn: ({ tenantId, workspaceId, files }) =>
      Promise.all(
        files.map((file) =>
          addAttachmentLibrary({
            tenantId,
            workspaceId,
            file,
          })
        )
      ),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({
        refetchType: 'all',
        queryKey: queryKeys.attachments.all(variables),
      });
    },
  });
};

export const useRemoveAttachment = (): UseMutationResult<
  void,
  AxiosError,
  DeleteAttachmentParams
> => {
  const queryClient = useQueryClient();
  return useMutation<void, AxiosError, DeleteAttachmentParams>({
    mutationFn: (params: DeleteAttachmentParams) => deleteAttachmentLibrary(params),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({
        queryKey: queryKeys.attachments.all(variables),
        refetchType: 'all',
      });
    },
  });
};

export const useRenameAttachmentFile = (): UseMutationResult<
  Attachment,
  AxiosError,
  RenameAttachmentParams
> => {
  const queryClient = useQueryClient();
  return useMutation<Attachment, AxiosError, RenameAttachmentParams>({
    mutationFn: (params: RenameAttachmentParams) => renameAttachmentLibrary(params),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({
        queryKey: queryKeys.attachments.all(variables),
        refetchType: 'all',
      });
    },
  });
};

export const useReplaceAttachmentFile = (): UseMutationResult<
  Attachment,
  AxiosError,
  ReplaceAttachmentParams
> => {
  const queryClient = useQueryClient();
  return useMutation<Attachment, AxiosError, ReplaceAttachmentParams>({
    mutationFn: (params: ReplaceAttachmentParams) => replaceAttachmentLibrary(params),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({
        queryKey: queryKeys.attachments.all(variables),
        refetchType: 'all',
      });
    },
  });
};
