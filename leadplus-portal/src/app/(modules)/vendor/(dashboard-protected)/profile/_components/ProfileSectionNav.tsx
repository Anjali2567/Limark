'use client';

import { cn } from '@/lib/utils/helpers';

import { Check, ChevronRight, LucideIcon } from 'lucide-react';

export enum ProfileSectionId {
  COMPANY_INFO = 'company-info',
  ABOUT_TEAM = 'about-team',
  SERVICES = 'services',
  INDUSTRIES = 'industries',
  ADDRESS = 'address',
  SOCIAL_MEDIA = 'social-media',
  CERTIFICATIONS = 'certifications',
  PORTFOLIO = 'portfolio',
}

export type ProfileSection = {
  id: ProfileSectionId;
  label: string;
  icon: LucideIcon;
};

type ProfileSectionNavProps = {
  sections: ProfileSection[];
  activeSection: ProfileSectionId;
  completedSections: Set<ProfileSectionId>;
  onSelect: (id: ProfileSectionId) => void;
};

export const ProfileSectionNav = ({
  sections,
  activeSection,
  completedSections,
  onSelect,
}: ProfileSectionNavProps) => {
  return (
    <div className="bg-card border-border overflow-hidden rounded-lg border">
      <div className="border-border bg-secondary/50 border-b px-4 py-3">
        <p className="text-muted-foreground text-xs font-semibold tracking-wider uppercase">
          Sections
        </p>
      </div>
      <nav className="py-2">
        {sections.map(({ id, label, icon: Icon }) => {
          const isActive = activeSection === id;
          const isDone = completedSections.has(id);
          return (
            <button
              key={id}
              type="button"
              onClick={() => onSelect(id)}
              className={cn(
                'group flex w-full items-center justify-between border-l-2 px-4 py-3 text-left transition-colors',
                isActive
                  ? 'border-primary bg-primary/10 text-primary'
                  : 'text-foreground hover:bg-secondary/60 border-transparent'
              )}
            >
              <div className="flex min-w-0 items-center gap-3">
                <Icon
                  className={cn(
                    'h-4 w-4 shrink-0',
                    isActive ? 'text-primary' : 'text-muted-foreground group-hover:text-foreground'
                  )}
                />
                <span className="truncate text-sm font-medium">{label}</span>
              </div>
              <div className="ml-2 flex shrink-0 items-center gap-1.5">
                {isDone ? (
                  <span className="bg-success/15 flex h-5 w-5 items-center justify-center rounded-full">
                    <Check className="text-success h-3 w-3" />
                  </span>
                ) : (
                  <span className="bg-muted flex h-5 w-5 items-center justify-center rounded-full">
                    <span className="bg-muted-foreground/40 h-2 w-2 rounded-full" />
                  </span>
                )}
                {isActive && <ChevronRight className="text-primary h-3.5 w-3.5" />}
              </div>
            </button>
          );
        })}
      </nav>
    </div>
  );
};
