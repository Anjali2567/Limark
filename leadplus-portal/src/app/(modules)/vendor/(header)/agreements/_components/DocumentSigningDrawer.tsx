'use client';

import { useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { X } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Label } from '@/components/ui/label';
import { Input } from '@/components/ui/input';
import {
  Sheet,
  SheetContent,
  SheetDescription,
  SheetHeader,
  SheetTitle,
} from '@/components/ui/sheet';
import { VendorAgreement } from '@/types/vendor-agreements.types';
import { Form, FormControl, FormField, FormItem, FormMessage } from '@/components/ui/form';
import { AgreementType } from '@/constants/agreement.constant';

type SignatureData = {
  signedBy: string;
  name: string;
  title: string;
  otp: string;
};

const documentSigningSchema = z.object({
  signedBy: z.string().min(1, 'Signed by is required'),
  name: z.string().min(1, 'Name is required'),
  title: z.string().min(1, 'Title is required'),
  otp: z.string().min(1, 'OTP is required'),
});

type DocumentSigningFormValues = z.infer<typeof documentSigningSchema>;

type CompanyInfo = {
  name: string;
  signedBy: string;
  signerName: string;
  signerTitle: string;
};

type DocumentSigningDrawerProps = {
  agreement: VendorAgreement | null;
  vendorCompanyName: string;
  companyInfo?: CompanyInfo;
  onClose: () => void;
  onSign: (data: SignatureData) => void;
  onRequestOtp: (agreementType: AgreementType) => void;
};

const DocumentSigningDrawer = ({
  agreement,
  vendorCompanyName,
  companyInfo = {
    name: 'LeadPlus Private Limited',
    signedBy: 'John Doe',
    signerName: 'John Doe',
    signerTitle: 'CEO',
  },
  onClose,
  onSign,
  onRequestOtp,
}: DocumentSigningDrawerProps) => {
  const form = useForm<DocumentSigningFormValues>({
    resolver: zodResolver(documentSigningSchema),
    defaultValues: {
      signedBy: '',
      name: '',
      title: '',
      otp: '',
    },
  });

  const { handleSubmit, reset } = form;

  useEffect(() => {
    if (!agreement) {
      reset();
    }
  }, [agreement, reset]);

  const handleRequestOtp = async () => {
    if (!agreement) return;
    onRequestOtp(agreement.agreementType);
  };

  const onSubmit = (data: DocumentSigningFormValues) => {
    if (!agreement) return;
    onSign({
      signedBy: data.signedBy,
      name: data.name,
      title: data.title,
      otp: data.otp,
    });
  };

  const getAgreementTitle = (agreementType: string) => {
    const titleMap: Record<string, string> = {
      PRIVACY_POLICY: 'Privacy Policy',
      TERMS_OF_SERVICE: 'Terms of Service',
    };
    return titleMap[agreementType] || agreementType;
  };

  const companyFields = [
    { label: 'By', value: companyInfo.signedBy },
    { label: 'Name', value: companyInfo.signerName },
    { label: 'Title', value: companyInfo.signerTitle },
  ];

  return (
    <Sheet open={!!agreement} onOpenChange={(open) => !open && onClose()}>
      <SheetContent side="right" className="flex w-full flex-col gap-0 sm:max-w-3xl">
        {agreement && (
          <Form {...form}>
            <form onSubmit={handleSubmit(onSubmit)} className="flex h-full flex-col">
              <SheetHeader className="border-border border-b bg-gray-100 p-4">
                <SheetTitle className="text-foreground text-2xl">
                  Sign {getAgreementTitle(agreement.agreementType)} Document
                </SheetTitle>
                <SheetDescription className="text-muted-foreground">
                  Please review and sign the document below
                </SheetDescription>
              </SheetHeader>
              <div className="flex-1 overflow-y-auto p-6">
                <div className="rounded-md border bg-gray-50 p-4">
                  <div
                    className="text-foreground leading-relaxed"
                    dangerouslySetInnerHTML={{ __html: agreement?.agreementText || '' }}
                  />
                </div>
                <div className="mt-6 space-y-4">
                  <div className="grid grid-cols-2 gap-4">
                    <div className="space-y-3">
                      <h3 className="text-foreground text-sm font-semibold">
                        &quot;The Company&quot;
                      </h3>
                      <div className="text-foreground text-sm font-medium">{companyInfo.name}</div>
                      {companyFields.map((field) => (
                        <div key={field.label} className="flex items-center gap-2">
                          <Label className="text-muted-foreground w-12 shrink-0 text-xs">
                            {field.label}
                          </Label>
                          <span className="text-foreground text-sm">: {field.value}</span>
                        </div>
                      ))}
                    </div>

                    <div className="space-y-3">
                      <h3 className="text-foreground text-sm font-semibold">
                        &quot;Third Party&quot;
                      </h3>
                      <div className="text-foreground text-sm font-medium">{vendorCompanyName}</div>
                      <FormField
                        control={form.control}
                        name="signedBy"
                        render={({ field }) => (
                          <FormItem>
                            <div className="flex items-center gap-2">
                              <Label className="text-muted-foreground w-12 shrink-0 text-xs">
                                By
                              </Label>
                              <FormControl>
                                <Input className="h-8 flex-1" placeholder="" {...field} />
                              </FormControl>
                            </div>
                            <FormMessage className="ml-14 text-xs" />
                          </FormItem>
                        )}
                      />
                      <FormField
                        control={form.control}
                        name="name"
                        render={({ field }) => (
                          <FormItem>
                            <div className="flex items-center gap-2">
                              <Label className="text-muted-foreground w-12 shrink-0 text-xs">
                                Name
                              </Label>
                              <FormControl>
                                <Input className="h-8 flex-1" placeholder="" {...field} />
                              </FormControl>
                            </div>
                            <FormMessage className="ml-14 text-xs" />
                          </FormItem>
                        )}
                      />
                      <FormField
                        control={form.control}
                        name="title"
                        render={({ field }) => (
                          <FormItem>
                            <div className="flex items-center gap-2">
                              <Label className="text-muted-foreground w-12 shrink-0 text-xs">
                                Title
                              </Label>
                              <FormControl>
                                <Input className="h-8 flex-1" placeholder="" {...field} />
                              </FormControl>
                            </div>
                            <FormMessage className="ml-14 text-xs" />
                          </FormItem>
                        )}
                      />
                    </div>
                  </div>

                  <div className="mt-6">
                    <FormField
                      control={form.control}
                      name="otp"
                      render={({ field }) => (
                        <FormItem>
                          <div className="flex items-center gap-2">
                            <Label className="text-muted-foreground w-12 shrink-0 text-xs">
                              OTP
                            </Label>
                            <FormControl>
                              <Input className="h-8 w-64" placeholder="" {...field} />
                            </FormControl>
                            <Button
                              type="button"
                              variant="link"
                              size="sm"
                              className="px-2 text-sky-500 hover:text-sky-600"
                              onClick={handleRequestOtp}
                            >
                              Request OTP
                            </Button>
                          </div>
                          <FormMessage className="ml-14 text-xs" />
                        </FormItem>
                      )}
                    />
                  </div>
                </div>

                <p className="text-muted-foreground mt-6 text-xs">
                  By clicking &quot;Sign&quot; or submitting this form, you acknowledge that your
                  electronic signature is the legal equivalent of your manual signature on this
                  agreement. You agree that this digital signature is valid, binding, and
                  enforceable.
                </p>
              </div>

              <div className="border-border mt-auto grid shrink-0 grid-cols-2 gap-3 border-t bg-gray-100 p-4">
                <Button type="button" variant="outline" onClick={onClose}>
                  <X className="mr-2 h-4 w-4" />
                  Cancel
                </Button>
                <Button
                  type="submit"
                  className="bg-sky-500 text-white hover:bg-sky-600"
                  disabled={form.formState.isSubmitting}
                >
                  {form.formState.isSubmitting ? 'Submitting...' : 'Submit'}
                </Button>
              </div>
            </form>
          </Form>
        )}
      </SheetContent>
    </Sheet>
  );
};

export { DocumentSigningDrawer };
