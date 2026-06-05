'use client';

import { useCallback, useRef } from 'react';
import { Upload, Download, Info, CheckCircle2 } from 'lucide-react';

import { Button } from '@/components/ui/button';
import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from '@/components/ui/tooltip';
import { ExcelColumnConfig } from '@/types/import.types';

type ExcelDropZoneProps = {
  isUploading: boolean;
  columns: ExcelColumnConfig[];
  onFileSelect: (file: File) => void;
  onDownloadTemplate: () => void;
};

export const ExcelDropZone = ({
  isUploading,
  columns,
  onFileSelect,
  onDownloadTemplate,
}: ExcelDropZoneProps) => {
  const fileInputRef = useRef<HTMLInputElement>(null);

  const handleDrop = useCallback(
    (e: React.DragEvent) => {
      e.preventDefault();
      const file = e.dataTransfer.files?.[0];
      if (file) onFileSelect(file);
    },
    [onFileSelect]
  );

  const handleFileChange = useCallback(
    (e: React.ChangeEvent<HTMLInputElement>) => {
      const file = e.target.files?.[0];
      if (file) onFileSelect(file);
      // Reset so the same file can be re-selected
      e.target.value = '';
    },
    [onFileSelect]
  );

  const previewColumns = columns;
  const requiredCount = columns.filter((c) => c.required).length;

  return (
    <div className="space-y-6">
      <div className="border-border bg-muted/30 space-y-4 rounded-xl border p-5">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <h3 className="text-foreground text-sm font-semibold">Expected File Format</h3>
            <TooltipProvider delayDuration={100}>
              <Tooltip>
                <TooltipTrigger asChild>
                  <button className="text-sky-500 transition-colors hover:text-sky-600 focus:outline-none">
                    <Info className="h-4 w-4" />
                  </button>
                </TooltipTrigger>
                <TooltipContent side="right" className="max-w-xs space-y-2 p-3 text-left">
                  {requiredCount > 0 && (
                    <div className="flex items-start gap-2 text-xs">
                      <span className="text-destructive mt-0.5 font-bold">*</span>
                      <span>
                        Fields marked with <span className="text-destructive font-medium">*</span>{' '}
                        are required. Rows missing them will be skipped.
                      </span>
                    </div>
                  )}
                  <div className="flex items-start gap-2 text-xs">
                    <CheckCircle2 className="mt-0.5 h-3.5 w-3.5 shrink-0 text-emerald-500" />
                    <span>
                      The first row must be the header row matching the column names below.
                    </span>
                  </div>
                  <div className="flex items-start gap-2 text-xs">
                    <CheckCircle2 className="mt-0.5 h-3.5 w-3.5 shrink-0 text-emerald-500" />
                    <span>
                      Duplicate rows (same identifier) within the file will be skipped
                      automatically.
                    </span>
                  </div>
                  <div className="flex items-start gap-2 text-xs">
                    <CheckCircle2 className="mt-0.5 h-3.5 w-3.5 shrink-0 text-emerald-500" />
                    <span>
                      Each import is tracked as a batch and the last one can be rolled back.
                    </span>
                  </div>
                </TooltipContent>
              </Tooltip>
            </TooltipProvider>
          </div>
          <Button
            variant="outline"
            size="sm"
            onClick={onDownloadTemplate}
            className="gap-2 border-sky-300 text-sky-600 hover:bg-sky-50"
          >
            <Download className="h-3.5 w-3.5" />
            Download Template
          </Button>
        </div>

        {/* Column header preview */}
        <div className="border-border overflow-x-auto rounded-lg border">
          <table className="w-full text-xs">
            <thead>
              <tr className="bg-muted border-border border-b">
                {previewColumns.map((col) => (
                  <th key={col.header} className="px-3 py-2 text-left whitespace-nowrap">
                    <span className="text-foreground font-semibold">
                      {col.label ?? col.header}
                      {col.required && <span className="text-destructive ml-0.5">*</span>}
                    </span>
                    {col.label && (
                      <span className="text-muted-foreground/70 mt-0.5 block font-normal">
                        {col.header}
                      </span>
                    )}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody>
              <tr className="bg-card text-muted-foreground italic">
                {previewColumns.map((col) => (
                  <td key={col.header} className="px-3 py-2 whitespace-nowrap">
                    {col.sampleValue}
                  </td>
                ))}
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      {/* Drag-drop zone */}
      <div
        onDragOver={(e) => e.preventDefault()}
        onDrop={handleDrop}
        onClick={() => !isUploading && fileInputRef.current?.click()}
        className="border-border hover:border-primary/50 hover:bg-muted/30 relative flex cursor-pointer flex-col items-center justify-center gap-4 rounded-xl border-2 border-dashed p-12 transition-all"
      >
        <input
          ref={fileInputRef}
          type="file"
          accept=".xlsx,.xls"
          className="hidden"
          onChange={handleFileChange}
        />

        <div className="bg-muted flex h-16 w-16 items-center justify-center rounded-2xl">
          {isUploading ? (
            <div className="border-primary h-7 w-7 animate-spin rounded-full border-2 border-t-transparent" />
          ) : (
            <Upload className="text-muted-foreground h-8 w-8" />
          )}
        </div>

        {isUploading ? (
          <p className="text-foreground font-medium">Uploading…</p>
        ) : (
          <>
            <div className="text-center">
              <p className="text-foreground font-semibold">Drop your Excel file here</p>
              <p className="text-muted-foreground mt-1 text-sm">
                or <span className="text-primary underline">browse your computer</span>
              </p>
            </div>
            <p className="text-muted-foreground text-xs">Supports .xlsx and .xls files</p>
          </>
        )}
      </div>
    </div>
  );
};
