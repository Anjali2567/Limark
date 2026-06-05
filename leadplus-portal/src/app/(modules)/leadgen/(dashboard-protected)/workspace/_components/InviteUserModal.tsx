'use client';

import { zodResolver } from '@hookform/resolvers/zod';
import { useCallback } from 'react';
import { useForm } from 'react-hook-form';
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

import { useAuth } from '@/context/AuthContext';
import useToggle from '@/hooks/useToggle';
import { useInviteUserToWorkspace } from '@/hooks/useWorkspace';
import { Mail, Plus } from 'lucide-react';
import { toast } from 'sonner';

const inviteSchema = z.object({
  email: z.email('Please enter a valid email address'),
  name: z.string().optional(),
});

type InviteFormValues = z.infer<typeof inviteSchema>;

type InviteUserModalProps = {
  workspaceId?: string;
};

const InviteUserModal = ({ workspaceId }: InviteUserModalProps) => {
  const { authenticatedUserDetails } = useAuth();
  const { value: isOpen, toggle } = useToggle();

  const { mutate, isPending } = useInviteUserToWorkspace();

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<InviteFormValues>({
    resolver: zodResolver(inviteSchema),
    defaultValues: {
      email: '',
      name: '',
    },
  });

  const handleInviteMember = useCallback(
    (values: InviteFormValues) => {
      mutate(
        {
          tenantId: authenticatedUserDetails?.tenantId || '',
          workspaceId: workspaceId || authenticatedUserDetails?.workspaceId || '',
          payload: values,
        },
        {
          onSuccess: () => {
            toast.success('Invitation sent successfully');
            toggle();
            reset();
          },
          onError: (error) => {
            toast.error(error?.message || 'Failed to send invitation');
          },
        }
      );
    },
    [mutate, reset, toggle, authenticatedUserDetails, workspaceId]
  );

  const handleClose = useCallback(() => {
    toggle();
    reset();
  }, [reset, toggle]);

  return (
    <div>
      <Button className="bg-primary text-primary-foreground" onClick={toggle}>
        <Plus className="mr-2 h-4 w-4" />
        Invite Member
      </Button>

      <Dialog open={isOpen} onOpenChange={() => !isPending && toggle()}>
        <DialogContent className="bg-card border-border">
          <DialogHeader>
            <DialogTitle>Invite Member</DialogTitle>
            <DialogDescription className="text-muted-foreground">
              Invite a new member to your workspace. They&apos;ll be able to create campaigns and
              send emails on behalf of your workspace.
            </DialogDescription>
          </DialogHeader>

          <form onSubmit={handleSubmit(handleInviteMember)} className="space-y-4 py-4">
            <FormInput
              type="email"
              label="Email Address *"
              placeholder="colleague@example.com"
              error={!!errors.email}
              errorMessage={errors.email?.message}
              {...register('email')}
            />
            <FormInput type="text" label="Name" placeholder="John Doe" {...register('name')} />
            <DialogFooter>
              <Button
                type="button"
                variant="outline"
                onClick={handleClose}
                disabled={isPending}
                className="border-border"
              >
                Cancel
              </Button>

              <Button
                type="submit"
                className="bg-primary text-primary-foreground"
                disabled={isPending}
              >
                <Mail className="mr-2 h-4 w-4" />
                Send Invitation
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>
    </div>
  );
};

export { InviteUserModal };
