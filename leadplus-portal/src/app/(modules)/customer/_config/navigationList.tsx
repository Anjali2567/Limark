import { appRoutes } from '@/config/routes';
import { PageOptions } from '@/constants/config.constants';
import { NavigationItem } from '@/types/navigation.types';

import { ClipboardList, Package } from 'lucide-react';

export const navigationList: NavigationItem[] = [
  {
    id: PageOptions.CUSTOMER,
    href: appRoutes.customer.dashboard.root,
    label: 'Customer',
    icon: Package,
    children: [
      {
        id: PageOptions.VENDOR_RFQ,
        href: appRoutes.customer.rfq.root,
        label: 'RFQs',
        icon: ClipboardList,
      },
      {
        id: PageOptions.CUSTOMER_RFP,
        href: appRoutes.customer.rfp.root,
        label: 'RFPs',
        icon: ClipboardList,
      },
    ],
  },
];
