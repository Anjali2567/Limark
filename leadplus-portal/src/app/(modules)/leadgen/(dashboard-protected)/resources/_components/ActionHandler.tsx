"use client";

import { useCallback, useEffect, useState } from "react";
import { toast } from "sonner";

import { ActionMenu } from "@/components/tables/ActionMenu";
import { useAuth } from "@/context/AuthContext";
import {
  useRemoveAttachment,
  useRenameAttachmentFile,
  useReplaceAttachmentFile,
} from "@/hooks/useAttachments";
import { useModal } from "@/hooks/useModal";
import { Attachment } from "@/types/attachment.types";
import { Actions } from "./Actions";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Button } from "@/components/ui/button";
import { FileUpload } from "@/components/FileUpload";
import useToggle from "@/hooks/useToggle";
import { Modal } from "@/components/Modal";

type ActionHandlerProps = {
  data: Attachment;
};

const ActionHandler = ({ data }: ActionHandlerProps) => {
  const { authenticatedUserDetails } = useAuth();
  const { renderModal } = useModal();

  const { mutate: renameAttachment } = useRenameAttachmentFile();
  const { mutate: replaceAttachment, isPending: isReplacePending } =
    useReplaceAttachmentFile();
  const { mutate: removeAttachment } = useRemoveAttachment();

  const { value: isRenameModalOpen, toggle: toggleRenameModal } = useToggle();
  const { value: isReplaceModalOpen, toggle: toggleReplaceModal } = useToggle();
  const [newName, setNewName] = useState<string>(data.filename);

  useEffect(() => {
    setNewName(data.filename);
  }, [data]);

  const handleRenameSave = () => {
    renameAttachment(
      {
        tenantId: authenticatedUserDetails?.tenantId || "",
        workspaceId: authenticatedUserDetails?.workspaceId || "",
        attachmentId: data.id,
        requestBody: { fileName: newName.trim() },
      },
      {
        onSuccess: () => {
          toast.success("Document renamed successfully.");
          toggleRenameModal();
        },
        onError: (error) => {
          toast.error(error?.message || "Failed to rename document.");
        },
      }
    );
  };

  const handleReplaceFile = (files: FileList | null) => {
    if (!files || files.length === 0) return;
    replaceAttachment(
      {
        tenantId: authenticatedUserDetails?.tenantId || "",
        workspaceId: authenticatedUserDetails?.workspaceId || "",
        attachmentId: data.id,
        file: files[0],
      },
      {
        onSuccess: () => {
          toast.success("Document replaced successfully.");
          toggleReplaceModal();
        },
        onError: (error) => {
          toast.error(error?.message || "Failed to replace document.");
        },
      }
    );
  };

  const handleDeleteResource = useCallback(() => {
    renderModal({
      type: "error",
      hideIcon: true,
      title: "Are you sure?",
      message: `This action cannot be undone. This will permanently delete ${data.filename} from the system.`,
      submitButtonText: "Delete",
      onConfirm: () => {
        removeAttachment(
          {
            tenantId: authenticatedUserDetails?.tenantId || "",
            workspaceId: authenticatedUserDetails?.workspaceId || "",
            attachmentId: data.id,
          },
          {
            onSuccess: () => {
              toast.success(`${data.filename} has been deleted successfully.`);
            },
            onError: (error) => {
              toast.error(error?.message || "Failed to delete attachment.");
            },
          }
        );
      },
    });
  }, [renderModal, data, removeAttachment, authenticatedUserDetails]);

  return (
    <>
      <Modal
        isOpen={isRenameModalOpen}
        toggleOpen={toggleRenameModal}
        title="Rename Document"
        description="Enter a new name for the document."
        content={
          <div className="grid gap-4 py-4">
            <div className="grid grid-cols-4 items-center gap-4">
              <Label htmlFor="name" className="text-right">
                Name
              </Label>
              <Input
                id="name"
                value={newName}
                onChange={(e) => setNewName(e.target.value)}
                className="col-span-3"
              />
            </div>
          </div>
        }
        contentClassName="sm:max-w-[425px]"
        footer={
          <>
            <Button variant="outline" onClick={toggleRenameModal}>
              Cancel
            </Button>
            <Button
              className="bg-sky-500 hover:bg-sky-600 text-white disabled:bg-muted disabled:text-muted-foreground"
              onClick={handleRenameSave}
              disabled={!newName.trim()}
            >
              Save changes
            </Button>
          </>
        }
      />

      <Modal
        isOpen={isReplaceModalOpen}
        toggleOpen={() => {
          if (!isReplacePending) toggleReplaceModal();
        }}
        showCloseButton={!isReplacePending}
        title="Replace File"
        content={
          <div className="p-6">
            <FileUpload
              onFilesSelected={handleReplaceFile}
              multiple={false}
              isLoading={isReplacePending}
            />
          </div>
        }
        contentClassName="sm:max-w-150 p-0 overflow-hidden gap-0"
        headerClassName="p-6 pb-0"
        footerClassName="p-6 bg-secondary/50 border-t border-border sm:justify-end gap-3"
        footer={
          <Button
            variant="outline"
            onClick={toggleReplaceModal}
            disabled={isReplacePending}
          >
            Cancel
          </Button>
        }
      />

      <ActionMenu
        actions={
          <Actions
            handleDeleteResource={handleDeleteResource}
            handleRenameResource={toggleRenameModal}
            handleReplaceResource={toggleReplaceModal}
          />
        }
      />
    </>
  );
};

export { ActionHandler };
