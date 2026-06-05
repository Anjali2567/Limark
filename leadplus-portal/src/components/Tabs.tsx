"use client";

import { ReactNode } from "react";
import {
  Tabs as ShadcnTabs,
  TabsContent,
  TabsList,
  TabsTrigger,
} from "@/components/ui/tabs";
import { cn } from "@/lib/utils/helpers";

type TabItem = {
  value: string;
  label: string;
  content: ReactNode;
  disabled?: boolean;
};

type TabsProps = {
  tabs: TabItem[];
  defaultValue?: string;
  title?: string;
  subText?: string;
  className?: string;
  listClassName?: string;
  triggerClassName?: string;
  contentClassName?: string;
};

export const Tabs = ({
  tabs,
  defaultValue,
  title,
  subText,
  className,
  listClassName,
  triggerClassName,
  contentClassName,
}: TabsProps) => {
  if (!tabs.length) return null;

  const initial = defaultValue ?? tabs[0].value;

  return (
    <ShadcnTabs defaultValue={initial} className={cn("w-full", className)}>
      <div className="sticky top-0 z-20 space-y-6 bg-slate-50">
        <div>
          {title && (
            <h2 className="text-3xl font-bold text-foreground">{title}</h2>
          )}
          {subText && (
            <p className="text-muted-foreground text-lg mt-1">{subText}</p>
          )}
        </div>
        <div className="border-b border-border">
          <TabsList
            className={cn(
              "flex h-auto gap-6 bg-transparent p-0",
              listClassName
            )}
          >
            {tabs.map(({ value, label, disabled }) => (
              <TabsTrigger
                key={value}
                value={value}
                disabled={disabled}
                className={cn(
                  "px-1 pb-3 cursor-pointer text-md",
                  "border-0 border-b-2 transition-colors",
                  "data-[state=active]:border-sky-500",
                  "data-[state=active]:text-sky-500",
                  "data-[state=active]:font-medium",
                  "data-[state=active]:shadow-none",
                  "data-[state=active]:bg-transparent",
                  "text-muted-foreground hover:text-foreground rounded-none",
                  triggerClassName
                )}
              >
                {label}
              </TabsTrigger>
            ))}
          </TabsList>
        </div>
      </div>
      {tabs.map(({ value, content }) => (
        <TabsContent
          key={value}
          value={value}
          className={cn(
            "mt-3 animate-in fade-in slide-in-from-bottom-2 duration-200",
            contentClassName
          )}
        >
          {content}
        </TabsContent>
      ))}
    </ShadcnTabs>
  );
};
