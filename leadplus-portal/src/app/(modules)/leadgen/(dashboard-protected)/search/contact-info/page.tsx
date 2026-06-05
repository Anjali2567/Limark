'use client';

import { useRouter, useSearchParams } from 'next/navigation';

import { DataError } from '@/components/data/DataError';
import { DataLoader } from '@/components/data/DataLoader';
import { useAuth } from '@/context/AuthContext';
import { useGetLeadContactInfo } from '@/hooks/useSearchLeads';
import { formatLocation, formatName } from '@/lib/utils/formatter';

import { ContactActivityCard } from './_components/ContactActivityCard';
import { ContactCompanyDetails } from './_components/ContactCompanyDetails';
import { ContactInsightsTabs, ContactViewTab } from './_components/ContactInsightsTabs';
import { ContactInfoCard } from './_components/ContactInfoCard';
import { ContactInfoHeader } from './_components/ContactInfoHeader';

const ContactInfoPage = () => {
  const router = useRouter();
  const searchParams = useSearchParams();
  const contactId = searchParams.get('contact') ?? '';
  const tabParam = searchParams.get('tab');
  const activeTab = Object.values(ContactViewTab).includes(tabParam as ContactViewTab)
    ? (tabParam as ContactViewTab)
    : ContactViewTab.Company;

  const onTabChange = (tab: ContactViewTab) => {
    const params = new URLSearchParams(searchParams.toString());
    params.set('tab', tab);
    router.replace(`?${params.toString()}`);
  };

  const { authenticatedUserDetails } = useAuth();
  const tenantId = authenticatedUserDetails?.tenantId ?? '';

  const { data: contact, isLoading } = useGetLeadContactInfo(tenantId, contactId);

  const onBack = () => router.back();

  if (isLoading) return <DataLoader />;
  if (!contact) return <DataError />;

  const contactName = formatName({ firstName: contact.firstName, lastName: contact.lastName });

  const contactLocation = formatLocation({
    city: contact.locationCity,
    state: contact.locationState,
    country: contact.locationCountry,
  });

  const companyLocation = formatLocation({
    city: contact.companyHqCity,
    state: contact.companyHqState,
    country: contact.companyHqCountry,
  });

  const websiteUrl =
    contact.companyWebsiteUrl || (contact.companyDomain ? `https://${contact.companyDomain}` : '');

  return (
    <div className="bg-secondary flex flex-1 flex-col">
      <ContactInfoHeader
        contactName={contactName}
        contact={contact}
        contactLocation={contactLocation}
        onBack={onBack}
      />
      <div className="grid grid-cols-[320px_1fr] gap-6 p-6">
        <div className="space-y-4">
          <ContactInfoCard contact={contact} contactLocation={contactLocation} />
          <ContactActivityCard campaigns={contact.currentCampaigns ?? []} />
        </div>
        <div className="space-y-6">
          <ContactInsightsTabs contact={contact} activeTab={activeTab} onTabChange={onTabChange} />
          <ContactCompanyDetails
            contact={contact}
            companyLocation={companyLocation}
            websiteUrl={websiteUrl}
          />
        </div>
      </div>
    </div>
  );
};

export default ContactInfoPage;
