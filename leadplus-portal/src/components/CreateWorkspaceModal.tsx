'use client';

import { zodResolver } from '@hookform/resolvers/zod';
import { useForm } from 'react-hook-form';
import { toast } from 'sonner';
import * as z from 'zod';

import { Button } from '@/components/ui/button';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { DropdownMenuItem } from '@/components/ui/dropdown-menu';
import { FormInput } from '@/components/ui/form-input';
import { stringValidation } from '@/lib/utils/validations';

import { useAuth } from '@/context/AuthContext';
import useToggle from '@/hooks/useToggle';
import { useCreateWorkspace } from '@/hooks/useWorkspace';

import { Plus } from 'lucide-react';
import { useCallback } from 'react';

const createWorkspaceSchema = z.object({
  workspaceName: stringValidation({ fieldName: 'Workspace Name' }),
});

type CreateWorkspaceFormValues = z.infer<typeof createWorkspaceSchema>;

export const CreateWorkspaceModal = () => {
  const { value: isOpen, toggle } = useToggle();
  const { authenticatedUser } = useAuth();
  const { mutate, isPending } = useCreateWorkspace();
  const {
    register,
    handleSubmit,
    formState: { errors },
    reset,
  } = useForm<CreateWorkspaceFormValues>({
    resolver: zodResolver(createWorkspaceSchema),
    defaultValues: {
      workspaceName: '',
    },
  });

  const onClose = useCallback(() => {
    if (!isPending) {
      reset();
      toggle();
    }
  }, [isPending, reset, toggle]);

  const onSubmit = (values: CreateWorkspaceFormValues) => {
    if (!authenticatedUser?.tenantId) return;
    mutate(
      {
        tenantId: authenticatedUser.tenantId,
        payload: {
          name: values.workspaceName,
          ccRecipients: [],
          bccRecipients: [],
          dailySendLimit: 30,
        },
      },
      {
        onSuccess: () => {
          toast.success('Workspace created successfully!');
          reset();
          toggle();
        },
        onError: (error) => {
          toast.error(`Failed to create workspace: ${error.message}`);
        },
      }
    );
  };

  return (
    <>
      <DropdownMenuItem
        className="text-primary hover:bg-muted flex cursor-pointer items-center gap-2"
        onSelect={(e) => {
          e.preventDefault();
          toggle();
        }}
      >
        <Plus className="h-4 w-4" />
        <span>Create Workspace</span>
      </DropdownMenuItem>

      <Dialog open={isOpen} onOpenChange={onClose}>
        <DialogContent className="sm:max-w-md">
          <form onSubmit={handleSubmit(onSubmit)}>
            <DialogHeader>
              <DialogTitle>Create New Workspace</DialogTitle>
              <DialogDescription>
                Enter a name for your new workspace. You can always change it later.
              </DialogDescription>
            </DialogHeader>

            <div className="py-4">
              <FormInput
                id="workspaceName"
                label="Workspace Name"
                placeholder="e.g. My Workspace"
                {...register('workspaceName')}
                error={!!errors.workspaceName}
                errorMessage={errors.workspaceName?.message}
                disabled={isPending}
                autoFocus
              />
            </div>

            <DialogFooter className="flex gap-2">
              <Button type="button" variant="outline" onClick={onClose} disabled={isPending}>
                Cancel
              </Button>
              <Button type="submit" disabled={isPending}>
                {isPending ? 'Creating...' : 'Create Workspace'}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>
    </>
  );
};
