'use client';

import { KeyboardEvent, useState } from 'react';
import { Control, Controller, UseFormSetValue, useWatch } from 'react-hook-form';
import { z } from 'zod';

import { LeadQueryType } from '@/constants/leadQueries.constant';
import { useDebounce } from '@/hooks/useDebounce';
import { useGetLeadQueries } from '@/hooks/useGetLeadQueries';
import { Building2, Plus, X, XCircle } from 'lucide-react';

import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { SearchableMultiSelect } from '@/components/ui/searchable-multi-select';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { Tooltip, TooltipContent, TooltipTrigger } from '@/components/ui/tooltip';
import { TenantAnnouncementContactSource } from '@/constants/announcement.constants';

export const contactFilterSchema = z.object({
  contactType: z.nativeEnum(TenantAnnouncementContactSource),
  company: z.array(z.string()),
  location: z.array(z.object({ value: z.string(), identifier: z.string().optional() })),
  keywords: z.array(z.string()),
});

export type ContactFilterValues = z.infer<typeof contactFilterSchema>;

export const contactFilterDefaults: ContactFilterValues = {
  contactType: TenantAnnouncementContactSource.LEAD,
  company: [],
  location: [],
  keywords: [],
};

type FilterTagInputProps = {
  placeholder?: string;
  values: string[];
  onChange: (values: string[]) => void;
};

const FilterTagInput = ({ placeholder, values, onChange }: FilterTagInputProps) => {
  const [input, setInput] = useState('');

  const add = () => {
    const trimmed = input.trim();
    if (trimmed && !values.includes(trimmed)) onChange([...values, trimmed]);
    setInput('');
  };

  const handleKeyDown = (e: KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter') {
      e.preventDefault();
      add();
    }
  };

  return (
    <div className="space-y-1.5">
      <div className="flex gap-1.5">
        <Input
          className="h-8 flex-1 text-xs"
          placeholder={placeholder}
          value={input}
          onChange={(e) => setInput(e.target.value)}
          onKeyDown={handleKeyDown}
        />
        <Button
          type="button"
          variant="outline"
          size="icon"
          className="h-8 w-8 shrink-0"
          onClick={add}
          disabled={!input.trim()}
        >
          <Plus className="h-3.5 w-3.5" />
        </Button>
      </div>
      {values.length > 0 && (
        <div className="flex flex-wrap gap-1">
          {values.map((v) => (
            <Badge
              key={v}
              className="bg-primary/10 text-primary gap-1 rounded-md px-2 py-0.5 text-xs font-medium"
            >
              {v}
              <button
                type="button"
                onClick={() => onChange(values.filter((x) => x !== v))}
                className="hover:text-primary/70 ml-0.5"
              >
                <X className="h-3 w-3" />
              </button>
            </Badge>
          ))}
        </div>
      )}
    </div>
  );
};

type ContactImportFiltersProps = {
  control: Control<ContactFilterValues>;
  setValue: UseFormSetValue<ContactFilterValues>;
};

const ContactImportFilters = ({ control, setValue }: ContactImportFiltersProps) => {
  const [locationSearch, setLocationSearch] = useState('');
  const [debouncedLocationSearch, setDebouncedLocationSearch] = useState('');
  useDebounce(locationSearch, 400, setDebouncedLocationSearch);

  const { data: locationOptions, isLoading: isLoadingLocations } = useGetLeadQueries({
    types: [LeadQueryType.COMPANY_CITY, LeadQueryType.COMPANY_STATE, LeadQueryType.COMPANY_COUNTRY],
    query: debouncedLocationSearch,
    sort: 'value,asc',
    page: 0,
    size: 20,
  });

  const locationOptionItems =
    locationOptions?.content.map((l) => ({
      label: l.value,
      value: l.value,
      identifier: l.type,
    })) ?? [];
  const contactType = useWatch({ control, name: 'contactType' });
  const company = useWatch({ control, name: 'company' });
  const location = useWatch({ control, name: 'location' });
  const keywords = useWatch({ control, name: 'keywords' });

  const leadFilterCount = company.length + location.length + keywords.length;

  return (
    <>
      <div className="space-y-1.5">
        <Label className="text-muted-foreground text-xs font-medium">Contact Type</Label>
        <Controller
          control={control}
          name="contactType"
          render={({ field }) => (
            <Select value={field.value} onValueChange={field.onChange}>
              <SelectTrigger className="w-full">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value={TenantAnnouncementContactSource.LEAD}>
                  <div className="flex items-center gap-2">
                    <Building2 className="h-4 w-4" />
                    <span>Leads</span>
                  </div>
                </SelectItem>
                <SelectItem value={TenantAnnouncementContactSource.CRM}>
                  <div className="flex items-center gap-2">
                    <Building2 className="h-4 w-4" />
                    <span>CRM</span>
                  </div>
                </SelectItem>
              </SelectContent>
            </Select>
          )}
        />
      </div>
      {contactType === TenantAnnouncementContactSource.LEAD && (
        <div className="border-border space-y-3 border-t pt-2.5">
          <div className="flex items-center justify-between">
            <span className="text-foreground text-xs font-semibold">Lead Filters</span>
            {leadFilterCount > 0 && (
              <Tooltip>
                <TooltipTrigger asChild>
                  <Button
                    variant="none"
                    type="button"
                    onClick={() => {
                      setValue('company', []);
                      setValue('location', []);
                      setValue('keywords', []);
                    }}
                    className="group h-auto p-0"
                  >
                    <Badge className="bg-primary text-primary-foreground pointer-events-none flex items-center rounded-full border-0 px-1.5 py-0.5 text-[10px] font-medium shadow-none">
                      {leadFilterCount}
                      <XCircle className="hidden h-4 w-4 group-hover:flex" />
                    </Badge>
                  </Button>
                </TooltipTrigger>
                <TooltipContent side="top">Clear Filters</TooltipContent>
              </Tooltip>
            )}
          </div>
          <div className="space-y-1.5">
            <Label className="text-muted-foreground text-xs font-medium">Company</Label>
            <Controller
              control={control}
              name="company"
              render={({ field }) => (
                <FilterTagInput
                  placeholder="Add company..."
                  values={field.value}
                  onChange={field.onChange}
                />
              )}
            />
          </div>

          <div className="space-y-1.5">
            <Label className="text-muted-foreground text-xs font-medium">Keywords</Label>
            <Controller
              control={control}
              name="keywords"
              render={({ field }) => (
                <FilterTagInput
                  placeholder="Add keyword..."
                  values={field.value}
                  onChange={field.onChange}
                />
              )}
            />
          </div>

          <div className="space-y-1.5">
            <Label className="text-muted-foreground text-xs font-medium">Location</Label>
            <Controller
              control={control}
              name="location"
              render={({ field }) => (
                <>
                  <SearchableMultiSelect
                    selected={field.value}
                    options={locationOptionItems}
                    onChange={(items) =>
                      field.onChange(
                        items.map((item) =>
                          typeof item === 'string'
                            ? { value: item }
                            : { value: item.value, identifier: item.identifier }
                        )
                      )
                    }
                    onSearchChange={setLocationSearch}
                    isLoading={isLoadingLocations}
                    placeholder="City, state or country..."
                  />
                  {field.value.length > 0 && (
                    <div className="flex flex-wrap gap-1">
                      {field.value.map((l) => {
                        const key = l.identifier ? `${l.identifier}:${l.value}` : l.value;
                        return (
                          <Badge
                            key={key}
                            className="bg-primary/10 text-primary gap-1 rounded-md px-2 py-0.5 text-xs font-medium"
                          >
                            {l.value}
                            <button
                              type="button"
                              onClick={() => field.onChange(field.value.filter((x) => x !== l))}
                              className="hover:text-primary/70 ml-0.5"
                            >
                              <X className="h-3 w-3" />
                            </button>
                          </Badge>
                        );
                      })}
                    </div>
                  )}
                </>
              )}
            />
          </div>
        </div>
      )}
    </>
  );
};

export { ContactImportFilters };
