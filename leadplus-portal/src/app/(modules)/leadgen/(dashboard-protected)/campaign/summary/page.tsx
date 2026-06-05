'use client';

import { useRouter, useSearchParams } from 'next/navigation';
import { motion, AnimatePresence } from 'motion/react';

import { useAuth } from '@/context/AuthContext';
import { useGetCampaignById } from '@/hooks/useCampaign';
import { CampaignSummaryCards } from './CampaignSummaryCards';
import { CampaignSearchCriteria } from './CampaignSearchCriteria';
import { Button } from '@/components/ui/button';
import { ArrowLeft, ArrowRight } from 'lucide-react';
import { CampaignTargetResults } from './CampaignTargetResults';
import { useConfigureCampaignEmails } from '@/hooks/useCampaignEmail';
import { LoadingIndicator } from '@/components/ui/loading-indicator';
import useToggle from '@/hooks/useToggle';
import { appRoutes } from '@/config/routes';
import { CampaignStatus } from '@/constants/Campaign';

const CampaignView = () => {
  const router = useRouter();
  const searchParams = useSearchParams();

  const { value, toggle } = useToggle(false);

  const { authenticatedUserDetails } = useAuth();

  const id = searchParams.get('id');

  const { data, isLoading } = useGetCampaignById({
    tenantId: authenticatedUserDetails?.tenantId || '',
    workspaceId: authenticatedUserDetails?.workspaceId || '',
    campaignId: id || '',
  });
  const { mutate, isPending } = useConfigureCampaignEmails();

  const handleBack = () => {
    window.history.back();
  };

  const handleOnConfigureSequence = () => {
    if (!id || !authenticatedUserDetails?.workspaceId || !authenticatedUserDetails?.tenantId)
      return;
    mutate(
      {
        tenantId: authenticatedUserDetails?.tenantId,
        workspaceId: authenticatedUserDetails?.workspaceId,
        campaignId: id,
      },
      {
        onSuccess: () => {
          router.push(appRoutes.leadgen.campaigns.sequence(id));
        },
      }
    );
  };

  return (
    <>
      <LoadingIndicator isLoading={isPending} />
      <AnimatePresence mode="wait">
        {isLoading ? (
          <motion.div
            key="loading"
            initial={{ opacity: 0, x: 20 }}
            animate={{ opacity: 1, x: 0 }}
            exit={{ opacity: 0, x: -20 }}
            transition={{ duration: 0.5 }}
            className="mx-auto flex min-h-100 max-w-6xl items-center justify-center space-y-6 p-4 md:p-8"
          >
            <div className="h-12 w-12 animate-spin rounded-full border-4 border-blue-500 border-t-transparent"></div>
          </motion.div>
        ) : (
          <motion.div
            key="content"
            initial={{ opacity: 0, x: 20 }}
            animate={{ opacity: 1, x: 0 }}
            exit={{ opacity: 0, x: -20 }}
            transition={{ duration: 0.5 }}
            className="mx-auto max-w-6xl space-y-6 overflow-x-hidden p-4 md:p-8"
          >
            <div className="flex flex-col justify-between gap-4 md:flex-row md:items-center">
              <div>
                <h2 className="text-foreground text-3xl font-bold">Leads Found</h2>
                <p className="text-muted-foreground text-lg">
                  We found {data?.totalCompanies} companies matching your criteria.
                </p>
              </div>
            </div>
            <div className="flex flex-col gap-6">
              <CampaignSearchCriteria leadFilter={data?.leadFilter} />
              <CampaignSummaryCards data={data} />
              {data?.campaignCompanies && (
                <CampaignTargetResults
                  campaignId={data.id}
                  tenantId={data.tenantId}
                  workspaceId={data.workspaceId}
                  result={data.campaignCompanies}
                  open={value}
                  toggle={toggle}
                  hideTrigger={data?.status === CampaignStatus.RUNNING}
                />
              )}
              <div className="border-border mt-8 flex items-center justify-between border-t pt-4">
                <Button variant="outline" className="px-6" onClick={handleBack}>
                  <ArrowLeft className="mr-2 h-4 w-4" />
                  Back
                </Button>
                <Button
                  className="text-primary-foreground bg-sky-500 px-8 shadow-md hover:bg-sky-600"
                  onClick={handleOnConfigureSequence}
                >
                  Configure Sequence
                  <ArrowRight className="ml-2 h-4 w-4" />
                </Button>
              </div>
            </div>
          </motion.div>
        )}
      </AnimatePresence>
    </>
  );
};

export default CampaignView;
