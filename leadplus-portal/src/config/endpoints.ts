const tenant = (tenantId: string, workspaceId: string) =>
  `/v1/tenants/${tenantId}/workspaces/${workspaceId}`;

const campaignById = (tenantId: string, workspaceId: string, campaignId: string) =>
  `${tenant(tenantId, workspaceId)}/campaigns/${campaignId}`;

export const apiEndpoints = {
  unsubscribe: '/v1/unsubscribe',
  auth: {
    login: '/v1/auth/login',
    signup: '/v1/auth/sign-up',
    refresh: '/v1/auth/refresh',
    me: '/v1/users/me',
    vendor: '/v1/users/me/vendor',
    vendorLogo: '/v1/users/me/vendor/logo',
    workspaces: '/v1/users/me/workspaces',
    forgotPassword: '/v1/auth/forgot-password',
    forgotPasswordRequest: '/v1/auth/forgot-password/request',
    google: '/v1/auth/google',
    verifyEmail: '/v1/auth/verify-email',
    resendEmail: '/v1/users/me/resend-email',
    vendorSubmit: '/v1/users/me/vendor/pending',
  },
  emailImage: {
    collection: '/v1/email-images',
  },
  promptSpecification: {
    get: '/v1/prompt-specifications',
    update: '/v1/prompt-specifications/update',
  },
  apolloSpecification: {
    collection: '/v1/apollo-specification',
  },
  services: {
    collection: '/v1/services',
  },
  serviceCategories: {
    collection: '/v1/service-categories',
  },
  specificationCategories: {
    collection: '/v1/specification-categories',
  },
  specifications: {
    collection: '/v1/specifications',
  },
  admin: {
    users: {
      collection: '/v1/admin/users',
      item: (userId: string) => `/v1/admin/users/${userId}`,
      approve: (userId: string) => `/v1/admin/users/${userId}/approve`,
      reject: (userId: string) => `/v1/admin/users/${userId}/reject`,
    },
    vendor: {
      collection: '/v1/admin/vendors',
      item: (vendorId: string) => `/v1/admin/vendors/${vendorId}`,
      showcase: (vendorId: string) => `/v1/admin/vendors/${vendorId}/showcases`,
      approve: (vendorId: string) => `/v1/admin/vendors/${vendorId}/approve`,
      reject: (vendorId: string) => `/v1/admin/vendors/${vendorId}/reject`,
    },
    industries: {
      collection: '/v1/admin/industries',
      item: (industryId: string) => `/v1/admin/industries/${industryId}`,
      imageUpload: (industryId: string) => `/v1/admin/industries/${industryId}/images`,
      toggleDisable: (industryId: string) => `/v1/admin/industries/${industryId}/disable`,
    },
    industryChecklistSection: {
      collection: '/v1/admin/question-sections',
      item: (sectionId: string) => `/v1/admin/question-sections/${sectionId}`,
      bulkSaveQuestions: (sectionId: string) =>
        `/v1/admin/question-sections/${sectionId}/questions`,
    },
    industryChecklistQuestion: {
      item: (questionId: string) => `/v1/admin/questions/${questionId}`,
      collection: '/v1/admin/questions',
    },
    agreements: {
      collection: '/v1/admin/agreements',
      item: (agreementId: string) => `/v1/admin/agreements/${agreementId}`,
    },
    feedbacks: {
      collection: '/v1/admin/feedbacks',
      item: (feedbackId: string) => `/v1/admin/feedbacks/${feedbackId}`,
      reviewed: (feedbackId: string) => `/v1/admin/feedbacks/${feedbackId}/reviewed`,
      reply: (feedbackId: string) => `/v1/admin/feedbacks/${feedbackId}/reply`,
    },
    tenants: {
      collection: '/v1/admin/tenants',
      item: (tenantId: string) => `/v1/admin/tenants/${tenantId}`,
      analytics: (tenantId: string) => `/v1/admin/tenants/${tenantId}/analytics`,
      users: (tenantId: string) => `/v1/admin/tenants/${tenantId}/users`,
    },
    analytics: {
      tenantActivityByTenantId: (tenantId: string) =>
        `/v1/admin/analytics/tenant-activity/${tenantId}`,
    },
    leadImports: {
      collection: '/v1/admin/lead-imports',
      uploadContacts: '/v1/admin/lead-imports/contacts',
      uploadCompanies: '/v1/admin/lead-imports/companies',
      preview: (id: number) => `/v1/admin/lead-imports/${id}/preview`,
      confirm: (id: number) => `/v1/admin/lead-imports/${id}/confirm`,
      rollback: (id: number) => `/v1/admin/lead-imports/${id}/rollback`,
      records: (id: number) => `/v1/admin/lead-imports/${id}/records`,
    },
  },
  vendor: {
    auth: {
      signup: '/v1/auth/sign-up/vendor',
    },
    showcase: {
      collection: (tenantId: string, vendorId: string) =>
        `/v1/tenants/${tenantId}/vendors/${vendorId}/vendor-showcases`,
      item: (tenantId: string, vendorId: string, showcaseId: string) =>
        `/v1/tenants/${tenantId}/vendors/${vendorId}/vendor-showcases/${showcaseId}`,
      attachments: {
        collection: (tenantId: string, vendorId: string, showcaseId: string) =>
          `/v1/tenants/${tenantId}/vendors/${vendorId}/vendor-showcases/${showcaseId}/attachments`,
        item: (tenantId: string, vendorId: string, showcaseId: string, attachmentId: string) =>
          `/v1/tenants/${tenantId}/vendors/${vendorId}/vendor-showcases/${showcaseId}/attachments/${attachmentId}`,
      },
    },
    agreements: {
      collection: (vendorId: string) => `/v1/vendor-agreements/${vendorId}`,
      sendOtp: () => `/v1/vendor-agreements/otp`,
      verifyOtp: () => `/v1/vendor-agreements/verify-otp`,
    },
    search: '/v1/vendors/search',
    searchParse: '/v1/vendors/search/parse',
    searchVendor: (vendorId: string) => `/v1/vendors/${vendorId}`,
    rfqQuotations: (tenantId: string, workspaceId: string) =>
      `/v1/tenants/${tenantId}/workspaces/${workspaceId}/quotations/request-for-quotes/vendors`,
    rfqQuotationById: (tenantId: string, workspaceId: string, quotationId: string) =>
      `/v1/tenants/${tenantId}/workspaces/${workspaceId}/quotations/request-for-quotes/${quotationId}`,
    rfqQuotationAccept: (tenantId: string, workspaceId: string, quotationId: string) =>
      `/v1/tenants/${tenantId}/workspaces/${workspaceId}/quotations/request-for-quotes/${quotationId}/accepted`,
    rfqQuotationReject: (tenantId: string, workspaceId: string, quotationId: string) =>
      `/v1/tenants/${tenantId}/workspaces/${workspaceId}/quotations/request-for-quotes/${quotationId}/rejected`,
  },
  tenants: {
    collection: '/v1/tenants',
    item: (tenantId: string) => `/v1/tenants/${tenantId}`,
    connectZohoCRM: (tenantId: string) => `/v1/tenants/${tenantId}/connect/zoho`,
    disconnectZohoCRM: (tenantId: string) => `/v1/tenants/${tenantId}/disconnect/zoho`,
    connectHubSpotCRM: (tenantId: string) => `/v1/tenants/${tenantId}/connect/hubspot`,
    disconnectHubSpotCRM: (tenantId: string) => `/v1/tenants/${tenantId}/disconnect/hubspot`,
    workspaces: (tenantId: string) => `/v1/tenants/${tenantId}/workspaces`,
    users: (tenantId: string) => `/v1/tenants/${tenantId}/users`,
    syncZohoRecords: (tenantId: string) => `/v1/tenants/${tenantId}/sync/zoho`,
    syncHubSpotRecords: (tenantId: string) => `/v1/tenants/${tenantId}/sync/hubspot`,
    recipients: (tenantId: string) => `/v1/tenants/${tenantId}/recipients`,
    announcementSmtp: (tenantId: string) => `/v1/tenants/${tenantId}/announcement/smtp`,
    announcements: {
      collection: (tenantId: string) => `/v1/tenants/${tenantId}/announcements`,
      item: (tenantId: string, announcementId: string) =>
        `/v1/tenants/${tenantId}/announcements/${announcementId}`,
      launch: (tenantId: string, announcementId: string) =>
        `/v1/tenants/${tenantId}/announcements/${announcementId}/launch`,
    },
    announcementContacts: {
      collection: (tenantId: string, announcementId: string) =>
        `/v1/tenants/${tenantId}/announcements/${announcementId}/contacts`,
      importLeads: (tenantId: string, announcementId: string) =>
        `/v1/tenants/${tenantId}/announcements/${announcementId}/contacts/import/leads`,
      importCrm: (tenantId: string, announcementId: string) =>
        `/v1/tenants/${tenantId}/announcements/${announcementId}/contacts/import/crm`,
      item: (tenantId: string, announcementId: string, contactId: string) =>
        `/v1/tenants/${tenantId}/announcements/${announcementId}/contacts/${contactId}`,
    },
    emailSequenceTemplates: {
      collection: (tenantId: string) => `/v1/tenants/${tenantId}/sequence-templates`,
      item: (tenantId: string, templateId: string) =>
        `/v1/tenants/${tenantId}/sequence-templates/${templateId}`,
    },
    contacts: (tenantId: string) => `/v1/tenants/${tenantId}/contacts`,
    modules: `/v1/tenants/modules`,
    leadFilters: {
      collection: (tenantId: string) => `/v1/tenants/${tenantId}/tenant-lead-filters`,
      item: (tenantId: string, id: string) => `/v1/tenants/${tenantId}/tenant-lead-filters/${id}`,
    },
  },
  industries: {
    collection: '/v1/industries',
    questionnaires: '/v1/industries/questionnaires',
  },
  workspace: {
    create: (tenantId: string) => `/v1/tenants/${tenantId}/workspaces`,
    item: (tenantId: string, workspaceId: string) => tenant(tenantId, workspaceId),
    users: (tenantId: string, workspaceId: string) => `${tenant(tenantId, workspaceId)}/users`,
    inviteUser: (tenantId: string, workspaceId: string) =>
      `${tenant(tenantId, workspaceId)}/invite`,
    acceptInvitation: `/v1/workspaces/invitation/accept`,
    revokeInvitation: (tenantId: string, workspaceId: string, userId: string) =>
      `${tenant(tenantId, workspaceId)}/users/${userId}`,
    transferOwnership: (tenantId: string, workspaceId: string, userId: string) =>
      `${tenant(tenantId, workspaceId)}/users/${userId}/transfer-ownership`,
    updateUserRole: (tenantId: string, workspaceId: string, userId: string) =>
      `${tenant(tenantId, workspaceId)}/users/${userId}/role`,
  },
  attachments: {
    collection: (tenantId: string, workspaceId: string) =>
      `${tenant(tenantId, workspaceId)}/attachment-libraries`,
    item: (tenantId: string, workspaceId: string, attachmentId: string) =>
      `${tenant(tenantId, workspaceId)}/attachment-libraries/${attachmentId}`,
    rename: (tenantId: string, workspaceId: string, attachmentId: string) =>
      `${tenant(tenantId, workspaceId)}/attachment-libraries/${attachmentId}/rename`,
    replace: (tenantId: string, workspaceId: string, attachmentId: string) =>
      `${tenant(tenantId, workspaceId)}/attachment-libraries/${attachmentId}/replace`,
  },
  campaigns: {
    collection: (tenantId: string, workspaceId: string) =>
      `${tenant(tenantId, workspaceId)}/campaigns`,
    item: (tenantId: string, workspaceId: string, campaignId: string) =>
      campaignById(tenantId, workspaceId, campaignId),
    generator: (tenantId: string, workspaceId: string) =>
      `${tenant(tenantId, workspaceId)}/campaigns/generator`,
    basicDetails: (tenantId: string, workspaceId: string, campaignId: string) =>
      `${campaignById(tenantId, workspaceId, campaignId)}/details`,
    recipients: (tenantId: string, workspaceId: string, campaignId: string) =>
      `${campaignById(tenantId, workspaceId, campaignId)}/recipients`,
    updateCampaignTargets: (tenantId: string, workspaceId: string, campaignId: string) =>
      `${campaignById(tenantId, workspaceId, campaignId)}/contacts`,
    addContacts: (tenantId: string, workspaceId: string, campaignId: string) =>
      `${campaignById(tenantId, workspaceId, campaignId)}/contacts`,
    createForSearch: (tenantId: string, workspaceId: string) =>
      `${tenant(tenantId, workspaceId)}/campaigns/search-results`,
    agent: {
      collection: (tenantId: string, workspaceId: string) =>
        `${tenant(tenantId, workspaceId)}/campaigns/agent`,
      details: (tenantId: string, workspaceId: string, chatMemoryId: string) =>
        `${tenant(tenantId, workspaceId)}/campaigns/agent/${chatMemoryId}`,
      updateTargetCriteria: (tenantId: string, workspaceId: string, chatMemoryId: string) =>
        `${tenant(tenantId, workspaceId)}/campaigns/agent/${chatMemoryId}/target-criteria`,
      proceedCampaign: (tenantId: string, workspaceId: string, chatMemoryId: string) =>
        `${tenant(tenantId, workspaceId)}/campaigns/agent/${chatMemoryId}/proceed`,
      conversations: {
        collection: (tenantId: string, workspaceId: string) =>
          `${tenant(tenantId, workspaceId)}/messages`,
        item: (tenantId: string, workspaceId: string, conversationId: string) =>
          `${tenant(tenantId, workspaceId)}/messages/${conversationId}`,
      },
    },
    assignMailbox: (tenantId: string, workspaceId: string, campaignId: string, mailboxId: string) =>
      `${campaignById(tenantId, workspaceId, campaignId)}/mailbox/${mailboxId}`,
    emails: {
      collection: (tenantId: string, workspaceId: string, campaignId: string) =>
        `${campaignById(tenantId, workspaceId, campaignId)}/emails`,
      item: (tenantId: string, workspaceId: string, campaignId: string, emailId: string) =>
        `${campaignById(tenantId, workspaceId, campaignId)}/emails/${emailId}`,
      configure: (tenantId: string, workspaceId: string, campaignId: string) =>
        `${campaignById(tenantId, workspaceId, campaignId)}/emails/configure`,
      generate: (tenantId: string, workspaceId: string, campaignId: string) =>
        `${campaignById(tenantId, workspaceId, campaignId)}/emails/generate`,
    },
    analytics: {
      workspace: (tenantId: string, workspaceId: string) =>
        `${tenant(tenantId, workspaceId)}/campaigns/analytics`,
      campaign: (tenantId: string, workspaceId: string, campaignId: string) =>
        `${campaignById(tenantId, workspaceId, campaignId)}/analytics`,
    },
    controls: {
      launch: (tenantId: string, workspaceId: string, campaignId: string) =>
        `${campaignById(tenantId, workspaceId, campaignId)}/launch`,
      pause: (tenantId: string, workspaceId: string, campaignId: string) =>
        `${campaignById(tenantId, workspaceId, campaignId)}/pause`,
      resume: (tenantId: string, workspaceId: string, campaignId: string) =>
        `${campaignById(tenantId, workspaceId, campaignId)}/resume`,
    },
  },
  mailboxes: {
    collection: (tenantId: string, workspaceId: string) =>
      `${tenant(tenantId, workspaceId)}/mailboxes`,
    item: (tenantId: string, workspaceId: string, mailboxId: string) =>
      `${tenant(tenantId, workspaceId)}/mailboxes/${mailboxId}`,
    awsSes: (tenantId: string, workspaceId: string) =>
      `${tenant(tenantId, workspaceId)}/mailboxes/aws-ses`,
    microsoft: (tenantId: string, workspaceId: string) =>
      `${tenant(tenantId, workspaceId)}/mailboxes/microsoft`,
    gmail: (tenantId: string, workspaceId: string) =>
      `${tenant(tenantId, workspaceId)}/mailboxes/google`,
    smtp: (tenantId: string, workspaceId: string) =>
      `${tenant(tenantId, workspaceId)}/mailboxes/smtp`,
    authorized: (tenantId: string, workspaceId: string) =>
      `${tenant(tenantId, workspaceId)}/mailboxes/authorized`,
  },
  leads: {
    root: (tenantId: string) => `/v1/tenants/${tenantId}/leads`,
    companyLeadSearch: (tenantId: string) => `/v1/tenants/${tenantId}/leads/companies`,
    contactLeadSearch: (tenantId: string) => `/v1/tenants/${tenantId}/leads/contacts`,
    contactLeadIds: (tenantId: string) => `/v1/tenants/${tenantId}/leads/contacts/ids`,
    companyLeadIds: (tenantId: string) => `/v1/tenants/${tenantId}/leads/companies/ids`,
    companyLookup: (tenantId: string) => `/v1/tenants/${tenantId}/leads/companies/lookup`,
    companyInfo: (tenantId: string, companyId: string) =>
      `/v1/tenants/${tenantId}/leads/companies/${companyId}`,
    contactInfo: (tenantId: string, contactId: string) =>
      `/v1/tenants/${tenantId}/leads/contacts/${contactId}`,
    companyContacts: (tenantId: string, companyId: string) =>
      `/v1/tenants/${tenantId}/leads/companies/${companyId}/contacts`,
    searchFilters: (tenantId: string) => `/v1/tenants/${tenantId}/leads/save-search`,
    metadataFilters: (tenantId: string) => `/v1/tenants/${tenantId}/leads/metadata-filters`,
    collection: () => `/v1/companies`,
    industries: () => `/v1/segments`,
    search: () => `/v1/companies/search`,
    companyJobs: (companyId: string) => `/v1/leads/companies/${companyId}/jobs`,
    contactsByCompanyId: (companyId: string) => `/v1/companies/${companyId}/contacts`,
    queries: () => `/v1/leads/queries`,
    parse: (tenantId: string) => `/v1/tenants/${tenantId}/leads/chat`,
    statistics: (tenantId: string) => `/v1/tenants/${tenantId}/leads/statistics`,
    sendEmail: (tenantId: string, workspaceId: string, contactId: string) =>
      `/v1/tenants/${tenantId}/workspaces/${workspaceId}/leads/contacts/${contactId}/emails`,
    generateEmail: (tenantId: string, workspaceId: string) =>
      `/v1/tenants/${tenantId}/workspaces/${workspaceId}/leads/contacts/emails/generate`,
    list: {
      collection: (tenantId: string, workspaceId: string) =>
        `/v1/tenants/${tenantId}/workspaces/${workspaceId}/lead-lists`,
      item: (tenantId: string, workspaceId: string, listId: string) =>
        `/v1/tenants/${tenantId}/workspaces/${workspaceId}/lead-lists/${listId}`,
      contacts: (tenantId: string, workspaceId: string, listId: string) =>
        `/v1/tenants/${tenantId}/workspaces/${workspaceId}/lead-lists/${listId}/contacts`,
      companies: (tenantId: string, workspaceId: string, listId: string) =>
        `/v1/tenants/${tenantId}/workspaces/${workspaceId}/lead-lists/${listId}/companies`,
    },
    notes: {
      collection: (tenantId: string, workspaceId: string) =>
        `/v1/tenants/${tenantId}/workspaces/${workspaceId}/lead-notes`,
      bySource: (tenantId: string, workspaceId: string, sourceId: string) =>
        `/v1/tenants/${tenantId}/workspaces/${workspaceId}/lead-notes/sources/${sourceId}`,
      item: (tenantId: string, workspaceId: string, noteId: string) =>
        `/v1/tenants/${tenantId}/workspaces/${workspaceId}/lead-notes/${noteId}`,
    },
    contacts: {
      root: `/v1/contacts`,
      events: {
        collection: (tenantId: string, contactId: string) =>
          `/v1/tenants/${tenantId}/leads/contacts/${contactId}/events`,
        counts: (tenantId: string, contactId: string) =>
          `/v1/tenants/${tenantId}/leads/contacts/${contactId}/events/counts`,
      },
    },
  },
  feedbacks: {
    collection: (tenantId: string, workspaceId: string) =>
      `/v1/tenants/${tenantId}/workspaces/${workspaceId}/feedbacks`,
  },
  customer: {
    chat: '/v1/chat',
    requestForQuotes: {
      collection: '/v1/request-for-quotes',
      item: (rfqId: string) => `/v1/request-for-quotes/${rfqId}`,
      attachments: (rfqId: string) => `/v1/request-for-quotes/${rfqId}/attachments`,
      attachmentById: (rfqId: string, attachmentId: string) =>
        `/v1/request-for-quotes/${rfqId}/attachments/${attachmentId}`,
    },
    quotations: {
      byRfqId: (rfqId: string) => `/v1/customers/quotations/request-for-quotes/${rfqId}`,
    },
  },
};
