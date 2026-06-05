import { useRouter } from 'next/navigation';
import { ReactNode } from 'react';

import { Button } from '@/components/ui/button';
import { appRoutes } from '@/config/routes';
import { LeadSearchType } from '@/constants/leadSearch.constants';
import { cn } from '@/lib/utils/helpers';
import { useFilterContext } from '../_context/FilterContext';

type FilterPanelLayoutProps = {
  searchType: LeadSearchType;
  isFilterPanelOpen: boolean;
  filterPanel: ReactNode;
  content: ReactNode;
  disableAnimation?: boolean;
};

const tabs = [
  {
    label: 'Company Search',
    value: LeadSearchType.COMPANIES,
    route: `${appRoutes.leadgen.search.companySearch}?tabSwitch=true`,
  },
  {
    label: 'People Search',
    value: LeadSearchType.PEOPLE,
    route: `${appRoutes.leadgen.search.contactSearch}?tabSwitch=true`,
  },
];

const FilterPanelLayout = ({
  isFilterPanelOpen,
  filterPanel,
  content,
  searchType,
}: FilterPanelLayoutProps) => {
  const router = useRouter();
  const { updateFilters } = useFilterContext();
  const onTabSwitch = (route: string, destination: LeadSearchType) => {
    updateFilters((prev) => {
      const next = { ...(prev || {}), page: '0' };
      if (destination === LeadSearchType.PEOPLE) {
        next.regions = [];
      }
      return next;
    });
    router.push(route);
  };
  return (
    <div className="border-border w-full border-b">
      <div className="border-border h-14 w-full place-content-center border-b">
        <div className="flex h-full w-80 items-center">
          {tabs.map(({ label, value, route }) => {
            const isActive = searchType === value;
            return (
              <div key={value} className="relative flex h-full w-40 items-center justify-center">
                <Button
                  variant="none"
                  onClick={() => onTabSwitch(route, value)}
                  className={cn(
                    'text-md transition-colors',
                    isActive
                      ? 'border-sky-500 font-medium text-sky-500'
                      : 'text-muted-foreground hover:text-foreground border-transparent'
                  )}
                >
                  {label}
                  <div
                    className={cn(
                      'absolute right-0 bottom-0 left-0 h-0.5 rounded-full transition-colors',
                      isActive ? 'bg-primary' : 'bg-gray-300'
                    )}
                  />
                </Button>
              </div>
            );
          })}
        </div>
      </div>
      <div className="flex h-[calc(100vh-8rem)] w-full">
        <aside
          className={cn(
            'border-border bg-card flex h-full flex-col overflow-hidden border-r',
            'transition-all duration-300 ease-in-out',
            isFilterPanelOpen ? 'w-80' : 'pointer-events-none w-0'
          )}
          aria-hidden={!isFilterPanelOpen}
        >
          {filterPanel}
        </aside>
        <main className="flex flex-1 flex-col overflow-hidden">{content}</main>
      </div>
    </div>
  );
};

export { FilterPanelLayout };
