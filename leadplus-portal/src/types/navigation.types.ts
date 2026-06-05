import { PageOptions } from '@/constants/config.constants';
import { UserRoles } from '@/constants/user.constants';
import { LucideIcon } from 'lucide-react';

export type NavigationItem = {
  id: PageOptions;
  label: string;
  href: string;
  icon?: LucideIcon;
  allowedRoles?: UserRoles[];
  children?: NavigationItem[];
  tag?: string;
};
