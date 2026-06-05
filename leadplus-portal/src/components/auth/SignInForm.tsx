'use client';

import { zodResolver } from '@hookform/resolvers/zod';
import { useForm } from 'react-hook-form';
import { toast } from 'sonner';
import * as z from 'zod';

import { DynamicForm } from '@/components/form/DynamicForm';
import { Button } from '@/components/ui/button';
import { Form } from '@/components/ui/form';
import { useLogin } from '@/hooks/useAuthentication';
import { emailValidation } from '@/lib/utils/validations';
import { TokenResponse } from '@/types/auth.types';
import { DynamicFormConfig, FormElementType } from '@/types/form.types';
import { GoogleAuthButton } from '../GoogleAuthButton';

const signInSchema = z.object({
  email: emailValidation(),
  password: z.string().min(1, 'Password is required'),
});

type SignInFormValues = z.infer<typeof signInSchema>;

const signInFormConfig: DynamicFormConfig<SignInFormValues> = [
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
      placeholder: 'Enter your password',
      size: 12,
    },
  ],
];

type SignInFormProps = {
  onSuccess: (data: TokenResponse) => void;
  onGoogleAuth: (token: string) => void;
  onSwitchToSignUp: () => void;
};

export function SignInForm({ onSuccess, onGoogleAuth, onSwitchToSignUp }: SignInFormProps) {
  const { mutate: loginMutate, isPending: isLoggingIn } = useLogin();

  const form = useForm<SignInFormValues>({
    resolver: zodResolver(signInSchema),
    defaultValues: {
      email: '',
      password: '',
    },
  });

  const handleSubmit = (values: SignInFormValues) => {
    loginMutate(values, {
      onSuccess: (data) => {
        toast.success('Signed in successfully!');
        onSuccess(data);
      },
      onError: () => {
        toast.error('Please check your credentials and try again.');
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
          <DynamicForm form={form} formConfig={signInFormConfig} className="gap-4" />
          <Button
            type="submit"
            className="bg-primary text-primary-foreground h-12 w-full font-sans text-base font-semibold"
            disabled={isLoggingIn}
          >
            {isLoggingIn ? 'Signing In...' : 'Sign In'}
          </Button>
        </form>
      </Form>
      <p className="text-muted-foreground space-x-1 text-center font-sans text-sm">
        <span>Don&apos;t have an account?</span>
        <Button
          variant="link"
          type="button"
          onClick={onSwitchToSignUp}
          className="text-primary p-0! font-semibold hover:underline"
          disabled={isLoggingIn}
        >
          Sign Up
        </Button>
      </p>
    </>
  );
}
