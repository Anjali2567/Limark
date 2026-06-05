'use client';

import Image from 'next/image';
import { useRouter, useSearchParams } from 'next/navigation';
import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { toast } from 'sonner';
import * as z from 'zod';

import { DynamicForm } from '@/components/form/DynamicForm';
import { GoogleAuthButton } from '@/components/GoogleAuthButton';
import { Button } from '@/components/ui/button';
import { CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Form } from '@/components/ui/form';
import { appRoutes } from '@/config/routes';
import { Module } from '@/constants/modules.constants';
import { useAuth } from '@/context/AuthContext';
import { useSignup } from '@/hooks/useAuthentication';
import { useCreateVendor } from '@/hooks/useVendor';
import { trimValues } from '@/lib/utils/formatter';
import { getAuthModuleFromUrl } from '@/lib/utils/userTypeTracker';
import { jwtDecoder } from '@/lib/utils/jwtDecode';
import { emailValidation, passwordValidation, stringValidation } from '@/lib/utils/validations';
import { DynamicFormConfig, FormElementType } from '@/types/form.types';
import { zodResolver } from '@hookform/resolvers/zod';

const signupSchema = z.object({
  name: stringValidation({ fieldName: 'Name', min: 2 }),
  email: emailValidation(),
  password: passwordValidation(),
  phoneNumber: z.string().optional(),
  company: stringValidation({ fieldName: 'Company', min: 2 }),
});

type SignupFormValues = z.infer<typeof signupSchema>;

const signupFormConfig: DynamicFormConfig<SignupFormValues> = [
  [
    {
      name: 'name',
      label: 'Full Name*',
      type: FormElementType.TEXT,
      inputType: 'text',
      placeholder: 'Enter your full name',
      size: 12,
    },
  ],
  [
    {
      name: 'email',
      label: 'Email*',
      type: FormElementType.TEXT,
      inputType: 'email',
      placeholder: 'Enter your email',
      size: 12,
    },
  ],
  [
    {
      name: 'password',
      label: 'Password*',
      type: FormElementType.TEXT,
      inputType: 'password',
      placeholder: 'Create a password',
      size: 12,
    },
  ],
  [
    {
      name: 'company',
      label: 'Company*',
      type: FormElementType.TEXT,
      inputType: 'text',
      placeholder: 'Enter your company name',
      size: 12,
    },
  ],
  [
    {
      name: 'phoneNumber',
      label: 'Phone (Optional)',
      type: FormElementType.TEXT,
      inputType: 'tel',
      placeholder: '+1 (555) 000-0000',
      size: 12,
    },
  ],
];

const SignupPage = () => {
  const router = useRouter();
  const searchParams = useSearchParams();

  const { login, refreshSession } = useAuth();
  const currentModule = getAuthModuleFromUrl(searchParams);

  const [isSignupPending, setIsSignupPending] = useState<boolean>(false);

  const { mutateAsync: createVendorAsync } = useCreateVendor();
  const { mutateAsync, isPending } = useSignup();

  const form = useForm<SignupFormValues>({
    resolver: zodResolver(signupSchema),
    defaultValues: {
      name: '',
      email: '',
      password: '',
      phoneNumber: '',
      company: '',
    },
  });

  const { isSubmitting } = form.formState;

  const onSubmit = async (values: SignupFormValues) => {
    setIsSignupPending(true);
    await mutateAsync(trimValues(values), {
      onSuccess: async (data) => {
        login(data);
        if (currentModule === Module.VENDOR) {
          await createVendorAsync(undefined, {
            onSuccess: async () => {
              if (data.accessToken) {
                const decoded = jwtDecoder(data.accessToken);
                if (decoded?.verified) {
                  await refreshSession();
                  router.replace(appRoutes.leadgen.profileOverview.root);
                  return;
                }
              }
              router.replace(appRoutes.emailVerification.root);
            },
            onSettled: () => {
              setIsSignupPending(false);
            },
          });
        } else {
          router.replace(appRoutes.emailVerification.root);
        }
      },
      onError: () => {
        setIsSignupPending(false);
        toast.error('Failed to create account. Please try again.');
      },
    });
  };

  const isFormDisabled = isSubmitting || isPending || isSignupPending;

  return (
    <>
      <CardHeader className="flex flex-col items-center space-y-4 p-8 pb-2 text-center">
        <div className="transition-transform duration-300 hover:scale-105">
          <Image
            src="/leadplus-logo.png"
            alt="LeadPlus Logo"
            width={140}
            height={32}
            className="object-contain"
          />
        </div>
        <div>
          <CardTitle className="text-2xl font-bold text-slate-900">Create Account</CardTitle>
        </div>
      </CardHeader>

      <CardContent className="space-y-4 px-8">
        <Form {...form}>
          <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
            <DynamicForm form={form} formConfig={signupFormConfig} className="gap-4" />
            <Button
              type="submit"
              className="bg-primary mt-4 h-11.5 w-full font-semibold text-white shadow-md transition-all hover:shadow-lg"
              disabled={isFormDisabled}
            >
              {isFormDisabled ? 'Creating Account...' : 'Create Account'}
            </Button>
          </form>
        </Form>
        <GoogleAuthButton />
      </CardContent>
    </>
  );
};

export default SignupPage;
