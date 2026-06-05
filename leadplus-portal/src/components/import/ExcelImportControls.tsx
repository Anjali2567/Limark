'use client';

import { Button } from '@/components/ui/button';
import { UseExcelImportReturn } from '@/hooks/useExcelImport';
import { ExcelImportSheet } from './ExcelImportSheet';
import { ImportHistoryDialog } from './ImportHistoryDialog';

import { ArrowUpFromLine, History } from 'lucide-react';

type ExcelImportControlsProps = {
  entityLabel: string;
  importHook: UseExcelImportReturn;
};

export const ExcelImportControls = ({ entityLabel, importHook }: ExcelImportControlsProps) => {
  return (
    <>
      <Button
        variant="outline"
        size="sm"
        onClick={importHook.openHistory}
        className="relative gap-2"
      >
        <History className="h-4 w-4" />
        Batch History
        {importHook.activeBatchesCount > 0 && (
          <span className="bg-primary text-primary-foreground ml-1 inline-flex h-4 w-4 items-center justify-center rounded-full text-xs">
            {importHook.activeBatchesCount}
          </span>
        )}
      </Button>

      <Button size="sm" onClick={importHook.openSheet} className="gap-2">
        <ArrowUpFromLine className="h-4 w-4" />
        Import {entityLabel}
      </Button>

      <ExcelImportSheet
        open={importHook.isSheetOpen}
        onClose={importHook.closeSheet}
        entityLabel={entityLabel}
        columns={importHook.columns}
        isUploading={importHook.isUploading}
        isConfirming={importHook.isConfirming}
        uploadedFile={importHook.uploadedFile}
        previewData={importHook.previewData}
        onFileSelect={importHook.handleFileSelect}
        onDownloadTemplate={importHook.downloadTemplate}
        onReset={importHook.resetToDropzone}
        onConfirm={importHook.confirmImport}
      />

      <ImportHistoryDialog
        open={importHook.isHistoryOpen}
        onClose={importHook.closeHistory}
        entityLabel={entityLabel}
        batches={importHook.batches}
        lastActiveBatch={importHook.lastActiveBatch}
        isRollingBack={importHook.isRollingBack}
        isLoadingHistory={importHook.isLoadingHistory}
        onRollback={importHook.rollbackBatch}
        rollbackConfirmId={importHook.rollbackConfirmId}
        onSetRollbackConfirmId={importHook.setRollbackConfirmId}
        onOpenImport={importHook.openSheet}
      />
    </>
  );
};
