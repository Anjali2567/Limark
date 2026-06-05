'use client';

import {
  CheckCircle,
  MapPin,
  Users,
  Building,
  Clock,
  DollarSign,
  ChevronRight,
} from 'lucide-react';
import Link from 'next/link';

import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { VendorDetailResponse } from '@/types/vendor.types';
import { VendorVerificationStatus } from '@/constants/vendor.constant';
import { getCachedImageUrl, getInitials, normalizeUrl } from '@/lib/utils/helpers';
import { formatLocation } from '@/lib/utils/formatter';
import { appRoutes } from '@/config/routes';

type VendorHeroProps = {
  vendor: VendorDetailResponse;
  onRequestQuote: () => void;
};

export const VendorHero = ({ vendor, onRequestQuote }: VendorHeroProps) => {
  const location = formatLocation(vendor.address || {});
  const websiteUrl = normalizeUrl(vendor.website);

  return (
    <section className="border-b border-slate-800 bg-slate-900 py-12 text-white">
      <div className="mx-auto max-w-7xl px-6">
        <div className="mb-6 flex items-center gap-1.5 text-sm text-slate-400">
          <Link
            href={appRoutes.customer.search.root}
            className="hover:text-primary transition-colors"
          >
            Search Results
          </Link>
          <ChevronRight className="h-3.5 w-3.5" />
          <span className="font-medium text-white">{vendor.companyName}</span>
        </div>

        <div className="mb-8 flex items-start gap-6">
          <Avatar className="h-24 w-24 shrink-0 rounded-xl">
            <AvatarImage
              src={getCachedImageUrl(vendor.logo, vendor.updatedAt)}
              alt={vendor.companyName}
            />
            <AvatarFallback className="bg-primary text-primary-foreground rounded-xl text-xl font-bold">
              {getInitials(vendor.companyName || '')}
            </AvatarFallback>
          </Avatar>

          <div className="flex-1">
            <div className="mb-2 flex items-center gap-3">
              <h1 className="text-3xl leading-tight font-bold">{vendor.companyName}</h1>
              {vendor.vendorVerificationStatus === VendorVerificationStatus.APPROVED && (
                <Badge className="bg-primary/20 text-primary border-primary/30 shrink-0 border px-2 py-1">
                  <CheckCircle className="mr-1 h-3 w-3" />
                  Verified Partner
                </Badge>
              )}
            </div>

            {vendor.tagline && <p className="mb-4 text-lg text-slate-300">{vendor.tagline}</p>}

            <div className="flex flex-wrap items-center gap-4 text-sm text-slate-400">
              {location && (
                <div className="flex items-center gap-1.5">
                  <MapPin className="h-4 w-4 text-blue-400" />
                  <span className="text-white">{location}</span>
                </div>
              )}
              {vendor.companySize && (
                <div className="flex items-center gap-1.5">
                  <Users className="h-4 w-4 text-green-400" />
                  <span className="text-white">{vendor.companySize}</span>
                </div>
              )}
              {vendor.yearEstablished && (
                <div className="flex items-center gap-1.5">
                  <Building className="h-4 w-4 text-purple-400" />
                  <span className="text-white">Est. {vendor.yearEstablished}</span>
                </div>
              )}
              {vendor.businessHours && (
                <div className="flex items-center gap-1.5">
                  <Clock className="h-4 w-4 text-orange-400" />
                  <span className="text-white">{vendor.businessHours}</span>
                </div>
              )}
              {vendor.annualRevenue && (
                <div className="flex items-center gap-1.5">
                  <DollarSign className="h-4 w-4 text-yellow-400" />
                  <span className="text-white">{vendor.annualRevenue}</span>
                </div>
              )}
            </div>
          </div>
        </div>

        <div className="flex gap-3">
          <Button
            onClick={onRequestQuote}
            className="bg-primary hover:bg-primary/90 text-primary-foreground px-6 font-semibold"
          >
            Request Quote
          </Button>
          {websiteUrl && (
            <Button
              variant="outline"
              className="border-2 border-slate-600 bg-transparent px-6 font-semibold text-white hover:bg-slate-800"
              asChild
            >
              <a href={websiteUrl} target="_blank" rel="noopener noreferrer">
                Visit Website
              </a>
            </Button>
          )}
        </div>
      </div>
    </section>
  );
};
