import { useSearchParams } from 'next/navigation';
import { useMemo } from 'react';

import { useAuth } from '@/context/AuthContext';
import { useGetCampaignEmails } from '@/hooks/useCampaignEmail';
import { EmailSequence } from '../../../configure-sequence/_components/EmailSequence';
import { useGetCampaignById } from '@/hooks/useCampaign';

const EmailTemplatesTab = () => {
  const searchParams = useSearchParams();
  const { authenticatedUserDetails } = useAuth();
  const id = searchParams.get('id');

  const { data: campaignData } = useGetCampaignById({
    tenantId: authenticatedUserDetails?.tenantId || '',
    workspaceId: authenticatedUserDetails?.workspaceId || '',
    campaignId: id || '',
  });

  const { data = [], isLoading } = useGetCampaignEmails({
    tenantId: authenticatedUserDetails?.tenantId || '',
    workspaceId: authenticatedUserDetails?.workspaceId || '',
    campaignId: id || '',
  });

  const sortedCampaigns = useMemo(() => {
    return [...data].sort((a, b) => a.stepNumber - b.stepNumber);
  }, [data]);

  const activeMailboxId = useMemo(() => {
    return campaignData?.sendingMailboxId || '';
  }, [campaignData?.sendingMailboxId]);

  if (isLoading) {
    return (
      <div className="flex min-h-[calc(100vh-200px)] items-center justify-center">
        <div className="relative h-16 w-16">
          <div className="border-primary absolute inset-0 animate-spin rounded-full border-4 border-t-transparent"></div>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      {sortedCampaigns.length === 0 && (
        <p className="text-muted-foreground text-center text-sm">No email templates found</p>
      )}
      {sortedCampaigns.map((template) => (
        <EmailSequence
          key={template.id}
          template={template}
          mailboxId={activeMailboxId}
          campaignId={id || ''}
          isReadOnly
        />
      ))}
    </div>
  );
};

export { EmailTemplatesTab };
