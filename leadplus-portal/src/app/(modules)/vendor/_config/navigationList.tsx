import { appRoutes } from '@/config/routes';
import { PageOptions } from '@/constants/config.constants';
import { NavigationItem } from '@/types/navigation.types';

import { ClipboardList, Package } from 'lucide-react';

export const navigationList: NavigationItem[] = [
  {
    id: PageOptions.DASHBOARD,
    href: appRoutes.vendor.dashboard.root,
    label: 'Vendor',
    icon: Package,
    children: [
      {
        id: PageOptions.VENDOR_PROFILE,
        href: appRoutes.vendor.profile.root,
        label: 'Vendor Profile',
        icon: Package,
      },
      {
        id: PageOptions.VENDOR_RFQ,
        href: appRoutes.vendor.rfq.root,
        label: 'RFQs',
        icon: ClipboardList,
      },
      {
        id: PageOptions.VENDOR_RFP,
        href: appRoutes.vendor.rfp.root,
        label: 'RFPs',
        icon: ClipboardList,
      },
    ],
  },
];
