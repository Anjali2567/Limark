'use client';

import { CheckCircle2, FileSpreadsheet } from 'lucide-react';

import { Button } from '@/components/ui/button';
import {
  Sheet,
  SheetContent,
  SheetDescription,
  SheetHeader,
  SheetTitle,
} from '@/components/ui/sheet';
import { ExcelColumnConfig, PreviewResponse } from '@/types/import.types';
import { ExcelDropZone } from './ExcelDropZone';
import { ImportPreviewTable } from './ImportPreviewTable';

type ExcelImportSheetProps = {
  open: boolean;
  onClose: () => void;
  entityLabel: string;
  columns: ExcelColumnConfig[];
  isUploading: boolean;
  isConfirming: boolean;
  uploadedFile: File | null;
  previewData: PreviewResponse | null;
  onFileSelect: (file: File) => void;
  onDownloadTemplate: () => void;
  onReset: () => void;
  onConfirm: () => void;
};

export const ExcelImportSheet = ({
  open,
  onClose,
  entityLabel,
  columns,
  isUploading,
  isConfirming,
  uploadedFile,
  previewData,
  onFileSelect,
  onDownloadTemplate,
  onReset,
  onConfirm,
}: ExcelImportSheetProps) => {
  const hasPreview = previewData !== null;
  const canConfirm = (previewData?.insertCount ?? 0) > 0 || (previewData?.updateCount ?? 0) > 0;

  return (
    <Sheet open={open} onOpenChange={(v) => !v && onClose()}>
      <SheetContent className="flex w-full flex-col gap-0 p-0 sm:max-w-5xl" side="right">
        <SheetHeader className="border-border bg-card shrink-0 border-b px-6 py-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-3">
              <div className="bg-primary/10 flex h-9 w-9 shrink-0 items-center justify-center rounded-lg">
                <FileSpreadsheet className="text-primary h-5 w-5" />
              </div>
              <div>
                <SheetTitle className="text-lg">
                  {hasPreview
                    ? `Preview - ${uploadedFile?.name ?? ''}`
                    : `Import ${entityLabel} from Excel`}
                </SheetTitle>
                <SheetDescription className="mt-0.5 text-xs">
                  {hasPreview
                    ? `${previewData.insertCount} to insert · ${previewData.updateCount} to update · ${previewData.skipCount} to skip`
                    : `Upload a .xlsx or .xls file to bulk-add ${entityLabel.toLowerCase()} records`}
                </SheetDescription>
              </div>
            </div>
          </div>
        </SheetHeader>
        <div className="min-h-0 flex-1 overflow-y-auto p-6">
          {hasPreview ? (
            <ImportPreviewTable preview={previewData} columns={columns} />
          ) : (
            <ExcelDropZone
              isUploading={isUploading}
              columns={columns}
              onFileSelect={onFileSelect}
              onDownloadTemplate={onDownloadTemplate}
            />
          )}
        </div>
        <div className="border-border bg-card flex shrink-0 items-center justify-between gap-3 border-t px-6 py-4">
          <Button variant="outline" onClick={hasPreview ? onReset : onClose}>
            {hasPreview ? 'Upload Different File' : 'Cancel'}
          </Button>

          {hasPreview && (
            <Button onClick={onConfirm} disabled={!canConfirm || isConfirming} className="gap-2">
              {isConfirming ? (
                <div className="border-primary-foreground h-4 w-4 animate-spin rounded-full border-2 border-t-transparent" />
              ) : (
                <CheckCircle2 className="h-4 w-4" />
              )}
              Confirm Import ({(previewData?.insertCount ?? 0) + (previewData?.updateCount ?? 0)}{' '}
              records)
            </Button>
          )}
        </div>
      </SheetContent>
    </Sheet>
  );
};
