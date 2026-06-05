import { useMemo } from "react";

import { Card, CardContent } from "@/components/ui/card";
import { Campaign } from "@/types/campaign.types";

import { Building, Users } from "lucide-react";

type CampaignSummaryCardsProps = {
  data?: Campaign;
};

const CampaignSummaryCards = ({ data }: CampaignSummaryCardsProps) => {
  const {
    totalCompanies,
    totalContacts,
    participatingCompanies,
    participatingContacts,
  } = useMemo(() => {
    let pCompanies = 0;
    let pContacts = 0;

    data?.campaignCompanies?.forEach((company) => {
      const participatingInCompany =
        company.campaignContacts?.filter((contact) => contact.participating)
          .length || 0;

      if (participatingInCompany > 0) {
        pCompanies++;
        pContacts += participatingInCompany;
      }
    });

    return {
      totalCompanies: data?.totalCompanies || 0,
      totalContacts: data?.totalContacts || 0,
      participatingCompanies: pCompanies,
      participatingContacts: pContacts,
    };
  }, [data]);
  return (
    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
      <Card className="bg-sky-100 border-none shadow-sm hover:shadow-md transition-shadow">
        <CardContent className="p-8 flex flex-col items-center justify-center text-center space-y-4">
          <div className="p-4 rounded-full bg-card text-sky-500 shadow-sm">
            <Building className="h-8 w-8" />
          </div>
          <div>
            <div className="text-4xl font-bold text-foreground">
              {participatingCompanies === totalCompanies
                ? totalCompanies
                : `${participatingCompanies}/${totalCompanies}`}
            </div>
            <div className="text-sm font-medium text-muted-foreground uppercase tracking-wide mt-1">
              Companies Found
            </div>
          </div>
        </CardContent>
      </Card>

      <Card className="bg-green-100 border-none shadow-sm hover:shadow-md transition-shadow">
        <CardContent className="p-8 flex flex-col items-center justify-center text-center space-y-4">
          <div className="p-4 rounded-full bg-card text-success shadow-sm">
            <Users className="h-8 w-8" />
          </div>
          <div>
            <div className="text-4xl font-bold text-foreground">
              {participatingContacts === totalContacts
                ? totalContacts
                : `${participatingContacts}/${totalContacts}`}
            </div>
            <div className="text-sm font-medium text-muted-foreground uppercase tracking-wide mt-1">
              Total Contacts
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  );
};

export { CampaignSummaryCards };
