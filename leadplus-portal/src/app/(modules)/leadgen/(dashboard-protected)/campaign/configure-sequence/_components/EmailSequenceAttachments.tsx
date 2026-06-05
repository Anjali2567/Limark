'use client';

import { useCallback, useEffect, useState } from 'react';

import { Button } from '@/components/ui/button';
import { Checkbox } from '@/components/ui/checkbox';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { ScrollArea } from '@/components/ui/scroll-area';
import { formatFileSize } from '@/lib/utils/file';
import { Attachment } from '@/types/attachment.types';

import { FileText, X } from 'lucide-react';
import { EmailSequenceUploadAttachment } from './EmailSequenceUploadAttachment';

type EmailSequenceAttachmentsProps = {
  isOpen: boolean;
  toggle: () => void;
  attachmentIds: string[];
  systemDocuments: Attachment[];
  onAttachmentsChange?: (ids: string[]) => void;
  isDisabled?: boolean;
};

const EmailSequenceAttachments = ({
  isOpen,
  toggle,
  attachmentIds = [],
  systemDocuments = [],
  onAttachmentsChange,
  isDisabled = false,
}: EmailSequenceAttachmentsProps) => {
  const [isBusy, setIsBusy] = useState<boolean>(false);
  const [localSelectedIds, setLocalSelectedIds] = useState<string[]>(attachmentIds);

  useEffect(() => {
    setLocalSelectedIds(attachmentIds);
  }, [attachmentIds]);

  const getAttachmentDetails = useCallback(
    (id: string) => {
      return systemDocuments.find((file) => file.id === id);
    },
    [systemDocuments]
  );

  const handleToggleAttachment = useCallback((id: string) => {
    setLocalSelectedIds((prev) =>
      prev.includes(id) ? prev.filter((existingId) => existingId !== id) : [...prev, id]
    );
  }, []);

  const handleConfirm = useCallback(() => {
    if (onAttachmentsChange) {
      onAttachmentsChange(localSelectedIds);
    }
    toggle();
  }, [localSelectedIds, onAttachmentsChange, toggle]);

  const handleRemoveAttachment = useCallback(
    (id: string) => {
      if (!onAttachmentsChange) return;
      onAttachmentsChange(attachmentIds.filter((existingId) => existingId !== id));
    },
    [attachmentIds, onAttachmentsChange]
  );

  return (
    <>
      <div className="border-border border-t pt-4">
        {attachmentIds && attachmentIds.length > 0 ? (
          <div className="flex flex-wrap gap-2">
            {attachmentIds.map((fileId: string, idx: number) => {
              const file = getAttachmentDetails(fileId);
              return (
                <div
                  key={idx}
                  className="bg-secondary/50 border-border flex items-center gap-2 rounded-md border px-3 py-1.5 text-sm"
                >
                  <FileText className="text-muted-foreground h-3 w-3" />
                  <span className="text-foreground max-w-37.5 truncate font-medium">
                    {file?.filename}
                  </span>
                  {!isDisabled && (
                    <Button
                      type="button"
                      variant="none"
                      onClick={() => handleRemoveAttachment(fileId)}
                      className="text-muted-foreground hover:text-destructive ml-1"
                    >
                      <X className="h-3 w-3" />
                    </Button>
                  )}
                </div>
              );
            })}
          </div>
        ) : (
          <div className="text-muted-foreground text-xs italic">No files attached</div>
        )}
      </div>
      <Dialog open={isOpen} onOpenChange={() => !isBusy && toggle()}>
        <DialogContent className="sm:max-w-3xl">
          <DialogHeader>
            <DialogTitle>Select Attachments</DialogTitle>
            <DialogDescription>
              Select files from your resources or upload new files to attach to this email.
            </DialogDescription>
          </DialogHeader>
          <EmailSequenceUploadAttachment
            onAttachmentSave={handleToggleAttachment}
            setIsBusy={setIsBusy}
          />
          {systemDocuments?.length > 0 ? (
            <>
              <div className="my-2 flex items-center gap-4">
                <div className="bg-border h-px flex-1"></div>
                <span className="text-muted-foreground text-sm">Or select from existing files</span>
                <div className="bg-border h-px flex-1"></div>
              </div>
              <ScrollArea className="max-h-100 pr-4">
                <div className="grid grid-cols-1 gap-4 p-1 md:grid-cols-2">
                  {systemDocuments.map((doc) => {
                    const isAttached = localSelectedIds?.includes(doc.id);
                    return (
                      <div
                        key={doc.id}
                        className={`flex cursor-pointer items-center space-x-3 rounded-lg border p-3 transition-colors ${
                          isAttached
                            ? 'border-sky-200 bg-sky-50'
                            : 'bg-secondary hover:bg-muted border-transparent'
                        }`}
                        onClick={() => handleToggleAttachment(doc.id)}
                      >
                        <Checkbox
                          checked={isAttached}
                          onCheckedChange={() => handleToggleAttachment(doc.id)}
                          className="border-gray-300"
                          onClick={(e) => e.stopPropagation()}
                        />
                        <div className="flex min-w-0 flex-1 items-center gap-3">
                          <div className="rounded border border-gray-100 bg-white p-1.5 shadow-sm">
                            <FileText className="h-4 w-4 text-sky-500" />
                          </div>
                          <div className="min-w-0">
                            <div
                              className="text-foreground truncate text-sm font-medium"
                              title={doc.filename}
                            >
                              {doc.filename}
                            </div>
                            {doc?.sizeBytes && (
                              <div className="text-muted-foreground text-xs">
                                {formatFileSize(doc.sizeBytes)}
                              </div>
                            )}
                          </div>
                        </div>
                      </div>
                    );
                  })}
                </div>
              </ScrollArea>
            </>
          ) : (
            <div className="text-muted-foreground text-xs italic">No files available</div>
          )}
          <DialogFooter>
            <Button variant="ghost" onClick={() => !isBusy && toggle()} className="mr-2">
              Cancel
            </Button>
            <Button
              onClick={handleConfirm}
              disabled={isBusy}
              className="bg-sky-500 text-white hover:bg-sky-600"
            >
              Add Attachments
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </>
  );
};

export { EmailSequenceAttachments };
