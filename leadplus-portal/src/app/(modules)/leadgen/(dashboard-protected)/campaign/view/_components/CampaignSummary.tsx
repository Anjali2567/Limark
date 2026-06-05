import { Tabs } from "@/components/Tabs";
import { ActivityTimelineTab } from "./tabs/ActivityTimelineTab";
import { EmailTemplatesTab } from "./tabs/EmailTemplatesTab";

const CampaignSummary = () => {
  const tabs = [
    {
      value: "companies",
      label: "Companies & Activity",
      content: <ActivityTimelineTab />,
    },
    {
      value: "templates",
      label: "Email Templates",
      content: <EmailTemplatesTab />,
    },
  ];
  return (
    <Tabs
      tabs={tabs}
      listClassName="w-full justify-start bg-transparent p-0 h-auto border-b border-border"
      triggerClassName="rounded-none border-0 border-b-2 border-transparent data-[state=active]:border-sky-500 data-[state=active]:bg-transparent data-[state=active]:text-sky-500 data-[state=active]:font-semibold data-[state=active]:shadow-none px-0 py-3 mr-6 bg-transparent text-muted-foreground font-normal"
      contentClassName="mt-6"
    />
  );
};

export { CampaignSummary };
