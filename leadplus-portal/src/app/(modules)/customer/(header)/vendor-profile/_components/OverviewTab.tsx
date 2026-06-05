'use client';

import { Globe } from 'lucide-react';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { Badge } from '@/components/ui/badge';
import { VendorDetailResponse } from '@/types/vendor.types';
import { getTechnologyIcon } from '@/lib/utils/icon';
import { QuickFactsCard } from './QuickFactsCard';
import { CertificationsSidebarCard } from './CertificationsSidebarCard';

type OverviewTabProps = {
  vendor: VendorDetailResponse;
};

export const OverviewTab = ({ vendor }: OverviewTabProps) => (
  <div className="grid grid-cols-1 gap-8 lg:grid-cols-3">
    <div className="space-y-10 lg:col-span-2">
      {vendor.description && (
        <div>
          <h2 className="text-foreground mb-4 text-2xl font-bold">About {vendor.companyName}</h2>
          <p className="text-muted-foreground leading-relaxed">{vendor.description}</p>
        </div>
      )}

      {vendor.serviceList && vendor.serviceList.length > 0 && (
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

      {vendor.specificationList && vendor.specificationList.length > 0 && (
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

      {vendor.regionsCovered && vendor.regionsCovered.length > 0 && (
        <div>
          <h2 className="text-foreground mb-4 text-[24px] font-bold">Regions Covered</h2>
          <div className="flex flex-wrap gap-2">
            {vendor.regionsCovered.map((region, idx) => (
              <Badge key={idx} variant="secondary" className="border-border border">
                <Globe className="mr-1 h-3 w-3" />
                {region}
              </Badge>
            ))}
          </div>
        </div>
      )}
    </div>

    <div className="space-y-6">
      <QuickFactsCard vendor={vendor} />
      <CertificationsSidebarCard certifications={vendor.certifications} />
    </div>
  </div>
);
