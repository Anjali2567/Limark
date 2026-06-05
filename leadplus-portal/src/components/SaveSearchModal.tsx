'use client';

import { zodResolver } from '@hookform/resolvers/zod';
import { useForm } from 'react-hook-form';
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
import { FormInput } from '@/components/ui/form-input';
import { stringValidation } from '@/lib/utils/validations';

const saveSearchSchema = z.object({
  searchName: stringValidation({ fieldName: 'Search Name', min: 2 }),
});

type SaveSearchFormValues = z.infer<typeof saveSearchSchema>;

interface SaveSearchModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSave: (searchName: string) => void;
  isPending?: boolean;
  resultCount?: number;
}

export const SaveSearchModal = ({
  isOpen,
  onClose,
  onSave,
  isPending = false,
  resultCount,
}: SaveSearchModalProps) => {
  const {
    register,
    handleSubmit,
    formState: { errors },
    reset,
  } = useForm<SaveSearchFormValues>({
    resolver: zodResolver(saveSearchSchema),
    defaultValues: {
      searchName: '',
    },
  });

  const handleClose = () => {
    if (!isPending) {
      reset();
      onClose();
    }
  };

  const onSubmit = (values: SaveSearchFormValues) => {
    onSave(values.searchName);
  };

  return (
    <Dialog open={isOpen} onOpenChange={handleClose}>
      <DialogContent className="sm:max-w-md">
        <form onSubmit={handleSubmit(onSubmit)}>
          <DialogHeader>
            <DialogTitle>Save Search</DialogTitle>
            <DialogDescription>
              {resultCount !== undefined
                ? `Save these filters with ${resultCount.toLocaleString()} result${resultCount !== 1 ? 's' : ''} for quick access later.`
                : 'Save these filters for quick access later.'}
            </DialogDescription>
          </DialogHeader>
          <div className="py-4">
            <FormInput
              {...register('searchName')}
              label="Search Name"
              placeholder="e.g., Tech Companies in California"
              error={!!errors.searchName}
              errorMessage={errors.searchName?.message}
              disabled={isPending}
            />
          </div>
          <DialogFooter>
            <Button type="button" variant="outline" onClick={handleClose} disabled={isPending}>
              Cancel
            </Button>
            <Button type="submit" disabled={isPending}>
              {isPending ? 'Saving...' : 'Save Search'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
};
