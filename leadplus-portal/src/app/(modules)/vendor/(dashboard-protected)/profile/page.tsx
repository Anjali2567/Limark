'use client';

import { useState } from 'react';

import { useGetAuthenticatedVendor } from '@/hooks/useAuthentication';
import { useGetAllVendorShowcases } from '@/hooks/useVendorShowcase';
import { ScrollArea } from '@/components/ui/scroll-area';
import { Button } from '@/components/ui/button';
import { Vendor } from '@/types/vendor.types';
import { ProfileSection, ProfileSectionId, ProfileSectionNav } from './_components/ProfileSectionNav';
import { CompanyInfoSection } from './_components/sections/CompanyInfoSection';
import { AboutTeamSection } from './_components/sections/AboutTeamSection';
import { ServicesSection } from './_components/sections/ServicesSection';
import { IndustriesSection } from './_components/sections/IndustriesSection';
import { AddressSection } from './_components/sections/AddressSection';
import { SocialMediaSection } from './_components/sections/SocialMediaSection';
import { CertificationsSection } from './_components/sections/CertificationsSection';
import { PortfolioSection } from './_components/sections/PortfolioSection';

import {
  Award,
  Briefcase,
  Building2,
  ChevronRight,
  FolderOpen,
  MapPin,
  Save,
  Share2,
  Tag,
  Users,
} from 'lucide-react';

const SECTIONS: ProfileSection[] = [
  { id: ProfileSectionId.COMPANY_INFO, label: 'Company Information', icon: Building2 },
  { id: ProfileSectionId.ABOUT_TEAM, label: 'About the Team', icon: Users },
  { id: ProfileSectionId.SERVICES, label: 'Services', icon: Briefcase },
  { id: ProfileSectionId.INDUSTRIES, label: 'Industries', icon: Tag },
  { id: ProfileSectionId.ADDRESS, label: 'Locations', icon: MapPin },
  { id: ProfileSectionId.SOCIAL_MEDIA, label: 'Social Media', icon: Share2 },
  { id: ProfileSectionId.CERTIFICATIONS, label: 'Certifications', icon: Award },
  { id: ProfileSectionId.PORTFOLIO, label: 'Portfolio', icon: FolderOpen },
];

type SectionCompleteness = {
  id: ProfileSectionId;
  isComplete: (vendor: Vendor | undefined, showcaseCount: number) => boolean;
};

const SECTION_COMPLETENESS: SectionCompleteness[] = [
  {
    id: ProfileSectionId.COMPANY_INFO,
    isComplete: (v) => !!(v?.companyName && v?.website && v?.tagline && v?.description),
  },
  {
    id: ProfileSectionId.ABOUT_TEAM,
    isComplete: (v) => !!v?.teamDescription,
  },
  {
    id: ProfileSectionId.SERVICES,
    isComplete: (v) => (v?.serviceIds?.length ?? 0) > 0,
  },
  {
    id: ProfileSectionId.INDUSTRIES,
    isComplete: (v) => (v?.industryIds?.length ?? 0) > 0,
  },
  {
    id: ProfileSectionId.ADDRESS,
    isComplete: (v) => !!(v?.address?.street || v?.address?.city),
  },
  {
    id: ProfileSectionId.SOCIAL_MEDIA,
    isComplete: (v) =>
      !!(v?.socialMedia?.linkedin || v?.socialMedia?.twitter || v?.socialMedia?.facebook),
  },
  {
    id: ProfileSectionId.CERTIFICATIONS,
    isComplete: (v) => (v?.certifications?.length ?? 0) > 0,
  },
  {
    id: ProfileSectionId.PORTFOLIO,
    isComplete: (_v, showcaseCount) => showcaseCount > 0,
  },
];

const SECTION_TITLES: Record<ProfileSectionId, { title: string; description: string }> = {
  [ProfileSectionId.COMPANY_INFO]: {
    title: 'Company Information',
    description: 'Help buyers learn more about your business by adding the profile items below.',
  },
  [ProfileSectionId.ABOUT_TEAM]: {
    title: 'About the Team',
    description: 'Give buyers a sense of the culture and people behind your company.',
  },
  [ProfileSectionId.SERVICES]: {
    title: 'Services',
    description: 'Search and add your services, then select the relevant specifications under each one.',
  },
  [ProfileSectionId.INDUSTRIES]: {
    title: 'Industries',
    description: 'Select the industries your company serves. This helps buyers in specific verticals find you.',
  },
  [ProfileSectionId.ADDRESS]: {
    title: 'Locations',
    description: 'Add your office locations. Buyers often search for vendors in their region.',
  },
  [ProfileSectionId.SOCIAL_MEDIA]: {
    title: 'Social Media',
    description: 'Connect your social profiles so buyers can learn more about your company.',
  },
  [ProfileSectionId.CERTIFICATIONS]: {
    title: 'Certifications',
    description: 'Showcase your certifications, accreditations, and awards to build buyer confidence.',
  },
  [ProfileSectionId.PORTFOLIO]: {
    title: 'Portfolio',
    description: 'Showcase past work to demonstrate your capabilities and experience.',
  },
};

const VendorProfilePage = () => {
  const [activeSection, setActiveSection] = useState<ProfileSectionId>(ProfileSectionId.COMPANY_INFO);
  const { data: vendor } = useGetAuthenticatedVendor();

  const { data: showcases = [] } = useGetAllVendorShowcases({
    tenantId: vendor?.tenantId || '',
    vendorId: vendor?.id || '',
  });

  const completedSections = new Set<ProfileSectionId>(
    SECTION_COMPLETENESS.filter((s) => s.isComplete(vendor, showcases.length)).map((s) => s.id)
  );
  const completedCount = completedSections.size;
  const progressPct = Math.round((completedCount / SECTION_COMPLETENESS.length) * 100);

  const meta = SECTION_TITLES[activeSection];

  const currentIdx = SECTIONS.findIndex((s) => s.id === activeSection);
  const nextSection = SECTIONS[currentIdx + 1] ?? null;

  const handleNext = () => {
    const formEl = document.getElementById(activeSection) as HTMLFormElement | null;
    formEl?.requestSubmit();
    setActiveSection(nextSection!.id);
  };

  const renderSection = () => {
    switch (activeSection) {
      case ProfileSectionId.COMPANY_INFO:
        return <CompanyInfoSection vendor={vendor} formId={activeSection} />;
      case ProfileSectionId.ABOUT_TEAM:
        return <AboutTeamSection vendor={vendor} formId={activeSection} />;
      case ProfileSectionId.SERVICES:
        return <ServicesSection vendor={vendor} formId={activeSection} />;
      case ProfileSectionId.INDUSTRIES:
        return <IndustriesSection vendor={vendor} formId={activeSection} />;
      case ProfileSectionId.ADDRESS:
        return <AddressSection vendor={vendor} formId={activeSection} />;
      case ProfileSectionId.SOCIAL_MEDIA:
        return <SocialMediaSection vendor={vendor} formId={activeSection} />;
      case ProfileSectionId.CERTIFICATIONS:
        return <CertificationsSection vendor={vendor} formId={activeSection} />;
      case ProfileSectionId.PORTFOLIO:
        return <PortfolioSection />;
      default:
        return null;
    }
  };

  return (
    <ScrollArea className="h-full">
      <div className="mx-auto max-w-6xl space-y-6 px-6 py-6">
        <div className="flex flex-col gap-4 md:flex-row md:items-start md:justify-between">
          <div>
            <h1 className="text-foreground text-md font-bold md:text-3xl">Profile</h1>
            <p className="text-muted-foreground mt-1 text-sm md:text-lg">
              Complete your vendor profile to attract more clients and opportunities.
            </p>
          </div>
          {activeSection !== ProfileSectionId.PORTFOLIO && (
            <Button
              className="gap-2 bg-sky-500 text-white hover:bg-sky-600"
              form={activeSection}
              type="submit"
            >
              <Save className="h-4 w-4" />
              Save Changes
            </Button>
          )}
        </div>
        <div className="bg-card border-border rounded-lg border p-4">
          <div className="mb-2 flex items-center justify-between">
            <span className="text-foreground text-sm font-medium">Profile Completeness</span>
            <span className="text-primary text-sm font-semibold">{progressPct}%</span>
          </div>
          <div
            className="bg-secondary h-2 overflow-hidden rounded-full"
            role="progressbar"
            aria-valuenow={progressPct}
            aria-valuemin={0}
            aria-valuemax={100}
          >
            <div
              className="bg-primary h-full rounded-full transition-[width] duration-500 ease-out"
              style={{ width: `${progressPct}%` }}
            />
          </div>
          <p className="text-muted-foreground mt-2 text-xs leading-relaxed">
            <span className="font-medium">{completedCount}</span> of{' '}
            <span className="font-medium">{SECTION_COMPLETENESS.length}</span> sections complete. A
            complete profile gets{' '}
            <span className="text-foreground font-medium">3× more visibility</span>.
          </p>
        </div>
        <div className="flex gap-8">
          <aside className="w-72 shrink-0">
            <ProfileSectionNav
              sections={SECTIONS}
              activeSection={activeSection}
              completedSections={completedSections}
              onSelect={setActiveSection}
            />
          </aside>
          <main className="min-w-0 flex-1">
            <div className="border-border bg-card rounded-xl border p-6">
              <div className="border-border mb-6 border-b pb-6">
                <h2 className="text-lg font-semibold">{meta.title}</h2>
                <p className="text-muted-foreground mt-0.5 text-sm">{meta.description}</p>
              </div>
              {renderSection()}
              <div className="border-border mt-8 flex justify-end gap-3 border-t pt-6">
                {activeSection !== ProfileSectionId.PORTFOLIO && (
                  <Button
                    variant="outline"
                    className="border-primary text-primary hover:bg-primary/5"
                    form={activeSection}
                    type="submit"
                  >
                    <Save className="mr-2 h-4 w-4" />
                    Save Section
                  </Button>
                )}
                {nextSection && (
                  <Button
                    type="button"
                    className="bg-sky-500 text-white hover:bg-sky-600"
                    onClick={handleNext}
                  >
                    Next: {nextSection.label}
                    <ChevronRight className="ml-2 h-4 w-4" />
                  </Button>
                )}
              </div>
            </div>
          </main>
        </div>
      </div>
    </ScrollArea>
  );
};

export default VendorProfilePage;
