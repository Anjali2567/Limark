'use client';

import { useRouter } from 'next/navigation';
import { JSX } from 'react';

import { CampaignStatsCards } from '@/components/chat/campaign/CampaignStatsCards';
import { SummarySection } from '@/components/chat/campaign/SummarySection';
import { appRoutes } from '@/config/routes';
import { MessageType } from '@/constants/campaign-agent.constant';
import { useAuth } from '@/context/AuthContext';
import { useGetCampaignDetails } from '@/hooks/useCampaignAgent';
import { TargetingCriteriaCount } from '@/types/campaign-agent.types';
import { Button } from '../ui/button';
import { AssistantWrapper } from './AssistantWrapper';
import { BotResponseWrapper } from './BotResponseWrapper';
import { MessageWrapper } from './MessageWrapper';
import { SimpleMarkdown } from './SimpleMarkdown';
import { UserWrapper } from './UserWrapper';

import { Rocket, Search } from 'lucide-react';

export type MultiSelectItem = {
  id: string;
  name: string;
  isNew?: boolean;
  toolTip?: string;
  children?: MultiSelectItem[];
  type?: string;
};

type MessageRenderProps = {
  user: MessageType;
  message: string | JSX.Element;
  time?: Date;
  isLast?: boolean;
  campaignId?: string;
  campaignState?: TargetingCriteriaCount;
  searchPerformed?: boolean;
  isCampaignLaunching?: boolean;
  onStartCampaign?: () => void;
};

const MessageRender = ({
  user,
  message,
  time,
  isLast = false,
  campaignId,
  campaignState,
  searchPerformed,
  isCampaignLaunching,
  onStartCampaign,
}: MessageRenderProps) => {
  const router = useRouter();
  const { authenticatedUserDetails } = useAuth();
  const tenantId = authenticatedUserDetails?.tenantId ?? '';
  const workspaceId = authenticatedUserDetails?.workspaceId ?? '';

  const hasLeads = (campaignState?.noOfContacts ?? 0) > 0;
  const hasData = (campaignState?.noOfCompanies ?? 0) > 0 || (campaignState?.noOfContacts ?? 0) > 0;
  const showResults = (searchPerformed ?? hasLeads) && hasData;

  const chatMemoryId = showResults && isLast && campaignId ? String(campaignId) : '';

  const handleExploreLeads = () => {
    const params = new URLSearchParams();
    const excludeKeys: string[] = [];
    Object.entries(data?.leadFilter ?? {}).forEach(([key, val]) => {
      if (excludeKeys.includes(key)) return;
      if (Array.isArray(val) && val.length > 0) params.set(key, val.join('|'));
      else if (typeof val === 'string' && val) params.set(key, val);
    });
    params.set('campaignEligibleOnly', 'true');
    params.set('aggregateTechSearch', 'true');
    const qs = params.toString();
    router.push(
      qs
        ? `${appRoutes.leadgen.search.contactSearch}?${qs}`
        : appRoutes.leadgen.search.contactSearch
    );
  };

  const { data, isLoading, isFetching } = useGetCampaignDetails({
    tenantId,
    workspaceId,
    chatMemoryId,
  });

  const locationCount = (() => {
    const cf = campaignState?.companyLocationFilter;
    const ctf = campaignState?.contactLocationFilter;
    const companyCount = cf
      ? (cf.cities?.length ?? 0) + (cf.states?.length ?? 0) + (cf.countries?.length ?? 0) + (cf.regions?.length ?? 0)
      : 0;
    const contactCount = ctf
      ? (ctf.cities?.length ?? 0) + (ctf.states?.length ?? 0) + (ctf.countries?.length ?? 0) + (ctf.regions?.length ?? 0)
      : 0;
    return companyCount + contactCount;
  })();

  switch (user) {
    case MessageType.USER:
      return <MessageWrapper message={message} time={time} />;

    case MessageType.CUSTOMER:
      return <UserWrapper message={message} />;

    case MessageType.ASSISTANT:
      return (
        <AssistantWrapper time={time}>
          {message && (
            <SimpleMarkdown text={message.toString()} className="text-sm text-gray-900" />
          )}
        </AssistantWrapper>
      );

    case MessageType.BOT:
      return (
        <BotResponseWrapper name="Agent" time={time}>
          {message && (
            <div className="border-sectionBorder rounded-lg border bg-white px-4 py-3">
              <SimpleMarkdown text={message.toString()} className="text-sm text-gray-900" />
            </div>
          )}
          {showResults && (!isLast || !data) && (
            <div className="border-sectionBorder mt-3 rounded-lg border bg-white px-4 py-3">
              <CampaignStatsCards
                companies={campaignState?.noOfCompanies ?? 0}
                contacts={campaignState?.noOfContacts ?? 0}
                locations={locationCount}
                isLoading={isLoading}
              />
            </div>
          )}
          {isLast && data && (
            <div className="border-sectionBorder mt-3 rounded-lg border bg-white px-4 py-3">
              <SummarySection campaign={data} isLast={isLast} isLoading={isFetching} />
            </div>
          )}
          {isLast && showResults && onStartCampaign && data && (
            <div className="mt-3 flex w-full items-center justify-between gap-2">
              <Button className="flex-1" onClick={handleExploreLeads}>
                <Search className="h-5 w-5" />
                Explore Leads
              </Button>
              <Button
                className="flex-1 bg-green-600 text-white hover:bg-green-700"
                onClick={onStartCampaign}
                disabled={isCampaignLaunching || isLoading}
              >
                <Rocket className="h-5 w-5" />
                {isCampaignLaunching ? 'Launching...' : 'Launch Campaign'}
              </Button>
            </div>
          )}
        </BotResponseWrapper>
      );

    default:
      return null;
  }
};

export { MessageRender };
