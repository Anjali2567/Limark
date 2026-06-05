'use client';

import { useMemo, useState } from 'react';
import { Controller, useForm, useWatch } from 'react-hook-form';
import { z } from 'zod';

import { MultiSelect } from '@/components/ui/multi-select';
import { EmployeeRange } from '@/constants/Campaign';
import { LeadQueryType } from '@/constants/leadQueries.constant';
import { useDebounce } from '@/hooks/useDebounce';
import { useGetLeadQueries } from '@/hooks/useGetLeadQueries';
import { LeadFilter } from '@/types/campaign.types';
import { defaultPaginationParams } from '@/types/paginated-params';
import { zodResolver } from '@hookform/resolvers/zod';
import { COMPANY_SIZE_OPTIONS } from '@/app/(modules)/leadgen/(dashboard-protected)/campaign-agent/_constants/filterOptions';

const filterSchema = z.object({
  companySize: z.array(z.nativeEnum(EmployeeRange)),
  titles: z.array(z.string()),
});

type FilterValues = z.infer<typeof filterSchema>;

type CampaignFiltersProps = {
  campaignState: LeadFilter;
  onSubmit: (filters: FilterValues) => void;
};

const queryParams = { ...defaultPaginationParams, sort: 'value,asc' };

const CampaignFilters = ({ campaignState, onSubmit }: CampaignFiltersProps) => {
  const [titleSearch, setTitleSearch] = useState<string>('');
  const [debouncedTitleSearch, setDebouncedTitleSearch] = useState<string>(titleSearch);

  useDebounce(titleSearch, 500, setDebouncedTitleSearch);

  const { data: titlesData, isLoading: isLoadingTitles } = useGetLeadQueries({
    types: [LeadQueryType.CONTACT_TITLE],
    query: debouncedTitleSearch,
    ...queryParams,
  });

  const titleOptions = useMemo(() => {
    const selectedTitles = campaignState?.titles ?? [];
    const apiOptions = titlesData?.content.map((t) => ({ label: t.value, value: t.value })) ?? [];

    const apiValues = new Set(apiOptions.map((o) => o.value));
    const extraOptions = selectedTitles
      .filter((t) => !apiValues.has(t))
      .map((t) => ({ label: t, value: t }));

    return [...extraOptions, ...apiOptions].sort((a, b) => {
      const aSelected = selectedTitles.includes(a.value);
      const bSelected = selectedTitles.includes(b.value);
      if (aSelected && !bSelected) return -1;
      if (!aSelected && bSelected) return 1;
      return 0;
    });
  }, [titlesData, campaignState?.titles]);

  const values = useMemo(
    () => ({
      companySize: campaignState?.employeeRanges ?? [],
      titles: campaignState?.titles ?? [],
    }),
    [campaignState]
  );

  const {
    control,
    handleSubmit,
    formState: { isDirty },
  } = useForm<FilterValues>({
    resolver: zodResolver(filterSchema),
    values,
  });

  const watchedValues = useWatch({ control });

  useDebounce(watchedValues, 1000, () => {
    if (isDirty) {
      handleSubmit(onSubmit)();
    }
  });

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="mb-6 space-y-4">
      <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
        <div className="space-y-2">
          <label className="text-foreground text-sm font-medium">Company Size</label>
          <Controller
            name="companySize"
            control={control}
            render={({ field }) => (
              <MultiSelect
                options={COMPANY_SIZE_OPTIONS}
                selected={field.value}
                onChange={field.onChange}
                placeholder="Select Company Size"
              />
            )}
          />
        </div>
        <div className="space-y-2">
          <label className="text-foreground text-sm font-medium">Target Designations</label>
          <Controller
            name="titles"
            control={control}
            render={({ field }) => (
              <MultiSelect
                options={titleOptions}
                selected={field.value}
                onChange={field.onChange}
                onSearchChange={setTitleSearch}
                isLoading={isLoadingTitles}
                placeholder="Select Designations"
              />
            )}
          />
        </div>
      </div>
    </form>
  );
};

export default CampaignFilters;
