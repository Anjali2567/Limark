'use client';

import { AlertCircle, CheckCircle2, Eye, MinusCircle } from 'lucide-react';

import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { ExcelColumnConfig, PreviewResponse } from '@/types/import.types';

type ImportPreviewTableProps = {
  preview: PreviewResponse;
  columns: ExcelColumnConfig[];
};

const SUMMARY_COLORS = {
  insert: {
    bg: 'bg-emerald-50',
    border: 'border-emerald-200',
    icon: 'text-emerald-600',
    label: 'text-emerald-700',
    count: 'text-emerald-900',
  },
  update: {
    bg: 'bg-sky-50',
    border: 'border-sky-200',
    icon: 'text-sky-600',
    label: 'text-sky-700',
    count: 'text-sky-900',
  },
  skip: {
    bg: 'bg-amber-50',
    border: 'border-amber-200',
    icon: 'text-amber-600',
    label: 'text-amber-700',
    count: 'text-amber-900',
  },
} as const;

const formatKey = (key: string): string => {
  return key
    .replace(/([A-Z])/g, ' $1')
    .replace(/_/g, ' ')
    .trim()
    .replace(/\b\w/g, (c) => c.toUpperCase());
};

const colLabel = (col: { label?: string; header: string }): string => {
  return col.label ?? formatKey(col.header);
};

export const ImportPreviewTable = ({ preview, columns }: ImportPreviewTableProps) => {
  const { toInsert, toUpdate, toSkip, insertCount, updateCount, skipCount } = preview;

  const previewCols = columns.filter((c) => c.showInPreview);

  const summaryCards = [
    {
      key: 'insert' as const,
      label: 'To Insert',
      count: insertCount,
      Icon: CheckCircle2,
      desc: 'New records',
    },
    {
      key: 'update' as const,
      label: 'To Update',
      count: updateCount,
      Icon: Eye,
      desc: 'Existing records',
    },
    {
      key: 'skip' as const,
      label: 'To Skip',
      count: skipCount,
      Icon: MinusCircle,
      desc: 'Invalid / duplicate',
    },
  ];

  return (
    <div className="space-y-5">
      <div className="grid grid-cols-3 gap-4">
        {summaryCards.map(({ key, label, count, Icon, desc }) => {
          const c = SUMMARY_COLORS[key];
          return (
            <div
              key={key}
              className={`rounded-xl border ${c.border} ${c.bg} flex items-center gap-3 p-4`}
            >
              <div className={c.icon}>
                <Icon className="h-5 w-5" />
              </div>
              <div>
                <p className={`text-2xl font-bold ${c.count}`}>{count}</p>
                <p className={`text-xs font-medium ${c.label}`}>{label}</p>
                <p className="text-muted-foreground text-xs">{desc}</p>
              </div>
            </div>
          );
        })}
      </div>

      <Tabs defaultValue="insert">
        <TabsList className="w-full">
          <TabsTrigger value="insert" className="flex-1">
            Insert ({insertCount})
          </TabsTrigger>
          <TabsTrigger value="update" className="flex-1">
            Update ({updateCount})
          </TabsTrigger>
          <TabsTrigger value="skip" className="flex-1">
            Skip ({skipCount})
          </TabsTrigger>
        </TabsList>

        <TabsContent value="insert">
          <div className="border-border mt-2 overflow-x-auto rounded-lg border">
            <table className="w-full text-sm">
              <thead>
                <tr className="bg-muted border-border text-muted-foreground border-b text-xs font-semibold">
                  {previewCols.map((col) => (
                    <th key={col.header} className="px-3 py-2 text-left whitespace-nowrap">
                      {colLabel(col)}
                    </th>
                  ))}
                </tr>
              </thead>
              <tbody className="divide-border divide-y">
                {toInsert.length === 0 ? (
                  <tr>
                    <td
                      colSpan={Math.max(previewCols.length, 1)}
                      className="text-muted-foreground px-3 py-8 text-center"
                    >
                      No records to insert
                    </td>
                  </tr>
                ) : (
                  toInsert.map((row, i) => (
                    <tr key={i} className="hover:bg-muted/30">
                      {previewCols.map((col) => (
                        <td
                          key={col.header}
                          className="text-muted-foreground max-w-50 truncate px-3 py-2.5"
                        >
                          {row.fieldsToCreate[col.fieldKey ?? col.header] || (
                            <span className="text-muted-foreground/40">-</span>
                          )}
                        </td>
                      ))}
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </TabsContent>

        <TabsContent value="update">
          <div className="border-border mt-2 overflow-x-auto rounded-lg border">
            <table className="w-full text-sm">
              <thead>
                <tr className="bg-muted border-border text-muted-foreground border-b text-xs font-semibold">
                  {previewCols.map((col) => (
                    <th key={col.header} className="px-3 py-2 text-left whitespace-nowrap">
                      {colLabel(col)}
                    </th>
                  ))}
                </tr>
              </thead>
              <tbody className="divide-border divide-y">
                {toUpdate.length === 0 ? (
                  <tr>
                    <td
                      colSpan={Math.max(previewCols.length, 1)}
                      className="text-muted-foreground px-3 py-8 text-center"
                    >
                      No records to update
                    </td>
                  </tr>
                ) : (
                  toUpdate.map((row, i) => (
                    <tr key={i} className="hover:bg-sky-50/40">
                      {previewCols.map((col) => (
                        <td
                          key={col.header}
                          className="text-muted-foreground max-w-50 truncate px-3 py-2.5"
                        >
                          {row.newValues[col.fieldKey ?? col.header] || (
                            <span className="text-muted-foreground/40">-</span>
                          )}
                        </td>
                      ))}
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </TabsContent>

        <TabsContent value="skip">
          <div className="border-border mt-2 overflow-hidden rounded-lg border">
            <table className="w-full text-sm">
              <thead>
                <tr className="bg-muted border-border text-muted-foreground border-b text-xs font-semibold">
                  <th className="px-3 py-2 text-left">Row</th>
                  <th className="px-3 py-2 text-left">Reason Skipped</th>
                </tr>
              </thead>
              <tbody className="divide-border divide-y">
                {toSkip.length === 0 ? (
                  <tr>
                    <td colSpan={2} className="text-muted-foreground px-3 py-8 text-center">
                      No rows will be skipped
                    </td>
                  </tr>
                ) : (
                  toSkip.map((row, i) => (
                    <tr key={i} className="hover:bg-amber-50/40">
                      <td className="text-muted-foreground max-w-50 truncate px-3 py-2.5 font-medium">
                        {row.matchKey || `Row ${row.rowNumber}`}
                      </td>
                      <td className="px-3 py-2.5">
                        <div className="flex items-center gap-2 text-amber-700">
                          <AlertCircle className="h-3.5 w-3.5 shrink-0" />
                          <span className="text-xs">{row.errorMessage || row.reason}</span>
                        </div>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </TabsContent>
      </Tabs>
    </div>
  );
};
