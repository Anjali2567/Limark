import { ReactNode } from "react";

import { Button } from "@/components/ui/button";

import { ArrowDown, ArrowUp, ArrowUpDown } from "lucide-react";

export type SortConfig = {
  key: string;
  direction: "asc" | "desc";
};

type SortableHeaderProps = {
  label: ReactNode;
  sortKey: string;
  sortConfig?: SortConfig | null;
  onSort?: (sortConfig: SortConfig) => void;
};

const SortableHeader = ({
  label,
  sortKey,
  sortConfig,
  onSort,
}: SortableHeaderProps) => {
  const isActive = sortConfig?.key === sortKey;

  const handleClick = () => {
    if (!onSort) return;
    const newDirection =
      isActive && sortConfig?.direction === "asc" ? "desc" : "asc";
    onSort({ key: sortKey, direction: newDirection });
  };

  return (
    <Button
      variant="ghost"
      className="-ml-3 h-8 font-semibold text-foreground hover:bg-transparent hover:text-foreground"
      onClick={handleClick}
    >
      {label}

      {isActive ? (
        sortConfig?.direction === "asc" ? (
          <ArrowUp className="ml-2 h-4 w-4" />
        ) : (
          <ArrowDown className="ml-2 h-4 w-4" />
        )
      ) : (
        <ArrowUpDown className="ml-2 h-4 w-4 text-muted-foreground" />
      )}
    </Button>
  );
};

export { SortableHeader };
