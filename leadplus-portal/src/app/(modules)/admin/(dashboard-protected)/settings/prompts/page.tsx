"use client";

import { useMemo, useState } from "react";

import { Tabs } from "@/components/Tabs";
import { promptSpecificationTypeList } from "@/constants/prompt-specification.constants";
import { PromptTabForm } from "./_components/PromptTabForm";

export default function PromptsPage() {
  const [isBusy, setIsBusy] = useState<boolean>(false);

  const tabs = useMemo(
    () =>
      promptSpecificationTypeList.map((type) => ({
        value: type.value,
        label: type.label,
        disabled: isBusy,
        content: (
          <PromptTabForm
            configType={type.value}
            label={type.label}
            setIsBusy={setIsBusy}
          />
        ),
      })),
    [isBusy]
  );

  return (
    <div className="p-8">
      <Tabs
        title="Prompts"
        subText="Manage AI prompts and templates"
        className="max-w-6xl mx-auto p-4 md:p-8"
        defaultValue={promptSpecificationTypeList[0].value}
        tabs={tabs}
      />
    </div>
  );
}
