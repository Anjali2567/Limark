import { appRoutes } from '@/config/routes';
import { UserRoles } from '@/constants/user.constants';

export enum Module {
  CUSTOMER = 'CUSTOMER',
  VENDOR = 'VENDOR',
  LEAD_GENERATION = 'LEAD_GENERATION',
  ADMINISTRATION = 'ADMINISTRATION',
}

// Colors
export const MODULE_COLORS: Record<string, string> = {
  CUSTOMER: 'bg-blue-100 text-blue-800',
  VENDOR: 'bg-green-100 text-green-800',
  LEAD_GENERATION: 'bg-purple-100 text-purple-800',
  ADMINISTRATION: 'bg-orange-100 text-orange-800',
};

// Roles required to access each module. Modules without an entry are accessible to any authenticated user.
export const moduleRequiredRoles: Partial<Record<Module, UserRoles[]>> = {
  [Module.ADMINISTRATION]: [UserRoles.ADMIN, UserRoles.TENANT_OWNER],
  [Module.VENDOR]: [UserRoles.VENDOR],
  [Module.LEAD_GENERATION]: [UserRoles.VENDOR],
};

export const moduleRouteMap: Record<Module, string> = {
  [Module.CUSTOMER]: appRoutes.customer.root,
  [Module.VENDOR]: appRoutes.vendor.root,
  [Module.LEAD_GENERATION]: appRoutes.leadgen.dashboard.root,
  [Module.ADMINISTRATION]: appRoutes.admin.dashboard.root,
};

// Login redirect priority
export const moduleHierarchy: Module[] = [
  Module.ADMINISTRATION,
  Module.VENDOR,
  Module.LEAD_GENERATION,
  Module.CUSTOMER,
];

// Sidebar display order
export const moduleDisplayOrder: Module[] = [
  Module.ADMINISTRATION,
  Module.LEAD_GENERATION,
  Module.VENDOR,
  Module.CUSTOMER,
];

// Logo click redirect priority
export const moduleLogoHref: Partial<Record<Module, string>> = {
  [Module.VENDOR]: appRoutes.vendor.root,
  [Module.CUSTOMER]: appRoutes.customer.root,
};

export const moduleDefaultRoute = appRoutes.customer.root;
