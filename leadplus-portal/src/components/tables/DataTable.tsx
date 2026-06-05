'use client';

import { AnimatePresence, motion } from 'framer-motion';
import { Fragment, MouseEvent, useCallback, useEffect, useMemo, useRef, useState } from 'react';

import { Button } from '@/components/ui/button';
import { Checkbox } from '@/components/ui/checkbox';
import { ScrollArea, ScrollBar } from '@/components/ui/scroll-area';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';
import { cn } from '@/lib/utils/helpers';
import { Page } from '@/types/paginated-params';
import { ColumnType, FilterOption } from '@/types/table.types';
import { FilterableHeader } from './FilterableHeader';
import { Pagination } from './Pagination';
import { SortableHeader, SortConfig } from './SortableHeader';

import { ChevronDown, ChevronUp, Loader2 } from 'lucide-react';

type SubRowConfig<T, S extends { id: string }> = {
  columns: ColumnType<S>[];
  getData: (row: T) => S[];
  onRowClick?: (parentRow: T, subRow: S) => void;
};

type TableProps<T extends { id: string }, S extends { id: string } = never> = {
  rowClassName?: string;
  columns: ColumnType<T>[];
  data: T[];
  onRowClick?: (rowId: string) => void;
  isLoading?: boolean;
  getRowDataTestId?: (row: T) => string;
  className?: string;
  defaultMessage?: string;
  pagination?: Page;
  initialPage?: number;
  onPageChange?: (page: number) => void;
  sortConfig?: SortConfig | null;
  onSortChange?: (sort: SortConfig | null) => void;
  filtersMap?: Record<string, FilterOption | null>;
  onFiltersChange?: (key: string, filter: FilterOption | null) => void;
  subRowConfig?: SubRowConfig<T, S>;
  selectable?: boolean;
  selectedRowIds?: string[];
  onSelectionChange?: (selectedIds: string[]) => void;
  scrollHorizontal?: boolean;
  freezeColumnsCount?: number;
  style?: 'grid' | 'normal';
};

const LoadingState = () => (
  <TableRow>
    <TableCell colSpan={100} className="h-32 text-center">
      <div className="flex h-full w-full items-center justify-center">
        <Loader2 className="text-primary h-6 w-6 animate-spin" />
      </div>
    </TableCell>
  </TableRow>
);

const EmptyState = ({ colSpan, message }: { colSpan: number; message: string }) => (
  <TableRow>
    <TableCell colSpan={colSpan} className="text-muted-foreground h-32 text-center">
      {message}
    </TableCell>
  </TableRow>
);

const DataTable = <T extends { id: string }, S extends { id: string } = never>({
  rowClassName = '',
  columns,
  data = [],
  onRowClick,
  isLoading,
  className = '',
  defaultMessage = 'No data available',
  pagination,
  initialPage,
  onPageChange,
  sortConfig,
  onSortChange,
  filtersMap,
  onFiltersChange,
  subRowConfig,
  selectable = false,
  selectedRowIds = [],
  onSelectionChange,
  scrollHorizontal = false,
  freezeColumnsCount = 0,
  style = 'normal',
}: TableProps<T, S>) => {
  const [expandedRowId, setExpandedRowId] = useState<string | null>(null);
  const [isOverflowing, setIsOverflowing] = useState<boolean>(false);
  const scrollContentRef = useRef<HTMLDivElement>(null);
  const containerRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const el = scrollContentRef.current;
    const container = containerRef.current;
    if (!el || !container || !scrollHorizontal) return;

    const checkOverflow = () => {
      setIsOverflowing(el.scrollWidth > container.clientWidth);
    };

    checkOverflow();

    const observer = new ResizeObserver(checkOverflow);
    observer.observe(el);
    observer.observe(container);

    return () => observer.disconnect();
  }, [scrollHorizontal]);

  const isExpandable = Boolean(subRowConfig);
  const isRowClickable = Boolean(onRowClick);

  const allRowIds = useMemo(() => data.map((row) => row.id), [data]);

  const selectionState = useMemo(() => {
    if (!selectable || allRowIds.length === 0) {
      return { isAllSelected: false, isSomeSelected: false };
    }

    const isAllSelected = allRowIds.every((id) => selectedRowIds.includes(id));

    return {
      isAllSelected,
      isSomeSelected: selectedRowIds.length > 0 && !isAllSelected,
    };
  }, [allRowIds, selectedRowIds, selectable]);

  const handleSelectAll = useCallback(() => {
    if (!onSelectionChange) return;
    if (selectionState.isAllSelected) {
      onSelectionChange(selectedRowIds.filter((id) => !allRowIds.includes(id)));
    } else {
      const newSelections = new Set(selectedRowIds);
      allRowIds.forEach((id) => newSelections.add(id));
      onSelectionChange(Array.from(newSelections));
    }
  }, [
    onSelectionChange,
    selectionState.isAllSelected,
    selectedRowIds,
    allRowIds,
  ]);

  const handleSelectRow = useCallback(
    (rowId: string) => {
      if (!onSelectionChange) return;

      if (selectedRowIds.includes(rowId)) {
        onSelectionChange(selectedRowIds.filter((id) => id !== rowId));
      } else {
        onSelectionChange([...selectedRowIds, rowId]);
      }
    },
    [onSelectionChange, selectedRowIds]
  );

  const handleToggleExpand = useCallback((rowId: string, e?: MouseEvent) => {
    e?.stopPropagation();
    setExpandedRowId((prev) => (prev === rowId ? null : rowId));
  }, []);

  const fixedRightZoneWidth = useMemo(() => {
    if (!scrollHorizontal) return 0;
    return columns
      .filter((col) => col.fixedRight === true)
      .reduce((sum, col) => {
        const w =
          typeof col.width === 'number' ? col.width : parseInt((col.width as string) || '0', 10);
        return sum + (isNaN(w) ? 0 : w);
      }, 0);
  }, [scrollHorizontal, columns]);

  const { frozenOffsets, frozenZoneWidth } = useMemo(() => {
    const offsets: number[] = [];
    if (!scrollHorizontal || (freezeColumnsCount <= 0 && !selectable)) {
      return { frozenOffsets: offsets, frozenZoneWidth: 0 };
    }

    let currentOffset = 0;

    if (selectable) {
      offsets.push(0);
      currentOffset += 48;
    }

    for (let i = 0; i < freezeColumnsCount; i++) {
      offsets.push(currentOffset);
      const widthValue = columns[i]?.width || '150px';
      const width = typeof widthValue === 'number' ? widthValue : parseInt(widthValue, 10);
      currentOffset += isNaN(width) ? 150 : width;
    }

    return { frozenOffsets: offsets, frozenZoneWidth: currentOffset };
  }, [scrollHorizontal, freezeColumnsCount, selectable, columns]);

  const isFrozen = useCallback(
    (index: number) => {
      if (!scrollHorizontal || !isOverflowing) return false;
      if (selectable) {
        return index < freezeColumnsCount + 1;
      }
      return index < freezeColumnsCount;
    },
    [scrollHorizontal, isOverflowing, selectable, freezeColumnsCount]
  );

  const getFrozenStyle = useCallback(
    (index: number) => {
      if (!isFrozen(index)) return {};
      return {
        position: 'sticky' as const,
        left: `${frozenOffsets[index] || 0}px`,
      };
    },
    [isFrozen, frozenOffsets]
  );

  const isFixedRight = useCallback(
    (column: ColumnType<T>) => scrollHorizontal && isOverflowing && column.fixedRight === true,
    [scrollHorizontal, isOverflowing]
  );

  const getFixedRightStyle = useCallback(
    (column: ColumnType<T>) => {
      if (!isFixedRight(column)) return {};
      return { position: 'sticky' as const, right: 0 };
    },
    [isFixedRight]
  );

  const renderSubTable = useCallback(
    (parentRow: T) => {
      if (!subRowConfig) return null;

      const { columns: subColumns, getData, onRowClick: onSubRowClick } = subRowConfig;
      const subData = getData(parentRow);

      if (subData.length === 0) {
        return (
          <div className="text-muted-foreground px-4 py-3 text-center text-sm italic">
            No data available
          </div>
        );
      }

      return (
        <div className="bg-muted/20 border-border border-b px-4 py-3">
          <Table className="w-full">
            <TableHeader>
              <TableRow>
                {subColumns.map((column) => (
                  <TableHead
                    key={String(column.id)}
                    style={{ width: column.width }}
                    className={cn(
                      'text-muted-foreground h-8 text-xs font-semibold whitespace-nowrap',
                      column.align === 'center' && 'text-center',
                      column.align === 'right' && 'text-right'
                    )}
                  >
                    {column.header}
                  </TableHead>
                ))}
              </TableRow>
            </TableHeader>
            <TableBody>
              {subData.map((subRow) => (
                <TableRow
                  key={subRow.id}
                  onClick={() => onSubRowClick?.(parentRow, subRow)}
                  className={cn('hover:bg-muted/30', onSubRowClick && 'cursor-pointer')}
                >
                  {subColumns.map((column) => (
                    <TableCell
                      key={String(column.id)}
                      className={cn(
                        'max-w-2xs truncate py-2 text-[13px]',
                        column.align === 'center' && 'text-center',
                        column.align === 'right' && 'text-right'
                      )}
                    >
                      {column.renderCell
                        ? column.renderCell(subRow)
                        : String(subRow[column.id as keyof S])}
                    </TableCell>
                  ))}
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </div>
      );
    },
    [subRowConfig]
  );

  const totalColumnsCount = columns.length + (selectable ? 1 : 0);

  const renderRow = useCallback(
    (row: T) => {
      const isExpanded = expandedRowId === row.id;
      const rowClickHandler = () => {
        if (isExpandable) {
          handleToggleExpand(row.id);
        } else if (isRowClickable) {
          onRowClick?.(row.id);
        }
      };

      return (
        <Fragment key={row.id}>
          <TableRow
            onClick={rowClickHandler}
            className={cn(
              (isRowClickable || isExpandable) && 'hover:bg-secondary/50 cursor-pointer',
              isExpanded && 'bg-secondary/30',
              rowClassName
            )}
          >
            {selectable && (
              <TableCell
                style={
                  scrollHorizontal
                    ? { ...getFrozenStyle(0), width: '48px', minWidth: '48px' }
                    : undefined
                }
                className={cn('bg-card text-center transition-colors', isFrozen(0) && 'z-10')}
              >
                <div className="flex items-center justify-center">
                  <Checkbox
                    checked={selectedRowIds.includes(row.id)}
                    onCheckedChange={() => handleSelectRow(row.id)}
                    onClick={(e) => e.stopPropagation()}
                  />
                </div>
              </TableCell>
            )}
            {columns.map((column, index) => {
              const colIndex = selectable ? index + 1 : index;
              const frozen = isFrozen(colIndex);
              const frozenStyle = getFrozenStyle(colIndex);
              const fixedRight = isFixedRight(column);
              const fixedRightStyle = getFixedRightStyle(column);

              return (
                <TableCell
                  key={`${row.id}-${String(column.id)}`}
                  style={{
                    ...frozenStyle,
                    ...fixedRightStyle,
                    ...(scrollHorizontal && column.width
                      ? { width: column.width, minWidth: column.width, maxWidth: column.width }
                      : undefined),
                  }}
                  className={cn(
                    'bg-card transition-colors',
                    scrollHorizontal
                      ? 'truncate px-4 py-3'
                      : 'max-w-2xs min-w-0 shrink truncate overflow-hidden',
                    frozen && 'z-10',
                    fixedRight && 'z-10',
                    style === 'grid' && !fixedRight && 'border-r',
                    column.align === 'center' && 'text-center',
                    column.align === 'right' && 'text-right',
                    column.cellClassName
                  )}
                >
                  {index === 0 && isExpandable ? (
                    <div className="flex items-center gap-2">
                      <Button
                        variant="ghost"
                        size="sm"
                        className="h-6 w-6 shrink-0 p-0"
                        onClick={(e) => handleToggleExpand(row.id, e)}
                      >
                        {isExpanded ? (
                          <ChevronUp className="h-4 w-4" />
                        ) : (
                          <ChevronDown className="h-4 w-4" />
                        )}
                      </Button>
                      <div className="max-w-2xs min-w-0">
                        {column.renderCell
                          ? column.renderCell(row)
                          : String(row[column.id as keyof T])}
                      </div>
                    </div>
                  ) : column.renderCell ? (
                    column.renderCell(row)
                  ) : (
                    String(row[column.id as keyof T])
                  )}
                </TableCell>
              );
            })}
          </TableRow>

          {isExpandable && (
            <tr className="border-0">
              <td colSpan={totalColumnsCount} className="border-0 p-0">
                <AnimatePresence initial={false}>
                  {isExpanded && (
                    <motion.div
                      initial={{ height: 0, opacity: 0 }}
                      animate={{ height: 'auto', opacity: 1 }}
                      exit={{ height: 0, opacity: 0 }}
                      transition={{ duration: 0.2, ease: 'easeInOut' }}
                      className="overflow-hidden"
                    >
                      <div className="bg-muted/20 border-border border-t">
                        {renderSubTable(row)}
                      </div>
                    </motion.div>
                  )}
                </AnimatePresence>
              </td>
            </tr>
          )}
        </Fragment>
      );
    },
    [
      columns,
      expandedRowId,
      selectable,
      selectedRowIds,
      scrollHorizontal,
      isFrozen,
      getFrozenStyle,
      isFixedRight,
      getFixedRightStyle,
      isExpandable,
      isRowClickable,
      rowClassName,
      handleSelectRow,
      handleToggleExpand,
      onRowClick,
      renderSubTable,
      totalColumnsCount,
      style,
    ]
  );

  const renderHeader = useMemo(
    () => (
      <TableRow className={cn('bg-muted shadow-sm', rowClassName)}>
        {selectable && (
          <TableHead
            style={getFrozenStyle(0)}
            className={cn('bg-muted w-12 text-center', isFrozen(0) && 'z-30')}
          >
            <Checkbox
              checked={
                selectionState.isAllSelected
                  ? true
                  : selectionState.isSomeSelected
                    ? 'indeterminate'
                    : false
              }
              onCheckedChange={handleSelectAll}
            />
          </TableHead>
        )}
        {columns.map((column, index) => {
          const colIndex = selectable ? index + 1 : index;
          const frozen = isFrozen(colIndex);
          const frozenStyle = getFrozenStyle(colIndex);
          const fixedRight = isFixedRight(column);
          const fixedRightStyle = getFixedRightStyle(column);

          return (
            <TableHead
              key={String(column.id)}
              style={{
                ...frozenStyle,
                ...fixedRightStyle,
                ...(scrollHorizontal && column.width
                  ? { width: column.width, minWidth: column.width, maxWidth: column.width }
                  : { width: column.width, maxWidth: column.width }),
              }}
              className={cn(
                'text-foreground bg-muted font-semibold',
                scrollHorizontal ? 'truncate' : 'whitespace-nowrap',
                frozen && 'z-30',
                fixedRight && 'z-30',
                column.align === 'center' && 'text-center',
                column.align === 'right' && 'text-right',
                column.cellClassName
              )}
            >
              {column.sortable ? (
                <SortableHeader
                  label={column.header}
                  sortKey={String(column.id)}
                  sortConfig={sortConfig}
                  onSort={onSortChange}
                />
              ) : column.filterable ? (
                <FilterableHeader
                  label={column.header}
                  filterConfig={filtersMap?.[String(column.id)]}
                  filterOptions={column.filterOptions}
                  onFilter={(filterOption) => onFiltersChange?.(String(column.id), filterOption)}
                />
              ) : (
                column.header
              )}
            </TableHead>
          );
        })}
      </TableRow>
    ),
    [
      columns,
      selectable,
      scrollHorizontal,
      filtersMap,
      sortConfig,
      onSortChange,
      onFiltersChange,
      rowClassName,
      isFrozen,
      getFrozenStyle,
      isFixedRight,
      getFixedRightStyle,
      selectionState,
      handleSelectAll,
    ]
  );

  return (
    <div className={cn('flex min-h-0 flex-col', className)}>
      <div
        ref={containerRef}
        className="bg-card relative flex min-h-0 flex-1 flex-col overflow-hidden rounded-lg border"
      >
        {scrollHorizontal && isOverflowing && frozenZoneWidth > 0 && (
          <div
            className="bg-border pointer-events-none absolute top-0 bottom-0 z-40 w-0.5 border"
            style={{ left: `${frozenZoneWidth}px` }}
          />
        )}
        {scrollHorizontal && isOverflowing && fixedRightZoneWidth > 0 && (
          <div
            className="bg-border pointer-events-none absolute top-0 bottom-0 z-40 w-px border"
            style={{ right: `${fixedRightZoneWidth - 1}px` }}
          />
        )}
        <ScrollArea type={scrollHorizontal ? 'always' : 'auto'} className="w-full flex-1">
          <div
            ref={scrollContentRef}
            className={cn(
              'flex min-h-full flex-col',
              scrollHorizontal ? 'w-max min-w-full' : 'w-full'
            )}
          >
            <Table className={cn(scrollHorizontal ? 'table-auto' : 'lg:table-fixed')}>
              <TableHeader className="sticky top-0 z-20">{renderHeader}</TableHeader>
              <TableBody>
                {isLoading ? (
                  <LoadingState />
                ) : data.length > 0 ? (
                  data.map(renderRow)
                ) : (
                  <EmptyState colSpan={totalColumnsCount} message={defaultMessage} />
                )}
              </TableBody>
            </Table>
          </div>
          <ScrollBar orientation="horizontal" className="z-10" />
        </ScrollArea>
      </div>
      {data.length > 0 && pagination && !isLoading && (
        <div className="mt-4">
          <Pagination params={pagination} initialPage={initialPage} onPageChange={onPageChange} />
        </div>
      )}
    </div>
  );
};

export default DataTable;
