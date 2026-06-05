'use client';

import { Controller, useForm } from 'react-hook-form';
import { toast } from 'sonner';
import { z } from 'zod';

import { Button } from '@/components/ui/button';
import { FormInput } from '@/components/ui/form-input';
import { Popover, PopoverContent, PopoverTrigger } from '@/components/ui/popover';
import { LeadType } from '@/constants/leadSearch.constants';
import { useCreateLeadList } from '@/hooks/useLeadList';
import useToggle from '@/hooks/useToggle';
import { stringValidation } from '@/lib/utils/validations';
import { zodResolver } from '@hookform/resolvers/zod';

import { Building2, Loader2, Plus, Users } from 'lucide-react';

const TYPE_OPTIONS = [
  { value: LeadType.LEAD_CONTACT, label: 'People', Icon: Users },
  { value: LeadType.LEAD_COMPANY, label: 'Companies', Icon: Building2 },
] as const;

const createListSchema = z.object({
  name: stringValidation({ fieldName: 'List name' }),
  type: z.enum([LeadType.LEAD_COMPANY, LeadType.LEAD_CONTACT]),
});

type CreateListFormValues = z.infer<typeof createListSchema>;

type CreateListPopoverProps = {
  tenantId: string;
  workspaceId: string;
  handleTabChange: (tab: LeadType) => void;
};

const CreateListPopover = ({ tenantId, workspaceId, handleTabChange }: CreateListPopoverProps) => {
  const { value: open, setTrue: openPopover, setFalse: closePopover } = useToggle();

  const { mutate: createList, isPending } = useCreateLeadList();

  const {
    register,
    control,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<CreateListFormValues>({
    resolver: zodResolver(createListSchema),
    defaultValues: { name: '', type: LeadType.LEAD_CONTACT },
  });

  const handleOpen = () => {
    reset();
    openPopover();
  };

  const handleClose = () => {
    if (isPending) return;
    closePopover();
    reset();
  };

  const onSubmit = ({ name, type }: CreateListFormValues) => {
    createList(
      {
        params: { tenantId, workspaceId, listId: '' },
        payload: { name, type, sourceIds: [] },
      },
      {
        onSuccess: (created) => {
          toast.success(`List "${created.name}" created`);
          closePopover();
          reset();
          handleTabChange(type);
        },
        onError: () => toast.error('Failed to create list'),
      }
    );
  };

  return (
    <Popover open={open} onOpenChange={(next) => (next ? handleOpen() : handleClose())}>
      <PopoverTrigger asChild>
        <Button>
          <Plus className="mr-2 h-4 w-4" />
          Create a list
        </Button>
      </PopoverTrigger>

      <PopoverContent align="end" className="w-80 p-0">
        <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col">
          <div className="border-border border-b px-4 py-3">
            <h3 className="text-foreground text-sm font-semibold">Create a list</h3>
          </div>
          <div className="space-y-4 px-4 py-4">
            <FormInput
              id="list-name"
              label="List name"
              placeholder="Enter list name"
              error={!!errors.name}
              errorMessage={errors.name?.message}
              autoFocus
              {...register('name')}
            />
            <div className="space-y-1.5">
              <label className="text-foreground text-sm font-medium">Type</label>
              <Controller
                control={control}
                name="type"
                render={({ field }) => (
                  <div className="flex gap-3">
                    {TYPE_OPTIONS.map(({ value, label, Icon }) => (
                      <button
                        key={value}
                        type="button"
                        onClick={() => field.onChange(value)}
                        className={`flex flex-1 flex-col items-center gap-1 rounded-md border px-4 py-2 transition-colors ${
                          field.value === value
                            ? 'border-primary bg-primary/10 text-foreground'
                            : 'border-border bg-card text-muted-foreground hover:text-foreground'
                        }`}
                      >
                        <Icon className="h-4 w-4" />
                        <span className="text-sm">{label}</span>
                      </button>
                    ))}
                  </div>
                )}
              />
            </div>
          </div>
          <div className="border-border flex items-center justify-end gap-2 border-t px-4 py-3">
            <Button
              type="button"
              variant="outline"
              size="sm"
              onClick={handleClose}
              disabled={isPending}
            >
              Cancel
            </Button>
            <Button type="submit" size="sm" disabled={isPending}>
              {isPending && <Loader2 className="mr-2 h-3.5 w-3.5 animate-spin" />}
              Create list
            </Button>
          </div>
        </form>
      </PopoverContent>
    </Popover>
  );
};

export { CreateListPopover };
