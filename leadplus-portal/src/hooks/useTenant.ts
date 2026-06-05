import { AxiosError } from 'axios';

import { queryKeys } from '@/config/queryKeys';
import {
  connectAnnouncementSmtp,
  connectHubSpotCRM,
  connectZohoCRM,
  createAnnouncement,
  deleteAnnouncement,
  disconnectHubSpotCRM,
  disconnectZohoCRM,
  getAllTenantAnnouncements,
  getAllTenantUsers,
  getAllTenantWorkspaces,
  getTenantDetails,
  getTenantModules,
  getTenantRecipients,
  importCrmContacts,
  importLeadContacts,
  launchAnnouncement,
  listAnnouncementContacts,
  listTenantContacts,
  removeAnnouncementContact,
  syncHubSpotRecords,
  syncZohoRecords,
  updateAnnouncement,
  updateTenantRecipients,
} from '@/lib/api/tenant.api';
import {
  CreateAnnouncementPayload,
  HubSpotIdentityAuthRequest,
  Tenant,
  TenantAnnouncement,
  TenantAnnouncementContact,
  TenantAnnouncementSearch,
  TenantAnnouncementSmtpRequest,
  TenantContact,
  TenantModules,
  TenantRecipientRequest,
  TenantRecipients,
  TenantRequest,
  TenantUserResponse,
  TenantUserSearch,
  TenantWorkspaceResponse,
  TenantWorkspaceSearch,
  UpdateAnnouncementPayload,
  ZohoIdentityAuthRequest,
} from '@/types/tenant.types';
import {
  useMutation,
  UseMutationResult,
  useQuery,
  useQueryClient,
  UseQueryResult,
} from '@tanstack/react-query';
import { PaginatedResponse, PaginationParams } from '@/types/paginated-params';
import { getRefreshToken } from '@/lib/auth/tokenStorage';
import { AuthToken } from '@/constants/Auth';

export const useGetTenantDetails = ({
  tenantId,
}: TenantRequest): UseQueryResult<Tenant, AxiosError> => {
  return useQuery({
    queryKey: [queryKeys.tenant.byId({ tenantId })],
    queryFn: () => getTenantDetails(tenantId),
    enabled: !!tenantId,
  });
};

export const useGetTenantRecipients = ({
  tenantId,
}: TenantRequest): UseQueryResult<TenantRecipients, AxiosError> => {
  return useQuery({
    queryKey: [queryKeys.tenant.recipients({ tenantId })],
    queryFn: () => getTenantRecipients(tenantId),
    enabled: !!tenantId,
  });
};

export const useConnectZohoCRM = (): UseMutationResult<
  Tenant,
  AxiosError,
  ZohoIdentityAuthRequest
> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ tenantId, payload }: ZohoIdentityAuthRequest) =>
      connectZohoCRM(tenantId, payload),
    onSuccess: (data, variables) => {
      queryClient.setQueryData<Tenant>([queryKeys.tenant.byId(variables)], data);
    },
  });
};

export const useDisconnectZohoCRM = (): UseMutationResult<Tenant, AxiosError, TenantRequest> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ tenantId }: TenantRequest) => disconnectZohoCRM(tenantId),
    onSuccess: (data, variables) => {
      queryClient.setQueryData<Tenant>([queryKeys.tenant.byId(variables)], data);
    },
  });
};

export const useGetAllTenantWorkspaces = ({
  tenantId,
  params,
}: TenantWorkspaceSearch): UseQueryResult<
  PaginatedResponse<TenantWorkspaceResponse>,
  AxiosError
> => {
  return useQuery({
    enabled: !!tenantId,
    queryKey: [queryKeys.tenant.workspaceSearch({ tenantId, params })],
    queryFn: () => getAllTenantWorkspaces({ tenantId, params }),
  });
};

export const useGetAllTenantUsers = ({
  tenantId,
  params,
}: TenantUserSearch): UseQueryResult<PaginatedResponse<TenantUserResponse>, AxiosError> => {
  return useQuery({
    enabled: !!tenantId,
    queryKey: [queryKeys.tenant.userSearch({ tenantId, params })],
    queryFn: () => getAllTenantUsers({ tenantId, params }),
  });
};

export const useConnectHubSpotCRM = (): UseMutationResult<
  Tenant,
  AxiosError,
  HubSpotIdentityAuthRequest
> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ tenantId, payload }: HubSpotIdentityAuthRequest) =>
      connectHubSpotCRM(tenantId, payload),
    onSuccess: (data, variables) => {
      queryClient.setQueryData<Tenant>([queryKeys.tenant.byId(variables)], data);
    },
  });
};

export const useDisconnectHubSpotCRM = (): UseMutationResult<Tenant, AxiosError, TenantRequest> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ tenantId }: TenantRequest) => disconnectHubSpotCRM(tenantId),
    onSuccess: (data, variables) => {
      queryClient.setQueryData<Tenant>([queryKeys.tenant.byId(variables)], data);
    },
  });
};

export const useSyncHubSpotRecords = (): UseMutationResult<
  void,
  AxiosError,
  { tenantId: string }
> => {
  return useMutation({
    mutationFn: syncHubSpotRecords,
  });
};

export const useZohoSyncRecords = (): UseMutationResult<void, AxiosError, { tenantId: string }> => {
  return useMutation({
    mutationFn: syncZohoRecords,
  });
};

export const useUpdateTenantRecipients = (): UseMutationResult<
  Tenant,
  AxiosError,
  TenantRecipientRequest
> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: updateTenantRecipients,
    onSuccess: (data, variables) => {
      queryClient.setQueryData<Tenant>([queryKeys.tenant.byId(variables)], data);
      queryClient.setQueryData<Tenant>(
        [queryKeys.tenant.recipients({ tenantId: variables.tenantId })],
        data
      );
    },
  });
};

export const useConnectAnnouncementSmtp = (): UseMutationResult<
  Tenant,
  AxiosError,
  TenantAnnouncementSmtpRequest
> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: connectAnnouncementSmtp,
    onSuccess: (data, variables) => {
      queryClient.setQueryData<Tenant>([queryKeys.tenant.byId(variables)], data);
    },
  });
};

export const useGetTenantModules = (
  tenantId?: string
): UseQueryResult<TenantModules, AxiosError> => {
  const hasToken = typeof window !== 'undefined' && getRefreshToken(AuthToken.REFRESH_TOKEN);
  const shouldFetch = hasToken ? !!tenantId : true;
  return useQuery({
    queryKey: queryKeys.tenant.modules(tenantId),
    queryFn: () => getTenantModules(tenantId),
    enabled: shouldFetch,
  });
};

export const useGetAllTenantAnnouncements = ({
  tenantId,
  params,
}: TenantAnnouncementSearch): UseQueryResult<PaginatedResponse<TenantAnnouncement>, AxiosError> => {
  return useQuery({
    queryKey: queryKeys.announcements.search({ tenantId, params }),
    queryFn: () => getAllTenantAnnouncements({ tenantId, params }),
    enabled: !!tenantId,
  });
};

export const useCreateAnnouncement = (): UseMutationResult<
  TenantAnnouncement,
  AxiosError,
  { tenantId: string; payload: CreateAnnouncementPayload }
> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ tenantId, payload }) => createAnnouncement(tenantId, payload),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({
        queryKey: queryKeys.announcements.all({ tenantId: variables.tenantId }),
      });
    },
  });
};

export const useUpdateAnnouncement = (): UseMutationResult<
  TenantAnnouncement,
  AxiosError,
  { tenantId: string; announcementId: string; payload: UpdateAnnouncementPayload }
> => {
  return useMutation({
    mutationFn: ({ tenantId, announcementId, payload }) =>
      updateAnnouncement(tenantId, announcementId, payload),
  });
};

export const useUpsertAnnouncement = (): UseMutationResult<
  TenantAnnouncement,
  AxiosError,
  { tenantId: string; announcementId: string | null; payload: CreateAnnouncementPayload }
> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ tenantId, announcementId, payload }) =>
      announcementId
        ? updateAnnouncement(tenantId, announcementId, payload)
        : createAnnouncement(tenantId, payload),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({
        queryKey: queryKeys.announcements.all({ tenantId: variables.tenantId }),
      });
    },
  });
};

export const useDeleteAnnouncement = (): UseMutationResult<
  void,
  AxiosError,
  { tenantId: string; announcementId: string }
> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ tenantId, announcementId }) => deleteAnnouncement(tenantId, announcementId),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({
        queryKey: queryKeys.announcements.all({ tenantId: variables.tenantId }),
      });
    },
  });
};

export const useLaunchAnnouncement = (): UseMutationResult<
  TenantAnnouncement,
  AxiosError,
  { tenantId: string; announcementId: string }
> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ tenantId, announcementId }) => launchAnnouncement(tenantId, announcementId),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({
        queryKey: queryKeys.announcements.all({ tenantId: variables.tenantId }),
      });
    },
  });
};

export const useListAnnouncementContacts = ({
  tenantId,
  announcementId,
  params,
  enabled = true,
}: {
  tenantId: string;
  announcementId: string;
  params?: PaginationParams;
  enabled?: boolean;
}): UseQueryResult<PaginatedResponse<TenantAnnouncementContact>, AxiosError> => {
  return useQuery({
    queryKey: queryKeys.announcements.contacts({ tenantId, announcementId }),
    queryFn: () => listAnnouncementContacts(tenantId, announcementId, params),
    enabled: !!tenantId && !!announcementId && enabled,
  });
};

export const useImportLeadContacts = (): UseMutationResult<
  void,
  AxiosError,
  { tenantId: string; announcementId: string; contactIds: string[] }
> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ tenantId, announcementId, contactIds }) =>
      importLeadContacts(tenantId, announcementId, contactIds),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({
        queryKey: queryKeys.announcements.contacts({
          tenantId: variables.tenantId,
          announcementId: variables.announcementId,
        }),
      });
    },
  });
};

export const useImportCrmContacts = (): UseMutationResult<
  void,
  AxiosError,
  { tenantId: string; announcementId: string; contactIds: string[] }
> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ tenantId, announcementId, contactIds }) =>
      importCrmContacts(tenantId, announcementId, contactIds),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({
        queryKey: queryKeys.announcements.contacts({
          tenantId: variables.tenantId,
          announcementId: variables.announcementId,
        }),
      });
    },
  });
};

export const useListTenantContacts = (
  tenantId: string,
  params: PaginationParams,
  enabled = true
): UseQueryResult<PaginatedResponse<TenantContact>, AxiosError> => {
  return useQuery({
    queryKey: queryKeys.tenant.contacts(tenantId, params),
    queryFn: () => listTenantContacts(tenantId, params),
    enabled: !!tenantId && enabled,
  });
};

export const useRemoveAnnouncementContact = (): UseMutationResult<
  void,
  AxiosError,
  { tenantId: string; announcementId: string; contactId: string }
> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ tenantId, announcementId, contactId }) =>
      removeAnnouncementContact(tenantId, announcementId, contactId),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({
        queryKey: queryKeys.announcements.contacts({
          tenantId: variables.tenantId,
          announcementId: variables.announcementId,
        }),
      });
    },
  });
};

export const useUpsertContactsToAnnouncement = (): UseMutationResult<
  void,
  AxiosError,
  {
    tenantId: string;
    announcementId: string;
    leadIdsToAdd: string[];
    crmContactIdsToAdd: string[];
    contactIdsToRemove: string[];
  }
> => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async ({
      tenantId,
      announcementId,
      leadIdsToAdd,
      crmContactIdsToAdd,
      contactIdsToRemove,
    }) => {
      await Promise.all([
        leadIdsToAdd.length > 0
          ? importLeadContacts(tenantId, announcementId, leadIdsToAdd)
          : Promise.resolve(),
        crmContactIdsToAdd.length > 0
          ? importCrmContacts(tenantId, announcementId, crmContactIdsToAdd)
          : Promise.resolve(),
        ...contactIdsToRemove.map((contactId) =>
          removeAnnouncementContact(tenantId, announcementId, contactId)
        ),
      ]);
    },
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({
        queryKey: queryKeys.announcements.contacts({
          tenantId: variables.tenantId,
          announcementId: variables.announcementId,
        }),
      });
    },
  });
};
