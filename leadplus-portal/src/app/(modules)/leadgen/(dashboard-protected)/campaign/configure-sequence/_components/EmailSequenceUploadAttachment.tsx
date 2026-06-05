import { ChangeEvent, DragEvent, useCallback, useEffect, useRef, useState } from 'react';
import { toast } from 'sonner';

import { Button } from '@/components/ui/button';
import { useAuth } from '@/context/AuthContext';
import { useMultipleAddAttachment } from '@/hooks/useAttachments';
import { cn } from '@/lib/utils/helpers';

import { Loader2, Upload } from 'lucide-react';

type EmailSequenceUploadAttachmentProps = {
  setIsBusy: (value: boolean) => void;
  onAttachmentSave: (attachmentIds: string) => void;
};

const EmailSequenceUploadAttachment = ({
  onAttachmentSave,
  setIsBusy,
}: EmailSequenceUploadAttachmentProps) => {
  const { authenticatedUserDetails } = useAuth();
  const { mutate, isPending } = useMultipleAddAttachment();

  useEffect(() => {
    setIsBusy(isPending);
  }, [isPending, setIsBusy]);

  const fileInputRef = useRef<HTMLInputElement | null>(null);
  const [isDragging, setIsDragging] = useState(false);

  const handleBrowseClick = useCallback(() => {
    fileInputRef.current?.click();
  }, []);

  const handleDragOver = useCallback(
    (e: DragEvent) => {
      e.preventDefault();
      e.stopPropagation();
      if (!isPending) setIsDragging(true);
    },
    [isPending]
  );

  const handleDragLeave = useCallback((e: DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragging(false);
  }, []);

  const handleUpload = useCallback(
    (files: FileList | null) => {
      if (!files?.length) return;
      const fileArray = Array.from(files);

      mutate(
        {
          tenantId: authenticatedUserDetails?.tenantId || '',
          workspaceId: authenticatedUserDetails?.workspaceId || '',
          files: fileArray,
        },
        {
          onSuccess: (data) => {
            toast.success('Files uploaded successfully.');
            data.forEach((file) => onAttachmentSave(file.id));
          },
          onError: (error) => {
            toast.error(error?.message || 'Failed to upload files.');
          },
        }
      );
    },
    [authenticatedUserDetails, mutate, onAttachmentSave]
  );

  const handleDrop = useCallback(
    (e: DragEvent) => {
      e.preventDefault();
      e.stopPropagation();
      setIsDragging(false);

      if (!isPending) {
        handleUpload(e.dataTransfer.files);
      }
    },
    [handleUpload, isPending]
  );

  const handleFileChange = useCallback(
    (e: ChangeEvent<HTMLInputElement>) => {
      handleUpload(e.target.files);
      e.target.value = '';
    },
    [handleUpload]
  );

  return (
    <div
      onDragOver={handleDragOver}
      onDragLeave={handleDragLeave}
      onDrop={handleDrop}
      className={cn(
        'flex items-center justify-between gap-4 rounded-lg border-2 border-dashed p-4 transition-colors',
        isDragging
          ? 'border-sky-500 bg-sky-50'
          : 'border-border bg-secondary/30 hover:bg-secondary/50',
        isPending && 'cursor-not-allowed opacity-60'
      )}
    >
      <div className="flex items-center gap-3">
        <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-sky-100">
          {isPending ? (
            <Loader2 className="h-5 w-5 animate-spin text-sky-500" />
          ) : (
            <Upload className="h-5 w-5 text-sky-500" />
          )}
        </div>
        <div className="text-muted-foreground">
          <p className="text-sm">
            {isPending ? 'Uploading files...' : 'Drag and drop files here or click to browse'}
          </p>
          <p className="mt-0.5 text-xs">
            Files will be automatically added to your Resources library
          </p>
        </div>
      </div>
      <Button
        type="button"
        variant="outline"
        disabled={isPending}
        className="shrink-0 border-sky-500 text-sky-500 hover:bg-sky-50 hover:text-sky-600"
        onClick={handleBrowseClick}
      >
        <Upload className="mr-2 h-4 w-4" />
        Upload from computer
      </Button>
      <input
        ref={fileInputRef}
        type="file"
        multiple
        className="hidden"
        onChange={handleFileChange}
        disabled={isPending}
        aria-label="Upload attachment files"
      />
    </div>
  );
};

export { EmailSequenceUploadAttachment };
