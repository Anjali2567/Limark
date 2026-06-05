'use client';

import { Tabs } from '@/components/Tabs';
import { AnnouncementTab } from './_components/tabs/AnnouncementTab';
import { DefaultFiltersTab } from './_components/tabs/DefaultFiltersTab';
import { EmailRecipientsTab } from './_components/tabs/EmailRecipientsTab';

const TABS = [
  {
    value: 'recipients',
    label: 'CC/BCC Recipients',
    content: <EmailRecipientsTab />,
  },
  {
    value: 'filters',
    label: 'Default Search Filters',
    content: <DefaultFiltersTab />,
  },
  {
    value: 'announcements',
    label: 'Announcements',
    content: <AnnouncementTab />,
  },
];

const TenantSettings = () => {
  return (
    <div className="p-8">
      <Tabs
        title="Tenant Settings"
        subText="Configure default settings for all workspaces in your organization"
        className="mx-auto max-w-6xl p-4 md:p-8"
        defaultValue="recipients"
        tabs={TABS}
      />
    </div>
  );
};

export default TenantSettings;
