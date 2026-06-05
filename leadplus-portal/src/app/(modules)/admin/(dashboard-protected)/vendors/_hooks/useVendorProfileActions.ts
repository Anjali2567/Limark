import { useCallback } from 'react';
import { toast } from 'sonner';

import { useModal } from '@/hooks/useModal';
import { useApproveVendor, useRejectVendor } from '@/hooks/useVendor';
import { Vendor } from '@/types/vendor.types';

export const useVendorActions = (vendor: Vendor) => {
  const { renderModal } = useModal();
  const { mutate: activateVendor } = useApproveVendor();
  const { mutate: deactivateVendor } = useRejectVendor();

  const handleActivate = useCallback(() => {
    activateVendor(
      { vendorId: vendor.id },
      {
        onSuccess: () => toast.success('Vendor status updated successfully'),
        onError: () => toast.error('Failed to update vendor status'),
      }
    );
  }, [activateVendor, vendor.id]);

  const handleDeactivate = useCallback(() => {
    deactivateVendor(
      { vendorId: vendor.id },
      {
        onSuccess: () => toast.success('Vendor status updated successfully'),
        onError: () => toast.error('Failed to update vendor status'),
      }
    );
  }, [deactivateVendor, vendor.id]);

  const openModal = useCallback(
    (
      type: 'success' | 'error',
      title: string,
      message: string,
      onConfirm: () => void,
      buttonText: string
    ) => {
      renderModal({ type, title, message, onConfirm, submitButtonText: buttonText });
    },
    [renderModal]
  );

  return {
    handleActivate,
    handleDeactivate,
    openModal,
  };
};
