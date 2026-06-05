"use client";

import { useMemo, useState } from "react";

import { Tabs } from "@/components/Tabs";
import { ApolloTab } from "./_components/ApolloTab";

const PeopleSearchPage = () => {
  const [isBusy, setIsBusy] = useState<boolean>(false);

  const tabs = useMemo(
    () => [
      {
        value: "apollo-specification",
        label: "Apollo",
        disabled: isBusy,
        content: <ApolloTab setIsBusy={setIsBusy} />,
      },
    ],
    [isBusy]
  );

  return (
    <div className="p-8">
      <Tabs
        title="People Search"
        subText="Configure your search providers and data enrichment preferences."
        className="max-w-6xl mx-auto p-4 md:p-8"
        defaultValue={tabs[0].value}
        tabs={tabs}
      />
    </div>
  );
};

export default PeopleSearchPage;
