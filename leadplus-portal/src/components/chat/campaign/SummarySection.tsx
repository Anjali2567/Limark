'use client';

import { useMemo } from 'react';

import { useAuth } from '@/context/AuthContext';
import { useUpdateTargetCriteria } from '@/hooks/useCampaignAgent';
import useToggle from '@/hooks/useToggle';
import { Campaign } from '@/types/campaign.types';
import { CampaignTargetResults } from '../../../app/(modules)/leadgen/(dashboard-protected)/campaign/summary/CampaignTargetResults';
import { EmployeeRange } from '@/constants/Campaign';
import { CampaignStatsCards } from './CampaignStatsCards';
import CampaignFilters from './CampaignFilters';

type SummarySectionProps = {
  campaign: Campaign;
  isLast: boolean;
  isLoading: boolean;
};

const SummarySection = ({ campaign, isLast, isLoading }: SummarySectionProps) => {
  const { authenticatedUserDetails } = useAuth();
  const { value: showCompaniesDrawer, toggle: toggleCompaniesDrawer } = useToggle(false);

  const tenantId = authenticatedUserDetails?.tenantId ?? '';
  const workspaceId = authenticatedUserDetails?.workspaceId ?? '';

  const { mutate: updateTargetCriteria, isPending } = useUpdateTargetCriteria();

  const locationCount = useMemo(() => {
    const leadFilter = campaign.leadFilter;
    const companyCities = leadFilter?.companyCities;
    const companyStates = leadFilter?.companyStates;
    const companyCountries = leadFilter?.companyCountries;
    const regions = leadFilter?.regions;
    const cities = leadFilter?.cities;
    const states = leadFilter?.states;
    const countries = leadFilter?.countries;

    return (
      (companyCities?.length || 0) +
      (companyStates?.length || 0) +
      (companyCountries?.length || 0) +
      (regions?.length || 0) +
      (cities?.length || 0) +
      (states?.length || 0) +
      (countries?.length || 0)
    );
  }, [campaign]);

  const handleFilterSubmit = (filters: { companySize: EmployeeRange[]; titles: string[] }) => {
    updateTargetCriteria({
      tenantId,
      workspaceId,
      chatMemoryId: campaign.id,
      payload: {
        ...campaign.leadFilter,
        employeeRanges: filters.companySize,
        titles: filters.titles,
      },
    });
  };

  return (
    <div className="flex flex-col gap-3">
      {/* // TODO: Re-enable filters once we have the API to support updating criteria on existing campaigns */}
      {/* {campaign.leadFilter && isLast && (
        <CampaignFilters campaignState={campaign?.leadFilter} onSubmit={handleFilterSubmit} />
      )} */}
      <CampaignStatsCards
        companies={campaign?.totalCompanies}
        contacts={campaign?.totalContacts}
        locations={locationCount}
        isLoading={isLoading || isPending}
      />
      <CampaignTargetResults
        campaignId={campaign?.id}
        tenantId={tenantId}
        workspaceId={workspaceId}
        result={campaign?.campaignCompanies ?? []}
        open={showCompaniesDrawer}
        toggle={toggleCompaniesDrawer}
        isViewMode
        hideTrigger
        isLoading={isLoading}
      />
    </div>
  );
};

export { SummarySection };
