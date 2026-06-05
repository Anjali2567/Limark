export const appRoutes = {
  auth: {
    login: '/login/',
    signup: '/signup/',
    forgotPassword: '/forgot-password/',
    passwordReset: '/password-reset/',
  },

  emailVerification: {
    root: '/email-verification',
    verify: '/verify-email',
  },

  dashboard: {
    root: '/dashboard',
  },

  admin: {
    root: '/admin',
    auth: {
      login: '/login',
    },
    dashboard: {
      root: '/admin/companies',
    },
    tenants: {
      root: '/admin/tenants',
    },
    vendors: {
      root: '/admin/vendors',
    },
    companies: {
      root: '/admin/companies',
    },
    industries: {
      root: '/admin/industries',
      list: '/admin/industries/list',
      checklist: '/admin/industries/checklist',
    },
    leads: {
      root: '/admin/leads',
    },
    users: {
      root: '/admin/users',
    },
    documents: {
      root: '/admin/documents',
    },
    settings: {
      root: '/admin/settings',
      prompts: '/admin/settings/prompts',
      peopleSearch: '/admin/settings/people-search',
    },
    feedback: {
      root: '/admin/feedbacks',
    },
  },

  leadgen: {
    root: '/leadgen',
    auth: {
      login: '/login',
      signup: '/signup',
      forgotPassword: '/forgot-password',
      passwordReset: '/password-reset',
    },
    dashboard: {
      root: '/leadgen/search/companies',
    },
    campaignAgent: {
      root: '/leadgen/campaign-agent',
    },
    campaignGenerator: {
      root: '/leadgen/generator',
    },
    search: {
      companySearch: '/leadgen/search/companies',
      contactSearch: '/leadgen/search/contacts',
      contactInfo: (contactId: string) => `/leadgen/search/contact-info/?contact=${contactId}`,
      companyInfo: (companyId: string) => `/leadgen/search/company-info/?company=${companyId}`,
    },
    leadList: {
      root: '/leadgen/lists',
      company: (listId: string) => `/leadgen/lists/companies/?listId=${listId}`,
      contact: (listId: string) => `/leadgen/lists/contacts/?listId=${listId}`,
    },
    campaigns: {
      root: '/leadgen/campaign',
      view: (id: string) => `/leadgen/campaign/view/?id=${id}`,
      summary: (id: string) => `/leadgen/campaign/summary/?id=${id}`,
      sequence: (id: string) => `/leadgen/campaign/configure-sequence/?id=${id}`,
    },
    resources: {
      root: '/leadgen/resources',
    },
    workspace: {
      root: '/leadgen/workspace',
    },
    settings: {
      root: '/leadgen/settings',
      emailConfig: '/leadgen/settings/emails',
      communications: '/leadgen/settings/communications',
      zoho: '/leadgen/settings/zoho',
      hubspot: '/leadgen/settings/hubspot',
    },
    profileOverview: {
      root: '/leadgen/profile-overview',
    },
    tenants: {
      root: '/leadgen/tenant',
      tenantUsers: '/leadgen/tenant/users',
      tenantWorkspaces: '/leadgen/tenant/workspaces',
      tenantSettings: '/leadgen/tenant/settings',
      tenantAnnouncements: '/leadgen/tenant/announcements',
    },
  },

  vendor: {
    root: '/vendor/',
    auth: {
      login: '/login/',
      signup: '/signup/',
      forgotPassword: '/forgot-password/',
      passwordReset: '/password-reset/',
    },
    dashboard: {
      root: '/vendor/profile/',
    },
    onboarding: {
      root: '/vendor/onboarding/',
    },
    profileOverview: {
      root: '/vendor/profile-overview/',
    },
    agreements: {
      root: '/vendor/agreements/',
    },
    profile: {
      root: '/vendor/profile/',
    },
    rfq: {
      root: '/vendor/rfqs/',
      summary: (id: string) => `/vendor/rfqs/summary/?id=${id}`,
    },
    rfp: {
      root: '/vendor/rfps/',
    },
  },

  customer: {
    root: '/customer/',
    vendor: {
      detail: (id: string) => `/customer/vendor-profile/?id=${id}`,
    },
    search: {
      root: '/customer/search/',
      withIndustry: (industryId: string) => `/customer/search/?industryId=${industryId}`,
    },
    dashboard: {
      root: '/customer/dashboard/',
    },
    rfq: {
      root: '/customer/rfqs/',
      quotations: (rfqId: string) => `/customer/rfqs/quotations/?rfqId=${rfqId}`,
    },
    rfp: {
      root: '/customer/rfps/',
    },
  },
};
