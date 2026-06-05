'use client';

import { Page } from '@/components/Page';
import { FeedbackTable } from './_components/FeedbackTable';

const FeedbacksPage = () => {
  return (
    <Page className="flex-col space-y-6 p-8">
      <div className="flex flex-col">
        <h1 className="text-foreground text-3xl font-bold">Feedbacks</h1>
        <p className="text-muted-foreground">Review and respond to feedback submitted by users</p>
      </div>
      <FeedbackTable />
    </Page>
  );
};

export default FeedbacksPage;
