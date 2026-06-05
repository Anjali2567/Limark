'use client';

import { TypeAnimation } from 'react-type-animation';

import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { LeadType } from '@/constants/leadSearch.constants';

import { Search } from 'lucide-react';

const PEOPLE_SEQUENCE = [
  'Find VPs of Operations in Texas with 500-1000 employees',
  1800,
  'Search for Directors of Supply Chain in Michigan',
  1800,
  'Discover Operations Managers in Pennsylvania with 1000+ employees',
  1800,
] as const;

const COMPANY_SEQUENCE = [
  'Find companies in Michigan with 500-1000 employees',
  1800,
  'Search for companies in United States with 1000+ employees',
  1800,
  'Discover companies in California with 500-1000 employees',
  1800,
] as const;

type SearchInputProps = {
  searchQuery: string;
  setSearchQuery: (searchQuery: string) => void;
  onSearch: () => void;
  isPending: boolean;
  leadType?: LeadType;
};

const SearchInput = ({
  searchQuery,
  setSearchQuery,
  onSearch,
  isPending,
  leadType,
}: SearchInputProps) => {
  const sequence = leadType === LeadType.LEAD_COMPANY ? COMPANY_SEQUENCE : PEOPLE_SEQUENCE;

  return (
    <div className="bg-card border-border flex w-full max-w-4xl flex-col space-y-6 rounded-lg border p-8">
      <div className="space-y-2 text-center">
        <h2 className="text-foreground text-2xl font-bold">What are you looking for today?</h2>
        <p className="text-muted-foreground text-sm">
          Enter a prompt to generate filters to refine your search
        </p>
      </div>
      <div className="relative w-full">
        <Search className="text-muted-foreground absolute top-1/2 left-4 h-5 w-5 -translate-y-1/2 transform" />
        <Input
          type="text"
          aria-label="Find CEOs at SaaS companies in San Francisco with 50-200 employees"
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          onKeyDown={(e) => {
            if (e.key === 'Enter' && searchQuery.trim() && !isPending) {
              e.preventDefault();
              onSearch();
            }
          }}
          className="h-14 pl-12 text-base"
          autoFocus
        />
        {!searchQuery && (
          <div className="text-muted-foreground pointer-events-none absolute top-1/2 left-12 -translate-y-1/2 text-base md:text-sm">
            <TypeAnimation
              key={leadType}
              sequence={[...sequence]}
              speed={60}
              deletionSpeed={80}
              repeat={Infinity}
              cursor={true}
            />
          </div>
        )}
      </div>
      <div className="flex justify-end">
        <Button onClick={onSearch} disabled={!searchQuery.trim() || isPending} size="lg">
          {isPending ? 'Generating Results...' : 'Generate Results'}
        </Button>
      </div>
    </div>
  );
};

export { SearchInput };
