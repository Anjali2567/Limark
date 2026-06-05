'use client';

import { zodResolver } from '@hookform/resolvers/zod';
import { AxiosError } from 'axios';
import { useForm } from 'react-hook-form';
import { toast } from 'sonner';
import * as z from 'zod';

import { DynamicForm } from '@/components/form/DynamicForm';
import { Button } from '@/components/ui/button';
import { Form } from '@/components/ui/form';
import { useSignup } from '@/hooks/useAuthentication';
import { emailValidation, passwordValidation, stringValidation } from '@/lib/utils/validations';
import { TokenResponse } from '@/types/auth.types';
import { DynamicFormConfig, FormElementType } from '@/types/form.types';
import { GoogleAuthButton } from '../GoogleAuthButton';

const signUpSchema = z.object({
  name: stringValidation({ fieldName: 'Name', min: 2 }),
  email: emailValidation(),
  password: passwordValidation(),
  phoneNumber: z.string().optional(),
  company: stringValidation({ fieldName: 'Company', min: 2 }),
});

type SignUpFormValues = z.infer<typeof signUpSchema>;

const signUpFormConfig: DynamicFormConfig<SignUpFormValues> = [
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
      placeholder: 'your@email.com',
      size: 12,
    },
  ],
  [
    {
      name: 'password',
      label: 'Password*',
      type: FormElementType.TEXT,
      inputType: 'password',
      placeholder: 'Min. 8 characters',
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
  [
    {
      name: 'company',
      label: 'Company*',
      type: FormElementType.TEXT,
      inputType: 'text',
      placeholder: 'Your Company Inc.',
      size: 12,
    },
  ],
];

type SignUpFormProps = {
  onSuccess: (data: TokenResponse) => void;
  onGoogleAuth: (token: string) => void;
  onSwitchToSignIn: () => void;
};

export function SignUpForm({ onSuccess, onGoogleAuth, onSwitchToSignIn }: SignUpFormProps) {
  const { mutate: signupMutate, isPending: isSigningUp } = useSignup();

  const form = useForm<SignUpFormValues>({
    resolver: zodResolver(signUpSchema),
    defaultValues: {
      name: '',
      email: '',
      password: '',
      phoneNumber: '',
      company: '',
    },
  });

  const handleSubmit = (values: SignUpFormValues) => {
    signupMutate(values, {
      onSuccess: (data) => {
        toast.success('Account created successfully!');
        onSuccess(data);
      },
      onError: (error: AxiosError) => {
        toast.error(error.message || 'Failed to create account. Please try again.');
      },
    });
  };

  return (
    <>
      <GoogleAuthButton onSuccess={onGoogleAuth} />
      <div className="relative">
        <div className="absolute inset-0 flex items-center">
          <div className="border-border w-full border-t" />
        </div>
        <div className="relative flex justify-center">
          <span className="bg-card text-muted-foreground px-3 font-sans text-sm font-medium">
            OR
          </span>
        </div>
      </div>
      <Form {...form}>
        <form onSubmit={form.handleSubmit(handleSubmit)} className="space-y-4">
          <DynamicForm form={form} formConfig={signUpFormConfig} className="gap-4" />
          <p className="text-muted-foreground text-center font-sans text-[11px] leading-relaxed">
            By creating an account, you agree to our{' '}
            <Button
              variant="link"
              type="button"
              className="text-primary p-0! text-[11px] font-semibold hover:underline"
            >
              Terms of Service
            </Button>
            <span> and </span>
            <Button
              variant="link"
              type="button"
              className="text-primary p-0! text-[11px] font-semibold hover:underline"
            >
              Privacy Policy
            </Button>
          </p>
          <Button
            type="submit"
            className="bg-primary text-primary-foreground h-12 w-full font-sans text-base font-semibold"
            disabled={isSigningUp}
          >
            {isSigningUp ? 'Creating Account...' : 'Create Account'}
          </Button>
        </form>
      </Form>
      <p className="text-muted-foreground space-x-1 text-center font-sans text-sm">
        <span>Already have an account?</span>
        <Button
          variant="link"
          type="button"
          onClick={onSwitchToSignIn}
          className="text-primary p-0! font-semibold hover:underline"
          disabled={isSigningUp}
        >
          Sign In
        </Button>
      </p>
    </>
  );
}
