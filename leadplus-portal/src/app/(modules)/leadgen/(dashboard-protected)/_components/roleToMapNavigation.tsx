import { PageOptions } from '@/constants/config.constants';
import { UserRoles } from '@/constants/user.constants';

const BASE_WINDOWS: PageOptions[] = [
  PageOptions.CONTACT_SEARCH,
  PageOptions.COMPANY_SEARCH,
  PageOptions.DASHBOARD,
  PageOptions.CAMPAIGNS,
  PageOptions.CAMPAIGN_GENERATOR,
  PageOptions.COMMUNICATIONS,
  PageOptions.RESOURCES,
  PageOptions.EMAILS,
  PageOptions.WORKSPACE,
];

export const roleAccessMap: Record<UserRoles, PageOptions[]> = {
  [UserRoles.ADMIN]: [
    ...BASE_WINDOWS,
    PageOptions.PROMPTS,
    PageOptions.LEADS,
    PageOptions.COMPANIES,
    PageOptions.PEOPLE_SEARCH,
    PageOptions.USERS,
  ],
  [UserRoles.USER]: BASE_WINDOWS,
  [UserRoles.GUEST]: [],
  [UserRoles.CUSTOMER]: [],
  [UserRoles.VENDOR]: [],
  [UserRoles.TENANT_OWNER]: [
    ...BASE_WINDOWS,
    PageOptions.ZOHO,
    PageOptions.HUBSPOT,
    PageOptions.CUSTOMERS,
    PageOptions.TENANTS,
    PageOptions.TENANT_USERS,
    PageOptions.TENANT_WORKSPACES,
    PageOptions.TENANT_SETTINGS,
    PageOptions.TENANT_ANNOUNCEMENTS,
  ],
};
