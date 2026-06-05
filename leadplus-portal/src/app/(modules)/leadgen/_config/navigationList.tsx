import { appRoutes } from '@/config/routes';
import { PageOptions } from '@/constants/config.constants';
import { UserRoles } from '@/constants/user.constants';
import { NavigationItem } from '@/types/navigation.types';

import {
  Bell,
  Briefcase,
  Database,
  FileText,
  Mail,
  Megaphone,
  Search,
  Settings,
  UserCog,
  Wand2,
  Wrench,
} from 'lucide-react';

export const leadgenNavList: NavigationItem[] = [
  {
    id: PageOptions.SEARCH,
    href: appRoutes.leadgen.search.companySearch,
    label: 'Search',
    icon: Search,
    children: [
      {
        id: PageOptions.COMPANY_SEARCH,
        href: appRoutes.leadgen.search.companySearch,
        label: 'Company Search',
      },
      {
        id: PageOptions.CONTACT_SEARCH,
        href: appRoutes.leadgen.search.contactSearch,
        label: 'People Search',
      },
      {
        id: PageOptions.LEAD_LIST,
        href: appRoutes.leadgen.leadList.root,
        label: 'Lists',
      },
    ],
  },
  {
    id: PageOptions.CAMPAIGN_GENERATOR,
    href: appRoutes.leadgen.campaignGenerator.root,
    label: 'Generate',
    icon: Wand2,
  },
  {
    id: PageOptions.CAMPAIGNS,
    href: appRoutes.leadgen.campaigns.root,
    label: 'All Campaigns',
    icon: Megaphone,
  },
  {
    id: PageOptions.RESOURCES,
    href: appRoutes.leadgen.resources.root,
    label: 'Resources',
    icon: FileText,
  },
  {
    id: PageOptions.WORKSPACE,
    href: appRoutes.leadgen.workspace.root,
    label: 'Workspace',
    icon: Briefcase,
  },
  {
    id: PageOptions.SETTINGS,
    href: appRoutes.leadgen.settings.root,
    label: 'Settings',
    icon: Settings,
    children: [
      {
        id: PageOptions.EMAILS,
        href: appRoutes.leadgen.settings.emailConfig,
        label: 'Emails',
        icon: Mail,
      },
      {
        id: PageOptions.COMMUNICATIONS,
        href: appRoutes.leadgen.settings.communications,
        label: 'Communications',
        icon: Mail,
      },
      {
        id: PageOptions.ZOHO,
        href: appRoutes.leadgen.settings.zoho,
        label: 'Zoho CRM',
        icon: Database,
        allowedRoles: [UserRoles.TENANT_OWNER],
      },
      {
        id: PageOptions.HUBSPOT,
        href: appRoutes.leadgen.settings.hubspot,
        label: 'HubSpot CRM',
        icon: Database,
        allowedRoles: [UserRoles.TENANT_OWNER],
      },
    ],
  },
];

export const tenantOwnerNavList: NavigationItem[] = [
  {
    id: PageOptions.TENANTS,
    href: appRoutes.leadgen.tenants.root,
    label: 'Tenant',
    icon: Wrench,
    children: [
      {
        id: PageOptions.TENANT_WORKSPACES,
        href: appRoutes.leadgen.tenants.tenantWorkspaces,
        label: 'Workspaces',
        icon: Briefcase,
      },
      {
        id: PageOptions.TENANT_USERS,
        href: appRoutes.leadgen.tenants.tenantUsers,
        label: 'Users',
        icon: UserCog,
      },
      {
        id: PageOptions.TENANT_SETTINGS,
        href: appRoutes.leadgen.tenants.tenantSettings,
        label: 'Settings',
        icon: Settings,
      },
      {
        id: PageOptions.TENANT_ANNOUNCEMENTS,
        href: appRoutes.leadgen.tenants.tenantAnnouncements,
        label: 'Announcements',
        icon: Bell,
      },
    ],
  },
];
