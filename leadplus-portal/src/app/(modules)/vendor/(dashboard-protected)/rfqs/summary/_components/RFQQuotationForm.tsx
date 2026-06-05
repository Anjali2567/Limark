import { useRouter } from 'next/navigation';
import { ChangeEvent, memo, useCallback, useMemo } from 'react';
import {
  useFieldArray,
  useForm,
  useWatch,
  type FieldErrors,
  type Resolver,
  type UseFormRegister,
} from 'react-hook-form';
import { toast } from 'sonner';
import z from 'zod';

import { DynamicForm } from '@/components/form/DynamicForm';
import { Button } from '@/components/ui/button';
import { Form } from '@/components/ui/form';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { appRoutes } from '@/config/routes';
import { QuotationStatus } from '@/constants/quotation.constant';
import { useUpdateRFQQuotation } from '@/hooks/useQuotation';
import { cn } from '@/lib/utils/helpers';
import { DynamicFormConfig, FormElementType } from '@/types/form.types';
import { CustomerQuotationResponse } from '@/types/quotation.types';
import { zodResolver } from '@hookform/resolvers/zod';

import { FileText, Paperclip, Plus, Trash2, Upload, X } from 'lucide-react';
import { useAuth } from '@/context/AuthContext';

const ALLOWED_FILE_TYPES = '.pdf,.doc,.docx' as const;
const MAX_FILE_SIZE_MB = 10;
const MAX_FILE_SIZE_BYTES = MAX_FILE_SIZE_MB * 1024 * 1024;
const GRID_COLUMNS = '3fr 5fr 2fr 2fr 32px' as const;

const priceItemSchema = z.object({
  service: z.string().min(1, 'Service is required'),
  description: z.string().default(''),
  duration: z.string().default(''),
  cost: z.coerce.number().nonnegative('Must be 0 or greater'),
});

const quotationSchema = z.object({
  priceBreakdown: z.array(priceItemSchema).min(1, 'At least one item is required'),
  paymentTerms: z.string().min(1, 'Payment terms are required'),
  deliverables: z
    .array(z.object({ text: z.string().min(1, 'Cannot be empty') }))
    .min(1, 'At least one deliverable is required'),
  attachments: z.array(z.instanceof(File)).optional(),
});

type QuotationFormValues = z.infer<typeof quotationSchema>;
type PriceItem = z.infer<typeof priceItemSchema>;
type DeliverableItem = { text: string };

const PAYMENT_TERMS_CONFIG: DynamicFormConfig<QuotationFormValues> = [
  [
    {
      name: 'paymentTerms',
      label: 'Payment Terms *',
      type: FormElementType.TEXTAREA,
      placeholder: 'e.g., 25% upfront, 50% at milestone, 25% on completion',
      rows: 4,
      size: 12,
    },
  ],
];

const PriceBreakdownHeader = memo(() => (
  <div
    className="border-border grid gap-4 border-b pb-3"
    style={{ gridTemplateColumns: GRID_COLUMNS }}
  >
    <Label className="text-foreground text-sm font-medium">Service *</Label>
    <Label className="text-foreground text-sm font-medium">Description</Label>
    <Label className="text-foreground text-sm font-medium">Duration</Label>
    <Label className="text-foreground text-sm font-medium">Cost *</Label>
    <div />
  </div>
));

PriceBreakdownHeader.displayName = 'PriceBreakdownHeader';

type PriceBreakdownRowProps = {
  index: number;
  field: Record<'id', string> & PriceItem;
  onRemove: () => void;
  canRemove: boolean;
  disabled: boolean;
  errors: FieldErrors<QuotationFormValues>;
  register: UseFormRegister<QuotationFormValues>;
};

const PriceBreakdownRow = memo<PriceBreakdownRowProps>(
  ({ index, onRemove, canRemove, disabled, errors, register }) => {
    const itemErrors = errors.priceBreakdown?.[index];

    return (
      <div className="grid items-start gap-4" style={{ gridTemplateColumns: GRID_COLUMNS }}>
        <div>
          <Input
            placeholder="e.g., UX Design"
            disabled={disabled}
            {...register(`priceBreakdown.${index}.service`)}
            className="border-border bg-input-background"
          />
          {itemErrors?.service?.message && (
            <p className="text-destructive mt-1 text-xs">{itemErrors.service.message}</p>
          )}
        </div>
        <div>
          <Input
            placeholder="Brief description"
            disabled={disabled}
            {...register(`priceBreakdown.${index}.description`)}
            className="border-border bg-input-background"
          />
        </div>
        <div>
          <Input
            placeholder="e.g., 2 weeks"
            disabled={disabled}
            {...register(`priceBreakdown.${index}.duration`)}
            className="border-border bg-input-background"
          />
        </div>
        <div>
          <Input
            type="number"
            min={0}
            placeholder="0"
            disabled={disabled}
            {...register(`priceBreakdown.${index}.cost`)}
            className="border-border bg-input-background"
          />
          {itemErrors?.cost?.message && (
            <p className="text-destructive mt-1 text-xs">{itemErrors.cost.message}</p>
          )}
        </div>
        <div className="flex h-9 items-center justify-center">
          {canRemove && (
            <Button
              type="button"
              variant="ghost"
              size="icon"
              disabled={disabled}
              onClick={onRemove}
              className="text-muted-foreground hover:text-destructive hover:bg-destructive/10 h-7 w-7"
            >
              <Trash2 className="h-3.5 w-3.5" />
              <span className="sr-only">Remove item</span>
            </Button>
          )}
        </div>
      </div>
    );
  }
);

PriceBreakdownRow.displayName = 'PriceBreakdownRow';

type DeliverableRowProps = {
  index: number;
  field: Record<'id', string> & DeliverableItem;
  onRemove: () => void;
  canRemove: boolean;
  disabled: boolean;
  error?: { message?: string };
  register: UseFormRegister<QuotationFormValues>;
};

const DeliverableRow = memo<DeliverableRowProps>(
  ({ index, onRemove, canRemove, disabled, error, register }) => (
    <div className="flex items-start gap-3">
      <div className="flex-1">
        <Input
          placeholder="e.g., Source code"
          disabled={disabled}
          {...register(`deliverables.${index}.text`)}
          className="border-border bg-input-background"
        />
        {error?.message && <p className="text-destructive mt-1 text-xs">{error.message}</p>}
      </div>
      {canRemove && (
        <Button
          type="button"
          variant="ghost"
          size="icon"
          disabled={disabled}
          onClick={onRemove}
          className="text-destructive hover:text-destructive hover:bg-destructive/10 h-9 w-9 shrink-0"
        >
          <Trash2 className="h-4 w-4" />
          <span className="sr-only">Remove deliverable</span>
        </Button>
      )}
    </div>
  )
);

DeliverableRow.displayName = 'DeliverableRow';

type AttachmentItemProps = {
  file: File;
  index: number;
  disabled: boolean;
  onRemove: (index: number) => void;
};

const AttachmentItem = memo<AttachmentItemProps>(({ file, index, disabled, onRemove }) => (
  <div className="bg-muted/30 border-border flex items-center gap-2 rounded-(--radius) border px-3 py-2">
    <div className="bg-primary/10 flex h-6 w-6 shrink-0 items-center justify-center rounded">
      <FileText className="text-primary h-3.5 w-3.5" />
    </div>
    <span className="text-foreground text-sm font-medium">{file.name}</span>
    <span className="text-muted-foreground text-xs">({(file.size / 1024).toFixed(1)} KB)</span>
    <Button
      type="button"
      variant="ghost"
      size="icon"
      disabled={disabled}
      onClick={() => onRemove(index)}
      className="text-muted-foreground hover:text-destructive h-5 w-5 p-0 hover:bg-transparent"
    >
      <X className="h-3.5 w-3.5" />
      <span className="sr-only">Remove attachment</span>
    </Button>
  </div>
));

AttachmentItem.displayName = 'AttachmentItem';

const RFQQuotationForm = ({ rfq }: { rfq: CustomerQuotationResponse }) => {
  const router = useRouter();
  const { authenticatedUserDetails } = useAuth();
  const isDisabled = rfq ? rfq.status !== QuotationStatus.ACCEPTED : false;

  const { mutate, isPending } = useUpdateRFQQuotation();

  const form = useForm<QuotationFormValues>({
    resolver: zodResolver(quotationSchema) as Resolver<QuotationFormValues>,
    defaultValues: {
      priceBreakdown: rfq.items.map((item) => ({
        service: item.serviceName,
        description: item.description,
        duration: item.duration,
        cost: item.price,
      })),
      paymentTerms: rfq.paymentTerms ?? '',
      deliverables: rfq.deliverables ? rfq.deliverables.map((d) => ({ text: d })) : [],
      attachments: [],
    },
  });

  const {
    control,
    register,
    setValue,
    formState: { errors },
  } = form;

  const {
    fields: priceFields,
    append: appendPrice,
    remove: removePrice,
  } = useFieldArray({
    control,
    name: 'priceBreakdown',
  });

  const {
    fields: deliverableFields,
    append: appendDeliverable,
    remove: removeDeliverable,
  } = useFieldArray({
    control,
    name: 'deliverables',
  });

  const watchedBreakdown = useWatch({
    control,
    name: 'priceBreakdown',
  }) as PriceItem[];

  const total = useMemo(
    () => watchedBreakdown.reduce((sum, item) => sum + (Number(item.cost) || 0), 0),
    [watchedBreakdown]
  );

  const watchedAttachments = useWatch({
    control,
    name: 'attachments',
  }) as File[] | undefined;

  const attachments = watchedAttachments ?? [];

  const handleFileUpload = useCallback(
    (e: ChangeEvent<HTMLInputElement>) => {
      if (isDisabled) return;

      const files = Array.from(e.target.files ?? []);
      const current = (form.getValues('attachments') ?? []) as File[];
      const validFiles = files.filter((file) => file.size <= MAX_FILE_SIZE_BYTES);
      setValue('attachments', [...current, ...validFiles], {
        shouldValidate: true,
      });

      e.target.value = '';
    },
    [setValue, form, isDisabled]
  );

  const removeAttachment = useCallback(
    (index: number) => {
      if (isDisabled) return;

      const current = (form.getValues('attachments') ?? []) as File[];
      setValue(
        'attachments',
        current.filter((_, i) => i !== index),
        { shouldValidate: true }
      );
    },
    [setValue, form, isDisabled]
  );

  const handleSubmit = useCallback(
    (values: QuotationFormValues) => {
      if (isDisabled) return;
      mutate(
        {
          params: {
            tenantId: authenticatedUserDetails?.tenantId || '',
            workspaceId: authenticatedUserDetails?.workspaceId || '',
            quotationId: rfq.id,
          },
          payload: {
            sourceId: rfq.sourceId,
            items: values.priceBreakdown.map((item) => ({
              serviceName: item.service,
              description: item.description,
              duration: item.duration,
              price: item.cost,
            })),
            paymentTerms: values.paymentTerms,
            deliverables: values.deliverables.map((d) => d.text),
          },
        },
        {
          onSuccess: () => {
            toast.success('Quotation submitted successfully');
            router.push(appRoutes.vendor.rfq.root);
          },
        }
      );
    },
    [isDisabled, mutate, rfq, router, authenticatedUserDetails]
  );

  const handleAddPriceItem = useCallback(() => {
    if (isDisabled) return;
    appendPrice({ service: '', description: '', duration: '', cost: 0 });
  }, [appendPrice, isDisabled]);

  const handleAddDeliverable = useCallback(() => {
    if (isDisabled) return;
    appendDeliverable({ text: '' });
  }, [appendDeliverable, isDisabled]);

  return (
    <Form {...form}>
      <form onSubmit={form.handleSubmit(handleSubmit)} className="relative mb-4">
        <div className="bg-card border-border rounded-lg border">
          <div className="p-6">
            <div className="mb-4 flex items-center justify-between">
              <h2 className="text-foreground text-base font-bold">Detailed Price Breakdown</h2>
              <Button
                type="button"
                variant="outline"
                size="sm"
                disabled={isDisabled}
                onClick={handleAddPriceItem}
                className="border-primary text-primary hover:bg-primary hover:text-white"
              >
                <Plus className="mr-1 h-4 w-4" />
                Add Item
              </Button>
            </div>

            {!isDisabled ? (
              <div className="space-y-4">
                <PriceBreakdownHeader />

                {priceFields.map((field, index) => (
                  <PriceBreakdownRow
                    key={field.id}
                    index={index}
                    field={field}
                    register={register}
                    errors={errors}
                    disabled={isDisabled}
                    onRemove={() => removePrice(index)}
                    canRemove={priceFields.length > 1}
                  />
                ))}

                <div
                  className="border-border grid gap-4 border-t pt-4"
                  style={{ gridTemplateColumns: GRID_COLUMNS }}
                >
                  <div />
                  <div />
                  <div className="flex items-center justify-end">
                    <Label className="text-foreground text-base font-bold">Total</Label>
                  </div>
                  <div className="flex items-center justify-end">
                    <p className="text-primary text-base font-bold">${total.toLocaleString()}</p>
                  </div>
                  <div />
                </div>
              </div>
            ) : (
              <div className="text-muted-foreground flex items-center justify-center py-3 text-xs italic">
                Accept the RFQ to submit a quotation
              </div>
            )}
          </div>

          <div className="border-border border-t" />

          <div className="p-6">
            <DynamicForm<QuotationFormValues>
              form={form}
              formConfig={PAYMENT_TERMS_CONFIG.map((row) =>
                row.map((field) => ({ ...field, disabled: isDisabled }))
              )}
            />
          </div>

          <div className="border-border border-t" />

          <div className="p-6">
            <div className="mb-4 flex items-center justify-between">
              <h2 className="text-foreground text-base font-bold">Deliverables</h2>
              <Button
                type="button"
                variant="outline"
                size="sm"
                disabled={isDisabled}
                onClick={handleAddDeliverable}
                className="border-primary text-primary hover:bg-primary hover:text-white"
              >
                <Plus className="mr-1 h-4 w-4" />
                Add
              </Button>
            </div>

            <div className="space-y-3">
              {deliverableFields.map((field, index) => (
                <DeliverableRow
                  key={field.id}
                  index={index}
                  field={field}
                  register={register}
                  error={errors.deliverables?.[index]?.text}
                  disabled={isDisabled}
                  onRemove={() => removeDeliverable(index)}
                  canRemove={deliverableFields.length > 1}
                />
              ))}
            </div>
          </div>

          <div className="border-border border-t" />

          <div className="p-6">
            <div className="flex items-center justify-between">
              <h2 className="text-foreground flex items-center gap-2 text-base font-bold">
                <Paperclip className="text-primary h-5 w-5" />
                Attachments
                {attachments.length > 0 && (
                  <span className="text-muted-foreground text-sm font-medium">
                    ({attachments.length})
                  </span>
                )}
              </h2>
              <Label
                htmlFor="file-upload"
                className={cn(
                  'border-primary hover:bg-primary text-primary rounded-button inline-flex cursor-pointer items-center gap-2 border px-4 py-2 text-sm transition-colors hover:text-white',
                  isDisabled && 'cursor-not-allowed opacity-50'
                )}
              >
                <Upload className="h-4 w-4" />
                Upload Files
                <input
                  id="file-upload"
                  type="file"
                  className="hidden"
                  multiple
                  disabled={isDisabled}
                  accept={ALLOWED_FILE_TYPES}
                  onChange={handleFileUpload}
                />
              </Label>
            </div>

            <p className="text-muted-foreground mt-1 text-sm">
              PDF, DOC, DOCX (max {MAX_FILE_SIZE_MB}MB per file)
            </p>

            {attachments.length > 0 && (
              <div className="mt-4 flex flex-wrap gap-2">
                {attachments.map((file, index) => (
                  <AttachmentItem
                    key={`${file.name}-${index}-${file.lastModified}`}
                    file={file}
                    index={index}
                    disabled={isDisabled}
                    onRemove={removeAttachment}
                  />
                ))}
              </div>
            )}
          </div>

          <div className="border-border border-t" />

          <div className="flex items-center justify-end p-6">
            <Button
              type="submit"
              disabled={isDisabled || isPending}
              className="bg-primary hover:bg-primary/90 text-primary-foreground"
            >
              {isPending ? 'Submitting...' : 'Submit Quote'}
            </Button>
          </div>
        </div>
      </form>
    </Form>
  );
};

export { RFQQuotationForm };
