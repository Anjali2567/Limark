import { Analytics } from "./_components/Analytics";
import { CampaignTable } from "./_components/CampaignTable";

export default function Campaigns() {
  return (
    <div className="p-8 space-y-6 animate-in fade-in slide-in-from-bottom-4 duration-500">
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold text-foreground">Campaigns</h1>
          <p className="text-muted-foreground">
            Manage and track your active outreach campaigns.
          </p>
        </div>
      </div>
      <Analytics />
      <CampaignTable />
    </div>
  );
}
