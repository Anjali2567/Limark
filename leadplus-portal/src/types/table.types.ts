import { ReactNode } from "react";

export type FilterOption = {
  label: string;
  value: string | null;
};

export type ColumnType<T> = {
  id: keyof T | string;
  header: ReactNode;
  width?: number | string;
  selectable?: boolean;
  sortable?: boolean;
  filterable?: boolean;
  filterOptions?: FilterOption[];
  align?: "left" | "center" | "right";
  fixedRight?: boolean;
  cellClassName?: string;
  renderCell?: (row: T) => ReactNode;
};
