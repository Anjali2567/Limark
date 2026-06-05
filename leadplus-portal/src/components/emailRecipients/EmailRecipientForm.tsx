'use client';

import { useForm } from 'react-hook-form';
import { z } from 'zod';

import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { FormInput } from '@/components/ui/form-input';
import { zodResolver } from '@hookform/resolvers/zod';

import { Plus, X } from 'lucide-react';
import { Label } from '../ui/label';

type EmailRecipientFormProps = {
  label: string;
  name: string;
  placeholder: string;
  list: string[];
  defaultList: string[];
  onAdd: (email: string) => void;
  onRemove: (email: string) => void;
  addButtonText?: string;
  badgeClass?: string;
};

const emailSchema = z.object({
  email: z.email(),
});

type FormValues = z.infer<typeof emailSchema>;

export const EmailRecipientForm = ({
  label,
  name,
  placeholder,
  list = [],
  defaultList = [],
  onAdd,
  onRemove,
  addButtonText = 'Add',
  badgeClass = 'bg-sky-50 border-sky-200',
}: EmailRecipientFormProps) => {
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<FormValues>({
    resolver: zodResolver(emailSchema),
    defaultValues: { email: '' },
  });

  const onSubmit = (data: FormValues) => {
    const email = data.email.trim();
    if (!email) return;

    if (!list.includes(email)) onAdd(email);
    reset();
  };

  return (
    <div className="space-y-2">
      <div className="flex gap-3">
        <div className="flex-1 space-y-1">
          <FormInput
            placeholder={placeholder}
            type="email"
            error={!!errors.email}
            errorMessage={errors.email?.message}
            className="w-full"
            id={name}
            {...register('email')}
          />
        </div>

        <Button
          type="button"
          onClick={() => handleSubmit(onSubmit)()}
          className="bg-sky-500 text-white hover:bg-sky-600"
        >
          <Plus className="mr-2 h-4 w-4" />
          {addButtonText}
        </Button>
      </div>

      {list.length > 0 || defaultList.length > 0 ? (
        <div className="space-y-2">
          <Label className="text-muted-foreground text-xs tracking-wide uppercase">
            {`${label} (${list.length + defaultList.length})`}
          </Label>
          <div className="flex flex-wrap gap-2">
            {list.map((email) => (
              <Badge
                key={email}
                variant="outline"
                className={`${badgeClass} flex items-center px-3 py-2 pr-2`}
              >
                <span className="mr-2">{email}</span>
                <button
                  type="button"
                  onClick={() => onRemove(email)}
                  className="text-muted-foreground hover:text-red-500"
                >
                  <X className="h-3 w-3" />
                </button>
              </Badge>
            ))}
            {defaultList.map((email) => (
              <Badge
                key={email}
                variant="outline"
                className={`${badgeClass} flex items-center px-3 py-2 pr-2`}
              >
                <span className="mr-2">{email}</span>
              </Badge>
            ))}
          </div>
        </div>
      ) : (
        <p className="text-muted-foreground text-sm italic">No recipients configured</p>
      )}
    </div>
  );
};
