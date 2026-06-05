import { ReactNode } from 'react';

import { Button } from './ui/button';
import { Tooltip, TooltipContent, TooltipTrigger } from './ui/tooltip';
import { cn } from '@/lib/utils/helpers';

type TooltipButtonProps = {
  icon?: ReactNode;
  text?: string;
  tooltip?: string;
  onClick?: () => void;
  disabled?: boolean;
  variant?: 'outline' | 'ghost' | 'default';
  className?: string;
};

export const TooltipButton = ({
  icon,
  text,
  tooltip,
  onClick,
  disabled,
  variant = 'ghost',
  className = '',
}: TooltipButtonProps) => {
  return (
    <Tooltip>
      <TooltipTrigger asChild>
        <span>
          <Button
            variant={variant}
            className={cn(
              'text-muted-foreground hover:text-foreground h-auto! w-auto! p-2!',
              className
            )}
            onClick={onClick}
            disabled={disabled}
          >
            {icon}
            {text}
          </Button>
        </span>
      </TooltipTrigger>
      <TooltipContent>
        <p>{tooltip}</p>
      </TooltipContent>
    </Tooltip>
  );
};
