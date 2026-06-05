'use client';

import { useState } from 'react';

import { Tabs } from '@/components/Tabs';
import { DailyEmailLimitTab } from './_components/DailyEmailLimitTab';
import { EmailListTab } from './_components/EmailListTab';

export default function EmailsPage() {
  const [isBusy, setIsBusy] = useState<boolean>(false);
  const tabs = [
    {
      value: 'ccBcc',
      label: 'CC/BCC List',
      disabled: isBusy,
      content: <EmailListTab setIsBusy={setIsBusy} />,
    },
    {
      value: 'dailyLimit',
      label: 'Daily Limit',
      disabled: isBusy,
      content: <DailyEmailLimitTab setIsBusy={setIsBusy} />,
    },
  ];

  return (
    <div className="p-8">
      <Tabs
        title="Email"
        subText="Manage email configuration and preferences"
        className="mx-auto max-w-6xl p-4 md:p-8"
        defaultValue="ccBcc"
        tabs={tabs}
      />
    </div>
  );
}
