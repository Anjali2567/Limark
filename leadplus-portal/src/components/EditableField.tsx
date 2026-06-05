import { zodResolver } from '@hookform/resolvers/zod';
import { useForm } from 'react-hook-form';
import { z } from 'zod';

import { Button } from '@/components/ui/button';
import { Tooltip, TooltipContent, TooltipTrigger } from '@/components/ui/tooltip';
import useToggle from '@/hooks/useToggle';
import { stringValidation } from '@/lib/utils/validations';

import { Pencil, Save } from 'lucide-react';
import { FormInput } from './ui/form-input';

type EditableFieldProps = {
  value: string;
  onChange: (newValue: string) => void;
  tooltip?: string;
};

const editableFieldSchema = z.object({
  value: stringValidation({}),
});

type FormData = z.infer<typeof editableFieldSchema>;

const EditableField = ({ value, onChange, tooltip }: EditableFieldProps) => {
  const { value: isEditing, toggle } = useToggle(false);

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, defaultValues },
  } = useForm<FormData>({
    resolver: zodResolver(editableFieldSchema),
    values: {
      value: value,
    },
  });

  const onSubmit = (data: FormData) => {
    const trimmedValue = data.value.trim();
    if (!trimmedValue) return;
    onChange(trimmedValue);
    reset({ value: trimmedValue });
    toggle();
  };

  return (
    <div className="flex items-center">
      {isEditing ? (
        <form onSubmit={handleSubmit(onSubmit)} className="flex items-start">
          <FormInput
            type="text"
            autoFocus
            {...register('value')}
            className="w-lg"
            error={!!errors.value}
            errorMessage={errors.value?.message}
          />
          <Button
            type="submit"
            variant="none"
            className="hover:text-foreground transition-all duration-200 hover:scale-110"
          >
            <Save className="text-muted-foreground" />
          </Button>
        </form>
      ) : (
        <>
          <h1 className="text-foreground max-w-lg truncate text-3xl font-bold">
            {defaultValues?.value}
          </h1>
          <Tooltip>
            <TooltipTrigger asChild>
              <Button
                variant="none"
                onClick={toggle}
                className="transition-all duration-200 hover:scale-110 hover:text-red-950"
              >
                <Pencil className="text-muted-foreground" />
              </Button>
            </TooltipTrigger>
            {tooltip && <TooltipContent>{tooltip}</TooltipContent>}
          </Tooltip>
        </>
      )}
    </div>
  );
};

export { EditableField };
