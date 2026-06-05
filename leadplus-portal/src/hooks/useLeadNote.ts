import { queryKeys } from '@/config/queryKeys';
import {
  createLeadNote,
  deleteLeadNote,
  getAllNotesForLead,
  getLeadNoteById,
  updateLeadNote,
} from '@/lib/api/lead-note.api';
import {
  CreateLeadNoteRequest,
  LeadNote,
  LeadNoteParams,
  LeadNoteSourceParams,
  UpdateLeadNoteRequest,
} from '@/types/lead-notes.types';
import { PaginatedResponse } from '@/types/paginated-params';
import { UserIdentity } from '@/types/user.types';
import {
  InfiniteData,
  useMutation,
  UseMutationResult,
  useInfiniteQuery,
  UseInfiniteQueryResult,
  useQuery,
  useQueryClient,
  UseQueryResult,
} from '@tanstack/react-query';
import { AxiosError } from 'axios';

export const useGetLeadNotesBySourceId = (
  params: LeadNoteSourceParams
): UseInfiniteQueryResult<InfiniteData<PaginatedResponse<LeadNote>>, AxiosError> => {
  return useInfiniteQuery({
    enabled: !!params.tenantId && !!params.workspaceId && !!params.sourceId,
    queryKey: queryKeys.leads.notes.all(params.tenantId, params.workspaceId, params.sourceId),
    queryFn: ({ pageParam = 0 }) =>
      getAllNotesForLead({ ...params, params: { ...params.params, page: pageParam } }),
    getNextPageParam: (lastPage) => {
      const { number, totalPages } = lastPage.page;
      return number + 1 < totalPages ? number + 1 : undefined;
    },
    initialPageParam: 0,
  });
};

export const useGetLeadNoteById = (
  params: LeadNoteParams
): UseQueryResult<LeadNote, AxiosError> => {
  return useQuery({
    enabled: !!params.tenantId && !!params.workspaceId && !!params.leadNoteId,
    queryKey: queryKeys.leads.notes.byId(params.tenantId, params.workspaceId, params.leadNoteId),
    queryFn: () => getLeadNoteById(params),
  });
};

export const useCreateLeadNote = (
  user: UserIdentity
): UseMutationResult<LeadNote, AxiosError, CreateLeadNoteRequest> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ params, payload }) => createLeadNote(params, payload),
    onSuccess: (data, { params }) => {
      const queryKey = queryKeys.leads.notes.all(
        params.tenantId,
        params.workspaceId,
        data.sourceId
      );
      queryClient.setQueryData<InfiniteData<PaginatedResponse<LeadNote>>>(queryKey, (old) => {
        if (!old) return old;
        const [firstPage, ...rest] = old.pages;
        return {
          ...old,
          pages: [
            {
              ...firstPage,
              content: [
                { ...data, createdBy: { id: user.id || '', name: user.name || '' } },
                ...firstPage.content,
              ],
            },
            ...rest,
          ],
        };
      });
    },
  });
};

export const useUpdateLeadNote = (
  user: UserIdentity
): UseMutationResult<LeadNote, AxiosError, UpdateLeadNoteRequest> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ params, note }) => updateLeadNote(params, note),
    onSuccess: (data, { params }) => {
      const queryKey = queryKeys.leads.notes.all(
        params.tenantId,
        params.workspaceId,
        data.sourceId
      );
      queryClient.setQueryData<InfiniteData<PaginatedResponse<LeadNote>>>(queryKey, (old) => {
        if (!old) return old;
        return {
          ...old,
          pages: old.pages.map((page) => ({
            ...page,
            content: page.content.map((n) =>
              n.id === data.id
                ? { ...data, createdBy: { id: user.id || '', name: user.name || '' } }
                : n
            ),
          })),
        };
      });
    },
  });
};

export const useDeleteLeadNote = (
  sourceId: string
): UseMutationResult<void, AxiosError, LeadNoteParams> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (params) => deleteLeadNote(params),
    onSuccess: (_, params) => {
      const queryKey = queryKeys.leads.notes.all(params.tenantId, params.workspaceId, sourceId);
      queryClient.setQueryData<InfiniteData<PaginatedResponse<LeadNote>>>(queryKey, (old) => {
        if (!old) return old;
        return {
          ...old,
          pages: old.pages.map((page) => ({
            ...page,
            content: page.content.filter((n) => n.id !== params.leadNoteId),
          })),
        };
      });
    },
  });
};
