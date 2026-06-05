import { useRouter } from 'next/navigation';

import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Spinner } from '@/components/ui/spinner';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Campaign } from '@/types/campaign.types';

import { Building, CheckCircle2, Mail, Megaphone, Users } from 'lucide-react';
import { appRoutes } from '@/config/routes';
import { CampaignEmail } from '@/types/campaign-email.types';

type CampaignLaunchSuccessProps = {
  campaign: Campaign;
  campaignEmails: CampaignEmail[];
  isLoading?: boolean;
};

const CampaignLaunchSuccess = ({
  campaign,
  campaignEmails = [],
  isLoading,
}: CampaignLaunchSuccessProps) => {
  const router = useRouter();

  const handleGoToCampaigns = () => {
    router.push(appRoutes.leadgen.campaigns.root);
  };

  const handleReset = () => {
    router.push(appRoutes.leadgen.campaignGenerator.root);
  };

  if (isLoading) {
    return (
      <div className="flex min-h-[calc(100vh-200px)] items-center justify-center">
        <Spinner className="text-primary size-10" />
      </div>
    );
  }

  return (
    <div className="animate-in fade-in zoom-in mx-auto max-w-2xl space-y-8 text-center duration-500">
      <div className="space-y-4">
        <div className="bg-success/10 text-success mx-auto flex h-16 w-16 items-center justify-center rounded-full">
          <CheckCircle2 className="h-8 w-8" />
        </div>
        <div className="space-y-2">
          <h2 className="text-foreground whitespace-nowrap">Campaign Launched!</h2>
          <p className="text-muted-foreground">
            Your campaign started successfully. You can keep track of it in the Campaigns page.
          </p>
        </div>
      </div>

      <Card className="bg-card border-border text-left shadow-sm">
        <CardHeader className="border-border bg-secondary/30 border-b pb-3">
          <CardTitle className="text-foreground font-semibold">Campaign Summary</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4 p-6">
          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-1">
              <div className="text-muted-foreground text-sm font-medium">Target Companies</div>
              <div className="text-foreground flex items-center gap-2 font-semibold">
                <Building className="h-4 w-4 text-sky-500" />
                {campaign.participatingCompaniesCount} Companies
              </div>
            </div>
            <div className="space-y-1">
              <div className="text-muted-foreground text-sm font-medium">Total Recipients</div>
              <div className="text-foreground flex items-center gap-2 font-semibold">
                <Users className="h-4 w-4 text-sky-500" />
                {campaign.participatingContactsCount} Contacts
              </div>
            </div>
            <div className="space-y-1">
              <div className="text-muted-foreground text-sm font-medium">Email Sequence</div>
              <div className="text-foreground flex items-center gap-2 font-semibold">
                <Mail className="h-4 w-4 text-sky-500" />
                {campaignEmails.length} Emails
              </div>
            </div>
            <div className="space-y-1">
              <div className="text-muted-foreground text-sm font-medium">Status</div>
              <Badge
                variant="outline"
                className="bg-success/10 text-success border-success/20 w-fit"
              >
                {campaign.status}
              </Badge>
            </div>
          </div>
        </CardContent>
      </Card>

      <div className="flex justify-center gap-4 pt-2">
        <Button variant="outline" onClick={handleReset}>
          Create Another
        </Button>
        <Button
          className="text-primary-foreground bg-sky-500 px-8 hover:bg-sky-600"
          onClick={handleGoToCampaigns}
        >
          <Megaphone className="mr-2 h-4 w-4" />
          Go to Campaigns
        </Button>
      </div>
    </div>
  );
};

export { CampaignLaunchSuccess };
