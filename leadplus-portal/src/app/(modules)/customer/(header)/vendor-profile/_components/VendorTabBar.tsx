'use client';

import { Button } from '@/components/ui/button';
import { cn } from '@/lib/utils/helpers';

enum Tab {
  Overview = 'overview',
  Services = 'services',
  CaseStudies = 'case-studies',
  Reviews = 'reviews',
  Certifications = 'certifications',
}

const TABS: { id: Tab; label: string }[] = [
  { id: Tab.Overview, label: 'Overview' },
  { id: Tab.Services, label: 'Services' },
  { id: Tab.CaseStudies, label: 'Case Studies' },
  { id: Tab.Reviews, label: 'Reviews' },
  { id: Tab.Certifications, label: 'Certifications' },
];

type VendorTabBarProps = {
  activeTab: Tab;
  onChange: (tab: Tab) => void;
};

export const VendorTabBar = ({ activeTab, onChange }: VendorTabBarProps) => (
  <div className="border-border bg-background sticky top-0 z-40 border-b">
    <div className="mx-auto max-w-7xl px-6">
      <div className="flex gap-8">
        {TABS.map((tab) => (
          <Button
            key={tab.id}
            variant="ghost"
            onClick={() => onChange(tab.id)}
            className={cn(
              'hover:text-foreground h-auto rounded-none border-b-2 px-0 py-4 text-sm font-medium transition-colors hover:bg-transparent',
              activeTab === tab.id
                ? 'border-primary text-primary'
                : 'text-muted-foreground border-transparent'
            )}
          >
            {tab.label}
          </Button>
        ))}
      </div>
    </div>
  </div>
);

export { Tab };
