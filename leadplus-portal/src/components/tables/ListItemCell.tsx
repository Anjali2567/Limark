import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from '@/components/ui/tooltip';

type ListItemCellProps = {
  items: string[];
};

const ListItemCell = ({ items }: ListItemCellProps) => {
  return (
    <div className="flex items-center gap-1.5">
      {items && items.length > 0 ? (
        <>
          {items.slice(0, 1).map((item, idx) => (
            <TooltipProvider key={idx}>
              <Tooltip>
                <TooltipTrigger asChild>
                  <span className="bg-secondary text-foreground border-border inline-block max-w-44 cursor-default truncate rounded border px-2 py-0.5 align-middle text-xs font-medium">
                    {item}
                  </span>
                </TooltipTrigger>
                <TooltipContent className="max-w-64 truncate p-2 text-xs wrap-break-word">
                  {item}
                </TooltipContent>
              </Tooltip>
            </TooltipProvider>
          ))}

          {items.length > 1 && (
            <TooltipProvider>
              <Tooltip>
                <TooltipTrigger asChild>
                  <span className="bg-secondary text-foreground border-border inline-flex shrink-0 cursor-help items-center rounded border px-2 py-0.5 text-xs font-medium">
                    +{items.length - 1}
                  </span>
                </TooltipTrigger>
                <TooltipContent className="max-h-40 max-w-64 overflow-y-auto p-2">
                  <ul className="list-inside list-disc space-y-1">
                    {items.map((item, idx) => (
                      <li key={idx} className="text-xs wrap-break-word">
                        {item}
                      </li>
                    ))}
                  </ul>
                </TooltipContent>
              </Tooltip>
            </TooltipProvider>
          )}
        </>
      ) : (
        <span className="text-muted-foreground text-sm">N/A</span>
      )}
    </div>
  );
};

export { ListItemCell };
