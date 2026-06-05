"use client";

import { useState } from "react";
import { toast } from "sonner";

import { FileUpload } from "@/components/FileUpload";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogTitle,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { useAuth } from "@/context/AuthContext";
import { useMultipleAddAttachment } from "@/hooks/useAttachments";
import useToggle from "@/hooks/useToggle";

import { FileIcon, Trash2, Upload } from "lucide-react";

type FileItem = {
  originalFile: File;
  name: string;
};

const AddResource = () => {
  const { authenticatedUserDetails } = useAuth();

  const { value: isUploadModalOpen, toggle } = useToggle(false);
  const [fileItems, setFileItems] = useState<FileItem[]>([]);

  const { mutate, isPending } = useMultipleAddAttachment();

  const addFilesToQueue = (files: FileList | null) => {
    if (!files) return;
    const newFiles = Array.from(files).map((file) => ({
      originalFile: file,
      name: file.name,
    }));
    setFileItems((prev) => [...prev, ...newFiles]);
  };

  const handleQueuedRename = (index: number, newName: string) => {
    setFileItems((prev) => {
      const newItems = [...prev];
      newItems[index] = { ...newItems[index], name: newName };
      return newItems;
    });
  };

  const handleRemoveQueuedFile = (index: number) => {
    setFileItems((prev) => prev.filter((_, i) => i !== index));
  };

  const handleSaveUploads = async () => {
    if (!authenticatedUserDetails?.workspaceId) return;

    const filesToUpload = fileItems.map((item) => {
      const { originalFile, name } = item;
      if (name !== originalFile.name) {
        const parts = originalFile.name.split(".");
        const ext = parts.length > 1 ? parts.pop() : "";

        let finalName = name;
        if (ext && !name.endsWith(`.${ext}`)) {
          finalName = `${name}.${ext}`;
        }

        return new File([originalFile], finalName, { type: originalFile.type });
      }
      return originalFile;
    });

    mutate(
      {
        tenantId: authenticatedUserDetails?.tenantId || "",
        workspaceId: authenticatedUserDetails.workspaceId,
        files: filesToUpload,
      },
      {
        onSuccess: () => {
          toast.success("Files uploaded successfully.");
          setFileItems([]);
          toggle();
        },
        onError: (error) => {
          toast.error(error?.message || "Failed to upload files.");
        },
      }
    );
  };

  return (
    <div>
      <Button
        className="bg-sky-500 hover:bg-sky-600 text-white disabled:bg-muted disabled:text-muted-foreground"
        onClick={toggle}
      >
        <Upload className="h-4 w-4 mr-2" />
        Upload Documents
      </Button>

      <Dialog
        open={isUploadModalOpen}
        onOpenChange={() => !isPending && toggle()}
      >
        <DialogContent className="sm:max-w-150 p-0 overflow-hidden gap-0">
          <div className="p-6 pb-0">
            <DialogTitle className="text-xl mb-4">Upload Files</DialogTitle>
            <DialogDescription className="sr-only">
              Upload new files to your resources.
            </DialogDescription>
          </div>

          <div className="px-6 pb-6 space-y-6">
            <FileUpload
              onFilesSelected={addFilesToQueue}
              isLoading={isPending}
            />

            {fileItems.length > 0 && (
              <div className="space-y-3">
                <h4 className="text-sm font-medium text-muted-foreground">
                  {fileItems.length} Files
                </h4>
                <div className="max-h-60 overflow-y-auto pr-2 space-y-3 [&::-webkit-scrollbar]:w-2 [&::-webkit-scrollbar-thumb]:bg-muted-foreground/50 [&::-webkit-scrollbar-track]:bg-muted [&::-webkit-scrollbar-thumb]:rounded-full">
                  {fileItems.map((item, index) => (
                    <div key={index} className="flex items-center gap-3">
                      <div className="h-10 w-10 flex items-center justify-center bg-secondary rounded border border-border shrink-0">
                        <FileIcon className="h-5 w-5 text-muted-foreground" />
                      </div>
                      <div className="flex-1">
                        <Input
                          value={item.name}
                          onChange={(e) =>
                            handleQueuedRename(index, e.target.value)
                          }
                          className="h-10 text-sm text-foreground"
                          placeholder="File name"
                        />
                      </div>
                      <Button
                        variant="ghost"
                        size="icon"
                        className="h-10 w-10 text-muted-foreground hover:text-destructive bg-destructive/10 hover:bg-destructive/20 rounded shrink-0"
                        disabled={isPending}
                        onClick={() => handleRemoveQueuedFile(index)}
                      >
                        <Trash2 className="h-4 w-4" />
                      </Button>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>

          <DialogFooter className="p-6 bg-secondary/50 border-t border-border sm:justify-end gap-3">
            <Button
              variant="outline"
              className="h-10 px-8"
              onClick={() => {
                toggle();
                setFileItems([]);
              }}
              disabled={isPending}
            >
              Cancel
            </Button>
            <Button
              className="bg-sky-500 hover:bg-sky-600 text-white h-10 px-8 disabled:bg-muted disabled:text-muted-foreground"
              onClick={handleSaveUploads}
              disabled={fileItems.length === 0 || isPending}
            >
              {isPending ? "Uploading..." : "Upload Files"}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
};

export { AddResource };
