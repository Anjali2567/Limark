'use client';

import {
  AlertTriangle,
  ArrowUpFromLine,
  ChevronLeft,
  FileSpreadsheet,
  History,
  RotateCcw,
} from 'lucide-react';

import { Button } from '@/components/ui/button';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { ImportStatus } from '@/constants/import.constants';
import { BatchResponse } from '@/types/import.types';

type ImportHistoryDialogProps = {
  open: boolean;
  onClose: () => void;
  entityLabel: string;
  batches: BatchResponse[];
  lastActiveBatch: BatchResponse | null;
  isRollingBack: boolean;
  isLoadingHistory: boolean;
  onRollback: (batchId: number) => void;
  rollbackConfirmId: number | null;
  onSetRollbackConfirmId: (id: number | null) => void;
  onOpenImport: () => void;
};

const formatBatchDate = (iso: string) => {
  return new Date(iso).toLocaleString('en-US', {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
};

export const ImportHistoryDialog = ({
  open,
  onClose,
  entityLabel,
  batches,
  lastActiveBatch,
  isRollingBack,
  isLoadingHistory,
  onRollback,
  rollbackConfirmId,
  onSetRollbackConfirmId,
  onOpenImport,
}: ImportHistoryDialogProps) => {
  const activeBatchesCount = batches.filter((b) => b.status !== ImportStatus.PENDING).length;
  const pendingBatch = batches.find((b) => b.id === rollbackConfirmId);

  return (
    <Dialog open={open} onOpenChange={(v) => !v && onClose()}>
      <DialogContent className="flex max-h-[80vh] flex-col gap-0 overflow-hidden p-0 sm:max-w-2xl">
        <DialogHeader className="border-border shrink-0 border-b px-6 py-4">
          <div className="flex items-center gap-3">
            <div className="bg-primary/10 flex h-9 w-9 shrink-0 items-center justify-center rounded-lg">
              <History className="text-primary h-5 w-5" />
            </div>
            <div>
              <DialogTitle>Import Batch History</DialogTitle>
              <DialogDescription className="mt-0.5 text-xs">
                {isLoadingHistory
                  ? 'Loading…'
                  : batches.length === 0
                    ? `No ${entityLabel.toLowerCase()} imports yet`
                    : `${batches.length} import${batches.length !== 1 ? 's' : ''} · ${activeBatchesCount} active`}
              </DialogDescription>
            </div>
          </div>
        </DialogHeader>

        <div className="min-h-0 flex-1 overflow-y-auto px-6 py-4">
          {isLoadingHistory ? (
            <div className="flex items-center justify-center py-16">
              <div className="border-primary h-7 w-7 animate-spin rounded-full border-2 border-t-transparent" />
            </div>
          ) : batches.length === 0 ? (
            <div className="flex flex-col items-center justify-center gap-3 py-16 text-center">
              <div className="bg-muted flex h-14 w-14 items-center justify-center rounded-2xl">
                <FileSpreadsheet className="text-muted-foreground h-7 w-7" />
              </div>
              <p className="text-foreground font-medium">No import batches yet</p>
              <p className="text-muted-foreground max-w-xs text-sm">
                Use the &ldquo;Import {entityLabel}&rdquo; button to upload an Excel file. Each
                upload will appear here and the last one can be rolled back.
              </p>
              <Button
                variant="outline"
                size="sm"
                onClick={() => {
                  onClose();
                  onOpenImport();
                }}
                className="mt-2 gap-2"
              >
                <ArrowUpFromLine className="h-4 w-4" />
                Import {entityLabel}
              </Button>
            </div>
          ) : (
            <div className="space-y-3">
              {batches
                .filter((b) => b.status !== ImportStatus.PENDING)
                .map((batch) => {
                  const isRollbackEligible = lastActiveBatch?.id === batch.id;
                  const isRolledBack = batch.status === ImportStatus.ROLLED_BACK;
                  return (
                    <div
                      key={batch.id}
                      className={`rounded-xl border p-4 transition-colors ${
                        isRolledBack
                          ? 'bg-muted/40 border-border opacity-60'
                          : 'bg-card border-border hover:border-primary/30'
                      }`}
                    >
                      <div className="flex items-start justify-between gap-3">
                        <div className="flex min-w-0 items-start gap-3">
                          <div
                            className={`flex h-9 w-9 shrink-0 items-center justify-center rounded-lg ${
                              !isRolledBack ? 'bg-violet-100' : 'bg-muted'
                            }`}
                          >
                            <FileSpreadsheet
                              className={`h-5 w-5 ${
                                !isRolledBack ? 'text-violet-600' : 'text-muted-foreground'
                              }`}
                            />
                          </div>
                          <div className="min-w-0">
                            <div className="flex flex-wrap items-center gap-2">
                              <p className="text-foreground truncate text-sm font-semibold">
                                {batch.originalFileName}
                              </p>
                              {isRolledBack && (
                                <span className="bg-muted text-muted-foreground border-border inline-flex items-center gap-1 rounded-full border px-2 py-0.5 text-xs line-through">
                                  Rolled back
                                </span>
                              )}
                            </div>
                            <p className="text-muted-foreground mt-0.5 text-xs">
                              {formatBatchDate(batch.createdAt)}
                            </p>
                            <div className="text-muted-foreground mt-1.5 flex items-center gap-3 text-xs">
                              <span className="font-medium text-emerald-600">
                                +{batch.insertedRecords} inserted
                              </span>
                              {batch.updatedRecords > 0 && (
                                <span className="font-medium text-sky-600">
                                  ~{batch.updatedRecords} updated
                                </span>
                              )}
                              {batch.skippedRecords > 0 && (
                                <span className="text-amber-600">
                                  {batch.skippedRecords} skipped
                                </span>
                              )}
                            </div>
                          </div>
                        </div>

                        {isRollbackEligible && rollbackConfirmId !== batch.id && (
                          <div className="shrink-0">
                            <Button
                              variant="outline"
                              size="sm"
                              onClick={() => onSetRollbackConfirmId(batch.id)}
                              className="text-muted-foreground hover:text-destructive hover:border-destructive gap-1.5"
                            >
                              <RotateCcw className="h-3.5 w-3.5" />
                              Rollback
                            </Button>
                          </div>
                        )}
                      </div>
                    </div>
                  );
                })}
            </div>
          )}
        </div>

        {pendingBatch && (
          <div className="mx-0 flex shrink-0 items-start gap-2 border-t border-amber-200 bg-amber-50 px-6 py-3">
            <AlertTriangle className="mt-0.5 h-4 w-4 shrink-0 text-amber-600" />
            <p className="text-sm text-amber-800">
              Rolling back{' '}
              <span className="font-semibold">&ldquo;{pendingBatch.originalFileName}&rdquo;</span>{' '}
              will revert the {pendingBatch.insertedRecords} inserted record
              {pendingBatch.insertedRecords !== 1 ? 's' : ''} from the server. This cannot be
              undone.
            </p>
          </div>
        )}

        <div className="border-border bg-card flex shrink-0 items-center justify-between gap-3 border-t px-6 py-3">
          {rollbackConfirmId ? (
            <Button
              variant="outline"
              onClick={() => onSetRollbackConfirmId(null)}
              className="gap-2"
            >
              <ChevronLeft className="h-4 w-4" />
              Back
            </Button>
          ) : (
            <Button variant="outline" onClick={onClose}>
              Close
            </Button>
          )}

          {rollbackConfirmId && (
            <div className="flex items-center gap-2">
              <Button
                variant="outline"
                onClick={() => {
                  onClose();
                  onSetRollbackConfirmId(null);
                }}
              >
                Cancel
              </Button>
              <Button
                variant="destructive"
                disabled={isRollingBack}
                onClick={() => onRollback(rollbackConfirmId)}
                className="gap-1.5"
              >
                {isRollingBack ? (
                  <div className="h-3.5 w-3.5 animate-spin rounded-full border-2 border-white border-t-transparent" />
                ) : (
                  <RotateCcw className="h-4 w-4" />
                )}
                Confirm Rollback
              </Button>
            </div>
          )}
        </div>
      </DialogContent>
    </Dialog>
  );
};
