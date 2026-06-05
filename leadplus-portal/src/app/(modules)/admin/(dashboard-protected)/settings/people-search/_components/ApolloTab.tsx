'use client';

import { useEffect, useMemo } from 'react';
import { toast } from 'sonner';
import { Controller, useForm, useWatch } from 'react-hook-form';

import { Button } from '@/components/ui/button';
import { Card, CardContent } from '@/components/ui/card';
import { Switch } from '@/components/ui/switch';
import { Textarea } from '@/components/ui/textarea';
import { Label } from '@/components/ui/label';
import {
  useGetLatestApolloSpecification,
  useUpdateApolloSpecification,
} from '@/hooks/useApolloSpecification';

import { Loader2 } from 'lucide-react';

type FormValues = {
  personTitleEnabled: boolean;
  personTitles: string;
  personSeniorityEnabled: boolean;
  personSeniorities: string;
};

type ApolloTabProps = {
  setIsBusy: (value: boolean) => void;
};

const parseCommaSeparated = (value: string) =>
  value
    .split(',')
    .map((v) => v.trim())
    .filter(Boolean);

const ApolloTab = ({ setIsBusy }: ApolloTabProps) => {
  const { data, isLoading } = useGetLatestApolloSpecification();
  const { mutate, isPending } = useUpdateApolloSpecification();

  const initialValues = useMemo<FormValues>(
    () => ({
      personTitleEnabled: data?.personTitleEnabled ?? false,
      personTitles: data?.personTitles?.join(', ') ?? '',
      personSeniorityEnabled: data?.personSeniorityEnabled ?? false,
      personSeniorities: data?.personSeniorities?.join(', ') ?? '',
    }),
    [data]
  );

  const {
    register,
    handleSubmit,
    control,
    formState: { isDirty },
  } = useForm<FormValues>({
    values: initialValues,
  });

  const personTitleEnabled = useWatch({ control, name: 'personTitleEnabled' });
  const personSeniorityEnabled = useWatch({
    control,
    name: 'personSeniorityEnabled',
  });

  useEffect(() => {
    setIsBusy?.(isLoading || isPending);
  }, [isLoading, isPending, setIsBusy]);

  const onSubmit = (values: FormValues) => {
    const payload = {
      ...values,
      personTitles: parseCommaSeparated(values.personTitles),
      personSeniorities: parseCommaSeparated(values.personSeniorities),
    };

    mutate(payload, {
      onSuccess: () => {
        toast.success('Apollo configuration has been successfully updated.');
      },
      onError: () => {
        toast.error('Failed to update Apollo configuration. Please try again.');
      },
    });
  };

  if (isLoading) {
    return (
      <div className="flex h-96 w-full items-center justify-center">
        <Loader2 className="text-muted-foreground h-6 w-6 animate-spin" />
      </div>
    );
  }

  return (
    <div className="relative space-y-4">
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
        <div className="flex h-10.5 items-center justify-between">
          <h3 className="text-primary font-medium">Configure Parameters</h3>
          <Button type="submit" disabled={!isDirty || isPending}>
            Save
          </Button>
        </div>

        <Card>
          <CardContent className="space-y-6 p-4">
            <div className="flex items-center gap-5 text-sm">
              <Label>Person Title</Label>
              <Controller
                name="personTitleEnabled"
                control={control}
                render={({ field }) => (
                  <Switch
                    checked={field.value}
                    onCheckedChange={field.onChange}
                    disabled={isPending}
                  />
                )}
              />
            </div>

            <div className="space-y-2">
              <Label>Include Keywords</Label>
              <Textarea
                {...register('personTitles')}
                className="min-h-25"
                placeholder="Supply Chain, Operations Manager"
                disabled={!personTitleEnabled || isPending}
              />
              <p className="text-muted-foreground text-xs">Use commas to separate keywords</p>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="space-y-6 p-4">
            <div className="flex items-center gap-5 text-sm">
              <Label>Person Seniority</Label>
              <Controller
                name="personSeniorityEnabled"
                control={control}
                render={({ field }) => (
                  <Switch
                    checked={field.value}
                    onCheckedChange={field.onChange}
                    disabled={isPending}
                  />
                )}
              />
            </div>

            <div className="space-y-2">
              <Label>Include Keywords</Label>
              <Textarea
                {...register('personSeniorities')}
                className="min-h-25"
                placeholder="c-suite, vp"
                disabled={!personSeniorityEnabled || isPending}
              />
              <p className="text-muted-foreground text-xs">Use commas to separate keywords</p>
            </div>
          </CardContent>
        </Card>
      </form>
    </div>
  );
};

export { ApolloTab };
