'use client';

import { VendorDetailResponse } from '@/types/vendor.types';
import { formatLocation } from '@/lib/utils/formatter';
import { normalizeUrl } from '@/lib/utils/helpers';

type QuickFactsCardProps = {
  vendor: VendorDetailResponse;
};

const Row = ({ label, children }: { label: string; children: React.ReactNode }) => (
  <div className="flex items-start justify-between gap-4">
    <span className="text-muted-foreground shrink-0 text-sm">{label}</span>
    <span className="text-foreground text-right text-sm font-medium">{children}</span>
  </div>
);

export const QuickFactsCard = ({ vendor }: QuickFactsCardProps) => {
  const location = formatLocation(vendor.address || {});
  const websiteUrl = normalizeUrl(vendor.website);

  const hasFacts =
    location ||
    vendor.yearEstablished ||
    vendor.companySize ||
    vendor.annualRevenue ||
    vendor.businessHours ||
    websiteUrl;

  if (!hasFacts) return null;

  return (
    <div className="bg-card border-border rounded-xl border p-6">
      <h3 className="text-muted-foreground mb-4 text-sm font-semibold tracking-wider uppercase">
        Quick Facts
      </h3>
      <div className="space-y-4">
        {location && <Row label="Headquarters">{location}</Row>}
        {vendor.yearEstablished && <Row label="Founded">{vendor.yearEstablished}</Row>}
        {vendor.companySize && <Row label="Employees">{vendor.companySize}</Row>}
        {vendor.annualRevenue && <Row label="Annual Revenue">{vendor.annualRevenue}</Row>}
        {vendor.businessHours && <Row label="Business Hours">{vendor.businessHours}</Row>}
        {websiteUrl && (
          <div className="flex items-start justify-between gap-4">
            <span className="text-muted-foreground shrink-0 text-sm">Website</span>
            <a
              href={websiteUrl}
              target="_blank"
              rel="noopener noreferrer"
              className="text-primary text-right text-sm font-medium break-all hover:underline"
            >
              {websiteUrl.replace(/^https?:\/\//, '').replace(/\/$/, '')}
            </a>
          </div>
        )}
      </div>
    </div>
  );
};
