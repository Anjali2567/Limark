'use client';

import { useCallback, useRef, useState } from 'react';
import * as XLSX from 'xlsx';
import { toast } from 'sonner';
import { useQueryClient } from '@tanstack/react-query';

import { QUERY_KEY } from '@/constants/queryKey.constants';

import { ImportStatus, TargetEntity } from '@/constants/import.constants';
import {
  BatchResponse,
  ExcelColumnConfig,
  PreviewResponse,
  UploadResponse,
} from '@/types/import.types';
import {
  confirmImport as confirmImportApi,
  listImportBatches,
  previewImport,
  rollbackImport as rollbackImportApi,
} from '@/lib/api/leadImport.api';

export type ExcelImportConfig = {
  columns: ExcelColumnConfig[];
  templateFilename: string;
  uploadFn: (file: File) => Promise<UploadResponse>;
  targetEntity: TargetEntity;
};

export const useExcelImport = ({
  columns,
  templateFilename,
  uploadFn,
  targetEntity,
}: ExcelImportConfig) => {
  const queryClient = useQueryClient();
  const [isSheetOpen, setIsSheetOpen] = useState(false);
  const [isHistoryOpen, setIsHistoryOpen] = useState(false);

  const [step, setStep] = useState<'dropzone' | 'preview'>('dropzone');
  const [uploadedFile, setUploadedFile] = useState<File | null>(null);
  const [batchId, setBatchId] = useState<number | null>(null);
  const [previewData, setPreviewData] = useState<PreviewResponse | null>(null);
  const [isUploading, setIsUploading] = useState(false);
  const [isConfirming, setIsConfirming] = useState(false);

  const [batches, setBatches] = useState<BatchResponse[]>([]);
  const [isLoadingHistory, setIsLoadingHistory] = useState(false);
  const [isRollingBack, setIsRollingBack] = useState(false);
  const [rollbackConfirmId, setRollbackConfirmId] = useState<number | null>(null);

  const fileInputRef = useRef<HTMLInputElement>(null);

  const downloadTemplate = useCallback(() => {
    const headers = columns.map((c) => c.header);
    const sampleRow = columns.map((c) => c.sampleValue);
    const ws = XLSX.utils.aoa_to_sheet([headers, sampleRow]);
    const wb = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, 'Template');
    XLSX.writeFile(wb, templateFilename);
  }, [columns, templateFilename]);

  const handleFileSelect = useCallback(
    async (file: File) => {
      const ext = file.name.split('.').pop()?.toLowerCase();
      if (!ext || !['xlsx', 'xls'].includes(ext)) {
        toast.error('Please upload a .xlsx or .xls file');
        return;
      }

      setUploadedFile(file);
      setIsUploading(true);

      try {
        const uploadResult = await uploadFn(file);
        const preview = await previewImport(uploadResult.batchId);
        setBatchId(uploadResult.batchId);
        setPreviewData(preview);
        setStep('preview');
      } catch {
        toast.error('Upload failed. Please try again.');
        setUploadedFile(null);
      } finally {
        setIsUploading(false);
      }
    },
    [uploadFn]
  );

  const openSheet = useCallback(() => {
    setStep('dropzone');
    setUploadedFile(null);
    setBatchId(null);
    setPreviewData(null);
    setIsSheetOpen(true);
  }, []);

  const closeSheet = useCallback(() => {
    setIsSheetOpen(false);
    setStep('dropzone');
    setUploadedFile(null);
    setBatchId(null);
    setPreviewData(null);
  }, []);

  const resetToDropzone = useCallback(() => {
    setStep('dropzone');
    setUploadedFile(null);
    setBatchId(null);
    setPreviewData(null);
  }, []);

  const confirmImport = useCallback(async () => {
    if (!batchId) return;
    setIsConfirming(true);
    try {
      const batch = await confirmImportApi(batchId);
      toast.success(
        `Import complete - ${batch.insertedRecords} inserted, ${batch.updatedRecords} updated, ${batch.skippedRecords} skipped`
      );
      queryClient.invalidateQueries({ queryKey: [QUERY_KEY.LEADS] });
      closeSheet();
    } catch {
      toast.error('Import failed. Please try again.');
    } finally {
      setIsConfirming(false);
    }
  }, [batchId, closeSheet, queryClient]);

  // ── History ───────────────────────────────────────────────────────────────

  const loadHistory = useCallback(async () => {
    setIsLoadingHistory(true);
    try {
      const result = await listImportBatches({
        targetEntity,
        page: 0,
        size: 20,
        sort: 'createdAt,desc',
      });
      setBatches(result.content);
    } catch {
      toast.error('Failed to load import history.');
    } finally {
      setIsLoadingHistory(false);
    }
  }, [targetEntity]);

  const openHistory = useCallback(() => {
    setIsHistoryOpen(true);
    loadHistory();
  }, [loadHistory]);

  const closeHistory = useCallback(() => {
    setIsHistoryOpen(false);
  }, []);

  const rollbackBatch = useCallback(
    async (id: number) => {
      setIsRollingBack(true);
      try {
        await rollbackImportApi(id);
        const batch = batches.find((b) => b.id === id);
        setBatches((prev) =>
          prev.map((b) => (b.id === id ? { ...b, status: ImportStatus.ROLLED_BACK } : b))
        );
        setRollbackConfirmId(null);
        toast.success(`Batch "${batch?.originalFileName ?? ''}" rolled back successfully`);
        queryClient.invalidateQueries({ queryKey: [QUERY_KEY.LEADS] });
      } catch {
        toast.error('Rollback failed. Please try again.');
      } finally {
        setIsRollingBack(false);
      }
    },
    [batches, queryClient]
  );

  const rollbackEligible = batches.filter(
    (b) => b.status === ImportStatus.COMPLETED || b.status === ImportStatus.PARTIAL_SUCCESS
  );
  const lastActiveBatch = rollbackEligible[0] ?? null;

  return {
    isSheetOpen,
    openSheet,
    closeSheet,
    uploadedFile,
    previewData,
    isUploading,
    step,
    handleFileSelect,
    resetToDropzone,
    fileInputRef,
    downloadTemplate,
    isConfirming,
    confirmImport,
    isHistoryOpen,
    openHistory,
    closeHistory,
    batches,
    isLoadingHistory,
    lastActiveBatch,
    activeBatchesCount: rollbackEligible.length,
    isRollingBack,
    rollbackBatch,
    rollbackConfirmId,
    setRollbackConfirmId,
    columns,
  };
};

export type UseExcelImportReturn = ReturnType<typeof useExcelImport>;
