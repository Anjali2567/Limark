import { useRouter, useSearchParams } from 'next/navigation';
import { useEffect, useMemo } from 'react';
import { toast } from 'sonner';

import { Breadcrumbs } from '@/components/Breadcrumbs';
import { SectionCard } from '@/components/SectionCard';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import { Badge } from '@/components/ui/badge';
import { appRoutes } from '@/config/routes';
import { VendorVerificationStatus } from '@/constants/vendor.constant';
import { useGetIndustryList } from '@/hooks/useIndustry';
import { useGetAllServices } from '@/hooks/useService';
import { useGetAllSpecifications } from '@/hooks/useSpecification';
import { useGetUserById } from '@/hooks/useUser';
import { useGetVendorById } from '@/hooks/useVendor';
import { formatLocation } from '@/lib/utils/formatter';
import { getCachedImageUrl } from '@/lib/utils/helpers';
import { convertDateISO } from '@/lib/utils/timeConversion';
import { VendorProfileActions } from './VendorProfileActions';
import { VendorQuestionnaireAnswers } from './VendorQuestionnaireAnswers';
import { VendorShowcase } from './VendorShowcase';

import {
  Building,
  Calendar,
  Globe,
  HandCoins,
  Hourglass,
  Loader2,
  LucideIcon,
  Mail,
  MapPin,
  Phone,
} from 'lucide-react';

const getStatusBadge = (status?: VendorVerificationStatus) => {
  switch (status) {
    case VendorVerificationStatus.APPROVED:
      return <Badge className="bg-success/10 text-success border-success/20">Approved</Badge>;
    case VendorVerificationStatus.PENDING:
      return (
        <Badge className="bg-warning/10 text-warning border-warning/20">Pending Approval</Badge>
      );
    case VendorVerificationStatus.REJECTED:
      return <Badge className="bg-muted text-muted-foreground border-border">Rejected</Badge>;
  }
};

const InfoRow = ({
  icon: Icon,
  label,
  value,
}: {
  icon: LucideIcon;
  label: string;
  value?: string | string[];
}) => (
  <div className="flex items-start gap-3 py-4">
    <div className="text-muted-foreground mt-0.5">
      <Icon className="h-5 w-5" />
    </div>
    <div className="min-w-0 flex-1">
      <p className="text-muted-foreground mb-1 text-sm">{label}</p>

      {!value ? (
        <span className="text-muted-foreground">N/A</span>
      ) : Array.isArray(value) ? (
        <div className="flex flex-wrap gap-2">
          {value.map((item, index) => (
            <span
              key={index}
              className="rounded-full bg-sky-50 px-3 py-1 text-sm font-medium text-sky-700"
            >
              {item}
            </span>
          ))}
        </div>
      ) : (
        <p className="text-foreground font-medium wrap-break-word">{value}</p>
      )}
    </div>
  </div>
);

const VendorProfile = () => {
  const router = useRouter();
  const searchParams = useSearchParams();
  const id = searchParams.get('id') || '';

  const { data, isLoading: isVendorDataLoading, isError } = useGetVendorById(id);
  const { data: userData, isLoading: isUserDataLoading } = useGetUserById(data?.userId || '');

  const industryList = useGetIndustryList();
  const { data: serviceList } = useGetAllServices({});
  const { data: specificationList } = useGetAllSpecifications({});

  const industryLabels = useMemo(() => {
    if (!data?.industryIds?.length || !industryList?.length) return [];
    return industryList
      .filter((industry) => data.industryIds?.includes(industry.value as unknown as number))
      .map((industry) => industry.label);
  }, [data, industryList]);

  const vendorServices = useMemo(() => {
    if (!data?.serviceIds || !serviceList?.length) return null;
    return (
      serviceList
        .filter((service) => data.serviceIds?.includes(service.id))
        .map((service) => service.name) || []
    );
  }, [data, serviceList]);

  const vendorSpecifications = useMemo(() => {
    if (!data?.specificationIds || !specificationList?.length) return null;
    return (
      specificationList
        .filter((spec) => data.specificationIds?.includes(spec.id))
        .map((spec) => spec.name) || []
    );
  }, [data, specificationList]);

  const getWebsiteUrl = () => {
    if (!data?.website) return '#';
    return data.website.startsWith('http') ? data.website : `https://${data.website}`;
  };

  useEffect(() => {
    if (isError) {
      toast.error('Failed to load vendor details');
      router.push(appRoutes.admin.vendors.root);
    }
  }, [isError, router]);

  if (isUserDataLoading || isVendorDataLoading) {
    return (
      <div className="flex h-full w-full items-center justify-center">
        <Loader2 className="h-10 w-10 animate-spin text-sky-500" />
      </div>
    );
  }

  return (
    <div className="animate-in fade-in slide-in-from-right-4 space-y-6 p-8 duration-300">
      <Breadcrumbs
        items={[
          { label: 'Vendors', href: appRoutes.admin.vendors.root },
          { label: data?.companyName || '' },
        ]}
      />

      <div className="flex items-center justify-between">
        <div className="flex items-center gap-3">
          <h1 className="text-foreground text-2xl font-bold">{data?.companyName}</h1>
          {getStatusBadge(data?.vendorVerificationStatus)}
          {industryLabels.map((label) => (
            <Badge
              key={label}
              variant="outline"
              className="border-sky-200 bg-sky-50 px-3 py-1 text-sm text-sky-700"
            >
              <Building className="mr-1.5 h-3.5 w-3.5" />
              {label}
            </Badge>
          ))}
        </div>
        {data && <VendorProfileActions vendor={data} />}
      </div>

      <div className="bg-card border-border overflow-hidden rounded-lg border shadow-sm">
        <div className="p-8">
          <div className="flex items-start gap-6">
            <Avatar className="border-border h-20 w-20 border-4 shadow-md">
              <AvatarImage
                src={getCachedImageUrl(data?.logo, data?.updatedAt)}
                alt={data?.companyName}
              />
              <AvatarFallback className="bg-sky-500 text-2xl font-bold text-white">
                {data?.companyName
                  ?.split(' ')
                  ?.map((n) => n[0])
                  ?.join('') ?? '?'}
              </AvatarFallback>
            </Avatar>

            <div className="flex-1">
              <h2 className="text-foreground mb-3 text-xl font-bold">
                {data?.companyName} Profile
              </h2>
              <p className="text-muted-foreground mb-4">{data?.tagline}</p>
              <p className="text-muted-foreground mb-4">{data?.description}</p>
              <div className="flex flex-col gap-4">
                <div className="flex flex-wrap items-center gap-x-6 gap-y-2 text-sm">
                  <div className="text-muted-foreground flex items-center gap-2">
                    <Building className="h-4 w-4" />
                    <span>{data?.companySize} Employees</span>
                  </div>
                  <div className="text-muted-foreground flex items-center gap-2">
                    <Globe className="h-4 w-4" />
                    <a
                      href={getWebsiteUrl()}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="text-sky-500 hover:underline"
                    >
                      {data?.website}
                    </a>
                  </div>
                  <div className="text-muted-foreground flex items-center gap-2">
                    <Calendar className="h-4 w-4" />
                    <span>Established {data?.yearEstablished}</span>
                  </div>
                  <div className="text-muted-foreground flex items-center gap-2">
                    <Calendar className="h-4 w-4" />
                    <span>
                      Joined {data?.createdAt ? convertDateISO(new Date(data?.createdAt)) : 'N/A'}
                    </span>
                  </div>
                </div>
                <div className="flex flex-wrap items-center gap-x-6 gap-y-2 text-sm">
                  <div className="text-muted-foreground flex items-center gap-2">
                    <HandCoins className="h-4 w-4" />
                    <span>Annual Revenue: {data?.annualRevenue}</span>
                  </div>
                  <div className="text-muted-foreground flex items-center gap-2">
                    <Hourglass className="h-4 w-4" />
                    <span>Business Hours: {data?.businessHours}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <SectionCard title="Contact Information">
        <InfoRow icon={Mail} label="Email Address" value={userData?.email} />
        <InfoRow icon={Phone} label="Phone Number" value={data?.phoneNumber} />
        <InfoRow
          icon={MapPin}
          label="Business Address"
          value={formatLocation({
            street: data?.address?.street,
            city: data?.address?.city,
            state: data?.address?.state,
            postalCode: data?.address?.postalCode,
            country: data?.address?.country,
          })}
        />
      </SectionCard>

      <SectionCard title="Specifications & Services">
        <InfoRow
          icon={Building}
          label="Specifications"
          value={
            vendorSpecifications && vendorSpecifications.length ? vendorSpecifications : undefined
          }
        />
        <InfoRow
          icon={Building}
          label="Services Offered"
          value={vendorServices && vendorServices.length ? vendorServices : undefined}
        />
      </SectionCard>

      <SectionCard title="Regions & Certifications">
        <InfoRow icon={Building} label="Regions Covered" value={data?.regionsCovered} />
        <InfoRow icon={Building} label="Certifications" value={data?.certifications} />
      </SectionCard>
      <VendorShowcase />
      <VendorQuestionnaireAnswers
        industryIds={data?.industryIds}
        questionnaire={data?.questionnaire}
      />
    </div>
  );
};
export { VendorProfile };
