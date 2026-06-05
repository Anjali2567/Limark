"use client";

import { useSearchParams } from "next/navigation";

import { Spinner } from "@/components/ui/spinner";
import { useAuth } from "@/context/AuthContext";
import { useGetCampaignById } from "@/hooks/useCampaign";
import { CampaignAnalytics } from "./_components/CampaignAnalytics";
import { Header } from "./_components/Header";
import { CampaignSummary } from "./_components/CampaignSummary";
import { CampaignSearchCriteria } from "../summary/CampaignSearchCriteria";

export default function CampaignView() {
  const searchParams = useSearchParams();
  const { authenticatedUserDetails } = useAuth();

  const campaignId = searchParams.get("id") || "";

  const { data: campaign, isLoading } = useGetCampaignById({
    tenantId: authenticatedUserDetails?.tenantId || "",
    workspaceId: authenticatedUserDetails?.workspaceId || "",
    campaignId,
  });

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-[calc(100vh-200px)]">
        <Spinner className="size-10 text-primary" />
      </div>
    );
  }

  return (
    <div className="p-8 space-y-6 animate-in fade-in slide-in-from-bottom-4 duration-500">
      {campaign && (
        <>
          <Header campaign={campaign} />
          <CampaignSearchCriteria leadFilter={campaign.leadFilter} />
          {authenticatedUserDetails?.workspaceId && (
            <CampaignAnalytics
              campaignId={campaignId}
              tenantId={authenticatedUserDetails.tenantId}
              workspaceId={authenticatedUserDetails.workspaceId}
            />
          )}
          <CampaignSummary />
        </>
      )}
    </div>
  );
}
