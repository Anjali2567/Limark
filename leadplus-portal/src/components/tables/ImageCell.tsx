import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import { Tooltip, TooltipContent, TooltipTrigger } from '../ui/tooltip';
import { cn, getInitials } from '@/lib/utils/helpers';

type ImageCellProps = {
  src?: string;
  alt?: string;
  text: string;
  subText?: string;
  initials?: string;
  size?: number;
  className?: string;
  tooltip?: string;
  hideInitials?: boolean;
};

const ImageCell = ({
  src,
  alt = 'logo',
  text,
  subText,
  initials,
  size = 25,
  className = '',
  tooltip,
  hideInitials = false,
}: ImageCellProps) => {
  const resolvedInitials = initials || getInitials(text);
  const resolvedSrc = src || undefined;

  const avatar = (
    <Avatar className="shrink-0" style={{ width: size, height: size }}>
      {resolvedSrc && <AvatarImage src={resolvedSrc} alt={alt} className="object-contain" />}
      {!hideInitials && resolvedInitials && (
        <AvatarFallback className="bg-primary/10 text-primary rounded-full text-[10px] font-semibold">
          {resolvedInitials}
        </AvatarFallback>
      )}
    </Avatar>
  );

  const avatarWithTooltip =
    resolvedSrc && tooltip ? (
      <Tooltip>
        <TooltipTrigger asChild>{avatar}</TooltipTrigger>
        <TooltipContent>
          <p>{tooltip}</p>
        </TooltipContent>
      </Tooltip>
    ) : (
      avatar
    );

  return (
    <div
      className={cn('text-foreground flex min-w-0 cursor-default items-center gap-2', className)}
    >
      {avatarWithTooltip}
      <div className="min-w-0 flex-1">
        <p className="truncate text-sm" title={text}>
          {text}
        </p>
        {subText && (
          <p className="text-muted-foreground truncate text-xs" title={subText}>
            {subText}
          </p>
        )}
      </div>
    </div>
  );
};

export { ImageCell };
