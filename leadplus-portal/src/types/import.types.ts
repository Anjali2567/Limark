export { ImportStatus, ImportType, TargetEntity, RecordStatus } from '@/constants/import.constants';
import { ImportStatus, ImportType, TargetEntity, RecordStatus } from '@/constants/import.constants';

export type UploadResponse = {
  batchId: number;
  targetEntity: TargetEntity;
  status: ImportStatus;
  originalFileName: string;
  createdAt: string;
  detectedColumns: string[];
  ignoredColumns: string[];
  missingRequiredColumn?: string;
  templateGuide?: string;
  appliedMapping?: Record<string, string>;
};

export type BatchResponse = {
  id: number;
  importTypes: ImportType[];
  targetEntity: TargetEntity;
  originalFileName: string;
  status: ImportStatus;
  totalRecords: number;
  insertedRecords: number;
  updatedRecords: number;
  skippedRecords: number;
  failedRecords: number;
  startedAt?: string;
  completedAt?: string;
  createdAt: string;
};

export type PreviewInsertItem = {
  rowNumber: number;
  matchKey: string;
  resolvedImportType: ImportType;
  fieldsToCreate: Record<string, string>;
};

export type PreviewUpdateItem = {
  rowNumber: number;
  matchKey: string;
  existingEntityId: number;
  resolvedImportType: ImportType;
  fieldsToFill: string[];
  newValues: Record<string, string>;
};

export type PreviewSkipItem = {
  rowNumber: number;
  matchKey: string;
  reason: RecordStatus;
  errorCode?: string;
  errorMessage?: string;
  normalizedPayload?: Record<string, string>;
};

export type PreviewResponse = {
  totalRows: number;
  insertCount: number;
  updateCount: number;
  skipCount: number;
  toInsert: PreviewInsertItem[];
  toUpdate: PreviewUpdateItem[];
  toSkip: PreviewSkipItem[];
};

export type RecordResponse = {
  id: number;
  importId: number;
  recordNumber: number;
  matchKey: string;
  resolvedImportType: ImportType;
  status: RecordStatus;
  targetEntityId?: number;
  errorCode?: string;
  errorMessage?: string;
  changesJson?: string;
  processedAt?: string;
};

export type ExcelColumnConfig = {
  header: string;
  label?: string;
  required?: boolean;
  sampleValue: string;
  showInPreview?: boolean;
  fieldKey?: string;
};
