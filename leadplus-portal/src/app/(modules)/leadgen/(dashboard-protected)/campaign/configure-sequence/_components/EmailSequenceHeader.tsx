import { FC } from 'react';

import { Button } from '@/components/ui/button';
import { CardDescription, CardHeader, CardTitle } from '@/components/ui/card';

import { ChevronDown, ChevronUp, TrashIcon, WandSparkles } from 'lucide-react';

type EmailSequenceHeaderProps = {
  isExpanded: boolean;
  toggleTemplate: () => void;
  templateStepNumber: number;
  templateDelayDays: number;
  onTemplateGenerate?: () => void;
  sequenceHeader: string;
  isDisabled?: boolean;
};

const EmailSequenceHeader: FC<EmailSequenceHeaderProps> = ({
  isExpanded,
  toggleTemplate,
  templateStepNumber,
  templateDelayDays,
  onTemplateGenerate,
  sequenceHeader,
  isDisabled = false,
}) => {
  return (
    <CardHeader
      className="hover:bg-secondary/50 border-border cursor-pointer border-b p-6 transition-colors"
      onClick={toggleTemplate}
    >
      <div className="flex items-center justify-between">
        <div className="space-y-1">
          <CardTitle className="flex items-center gap-2 text-lg">
            <span className="text-primary-foreground flex h-6 w-6 items-center justify-center rounded-full bg-sky-500 text-xs font-bold">
              {templateStepNumber}
            </span>
            {sequenceHeader}
          </CardTitle>
          <CardDescription>
            {templateStepNumber === 1
              ? 'Sent immediately after campaign launch'
              : `Sent ${templateDelayDays} days after previous email`}
          </CardDescription>
        </div>
        <div>
          {templateStepNumber === 1 && onTemplateGenerate && (
            <Button
              type="button"
              variant="none"
              disabled={isDisabled}
              className="transition-all hover:scale-125 hover:text-cyan-950"
              onClick={(e) => {
                e.stopPropagation();
                onTemplateGenerate();
              }}
            >
              <WandSparkles className="h-4 w-4" />
            </Button>
          )}
          <Button
            type="button"
            variant="none"
            className="hover:text-primary transition-all hover:scale-125"
          >
            {isExpanded ? <ChevronUp className="h-4 w-4" /> : <ChevronDown className="h-4 w-4" />}
          </Button>
        </div>
      </div>
    </CardHeader>
  );
};

export { EmailSequenceHeader };
