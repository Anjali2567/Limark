import { SourceType } from '@/constants/attachment.constant';
import { PaginationParams } from './paginated-params';
import { BasicParams } from './user.types';

export type SourceAttachment = {
  id: string;
  sourceId: string;
  sourceType: SourceType;
  fileName: string;
  fileType: string;
  fileUrl: string;
  sizeBytes: number;
  createdBy: string;
  createdAt: Date;
};

export type Attachment = {
  id: string;
  workspaceId: string;
  filename: string;
  fileUrl: string;
  fileType: string;
  sizeBytes: number;
  updatedAt: string;
  updatedBy: string;
};

export type AttachmentSearchParams = {
  query?: string;
  fileTypes?: string[];
} & PaginationParams;

export type GetAttachmentParams = BasicParams & {
  params?: AttachmentSearchParams;
};

export type AddAttachmentRequestParams = BasicParams & {
  file: File;
};

export type AddMultipleAttachmentRequestParams = BasicParams & {
  files: File[];
};

export type DeleteAttachmentParams = BasicParams & {
  attachmentId: string;
};

export type RenameAttachmentParams = BasicParams & {
  attachmentId: string;
  requestBody: {
    fileName: string;
  };
};

export type ReplaceAttachmentParams = BasicParams & {
  attachmentId: string;
  file: File;
};
