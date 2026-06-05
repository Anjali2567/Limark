import { LeadType } from '@/constants/leadSearch.constants';
import { QUERY_KEY } from '@/constants/queryKey.constants';
import { AgreementParams } from '@/types/agreements.types';
import { AttachmentSearchParams } from '@/types/attachment.types';
import { LeadContactEventSearchParams } from '@/types/lead-contact-event.types';
import { LeadQueryRequest } from '@/types/leadQueries.types';
import { LeadSearchParams } from '@/types/leads.types';
import {
  LeadCompanySearchPayload,
  LeadContactSearchPayload,
  LeadSearchFilterParams,
} from '@/types/leadSearch.types';
import { PaginationParams } from '@/types/paginated-params';
import { CheckListParams } from '@/types/question.types';
import { VendorQuotationsRequest } from '@/types/quotation.types';
import { ServiceParams } from '@/types/services.types';
import { SpecificationParams } from '@/types/specifications.types';
import { FeedbackSearchParams } from '@/types/user-feedback.types';
import { UserListingParams } from '@/types/user.types';
import { VendorAgreementParams } from '@/types/vendor-agreements.types';
import { VendorShowcaseBasicDetails, VendorShowcaseDetails } from '@/types/vendor-showcase.types';
import { VendorListingParams, VendorSearchParams } from '@/types/vendor.types';

export const queryKeys = {
  unsubscribe: {
    // [unsubscribe]
    all: [QUERY_KEY.UNSUBSCRIBE],
  },
  user: {
    // [authenticatedUser]
    authenticatedUser: [QUERY_KEY.AUTHENTICATED_USER],
    // [authenticatedUser, vendor]
    authenticatedVendor: [QUERY_KEY.AUTHENTICATED_USER, QUERY_KEY.AUTHENTICATED_VENDOR],
    // [authenticatedUser, workspaces]
    authenticatedUserWorkspaces: [QUERY_KEY.AUTHENTICATED_USER, QUERY_KEY.WORKSPACES],
  },
  industries: {
    // [industries]
    all: [QUERY_KEY.INDUSTRIES],
    // [industries, search, params]
    search: (params: PaginationParams) => [QUERY_KEY.INDUSTRIES, params],
    // [industries, id]
    byId: (industryId: string) => [QUERY_KEY.INDUSTRIES, industryId],
    // [industries, ids, checklist]
    checklist: (industryIds: string[]) => [
      QUERY_KEY.INDUSTRIES,
      industryIds,
      QUERY_KEY.INDUSTRY_CHECKLIST,
    ],
  },
  feedbacks: {
    // [feedbacks]
    all: [QUERY_KEY.FEEDBACKS],
    // [feedbacks, params]
    search: (params: FeedbackSearchParams) => [QUERY_KEY.FEEDBACKS, params],
    // [feedbacks, id]
    byId: (feedbackId: string) => [QUERY_KEY.FEEDBACKS, feedbackId],
  },
  services: {
    // [services]
    all: [QUERY_KEY.SERVICES],
    // [services, params]
    search: (params: ServiceParams) => [QUERY_KEY.SERVICES, params],
  },
  serviceCategories: {
    // [service-categories]
    all: [QUERY_KEY.SERVICE_CATEGORIES],
  },
  specifications: {
    // [specifications]
    all: [QUERY_KEY.SPECIFICATIONS],
    // [specifications, params]
    search: (params: SpecificationParams) => [QUERY_KEY.SPECIFICATIONS, params],
  },
  agreements: {
    // [agreement]
    all: [QUERY_KEY.AGREEMENT],
    // [agreement, search, params]
    search: (params: AgreementParams) => [QUERY_KEY.AGREEMENT, params],
    // [agreement, id]
    byId: (agreementId: string) => [QUERY_KEY.AGREEMENT, agreementId],
  },
  leads: {
    // [leads, params]
    all: (params: LeadSearchParams) => [QUERY_KEY.LEADS, params],
    // [leads, tenantId, statistics]
    statistics: (tenantId: string) => [QUERY_KEY.LEADS, tenantId, QUERY_KEY.STATISTICS],
    // [leads, industries]
    industries: [QUERY_KEY.LEADS, 'industries'],
    // [leads, counts]
    counts: [QUERY_KEY.LEADS, 'counts'],
    // [leads, companies, companyId, jobs]
    companyJobs: (companyId: string) => [
      QUERY_KEY.LEADS,
      QUERY_KEY.LEADS_COMPANIES,
      companyId,
      QUERY_KEY.JOBS,
    ],
    // [leads, contactsByCompanyId]
    contactsByCompanyId: (companyId: string) => [
      QUERY_KEY.LEADS,
      companyId,
      QUERY_KEY.LEADS_CONTACTS,
    ],
    // [leads, contacts]
    contacts: (params: PaginationParams) => [QUERY_KEY.LEADS, QUERY_KEY.CONTACTS, params],
    contactEvents: {
      // [leads, contacts, contactId, events]
      collection: (tenantId: string, contactId: string, params?: LeadContactEventSearchParams) => [
        QUERY_KEY.TENANT,
        tenantId,
        QUERY_KEY.LEADS_CONTACTS,
        contactId,
        QUERY_KEY.EVENTS,
        params,
      ],
      // [leads, contacts, contactId, events, counts]
      counts: (tenantId: string, contactId: string) => [
        QUERY_KEY.TENANT,
        tenantId,
        QUERY_KEY.LEADS_CONTACTS,
        contactId,
        QUERY_KEY.EVENTS,
        QUERY_KEY.COUNTS,
      ],
    },
    // [leads, queries, type, params]
    queries: (params: LeadQueryRequest) => [
      QUERY_KEY.LEADS,
      QUERY_KEY.QUERIES,
      params.types,
      params,
    ],
    // [leads, searchLeads, payload, params]
    companyLookup: (tenantId: string, query: string) => [
      QUERY_KEY.LEADS,
      QUERY_KEY.LEADS_COMPANIES,
      'lookup',
      tenantId,
      query,
    ],
    searchContactLeads: (
      params: { payload: LeadContactSearchPayload; params: PaginationParams } | null
    ) => [QUERY_KEY.LEADS, QUERY_KEY.LEADS_CONTACTS, params?.payload, params?.params],
    // [leads, searchLeads, payload, params]
    searchCompanyLeads: (
      params: { payload: LeadCompanySearchPayload; params: PaginationParams } | null
    ) => [QUERY_KEY.LEADS, QUERY_KEY.LEADS_COMPANIES, params?.payload, params?.params],
    // [tenant, tenantId, filters, type]
    savedSearchFilters: (params: LeadSearchFilterParams) => [
      QUERY_KEY.TENANT,
      params.tenantId,
      QUERY_KEY.FILTERS,
      params.type,
    ],
    // [tenant, tenantId, company, companyId]
    companyInfo: (tenantId: string, companyId: string) => [
      QUERY_KEY.TENANT,
      tenantId,
      QUERY_KEY.LEADS_COMPANIES,
      companyId,
    ],
    // [tenant, tenantId, leadContacts, contactId]
    contactInfo: (tenantId: string, contactId: string) => [
      QUERY_KEY.TENANT,
      tenantId,
      QUERY_KEY.LEADS_CONTACTS,
      contactId,
    ],
    // [tenant, tenantId, company, companyId, contacts]
    companyContacts: (tenantId: string, companyId: string, params: PaginationParams) => [
      QUERY_KEY.TENANT,
      tenantId,
      QUERY_KEY.LEADS_COMPANIES,
      companyId,
      QUERY_KEY.LEADS_CONTACTS,
      params,
    ],
    // [tenant, tenantId, leads, metadataFilters]
    metadataFilters: (tenantId: string) => [
      QUERY_KEY.TENANT,
      tenantId,
      QUERY_KEY.LEADS,
      'metadataFilters',
    ],
    // [tenant, tenantId, leads, chat, conversationId, messages]
    chatMessages: (tenantId: string, conversationId?: number) => [
      QUERY_KEY.TENANT,
      tenantId,
      QUERY_KEY.LEADS,
      'chat',
      conversationId,
      QUERY_KEY.MESSAGES,
    ],
    leadList: {
      // [tenant, tenantId, workspace, workspaceId, leadLists]
      all: (tenantId: string, workspaceId: string) => [
        QUERY_KEY.TENANT,
        tenantId,
        QUERY_KEY.WORKSPACE,
        workspaceId,
        QUERY_KEY.LEAD_LISTS,
      ],
      // [tenant, tenantId, workspace, workspaceId, leadLists, type]
      allByType: (tenantId: string, workspaceId: string, type: LeadType) => [
        QUERY_KEY.TENANT,
        tenantId,
        QUERY_KEY.WORKSPACE,
        workspaceId,
        QUERY_KEY.LEAD_LISTS,
        type,
      ],
      // [tenant, tenantId, workspace, workspaceId, leadLists, type, params]
      search: (tenantId: string, workspaceId: string, type: LeadType, params: PaginationParams) => [
        QUERY_KEY.TENANT,
        tenantId,
        QUERY_KEY.WORKSPACE,
        workspaceId,
        QUERY_KEY.LEAD_LISTS,
        type,
        params,
      ],
      // [tenant, tenantId, workspace, workspaceId, leadLists, listId]
      byId: (tenantId: string, workspaceId: string, listId: string) => [
        QUERY_KEY.TENANT,
        tenantId,
        QUERY_KEY.WORKSPACE,
        workspaceId,
        QUERY_KEY.LEAD_LISTS,
        listId,
      ],
      // [tenant, tenantId, workspace, workspaceId, leadLists, listId, contacts]
      contacts: (
        tenantId: string,
        workspaceId: string,
        listId: string,
        params: PaginationParams
      ) => [
        QUERY_KEY.TENANT,
        tenantId,
        QUERY_KEY.WORKSPACE,
        workspaceId,
        QUERY_KEY.LEAD_LISTS,
        listId,
        QUERY_KEY.CONTACTS,
        params,
      ],
      // [tenant, tenantId, workspace, workspaceId, leadLists, listId, companies]
      companies: (
        tenantId: string,
        workspaceId: string,
        listId: string,
        params: PaginationParams
      ) => [
        QUERY_KEY.TENANT,
        tenantId,
        QUERY_KEY.WORKSPACE,
        workspaceId,
        QUERY_KEY.LEAD_LISTS,
        listId,
        QUERY_KEY.LEADS_COMPANIES,
        params,
      ],
    },
    notes: {
      // [tenant, tenantId, workspace, workspaceId, leadNotes, source, sourceId]
      all: (tenantId: string, workspaceId: string, sourceId: string) => [
        QUERY_KEY.TENANT,
        tenantId,
        QUERY_KEY.WORKSPACE,
        workspaceId,
        QUERY_KEY.LEAD_NOTES,
        QUERY_KEY.SOURCE,
        sourceId,
      ],
      // [tenant, tenantId, workspace, workspaceId, leadNotes,noteId]
      byId: (tenantId: string, workspaceId: string, noteId: string) => [
        QUERY_KEY.TENANT,
        tenantId,
        QUERY_KEY.WORKSPACE,
        workspaceId,
        QUERY_KEY.LEAD_NOTES,
        noteId,
      ],
    },
  },
  tenant: {
    // [tenant, tenantId]
    byId: (params: { tenantId: string }) => [QUERY_KEY.TENANT, params.tenantId],
    // [tenant, modules, tenantDomain]
    modules: (tenantId?: string) => [QUERY_KEY.TENANT, QUERY_KEY.MODULES, tenantId],
    // [tenant, tenantId, recipients]
    recipients: (params: { tenantId: string }) => [
      QUERY_KEY.TENANT,
      params.tenantId,
      QUERY_KEY.RECIPIENT,
    ],
    // [tenant, tenantId, workspaces]
    workspaces: (params: { tenantId: string }) => [
      QUERY_KEY.TENANT,
      params.tenantId,
      QUERY_KEY.WORKSPACES,
    ],
    // [tenant, tenantId, workspaces, params]
    workspaceSearch: (params: { tenantId: string; params: PaginationParams }) => [
      QUERY_KEY.TENANT,
      params.tenantId,
      QUERY_KEY.WORKSPACES,
      params.params,
    ],
    // [tenant, tenantId, users]
    users: (params: { tenantId: string }) => [QUERY_KEY.TENANT, params.tenantId, QUERY_KEY.USERS],
    // [tenant, tenantId, users, params]
    userSearch: (params: { tenantId: string; params: PaginationParams }) => [
      QUERY_KEY.TENANT,
      params.tenantId,
      QUERY_KEY.USERS,
      params.params,
    ],
    leadFilters: {
      // [tenant, tenantId, lead-filters]
      all: (tenantId: string) => [QUERY_KEY.TENANT, tenantId, QUERY_KEY.LEAD_FILTERS],
    },
    // [tenant, tenantId, tenant-contacts, params]
    contacts: (tenantId: string, params: PaginationParams) => [
      QUERY_KEY.TENANT,
      tenantId,
      QUERY_KEY.TENANT_CONTACTS,
      params,
    ],
  },
  workspace: {
    // [tenant, tenantId, workspace, workspaceId]
    byId: (params: { tenantId: string; workspaceId: string }) => [
      QUERY_KEY.TENANT,
      params.tenantId,
      QUERY_KEY.WORKSPACE,
      params.workspaceId,
    ],
    users: (params: { tenantId: string; workspaceId: string }) => [
      QUERY_KEY.TENANT,
      params.tenantId,
      QUERY_KEY.WORKSPACE,
      params.workspaceId,
      QUERY_KEY.USERS,
    ],
    search: (params: {
      tenantId: string;
      workspaceId: string;
      paginationParams?: PaginationParams;
    }) => [
      QUERY_KEY.TENANT,
      params.tenantId,
      QUERY_KEY.WORKSPACE,
      params.workspaceId,
      QUERY_KEY.USERS,
      params.paginationParams,
    ],
  },
  vendor: {
    showcase: {
      all: (params: VendorShowcaseBasicDetails) => [
        QUERY_KEY.TENANT,
        params.tenantId,
        QUERY_KEY.VENDORS,
        params.vendorId,
      ],
      byId: (params: VendorShowcaseDetails) => [
        QUERY_KEY.TENANT,
        params.tenantId,
        QUERY_KEY.VENDORS,
        params.vendorId,
        QUERY_KEY.SHOWCASE,
        params.id,
      ],
    },
    agreement: {
      byType: (params: VendorAgreementParams) => [
        QUERY_KEY.VENDORS,
        params.vendorId,
        QUERY_KEY.AGREEMENT,
        params.agreementType,
      ],
    },
    rfq: {
      // [quotations, requestForQuotes, vendors]
      all: [QUERY_KEY.QUOTATIONS, QUERY_KEY.REQUEST_FOR_QUOTES, QUERY_KEY.VENDORS],
      // [quotations, requestForQuotes, vendors, params]
      quotations: (params: VendorQuotationsRequest) => [
        QUERY_KEY.QUOTATIONS,
        QUERY_KEY.REQUEST_FOR_QUOTES,
        QUERY_KEY.VENDORS,
        params,
      ],
      // [quotation, requestForQuotes, vendors, id]
      quotationById: (quotationId: string) => [
        QUERY_KEY.QUOTATIONS,
        QUERY_KEY.REQUEST_FOR_QUOTES,
        QUERY_KEY.VENDORS,
        quotationId,
      ],
    },
  },
  vendors: {
    // [vendors, search, params]
    search: (params: VendorSearchParams) => [QUERY_KEY.VENDORS, QUERY_KEY.SEARCH, params],
    // [vendors, vendorId]
    byId: (vendorId: string) => [QUERY_KEY.VENDORS, vendorId],
  },
  admin: {
    tenants: {
      // [admin, tenants, params]
      search: (params: PaginationParams) => [QUERY_KEY.ADMIN, QUERY_KEY.TENANTS, params],
      // [admin, tenants, tenantId, analytics]
      analytics: (tenantId: string) => [
        QUERY_KEY.ADMIN,
        QUERY_KEY.TENANTS,
        tenantId,
        QUERY_KEY.ANALYTICS,
      ],
      // [admin, tenants, tenantId, users, params]
      users: (tenantId: string, params: PaginationParams) => [
        QUERY_KEY.ADMIN,
        QUERY_KEY.TENANTS,
        tenantId,
        QUERY_KEY.USERS,
        params,
      ],
      // [admin, tenants, tenantId, analytics, tenant-activity]
      tenantActivityByTenantId: (tenantId: string) => [
        QUERY_KEY.ADMIN,
        QUERY_KEY.TENANTS,
        tenantId,
        QUERY_KEY.ANALYTICS,
        QUERY_KEY.TENANT_ACTIVITY,
      ],
    },
    users: {
      // [admin, users]
      all: [QUERY_KEY.ADMIN, QUERY_KEY.USERS],
      // [admin, users, userId]
      byId: (userId: string) => [QUERY_KEY.ADMIN, QUERY_KEY.USERS, userId],
      // [admin, users, params]
      search: (params: UserListingParams) => [QUERY_KEY.ADMIN, QUERY_KEY.USERS, params],
    },
    vendors: {
      // [admin, vendors]
      all: [QUERY_KEY.ADMIN, QUERY_KEY.VENDORS],
      // [admin, vendors, vendorId]
      byId: (vendorId: string) => [QUERY_KEY.ADMIN, QUERY_KEY.VENDORS, vendorId],
      // [admin, vendors, params]
      search: (params: VendorListingParams) => [QUERY_KEY.ADMIN, QUERY_KEY.VENDORS, params],
      // [admin, vendors, vendorId, showcases]
      showcase: (vendorId: string) => [
        QUERY_KEY.ADMIN,
        QUERY_KEY.VENDORS,
        vendorId,
        QUERY_KEY.SHOWCASE,
      ],
    },
    industries: {
      section: {
        // [admin, industry-checklist]
        all: [QUERY_KEY.ADMIN, QUERY_KEY.INDUSTRY_CHECKLIST],
      },
      questions: {
        // [admin, checklist-questions]
        all: [QUERY_KEY.ADMIN, QUERY_KEY.INDUSTRY_CHECKLIST_QUESTIONS],
        // [admin, checklist-questions, params]
        search: (params: CheckListParams) => [
          QUERY_KEY.ADMIN,
          QUERY_KEY.INDUSTRY_CHECKLIST_QUESTIONS,
          params,
        ],
      },
    },
  },
  announcements: {
    // [tenant, tenantId, announcements]
    all: (params: { tenantId: string }) => [
      QUERY_KEY.TENANT,
      params.tenantId,
      QUERY_KEY.ANNOUNCEMENTS,
    ],
    // [tenant, tenantId, announcements, params]
    search: (params: { tenantId: string; params: PaginationParams }) => [
      QUERY_KEY.TENANT,
      params.tenantId,
      QUERY_KEY.ANNOUNCEMENTS,
      params.params,
    ],
    // [tenant, tenantId, announcements, announcementId]
    byId: (params: { tenantId: string; announcementId: string }) => [
      QUERY_KEY.TENANT,
      params.tenantId,
      QUERY_KEY.ANNOUNCEMENTS,
      params.announcementId,
    ],
    // [tenant, tenantId, announcements, announcementId, contacts]
    contacts: (params: { tenantId: string; announcementId: string }) => [
      QUERY_KEY.TENANT,
      params.tenantId,
      QUERY_KEY.ANNOUNCEMENTS,
      params.announcementId,
      QUERY_KEY.CONTACTS,
    ],
  },
  emailSequenceTemplates: {
    // [tenant, tenantId, email-sequence-templates]
    all: (tenantId: string) => [QUERY_KEY.TENANT, tenantId, QUERY_KEY.EMAIL_SEQUENCE_TEMPLATES],
    // [tenant, tenantId, email-sequence-templates, templateId]
    byId: (tenantId: string, templateId: string) => [
      QUERY_KEY.TENANT,
      tenantId,
      QUERY_KEY.EMAIL_SEQUENCE_TEMPLATES,
      templateId,
    ],
  },
  promptSpecification: {
    // [promptSpecification, type]
    byType: (params: { type: string }) => [QUERY_KEY.PROMPT_SPECIFICATION, params.type],
  },
  apolloSpecification: {
    // [apolloSpecification, latest]
    latest: [QUERY_KEY.APOLLO_SPECIFICATION, 'latest'],
  },

  campaigns: {
    agent: {
      // [tenant, tenantId, workspace, workspaceId, campaigns, chatMemoryId]
      details: (params: { tenantId: string; workspaceId: string; chatMemoryId: string }) => [
        QUERY_KEY.TENANT,
        params.tenantId,
        QUERY_KEY.WORKSPACE,
        params.workspaceId,
        QUERY_KEY.CAMPAIGNS,
        params.chatMemoryId,
      ],
      // [tenant, tenantId, workspace, workspaceId, messages]
      conversations: (params: { tenantId: string; workspaceId: string }) => [
        QUERY_KEY.TENANT,
        params.tenantId,
        QUERY_KEY.WORKSPACE,
        params.workspaceId,
        QUERY_KEY.MESSAGES,
      ],
      // [tenant, tenantId, workspace, workspaceId, messages, messageId]
      conversationsById: (params: {
        tenantId: string;
        workspaceId: string;
        conversationId: string;
      }) => [
        QUERY_KEY.TENANT,
        params.tenantId,
        QUERY_KEY.WORKSPACE,
        params.workspaceId,
        QUERY_KEY.MESSAGES,
        params.conversationId,
      ],
    },
    // [tenant, tenantId, workspace, workspaceId, campaigns]
    all: (params: { tenantId: string; workspaceId: string }) => [
      QUERY_KEY.TENANT,
      params.tenantId,
      QUERY_KEY.WORKSPACE,
      params.workspaceId,
      QUERY_KEY.CAMPAIGNS,
    ],
    // [tenant, tenantId, workspace, workspaceId, campaigns, query]
    search: (params: { tenantId: string; workspaceId: string; query?: PaginationParams }) => [
      QUERY_KEY.TENANT,
      params.tenantId,
      QUERY_KEY.WORKSPACE,
      params.workspaceId,
      QUERY_KEY.CAMPAIGNS,
      params.query,
    ],
    // [tenant, tenantId, workspace, workspaceId, campaigns, campaignId]
    byId: (params: { tenantId: string; workspaceId: string; campaignId: string }) => [
      QUERY_KEY.TENANT,
      params.tenantId,
      QUERY_KEY.WORKSPACE,
      params.workspaceId,
      QUERY_KEY.CAMPAIGNS,
      params.campaignId,
    ],
    // [tenant, tenantId, workspace, workspaceId, campaigns, campaignId, basicDetails]
    basicDetails: (params: { tenantId: string; workspaceId: string; campaignId: string }) => [
      QUERY_KEY.TENANT,
      params.tenantId,
      QUERY_KEY.WORKSPACE,
      params.workspaceId,
      QUERY_KEY.CAMPAIGNS,
      params.campaignId,
      QUERY_KEY.BASIC_DETAILS,
    ],
    // [tenant, tenantId, workspace, workspaceId, campaigns, analytics]
    analytics: (params: { tenantId: string; workspaceId: string }) => [
      QUERY_KEY.TENANT,
      params.tenantId,
      QUERY_KEY.WORKSPACE,
      params.workspaceId,
      QUERY_KEY.CAMPAIGNS,
      QUERY_KEY.ANALYTICS,
    ],
    // [tenant, tenantId, workspace, workspaceId, campaigns, campaignId, analytics]
    analyticsById: (params: { tenantId: string; workspaceId: string; campaignId: string }) => [
      QUERY_KEY.TENANT,
      params.tenantId,
      QUERY_KEY.WORKSPACE,
      params.workspaceId,
      QUERY_KEY.CAMPAIGNS,
      params.campaignId,
      QUERY_KEY.ANALYTICS,
    ],
    emailsById: (params: { tenantId: string; workspaceId: string; campaignId: string }) => [
      QUERY_KEY.TENANT,
      params.tenantId,
      QUERY_KEY.WORKSPACE,
      params.workspaceId,
      QUERY_KEY.CAMPAIGNS,
      params.campaignId,
      QUERY_KEY.EMAILS,
    ],
  },
  mailboxes: {
    // [tenant, tenantId, workspace, workspaceId, mailboxes]
    all: (params: { tenantId: string; workspaceId: string }) => [
      QUERY_KEY.TENANT,
      params.tenantId,
      QUERY_KEY.WORKSPACE,
      params.workspaceId,
      QUERY_KEY.MAILBOXES,
    ],
    sesCheck: (params: { tenantId: string; workspaceId: string }) => [
      QUERY_KEY.TENANT,
      params.tenantId,
      QUERY_KEY.WORKSPACE,
      params.workspaceId,
      QUERY_KEY.MAILBOXES,
      QUERY_KEY.SES_CHECK,
    ],
    authorized: (params: { tenantId: string; workspaceId: string }) => [
      QUERY_KEY.TENANT,
      params.tenantId,
      QUERY_KEY.WORKSPACE,
      params.workspaceId,
      QUERY_KEY.MAILBOXES,
      QUERY_KEY.AUTHORIZED,
    ],
  },
  attachments: {
    // [tenant, tenantId, workspace, workspaceId, attachments]
    all: (params: { tenantId: string; workspaceId: string }) => [
      QUERY_KEY.TENANT,
      params.tenantId,
      QUERY_KEY.WORKSPACE,
      params.workspaceId,
      QUERY_KEY.ATTACHMENTS,
    ],
    // [tenant, tenantId, workspace, workspaceId, attachments, params]
    search: (params: {
      tenantId: string;
      workspaceId: string;
      params?: AttachmentSearchParams;
    }) => [
      QUERY_KEY.TENANT,
      params.tenantId,
      QUERY_KEY.WORKSPACE,
      params.workspaceId,
      QUERY_KEY.ATTACHMENTS,
      params.params,
    ],
  },
  customer: {
    requestForQuotes: {
      // [requestForQuotes]
      all: [QUERY_KEY.REQUEST_FOR_QUOTES],
      // [requestForQuotes, params]
      search: (params: PaginationParams) => [
        QUERY_KEY.REQUEST_FOR_QUOTES,
        QUERY_KEY.SEARCH,
        params,
      ],
      // [requestForQuotes, id]
      byId: (rfqId: string) => [QUERY_KEY.REQUEST_FOR_QUOTES, rfqId],
    },
    quotations: {
      // [customer, quotations, byRfqId, rfqId, params]
      byRfqId: (rfqId: string, params: PaginationParams) => [
        QUERY_KEY.CUSTOMER,
        QUERY_KEY.QUOTATIONS,
        QUERY_KEY.REQUEST_FOR_QUOTES,
        rfqId,
        params,
      ],
    },
  },
};
