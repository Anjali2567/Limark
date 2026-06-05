import { Fragment } from 'react';

import { cn } from '@/lib/utils/helpers';
import { WizardStep } from './types';

const STEPS: { key: WizardStep; label: string; num: number }[] = [
  { key: WizardStep.COMPOSE, label: 'Compose', num: 1 },
  { key: WizardStep.CONTACTS, label: 'Add Contacts', num: 2 },
  { key: WizardStep.REVIEW, label: 'Review & Send', num: 3 },
];

type StepIndicatorProps = {
  current: WizardStep;
  onChange: (step: WizardStep) => void;
  announcementId: string | null;
  hasContactSelection?: boolean;
};

const StepIndicator = ({
  current,
  onChange,
  announcementId,
  hasContactSelection,
}: StepIndicatorProps) => (
  <div className="border-border mt-4 flex w-full items-center border-y py-3">
    {STEPS.map((step, i) => (
      <Fragment key={step.key}>
        <button
          onClick={() => announcementId && onChange(step.key)}
          disabled={
            (step.key !== WizardStep.COMPOSE && !announcementId) ||
            (step.key === WizardStep.REVIEW && !hasContactSelection)
          }
          className={cn(
            'flex cursor-pointer items-center gap-2 px-4 py-1 text-sm transition-opacity hover:opacity-80 disabled:cursor-not-allowed disabled:opacity-40',
            current === step.key ? 'text-primary font-medium' : 'text-muted-foreground'
          )}
        >
          <div
            className={cn(
              'flex h-6 w-6 items-center justify-center rounded-full border-2 text-xs font-semibold',
              current === step.key
                ? 'border-primary bg-primary text-white'
                : 'border-muted-foreground/30 bg-background text-muted-foreground'
            )}
          >
            {step.num}
          </div>
          <span>{step.label}</span>
        </button>
        {i < STEPS.length - 1 && <div className="bg-border mx-2 h-px flex-1" />}
      </Fragment>
    ))}
  </div>
);

export { StepIndicator };
