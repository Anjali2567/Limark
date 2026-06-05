'use client';

import { Button } from '@/components/ui/button';
import { ScrollArea } from '@/components/ui/scroll-area';
import { cn } from '@/lib/utils/helpers';
import { LeadContactData } from '@/types/leadSearch.types';

import { TagsTab } from '../../_components/TagsTab';
import { ContactCompanyTab } from './ContactCompanyTab';
import { ContactHistoryTab } from './ContactHistoryTab';

export enum ContactViewTab {
  Company = 'company',
  Keywords = 'keywords',
  Technologies = 'technologies',
  History = 'history',
}

const TABS: { id: ContactViewTab; label: string }[] = [
  { id: ContactViewTab.Company, label: 'Company' },
  { id: ContactViewTab.Keywords, label: 'Keywords' },
  { id: ContactViewTab.Technologies, label: 'Technologies' },
  { id: ContactViewTab.History, label: 'History' },
];

type Props = {
  contact: LeadContactData;
  activeTab: ContactViewTab;
  onTabChange: (tab: ContactViewTab) => void;
};

export const ContactInsightsTabs = ({ contact, activeTab, onTabChange }: Props) => {
  const renderTabContent = () => {
    switch (activeTab) {
      case ContactViewTab.Company:
        return <ContactCompanyTab contact={contact} />;
      case ContactViewTab.Keywords:
        return (
          <TagsTab items={contact.companyKeywords ?? []} emptyMessage="No keywords available." />
        );
      case ContactViewTab.Technologies:
        return (
          <TagsTab
            items={contact.companyTechnologies ?? []}
            emptyMessage="No technologies available."
          />
        );
      case ContactViewTab.History:
        return <ContactHistoryTab contact={contact} />;
      default:
        return null;
    }
  };

  return (
    <div className="border-border bg-card rounded-lg border">
      <div className="border-border border-b px-4 py-3">
        <h2 className="text-foreground text-sm font-medium">Professional Overview</h2>
      </div>
      <div className="border-border flex items-center gap-1 border-b px-4 py-2">
        {TABS.map(({ id, label }) => (
          <Button
            key={id}
            variant={activeTab === id ? 'secondary' : 'ghost'}
            size="sm"
            className={cn(
              'text-xs',
              activeTab !== id &&
                'text-muted-foreground hover:text-muted-foreground hover:scale-105 hover:bg-white'
            )}
            onClick={() => onTabChange(id)}
          >
            {label}
          </Button>
        ))}
      </div>
      <ScrollArea className="h-fit max-h-72 overflow-y-auto p-4">{renderTabContent()}</ScrollArea>
    </div>
  );
};
