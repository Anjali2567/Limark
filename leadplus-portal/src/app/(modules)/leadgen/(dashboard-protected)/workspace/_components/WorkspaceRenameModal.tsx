import { useForm } from 'react-hook-form';
import { toast } from 'sonner';
import { z } from 'zod';

import { Button } from '@/components/ui/button';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { FormInput } from '@/components/ui/form-input';
import { WorkspaceUserRole } from '@/constants/user.constants';
import useToggle from '@/hooks/useToggle';
import { useGetWorkspaceDetails, useUpdateWorkspaceDetails } from '@/hooks/useWorkspace';
import { stringValidation } from '@/lib/utils/validations';
import { CurrentWorkspace, UserWorkspace } from '@/types/user.types';
import { zodResolver } from '@hookform/resolvers/zod';

import { Pencil } from 'lucide-react';
import { useCallback } from 'react';

type WorkspaceRenameModalProps = {
  currentWorkspace: UserWorkspace;
  userDetails: CurrentWorkspace;
};

const renameWorkspaceSchema = z.object({
  workspaceName: stringValidation({ fieldName: 'Workspace Name' }),
});

type RenameWorkspaceFormValues = z.infer<typeof renameWorkspaceSchema>;

const WorkspaceRenameModal = ({ currentWorkspace, userDetails }: WorkspaceRenameModalProps) => {
  const { value: isHoveringTitle, setValue: setIsHoveringTitle } = useToggle();
  const { value: isRenameDialogOpen, toggle: toggleRenameDialog } = useToggle();

  const { data } = useGetWorkspaceDetails(userDetails);
  const { mutate, isPending } = useUpdateWorkspaceDetails();

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<RenameWorkspaceFormValues>({
    resolver: zodResolver(renameWorkspaceSchema),
    values: {
      workspaceName: currentWorkspace?.workspaceName ?? '',
    },
  });

  const onRename = ({ workspaceName }: { workspaceName: string }) => {
    mutate(
      {
        tenantId: userDetails.tenantId,
        workspaceId: userDetails.workspaceId,
        payload: {
          ccRecipients: data?.ccRecipients || [],
          bccRecipients: data?.bccRecipients || [],
          dailySendLimit: data?.dailySendLimit || 30,
          name: workspaceName,
        },
      },
      {
        onSuccess: () => {
          toast.success('Workspace renamed successfully');
          toggleRenameDialog();
          reset({ workspaceName });
        },
        onError: (error) => {
          toast.error(`Failed to rename workspace: ${error.message}`);
        },
      }
    );
  };

  const onClose = useCallback(() => {
    if (isPending) return;
    toggleRenameDialog();
    reset();
  }, [isPending, toggleRenameDialog, reset]);

  return (
    <>
      <div
        className="group flex items-center gap-2"
        onMouseEnter={() => setIsHoveringTitle(true)}
        onMouseLeave={() => setIsHoveringTitle(false)}
      >
        <h1 className="text-foreground text-3xl font-bold">{currentWorkspace?.workspaceName}</h1>

        {currentWorkspace?.role === WorkspaceUserRole.OWNER && (
          <button
            onClick={toggleRenameDialog}
            className={`hover:bg-muted rounded-md p-1.5 transition-opacity ${
              isHoveringTitle ? 'opacity-100' : 'opacity-0'
            }`}
          >
            <Pencil className="text-muted-foreground h-4 w-4" />
          </button>
        )}
      </div>

      <Dialog open={isRenameDialogOpen} onOpenChange={onClose}>
        <DialogContent className="bg-card border-border">
          <DialogHeader>
            <DialogTitle>Rename Workspace</DialogTitle>
            <DialogDescription className="text-muted-foreground">
              Rename your workspace to a new name.
            </DialogDescription>
          </DialogHeader>

          <form onSubmit={handleSubmit(onRename)} className="space-y-4 py-4">
            <FormInput
              label="Workspace Name *"
              id="rename-workspace"
              placeholder="Enter workspace name"
              error={!!errors.workspaceName}
              errorMessage={errors.workspaceName?.message}
              {...register('workspaceName')}
              className="bg-background border-border"
            />

            <DialogFooter>
              <Button
                type="button"
                variant="outline"
                disabled={isPending}
                onClick={toggleRenameDialog}
                className="border-border"
              >
                Cancel
              </Button>

              <Button
                type="submit"
                disabled={isPending}
                className="bg-primary text-primary-foreground"
              >
                <Pencil className="mr-2 h-4 w-4" />
                Rename Workspace
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>
    </>
  );
};

export { WorkspaceRenameModal };
