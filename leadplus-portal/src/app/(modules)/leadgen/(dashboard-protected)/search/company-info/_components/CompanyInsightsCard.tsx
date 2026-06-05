import { Button } from '@/components/ui/button';
import { ScrollArea } from '@/components/ui/scroll-area';
import { useGetLeadCompanyJobsInfinite } from '@/hooks/useLeads';
import { cn } from '@/lib/utils/helpers';
import { LeadCompanyData } from '@/types/leadSearch.types';
import { CompanyFinancialTab } from './CompanyFinancialTab';
import { CompanyJobPostingsTab } from './CompanyJobPostingsTab';
import { TagsTab } from '../../_components/TagsTab';
import { CompanyOverviewTab } from './CompanyOverviewTab';

export enum CompanyViewTab {
  Overview = 'overview',
  Technologies = 'technologies',
  Keywords = 'keywords',
  Financial = 'financial',
  JobPostings = 'job-postings',
}

type Props = {
  company: LeadCompanyData;
  location: string;
  insightTab: CompanyViewTab;
  onTabChange: (tab: CompanyViewTab) => void;
};

export const CompanyInsightsCard = ({ company, location, insightTab, onTabChange }: Props) => {
  const {
    data: jobsData,
    isLoading: isJobsLoading,
    isFetchingNextPage,
    hasNextPage,
    fetchNextPage,
  } = useGetLeadCompanyJobsInfinite(company.id);

  const jobsTotalCount = jobsData?.pages[0]?.page.totalElements ?? null;

  const insightTabs = [
    { id: CompanyViewTab.Overview, label: 'Overview', count: null },
    { id: CompanyViewTab.JobPostings, label: 'Job Postings', count: jobsTotalCount },
    {
      id: CompanyViewTab.Technologies,
      label: 'Technologies',
      count: company.technologies?.length || null,
    },
    { id: CompanyViewTab.Keywords, label: 'Keywords', count: company.keywords?.length || null },
  ];

  const renderTabContent = () => {
    switch (insightTab) {
      case CompanyViewTab.Overview:
        return <CompanyOverviewTab company={company} location={location} />;
      case CompanyViewTab.Keywords:
        return <TagsTab items={company.keywords ?? []} emptyMessage="No keywords listed." />;
      case CompanyViewTab.Technologies:
        return (
          <TagsTab items={company.technologies ?? []} emptyMessage="No technologies listed." />
        );
      case CompanyViewTab.Financial:
        return <CompanyFinancialTab company={company} />;
      case CompanyViewTab.JobPostings:
        return (
          <CompanyJobPostingsTab
            jobs={jobsData ? jobsData.pages.flatMap((page) => page.content) : []}
            isLoading={isJobsLoading}
            isFetchingNextPage={isFetchingNextPage}
            hasNextPage={hasNextPage}
            fetchNextPage={fetchNextPage}
          />
        );
      default:
        return null;
    }
  };

  return (
    <div className="border-border bg-card rounded-lg border">
      <div className="flex items-center justify-between px-5 py-4">
        <h2 className="text-foreground text-base font-semibold">Company insights</h2>
      </div>
      <div className="border-border flex items-center gap-1 border-b px-4 py-2">
        {insightTabs.map(({ id, label, count }) => (
          <Button
            key={id}
            variant={insightTab === id ? 'secondary' : 'ghost'}
            size="sm"
            className={cn(
              'text-xs',
              insightTab !== id &&
                'text-muted-foreground hover:text-muted-foreground hover:scale-105 hover:bg-white'
            )}
            onClick={() => onTabChange(id)}
          >
            {label}
            {count ? <span className="text-muted-foreground ml-1">{count}</span> : null}
          </Button>
        ))}
      </div>
      <ScrollArea className="h-fit max-h-72 overflow-y-auto p-4">{renderTabContent()}</ScrollArea>
    </div>
  );
};
