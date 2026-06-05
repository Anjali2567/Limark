import { appRoutes } from '@/config/routes';
import { PageOptions } from '@/constants/config.constants';
import { NavigationItem } from '@/types/navigation.types';

import {
  Briefcase,
  Building,
  Factory,
  FileText,
  MessageSquare,
  Settings,
  UserCog,
  UserPlus,
  Users2,
} from 'lucide-react';

export const navigationList: NavigationItem[] = [
  {
    id: PageOptions.COMPANIES,
    href: appRoutes.admin.companies.root,
    label: 'Companies',
    icon: Building,
  },
  {
    id: PageOptions.LEADS,
    href: appRoutes.admin.leads.root,
    label: 'Leads',
    icon: UserPlus,
  },
  {
    id: PageOptions.TENANTS,
    href: appRoutes.admin.tenants.root,
    label: 'Tenants',
    icon: Users2,
  },
  {
    id: PageOptions.VENDORS,
    href: appRoutes.admin.vendors.root,
    label: 'Vendors',
    icon: Briefcase,
  },
  {
    id: PageOptions.INDUSTRIES,
    href: appRoutes.admin.industries.root,
    label: 'Industry Management',
    icon: Factory,
    children: [
      {
        id: PageOptions.INDUSTRY_LIST,
        href: appRoutes.admin.industries.list,
        label: 'All Industries',
        icon: FileText,
      },
      {
        id: PageOptions.INDUSTRY_CHECKLIST,
        href: appRoutes.admin.industries.checklist,
        label: 'Industry Checklist',
        icon: FileText,
      },
    ],
  },
  {
    id: PageOptions.USERS,
    href: appRoutes.admin.users.root,
    label: 'Users',
    icon: UserCog,
  },
  {
    id: PageOptions.DOCUMENTS,
    href: appRoutes.admin.documents.root,
    label: 'Documents',
    icon: FileText,
  },
  {
    id: PageOptions.FEEDBACK,
    href: appRoutes.admin.feedback.root,
    label: 'Feedbacks',
    icon: MessageSquare,
  },
  {
    id: PageOptions.SETTINGS,
    href: appRoutes.admin.settings.root,
    label: 'Settings',
    icon: Settings,
    children: [
      {
        id: PageOptions.PROMPTS,
        href: appRoutes.admin.settings.prompts,
        label: 'Prompts',
        icon: FileText,
      },
      {
        id: PageOptions.PEOPLE_SEARCH,
        href: appRoutes.admin.settings.peopleSearch,
        label: 'People Search',
        icon: FileText,
      },
    ],
  },
];
