import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from '@/components/ui/tooltip';
import { Badge } from '@/components/ui/badge';
import { Card, CardContent } from '@/components/ui/card';
import { cn } from '@/lib/utils/helpers';
import { LeadFilterCriteria } from '@/types/leadSearch.types';
import { LEAD_FILTER_CRITERIA_DISPLAY_CONFIG } from '../../_constants/leadFilterCriteria.constants';

type CampaignSearchCriteriaProps = {
  leadFilter?: LeadFilterCriteria | null;
};

const CampaignSearchCriteria = ({ leadFilter }: CampaignSearchCriteriaProps) => {
  if (!leadFilter) return null;

  const activeCriteria = LEAD_FILTER_CRITERIA_DISPLAY_CONFIG
    .map((config) => ({
      icon: config.icon,
      title: config.title,
      data: config.criteriaKeys.flatMap((k) => (leadFilter[k] as string[] | undefined) ?? []),
    }))
    .filter((item) => item.data.length > 0);

  if (activeCriteria.length === 0) return null;

  const getDisplayText = (items: string[]) => {
    if (items.length === 1) return items[0];
    return `${items[0]} +${items.length - 1}`;
  };

  return (
    <Card className="bg-secondary/30 border-border shadow-sm">
      <CardContent className="p-4 last:p-4">
        <div className="flex flex-col items-start gap-3 text-sm sm:flex-row sm:items-center">
          <span className="text-muted-foreground min-w-fit font-medium">Targeting:</span>
          <TooltipProvider>
            <div className="flex flex-wrap gap-2">
              {activeCriteria.map((criterion, index) => {
                const Icon = criterion.icon;
                const displayText = getDisplayText(criterion.data);
                const hasMultiple = criterion.data.length > 1;

                const badgeContent = (
                  <Badge
                    key={index}
                    variant="outline"
                    className={cn(
                      'bg-card border-border text-secondary-foreground px-3 py-1.5 font-normal',
                      hasMultiple && 'cursor-pointer'
                    )}
                  >
                    <Icon className="text-muted-foreground mr-2 h-3 w-3" />
                    <span className="text-muted-foreground mr-1 text-xs">{criterion.title}:</span>
                    {displayText}
                  </Badge>
                );

                if (hasMultiple) {
                  return (
                    <Tooltip key={index}>
                      <TooltipTrigger asChild>{badgeContent}</TooltipTrigger>
                      <TooltipContent>
                        <div className="flex max-w-xs flex-col gap-1">
                          <p className="mb-1 text-xs font-semibold">{criterion.title}</p>
                          {criterion.data.map((item, i) => (
                            <p key={i} className="text-xs">
                              • {item}
                            </p>
                          ))}
                        </div>
                      </TooltipContent>
                    </Tooltip>
                  );
                }

                return badgeContent;
              })}
            </div>
          </TooltipProvider>
        </div>
      </CardContent>
    </Card>
  );
};

export { CampaignSearchCriteria };
