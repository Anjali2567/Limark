'use client';

import { useState } from 'react';

import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import { Button } from '@/components/ui/button';
import useToggle from '@/hooks/useToggle';
import { useGetPublicVendorById } from '@/hooks/useVendor';
import { getCachedImageUrl, getInitials } from '@/lib/utils/helpers';
import { VendorCardDetails, VendorDetailResponse } from '@/types/vendor.types';
import { RequestForQuoteFormDrawer } from '../../search/_components/RequestForQuoteFormDrawer';
import { CertificationsTab } from './CertificationsTab';
import { EmptyStateCard } from './EmptyStateCard';
import { OverviewTab } from './OverviewTab';
import { ServicesTab } from './ServicesTab';
import { VendorHero } from './VendorHero';
import { Tab, VendorTabBar } from './VendorTabBar';

import { Award, Briefcase, Loader2 } from 'lucide-react';

type VendorPublicProfileProps = {
  vendorId: string;
};

const toVendorCardDetails = (vendor: VendorDetailResponse): VendorCardDetails => {
  return { ...vendor, rating: 0, reviews: 0, responseTime: '', projects: 0 };
};

export const VendorPublicProfile = ({ vendorId }: VendorPublicProfileProps) => {
  const [activeTab, setActiveTab] = useState<Tab>(Tab.Overview);
  const { value: rfqOpen, setValue: setRfqOpen } = useToggle();
  const [selectedVendorIds, setSelectedVendorIds] = useState<string[]>([]);

  const { data: vendor, isLoading, isError } = useGetPublicVendorById(vendorId);

  if (isLoading) {
    return (
      <div className="flex h-screen items-center justify-center">
        <Loader2 className="text-primary h-8 w-8 animate-spin" />
      </div>
    );
  }

  if (isError || !vendor) {
    return (
      <div className="flex h-64 items-center justify-center">
        <p className="text-muted-foreground">Vendor not found.</p>
      </div>
    );
  }

  const handleRequestQuote = () => {
    setSelectedVendorIds([vendor.id]);
    setRfqOpen(true);
  };

  const renderTabContent = () => {
    switch (activeTab) {
      case Tab.Overview:
        return <OverviewTab vendor={vendor} />;
      case Tab.Services:
        return <ServicesTab vendor={vendor} />;
      case Tab.CaseStudies:
        return (
          <EmptyStateCard
            icon={<Briefcase className="text-muted-foreground h-12 w-12" />}
            message="Case studies coming soon"
          />
        );
      case Tab.Reviews:
        return (
          <EmptyStateCard
            icon={<Award className="text-muted-foreground h-12 w-12" />}
            message="No reviews yet"
          />
        );
      case Tab.Certifications:
        return <CertificationsTab certifications={vendor.certifications} />;
      default:
        return null;
    }
  };

  return (
    <div className="min-h-screen pb-20">
      <VendorHero vendor={vendor} onRequestQuote={handleRequestQuote} />

      <VendorTabBar activeTab={activeTab} onChange={setActiveTab} />

      <div className="mx-auto max-w-7xl px-6 py-12">{renderTabContent()}</div>

      <div className="bg-card border-border fixed right-0 bottom-0 left-0 z-20 border-t px-6 py-3">
        <div className="mx-auto flex max-w-7xl items-center justify-between gap-4">
          <div className="flex items-center gap-3">
            <Avatar className="border-border h-10 w-10 border shadow-sm">
              <AvatarImage
                src={getCachedImageUrl(vendor.logo, vendor.updatedAt)}
                alt={vendor.companyName}
              />
              <AvatarFallback className="bg-primary text-primary-foreground text-sm font-bold">
                {getInitials(vendor.companyName || '')}
              </AvatarFallback>
            </Avatar>
            <span className="text-foreground font-semibold">{vendor.companyName}</span>
          </div>
          <Button onClick={handleRequestQuote} className="px-6 font-semibold">
            Request Quote
          </Button>
        </div>
      </div>

      <RequestForQuoteFormDrawer
        open={rfqOpen}
        onOpenChange={setRfqOpen}
        selectedVendorIds={selectedVendorIds}
        setSelectedVendors={setSelectedVendorIds}
        vendors={[toVendorCardDetails(vendor)]}
        onClearSelection={() => setSelectedVendorIds([])}
      />
    </div>
  );
};
