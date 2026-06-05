import { Module, moduleRouteMap } from '@/constants/modules.constants';
import { FEATURES } from '@/constants/features.constants';

export const publicNavLinks = [
  ...(FEATURES.CUSTOMER_MODULE_ENABLED
    ? [{ label: 'Customer Marketplace', href: moduleRouteMap[Module.CUSTOMER] }]
    : []),
  { label: 'Vendor', href: moduleRouteMap[Module.VENDOR] },
];
