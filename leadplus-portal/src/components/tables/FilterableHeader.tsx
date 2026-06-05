import { ReactNode } from "react";

import { Button } from "@/components/ui/button";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { cn } from "@/lib/utils/helpers";
import { FilterOption } from "@/types/table.types";

import { Filter } from "lucide-react";

type FilterableHeaderProps = {
  label: ReactNode;
  filterConfig?: FilterOption | null;
  filterOptions?: FilterOption[];
  onFilter?: (filterConfig: FilterOption | null) => void;
};

const FilterableHeader = ({
  label,
  filterConfig,
  filterOptions = [],
  onFilter,
}: FilterableHeaderProps) => {
  const handleFilterChange = (option: FilterOption | null) => {
    onFilter?.(option);
  };

  return (
    <div className="flex items-center gap-2">
      {label}
      <DropdownMenu>
        <DropdownMenuTrigger asChild>
          <Button
            variant="ghost"
            size="icon"
            className="h-6 w-6 p-0 hover:bg-transparent text-muted-foreground hover:text-foreground relative"
          >
            <Filter className="h-4 w-4" fill="currentColor" />
            <span className="sr-only">Filter {String(label)}</span>
            {filterConfig && filterConfig.value !== null && (
              <div className="absolute top-0 right-0 h-2 w-2 bg-sky-500 rounded-full" />
            )}
          </Button>
        </DropdownMenuTrigger>
        <DropdownMenuContent align="start">
          {filterOptions.map((option) => (
            <DropdownMenuItem
              key={option.value || "all"}
              onClick={() => handleFilterChange(option)}
              className={cn(
                option.value === filterConfig?.value && "bg-primary/10"
              )}
            >
              {option.label}
            </DropdownMenuItem>
          ))}
        </DropdownMenuContent>
      </DropdownMenu>
    </div>
  );
};

export { FilterableHeader };
