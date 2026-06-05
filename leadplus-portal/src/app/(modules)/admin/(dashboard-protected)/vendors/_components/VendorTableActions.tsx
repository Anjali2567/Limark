import { useMemo } from 'react';

import { Button } from '@/components/ui/button';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import { VendorVerificationStatus } from '@/constants/vendor.constant';
import { Vendor } from '@/types/vendor.types';
import { useVendorActions } from '../_hooks/useVendorProfileActions';

import { CheckCircle2, MoreHorizontal, UserCheck, UserX, XCircle } from 'lucide-react';

type VendorTableActionsProps = {
  vendor: Vendor;
};

const VendorTableActions = ({ vendor }: VendorTableActionsProps) => {
  const { handleActivate, handleDeactivate, openModal } = useVendorActions(vendor);

  const dropdownItems = useMemo(() => {
    if (vendor.vendorVerificationStatus === VendorVerificationStatus.PENDING) {
      return [
        {
          label: 'Approve',
          icon: <UserCheck className="mr-2 h-4 w-4" />,
          action: () =>
            openModal(
              'success',
              'Approve Vendor',
              `Are you sure you want to approve ${vendor.companyName}? They will gain access immediately.`,
              handleActivate,
              'Approve'
            ),
          className: 'text-green-600 focus:text-green-600',
        },
        {
          label: 'Reject',
          icon: <UserX className="mr-2 h-4 w-4" />,
          action: () =>
            openModal(
              'error',
              'Reject Vendor Request',
              `Are you sure you want to reject ${vendor.companyName}'s request?`,
              handleDeactivate,
              'Reject'
            ),
          className: 'text-destructive focus:text-destructive',
        },
      ];
    }

    if (vendor.vendorVerificationStatus === VendorVerificationStatus.APPROVED) {
      return [
        {
          label: 'Deactivate',
          icon: <XCircle className="mr-2 h-4 w-4" />,
          action: () =>
            openModal(
              'error',
              'Deactivate Vendor',
              `Are you sure you want to deactivate ${vendor.companyName}?`,
              handleDeactivate,
              'Deactivate'
            ),
          className: 'text-destructive focus:text-destructive',
        },
      ];
    }

    if (vendor.vendorVerificationStatus === VendorVerificationStatus.REJECTED) {
      return [
        {
          label: 'Activate',
          icon: <CheckCircle2 className="mr-2 h-4 w-4" />,
          action: () =>
            openModal(
              'success',
              'Activate Vendor',
              `Are you sure you want to activate ${vendor.companyName}?`,
              handleActivate,
              'Activate'
            ),
          className: 'text-green-600 focus:text-green-600',
        },
      ];
    }

    return [];
  }, [
    vendor.vendorVerificationStatus,
    vendor.companyName,
    handleActivate,
    handleDeactivate,
    openModal,
  ]);

  if (vendor.vendorVerificationStatus === VendorVerificationStatus.INCOMPLETE) {
    return null;
  }

  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button variant="ghost" className="text-foreground hover:text-foreground h-8 w-8 p-0">
          <span className="sr-only">Open menu</span>
          <MoreHorizontal className="h-4 w-4" />
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent align="end">
        {dropdownItems.map((item) => (
          <DropdownMenuItem
            key={item.label}
            className={item.className}
            onClick={(e) => {
              e.stopPropagation();
              item.action();
            }}
          >
            {item.icon} {item.label}
          </DropdownMenuItem>
        ))}
      </DropdownMenuContent>
    </DropdownMenu>
  );
};

export { VendorTableActions };
