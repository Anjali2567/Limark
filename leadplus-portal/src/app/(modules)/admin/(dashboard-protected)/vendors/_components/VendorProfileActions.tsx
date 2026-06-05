import { useMemo } from 'react';

import { Button } from '@/components/ui/button';
import { VendorVerificationStatus } from '@/constants/vendor.constant';
import { cn } from '@/lib/utils/helpers';
import { Vendor } from '@/types/vendor.types';
import { useVendorActions } from '../_hooks/useVendorProfileActions';

import { CheckCircle2, UserCheck, UserX, XCircle } from 'lucide-react';

type VendorProfileActionsProps = { vendor: Vendor };

const VendorProfileActions = ({ vendor }: VendorProfileActionsProps) => {
  const { handleActivate, handleDeactivate, openModal } = useVendorActions(vendor);

  const options = useMemo(() => {
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
          className: 'bg-green-600 hover:bg-green-700 text-white',
        },
        {
          label: 'Reject',
          icon: <UserX className="mr-2 h-4 w-4" />,
          action: () =>
            openModal(
              'error',
              'Reject Vendor Request',
              `Are you sure you want to reject ${vendor.companyName}?`,
              handleDeactivate,
              'Reject'
            ),
          className: 'border-destructive text-destructive hover:bg-destructive hover:text-white',
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
          className: 'border-destructive text-destructive hover:bg-destructive hover:text-white',
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
          className: 'bg-green-600 hover:bg-green-700 text-white',
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

  return (
    <div className="flex gap-2">
      {options.map((option) => (
        <Button
          key={option.label}
          onClick={option.action}
          variant="none"
          className={cn(
            'inline-flex items-center rounded-md border px-3 py-1 text-sm font-medium transition-colors focus:ring-2 focus:ring-offset-2 focus:outline-none',
            option.className
          )}
        >
          {option.icon} {option.label}
        </Button>
      ))}
    </div>
  );
};

export { VendorProfileActions };
