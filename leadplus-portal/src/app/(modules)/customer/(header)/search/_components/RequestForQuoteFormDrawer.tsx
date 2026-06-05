'use client';

import Image from 'next/image';
import { Dispatch, SetStateAction, useEffect, useMemo, useRef, useState } from 'react';
import { useForm, useWatch } from 'react-hook-form';
import { toast } from 'sonner';
import { z } from 'zod';

import { Drawer } from '@/components/Drawer';
import { FileUpload } from '@/components/FileUpload';
import { DynamicForm } from '@/components/form/DynamicForm';
import { Button } from '@/components/ui/button';
import { Form, FormLabel } from '@/components/ui/form';
import { BUDGETS } from '@/constants/request-for-quote.constants';
import { useUpsertRequestForQuote } from '@/hooks/useRequestForQuote';
import { getCachedImageUrl } from '@/lib/utils/helpers';
import { dateValidation, stringValidation } from '@/lib/utils/validations';
import { DynamicFormConfig, FormElementType } from '@/types/form.types';
import { VendorCardDetails } from '@/types/vendor.types';
import { zodResolver } from '@hookform/resolvers/zod';

import { Loader2, Paperclip, X } from 'lucide-react';

const quoteRequestSchema = z.object({
  title: stringValidation({ fieldName: 'Title', min: 3 }),
  description: stringValidation({ fieldName: 'Description', min: 10 }),
  budget: z.string().min(1, 'Budget is required'),
  services: z.array(z.string()).min(1, 'At least one service must be selected'),
  deadline: dateValidation({ fieldName: 'Deadline', futureOnly: true, required: true }),
});

type QuoteRequestFormValues = z.infer<typeof quoteRequestSchema>;

type RequestForQuoteFormDrawerProps = {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  selectedVendorIds: string[];
  setSelectedVendors: Dispatch<SetStateAction<string[]>>;
  vendors: VendorCardDetails[];
  onClearSelection?: () => void;
};

type QuoteRequestFooterProps = {
  isPending: boolean;
  selectedVendorIds: string[];
};

const QuoteRequestFooter = ({ isPending, selectedVendorIds }: QuoteRequestFooterProps) => {
  return (
    <div className="flex justify-end">
      <Button
        type="submit"
        form="quote-request-form"
        disabled={isPending || selectedVendorIds.length === 0}
        className="bg-primary text-primary-foreground hover:bg-primary/90 h-11 font-semibold"
      >
        {isPending ? (
          <>
            <Loader2 className="mr-2 h-4 w-4 animate-spin" />
            Sending...
          </>
        ) : (
          'Send Quote Request'
        )}
      </Button>
    </div>
  );
};

const RequestForQuoteFormDrawer = ({
  open,
  onOpenChange,
  selectedVendorIds,
  setSelectedVendors,
  vendors,
  onClearSelection,
}: RequestForQuoteFormDrawerProps) => {
  const [files, setFiles] = useState<File[]>([]);
  const servicesInitializedRef = useRef(false);

  const { mutate: upsertQuote, isPending } = useUpsertRequestForQuote();

  const selectedVendors = vendors.filter((vendor) => selectedVendorIds.includes(vendor.id));

  const availableServices = useMemo(() => {
    const servicesMap = new Map<string, { id: number; name: string }>();
    selectedVendors.forEach((vendor) => {
      vendor.serviceList.forEach((service) => {
        if (!servicesMap.has(String(service.id))) {
          servicesMap.set(String(service.id), service);
        }
      });
    });
    return Array.from(servicesMap.values());
  }, [selectedVendors]);

  const allServiceIds = useMemo(
    () => availableServices.map((service) => service.id),
    [availableServices]
  );

  const form = useForm<QuoteRequestFormValues>({
    resolver: zodResolver(quoteRequestSchema),
    defaultValues: {
      title: '',
      description: '',
      budget: '',
      services: [],
      deadline: new Date(),
    },
  });

  const { handleSubmit, formState, reset, setValue, control } = form;
  const { isDirty } = formState;
  const selectedServices = useWatch({ control, name: 'services', defaultValue: [] });

  useEffect(() => {
    if (!open) {
      servicesInitializedRef.current = false;
      return;
    }
    if (
      allServiceIds.length > 0 &&
      selectedServices.length === 0 &&
      !servicesInitializedRef.current
    ) {
      servicesInitializedRef.current = true;
      setValue('services', allServiceIds.map(String), { shouldValidate: false });
    }
  }, [open, allServiceIds, setValue, selectedServices.length]);

  useEffect(() => {
    if (selectedServices.length > 0 && allServiceIds.length > 0) {
      const validServices = selectedServices.filter((serviceId) =>
        allServiceIds.includes(serviceId as unknown as number)
      );
      if (validServices.length !== selectedServices.length) {
        setValue('services', validServices, { shouldValidate: true });
      }
    }
  }, [allServiceIds, selectedServices, setValue]);

  const handleRemoveVendor = (id: string) => {
    if (selectedVendorIds.length <= 1) {
      toast.error('At least one vendor must be selected');
      return;
    }
    setSelectedVendors((prev) => prev.filter((vendorId) => vendorId !== id));
  };

  const handleFilesSelected = (selectedFiles: FileList | null) => {
    if (selectedFiles) {
      setFiles((prev) => [...prev, ...Array.from(selectedFiles)]);
    }
  };

  const handleRemoveFile = (index: number) => {
    setFiles((prev) => prev.filter((_, i) => i !== index));
  };

  const handleDiscard = () => {
    reset();
    setFiles([]);
  };

  const onSubmit = async (values: QuoteRequestFormValues) => {
    const payload = {
      title: values.title,
      description: values.description,
      budget: values.budget,
      deadline: values.deadline!,
      vendorIds: selectedVendorIds,
      serviceIds: values.services,
    };

    upsertQuote(
      { payload, files: files.length > 0 ? files : undefined },
      {
        onSuccess: () => {
          toast.success('Quote request sent successfully!');
          reset();
          setFiles([]);
          onOpenChange(false);
          onClearSelection?.();
        },
        onError: (error) => {
          toast.error(`Failed to send quote request: ${error.message || 'Unknown error'}`);
        },
      }
    );
  };

  const getMinDeadlineDate = () => {
    const today = new Date();
    today.setDate(today.getDate() + 1);
    return today.toISOString().split('T')[0];
  };

  const formConfig: DynamicFormConfig<QuoteRequestFormValues> = useMemo(
    () => [
      [
        {
          name: 'title',
          type: FormElementType.TEXT,
          label: 'Title *',
          placeholder: 'e.g., Mobile App Development',
          size: 12,
        },
      ],
      [
        {
          name: 'description',
          type: FormElementType.TEXTAREA,
          label: 'Description *',
          placeholder: 'Briefly describe your project requirements',
          rows: 4,
          size: 12,
        },
      ],
      [
        {
          name: 'services',
          type: FormElementType.MULTI_SELECT,
          label: 'Services Required *',
          placeholder: 'Select services...',
          options: availableServices.map((service) => ({
            label: service.name,
            value: String(service.id),
          })),
          size: 12,
        },
      ],
      [
        {
          name: 'budget',
          type: FormElementType.SELECT,
          label: 'Budget *',
          placeholder: 'Select budget range',
          options: BUDGETS.map((b) => ({ label: b.label, value: b.value })),
          size: 6,
        },
        {
          name: 'deadline',
          type: FormElementType.DATE,
          label: 'Deadline *',
          placeholder: 'Select deadline',
          min: getMinDeadlineDate(),
          size: 6,
        },
      ],
    ],
    [availableServices]
  );

  return (
    <Drawer
      open={open}
      onOpenChange={onOpenChange}
      title="Request Quote"
      description="Request quotes from selected vendors"
      hasUnsavedChanges={isDirty || files.length > 0}
      onDiscard={handleDiscard}
      footer={<QuoteRequestFooter isPending={isPending} selectedVendorIds={selectedVendorIds} />}
    >
      <Form {...form}>
        <form id="quote-request-form" onSubmit={handleSubmit(onSubmit)} className="space-y-6">
          <div className="space-y-3">
            <FormLabel className="text-foreground text-sm font-medium">Selected Vendors</FormLabel>
            <div className="flex flex-wrap gap-2">
              {selectedVendors.map((vendor) => {
                const canRemove = selectedVendorIds.length > 1;
                return (
                  <div
                    key={vendor.id}
                    className="border-border bg-muted flex items-center gap-2 rounded-lg border px-3 py-1.5"
                  >
                    <Image
                      src={getCachedImageUrl(vendor.logo, vendor.updatedAt) || '/placeholder.png'}
                      alt={vendor.companyName}
                      width={20}
                      height={20}
                      className="h-5 w-5 rounded object-cover"
                    />
                    <span className="text-foreground text-sm font-medium">
                      {vendor.companyName}
                    </span>
                    {canRemove && (
                      <button
                        type="button"
                        onClick={() => handleRemoveVendor(vendor.id)}
                        className="hover:bg-background ml-1 rounded p-0.5 transition-colors"
                      >
                        <X className="text-muted-foreground h-3.5 w-3.5" />
                      </button>
                    )}
                  </div>
                );
              })}
            </div>
          </div>

          <div className="space-y-4">
            <div>
              <h3 className="text-foreground mb-1 text-base font-semibold">Project Details</h3>
              <p className="text-muted-foreground text-sm">Share information about your project</p>
            </div>

            <DynamicForm form={form} formConfig={formConfig} />
            <div className="space-y-3">
              <FormLabel className="text-sm font-medium">Attachments</FormLabel>
              <div className="flex flex-col gap-3">
                <FileUpload multiple onFilesSelected={handleFilesSelected} />
                {files.length > 0 && (
                  <div className="flex flex-wrap gap-2">
                    {files.map((file, idx) => (
                      <div
                        key={idx}
                        className="border-border bg-muted group flex items-center gap-2 rounded-lg border px-3 py-2 text-sm"
                      >
                        <Paperclip className="text-muted-foreground h-4 w-4" />
                        <span className="text-foreground">{file.name}</span>
                        <button
                          type="button"
                          onClick={() => handleRemoveFile(idx)}
                          className="hover:bg-background ml-1 rounded p-0.5 opacity-0 transition-all group-hover:opacity-100"
                        >
                          <X className="text-muted-foreground h-3.5 w-3.5" />
                        </button>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </div>
          </div>
        </form>
      </Form>
    </Drawer>
  );
};

export { RequestForQuoteFormDrawer };
