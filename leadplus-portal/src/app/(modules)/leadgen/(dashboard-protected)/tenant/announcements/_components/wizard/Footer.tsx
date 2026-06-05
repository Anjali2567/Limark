import { Button } from '@/components/ui/button';
import { WizardStep } from './types';

import { Loader2 } from 'lucide-react';

type FooterProps = {
  currentStep: WizardStep;
  onChange: (step: WizardStep) => void;
  isLoading?: boolean;
  handleClose?: () => void;
  disabled?: boolean;
  onNext?: () => void;
};

const Footer = ({
  currentStep,
  onChange,
  isLoading,
  handleClose,
  disabled,
  onNext,
}: FooterProps) => {
  return (
    <div className="flex w-full shrink-0 items-center justify-between border-t px-6 py-4">
      <div className="flex gap-2">
        {currentStep !== WizardStep.COMPOSE && (
          <Button
            variant="outline"
            onClick={() =>
              onChange(currentStep === WizardStep.REVIEW ? WizardStep.CONTACTS : WizardStep.COMPOSE)
            }
            disabled={isLoading}
          >
            Back
          </Button>
        )}
      </div>
      <div className="flex gap-2">
        <Button variant="outline" onClick={handleClose}>
          Cancel
        </Button>
        {currentStep === WizardStep.COMPOSE && (
          <Button onClick={onNext} disabled={isLoading}>
            {isLoading && <Loader2 className="h-4 w-4 animate-spin" />}
            Next
          </Button>
        )}
        {currentStep === WizardStep.CONTACTS && (
          <Button onClick={onNext} disabled={isLoading || disabled}>
            {isLoading && <Loader2 className="h-4 w-4 animate-spin" />}
            Next
          </Button>
        )}
        {currentStep === WizardStep.REVIEW && (
          <Button onClick={onNext} disabled={isLoading}>
            {isLoading && <Loader2 className="h-4 w-4 animate-spin" />}
            Send Announcement
          </Button>
        )}
      </div>
    </div>
  );
};

export { Footer };
