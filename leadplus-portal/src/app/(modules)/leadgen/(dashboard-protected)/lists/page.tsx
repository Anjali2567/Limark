'use client';

import { useState } from 'react';
import { toast } from 'sonner';

import { Page } from '@/components/Page';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { LeadType } from '@/constants/leadSearch.constants';
import { useAuth } from '@/context/AuthContext';
import { useDebounce } from '@/hooks/useDebounce';
import { useDeleteLeadList } from '@/hooks/useLeadList';
import { cn } from '@/lib/utils/helpers';
import { CompanyListTab } from './_components/CompanyListTab';
import { CreateListPopover } from './_components/CreateListPopover';
import { PeopleListTab } from './_components/PeopleListTab';

import { Building2, Search, Trash2, Users } from 'lucide-react';

const SEARCH_DEBOUNCE_DELAY = 300;

const TABS = [
  { value: LeadType.LEAD_CONTACT, label: 'People', Icon: Users },
  { value: LeadType.LEAD_COMPANY, label: 'Companies', Icon: Building2 },
] as const;

const LeadListPage = () => {
  const [activeTab, setActiveTab] = useState<LeadType>(LeadType.LEAD_CONTACT);
  const [searchQuery, setSearchQuery] = useState<string>('');
  const [debouncedSearchQuery, setDebouncedSearchQuery] = useState<string>('');

  const [selectedIds, setSelectedIds] = useState<string[]>([]);
  const [peopleCount, setPeopleCount] = useState<number>(0);
  const [companyCount, setCompanyCount] = useState<number>(0);

  const { authenticatedUserDetails } = useAuth();
  const tenantId = authenticatedUserDetails?.tenantId ?? '';
  const workspaceId = authenticatedUserDetails?.workspaceId ?? '';

  useDebounce(searchQuery, SEARCH_DEBOUNCE_DELAY, (val) => setDebouncedSearchQuery(val));

  const { mutate: deleteList, isPending: isDeleting } = useDeleteLeadList(activeTab);

  const counts: Record<LeadType, number> = {
    [LeadType.LEAD_CONTACT]: peopleCount,
    [LeadType.LEAD_COMPANY]: companyCount,
  };

  const handleTabChange = (tab: LeadType) => {
    setActiveTab(tab);
    setSearchQuery('');
    setDebouncedSearchQuery('');
    setSelectedIds([]);
  };

  const handleBulkDelete = () => {
    const ids = [...selectedIds];
    ids.forEach((listId) => {
      deleteList(
        { tenantId, workspaceId, listId },
        { onError: () => toast.error('Failed to delete one or more lists') }
      );
    });
    setSelectedIds([]);
    toast.success(`Deleted ${ids.length} list${ids.length !== 1 ? 's' : ''}`);
  };

  return (
    <Page className="flex-col">
      <div className="border-border bg-card flex items-center justify-between border-b px-6 py-4">
        <h1 className="text-foreground text-lg font-semibold">My lists</h1>
        <div className="flex items-center gap-3">
          {selectedIds.length > 0 && (
            <Button variant="destructive" onClick={handleBulkDelete} disabled={isDeleting}>
              <Trash2 className="mr-2 h-4 w-4" />
              Delete ({selectedIds.length})
            </Button>
          )}
          <div className="relative w-72">
            <Search className="text-muted-foreground absolute top-1/2 left-3 h-4 w-4 -translate-y-1/2" />
            <Input
              type="text"
              placeholder={
                activeTab === LeadType.LEAD_CONTACT
                  ? 'Search people lists...'
                  : 'Search company lists...'
              }
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="h-9 pl-10"
            />
          </div>
          <CreateListPopover
            tenantId={tenantId}
            workspaceId={workspaceId}
            handleTabChange={handleTabChange}
          />
        </div>
      </div>
      <div className="border-border bg-card flex items-center gap-8 border-b px-6">
        {TABS.map(({ value, label, Icon }) => {
          const isActive = activeTab === value;
          const count = counts[value];
          return (
            <Button
              key={value}
              onClick={() => handleTabChange(value)}
              className={cn(
                'text-md flex h-full items-center gap-2 rounded-none border-b-2 bg-transparent py-3 transition-colors hover:bg-transparent',
                isActive
                  ? 'border-primary text-foreground font-semibold'
                  : 'text-muted-foreground hover:text-foreground border-transparent'
              )}
            >
              <Icon className="h-4 w-4" />
              {label}
              <span className="bg-muted text-muted-foreground rounded-full px-2 py-0.5 text-xs">
                {count}
              </span>
            </Button>
          );
        })}
      </div>
      <div className={activeTab !== LeadType.LEAD_CONTACT ? 'hidden' : undefined}>
        <PeopleListTab
          tenantId={tenantId}
          workspaceId={workspaceId}
          searchQuery={activeTab === LeadType.LEAD_CONTACT ? debouncedSearchQuery : ''}
          selectedIds={selectedIds}
          onSelectionChange={setSelectedIds}
          onCountChange={setPeopleCount}
        />
      </div>
      <div className={activeTab !== LeadType.LEAD_COMPANY ? 'hidden' : undefined}>
        <CompanyListTab
          tenantId={tenantId}
          workspaceId={workspaceId}
          searchQuery={activeTab === LeadType.LEAD_COMPANY ? debouncedSearchQuery : ''}
          selectedIds={selectedIds}
          onSelectionChange={setSelectedIds}
          onCountChange={setCompanyCount}
        />
      </div>
    </Page>
  );
};

export default LeadListPage;
