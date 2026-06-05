'use client';

import { useEffect } from 'react';
import { zodResolver } from '@hookform/resolvers/zod';
import { Loader2 } from 'lucide-react';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { toast } from 'sonner';

import { Button } from '@/components/ui/button';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { Form } from '@/components/ui/form';
import { DynamicForm } from '@/components/form/DynamicForm';
import { useUpdateAuthenticatedUser } from '@/hooks/useUpdateAuthUser';
import { DynamicFormConfig, FormElementType } from '@/types/form.types';
import { User } from '@/types/user.types';

const updateProfileSchema = z.object({
  name: z.string().trim().min(1, 'Name is required').max(120, 'Name is too long').optional(),
  phoneNumber: z.string().max(30, 'Phone number is too long'),
  company: z.string().max(120, 'Company is too long'),
});

type UpdateProfileFormValues = z.infer<typeof updateProfileSchema>;

const profileFormConfig: DynamicFormConfig<UpdateProfileFormValues> = [
  [
    {
      type: FormElementType.TEXT,
      name: 'name',
      label: 'Name',
      placeholder: 'Enter your name',
      required: true,
      size: 12,
    },
  ],
  [
    {
      type: FormElementType.TEXT,
      name: 'phoneNumber',
      label: 'Phone number',
      placeholder: 'Enter your phone number',
      size: 12,
    },
  ],
  [
    {
      type: FormElementType.TEXT,
      name: 'company',
      label: 'Company',
      placeholder: 'Enter your company',
      disabled: true,
      size: 12,
    },
  ],
];

type UpdateProfileDialogProps = {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  user?: Pick<User, 'name' | 'email' | 'phoneNumber' | 'company'>;
};

const UpdateProfileDialog = ({ open, onOpenChange, user }: UpdateProfileDialogProps) => {
  const { mutate: updateUser, isPending } = useUpdateAuthenticatedUser();

  const form = useForm<UpdateProfileFormValues>({
    resolver: zodResolver(updateProfileSchema),
    defaultValues: {
      name: user?.name ?? '',
      phoneNumber: user?.phoneNumber ?? '',
      company: user?.company ?? '',
    },
  });

  useEffect(() => {
    if (!open) return;
    form.reset({
      name: user?.name ?? '',
      phoneNumber: user?.phoneNumber ?? '',
      company: user?.company ?? '',
    });
  }, [form, open, user?.company, user?.name, user?.phoneNumber]);

  const handleSubmit = (values: UpdateProfileFormValues) => {
    updateUser(
      {
        name: values.name?.trim() || null,
        phoneNumber: values.phoneNumber.trim(),
        company: values.company.trim(),
      },
      {
        onSuccess: () => {
          toast.success('Profile updated successfully.');
          onOpenChange(false);
        },
        onError: (error) => {
          toast.error(error.message || 'Failed to update profile.');
        },
      }
    );
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>Update profile</DialogTitle>
          <DialogDescription>
            Change your display name, phone number, and company for the current account.
          </DialogDescription>
        </DialogHeader>

        <Form {...form}>
          <form className="space-y-4" onSubmit={form.handleSubmit(handleSubmit)}>
            <DynamicForm form={form} formConfig={profileFormConfig} className="gap-4" />

            {user?.email ? <p className="text-muted-foreground text-sm">Signed in as {user.email}</p> : null}

            <DialogFooter>
              <Button type="button" variant="outline" onClick={() => onOpenChange(false)} disabled={isPending}>
                Cancel
              </Button>
              <Button type="submit" disabled={isPending}>
                {isPending ? <Loader2 className="h-4 w-4 animate-spin" /> : null}
                Save changes
              </Button>
            </DialogFooter>
          </form>
        </Form>
      </DialogContent>
    </Dialog>
  );
};

export { UpdateProfileDialog };
