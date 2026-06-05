'use client';

import Link from 'next/link';
import { Store } from 'lucide-react';

import { Button } from '@/components/ui/button';
import { useModule } from '@/context/ModuleContext';
import { Module } from '@/constants/modules.constants';
import { appRoutes } from '@/config/routes';

interface CustomerMarketplaceButtonProps {
  href?: string;
}

export const CustomerMarketplaceButton = ({
  href = appRoutes.customer.root,
}: CustomerMarketplaceButtonProps) => {
  const { enabledModules } = useModule();
  const hasCustomerModule = enabledModules.includes(Module.CUSTOMER);

  if (!hasCustomerModule) {
    return null;
  }

  return (
    <Link href={href}>
      <Button variant="outline" size="sm" className="gap-2">
        <Store className="h-4 w-4" />
        Customer Marketplace
      </Button>
    </Link>
  );
};
