'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { useForm, useWatch } from 'react-hook-form';
import { TypeAnimation } from 'react-type-animation';
import { toast } from 'sonner';

import { Button } from '@/components/ui/button';
import { useParseVendorQuery } from '@/hooks/useVendor';
import { VendorSearchParseResult } from '@/types/vendor.types';

import { Search } from 'lucide-react';

type SearchFormValues = {
  query: string;
};

const searchSuggestions: Record<string, string[]> = {
  services: ['SAP S/4HANA implementation', 'Cloud migration', 'Custom software development'],
  technologies: ['Salesforce consulting', 'React/Node.js development', 'Azure DevOps'],
  trending: ['AI/ML integration', 'SOC2 compliance audit', 'Kubernetes migration'],
};

const animatedTexts = [
  ...searchSuggestions.services,
  ...searchSuggestions.technologies,
  ...searchSuggestions.trending,
];

const buildSearchParams = (result: VendorSearchParseResult): string => {
  const params = new URLSearchParams();
  if (result.serviceIds?.length) params.set('serviceIds', result.serviceIds.join(','));
  if (result.specificationIds?.length) params.set('specificationIds', result.specificationIds.join(','));
  return params.toString();
};

const SearchBar = () => {
  const router = useRouter();
  const [searchFocused, setSearchFocused] = useState(false);
  const { mutateAsync: parseQuery, isPending: isParsingQuery } = useParseVendorQuery();

  const { register, handleSubmit, control } = useForm<SearchFormValues>({
    defaultValues: { query: '' },
  });

  const queryValue = useWatch({ control, name: 'query' });

  const onSubmit = async ({ query }: SearchFormValues) => {
    const trimmed = query.trim();
    if (!trimmed) return;

    await parseQuery(trimmed, {
      onSuccess: (result) => {
        if (result.serviceIds.length === 0 && result.specificationIds.length === 0) {
          toast.info(
            'No specific services identified in your query.'
          );
          return;
        }
        router.push(`/customer/search?${buildSearchParams(result)}`);
      },
      onError: () => {
        toast.error('Failed to parse your query. Please try again with different keywords.');
      },
    });
  };

  return (
    <form
      onSubmit={handleSubmit(onSubmit)}
      className="lp-search-box animate-lp-fade-up-d2 relative max-w-145 rounded-2xl p-1.25"
      style={{ background: 'white', boxShadow: '0 20px 60px -15px rgba(0,0,0,0.4)' }}
    >
      <div className="flex items-center">
        <div className="flex flex-1 items-center gap-3 px-4 py-3.5">
          <Search className="size-4.5 shrink-0 text-slate-400" />
          <div className="relative flex-1">
            <input
              {...register('query')}
              onFocus={() => setSearchFocused(true)}
              onBlur={() => setTimeout(() => setSearchFocused(false), 200)}
              className="text-lp-dark w-full border-none bg-transparent text-[15px] outline-none"
            />
            {!queryValue && !searchFocused && (
              <div className="pointer-events-none absolute top-1/2 left-0 -translate-y-1/2 text-[15px] text-slate-400">
                <TypeAnimation
                  sequence={animatedTexts.flatMap((text) => [text, 2000])}
                  speed={50}
                  repeat={Infinity}
                  cursor
                />
              </div>
            )}
          </div>
        </div>
        <Button
          type="submit"
          disabled={isParsingQuery}
          className="lp-cta-btn h-auto rounded-[10px] px-6 py-3 text-sm font-semibold text-white"
        >
          {isParsingQuery ? 'Searching...' : 'Find Vendors'}
        </Button>
      </div>
    </form>
  );
};

export { SearchBar };
