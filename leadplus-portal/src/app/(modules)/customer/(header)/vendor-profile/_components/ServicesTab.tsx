'use client';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { Package } from 'lucide-react';

import { getTechnologyIcon } from '@/lib/utils/icon';
import { VendorDetailResponse } from '@/types/vendor.types';
import { EmptyStateCard } from './EmptyStateCard';

type ServicesTabProps = {
  vendor: VendorDetailResponse;
};

const ServicesTab = ({ vendor }: ServicesTabProps) => {
  const hasServices = vendor.serviceList && vendor.serviceList.length > 0;
  const hasSpecs = vendor.specificationList && vendor.specificationList.length > 0;

  if (!hasServices && !hasSpecs) {
    return (
      <EmptyStateCard
        icon={<Package className="text-muted-foreground h-12 w-12" />}
        message="No services listed"
      />
    );
  }

  return (
    <div className="space-y-10">
      {hasServices && (
        <div>
          <h2 className="text-foreground mb-4 text-2xl font-bold">Services Offered</h2>
          <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
            {vendor.serviceList.map((service) => (
              <div key={service.id} className="bg-card border-border rounded-lg border p-5">
                <div className="flex items-start justify-between gap-2">
                  <p className="text-foreground font-semibold">{service.name}</p>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {hasSpecs && (
        <div>
          <h2 className="text-foreground mb-4 text-2xl font-bold">Technology Stack</h2>
          <div className="grid grid-cols-2 gap-3 sm:grid-cols-3 md:grid-cols-4">
            {vendor.specificationList.map((spec) => (
              <div
                key={spec.id}
                className="bg-card border-border hover:border-primary/30 flex items-center gap-2 rounded-lg border px-4 py-3 transition-colors"
              >
                {spec.icon && (
                  <FontAwesomeIcon
                    icon={getTechnologyIcon(spec.icon.toString())}
                    className="text-muted-foreground h-5 w-5 shrink-0"
                  />
                )}
                <span className="text-foreground truncate text-sm font-medium">{spec.name}</span>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
};

export { ServicesTab };
