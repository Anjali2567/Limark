'use client';

import { AxiosError } from 'axios';
import { useRouter } from 'next/navigation';
import { useForm } from 'react-hook-form';
import { toast } from 'sonner';
import * as z from 'zod';

import { Button } from '@/components/ui/button';
import {
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from '@/components/ui/card';
import { FormInput } from '@/components/ui/form-input';
import { useForgotPasswordRequest } from '@/hooks/useAuthentication';
import { zodResolver } from '@hookform/resolvers/zod';

import { appRoutes } from '@/config/routes';
import useToggle from '@/hooks/useToggle';
import { PasswordResetSentView } from './_components/PasswordResetSentView';

import { ArrowLeft, ArrowRight, User } from 'lucide-react';

const forgotPasswordSchema = z.object({
  email: z.email('Invalid email address'),
});

type ForgotPasswordFormValues = z.infer<typeof forgotPasswordSchema>;

const ForgotPasswordPage = () => {
  const router = useRouter();
  const { value: isSubmitted, setValue: setIsSubmitted, toggle } = useToggle();
  const { mutate, isPending } = useForgotPasswordRequest();
  const {
    register,
    handleSubmit,
    getValues,
    formState: { errors, isSubmitting },
  } = useForm<ForgotPasswordFormValues>({
    resolver: zodResolver(forgotPasswordSchema),
    defaultValues: {
      email: '',
    },
  });

  const onSubmit = async (values: ForgotPasswordFormValues) => {
    mutate(
      { email: values.email },
      {
        onSuccess: () => {
          setIsSubmitted(true);
        },
        onError: (error: AxiosError) => {
          toast.error(error.message || 'An error occurred. Please try again later.');
        },
      }
    );
  };

  const onBackToLogin = () => {
    router.push(appRoutes.auth.login);
  };

  if (isSubmitted) {
    return <PasswordResetSentView forgotEmail={getValues('email')} onBack={toggle} />;
  }

  return (
    <>
      <CardHeader className="flex flex-col items-center space-y-4 p-8 pb-2 text-center">
        <div>
          <CardTitle className="text-2xl font-bold text-slate-900">Forgot Password</CardTitle>
          <CardDescription className="mt-2 text-slate-600">
            Enter your email address and we&apos;ll send you a link to reset your password.
          </CardDescription>
        </div>
      </CardHeader>
      <CardContent className="p-8 pt-0">
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <FormInput
            id="forgot-email"
            type="email"
            label="Email Address*"
            placeholder="Enter your email"
            icon={User}
            {...register('email')}
            error={!!errors.email}
            errorMessage={errors.email?.message}
          />
          <Button
            type="submit"
            className="bg-primary mt-4 h-11.5 w-full font-semibold text-white shadow-md transition-all hover:shadow-lg"
            disabled={isPending || isSubmitting}
          >
            {isPending || isSubmitting ? 'Sending...' : 'Send Reset Link'}
            {(!isPending || !isSubmitting) && <ArrowRight className="ml-2 h-4 w-4" />}
          </Button>
        </form>
      </CardContent>
      <CardFooter className="flex flex-col space-y-4 rounded-b-lg border-t border-slate-100 bg-slate-50/50 p-8">
        <Button
          variant="ghost"
          onClick={onBackToLogin}
          className="w-full text-slate-600 hover:bg-slate-100 hover:text-slate-900"
        >
          <ArrowLeft className="mr-2 h-4 w-4" />
          Back to Login
        </Button>
      </CardFooter>
    </>
  );
};

export default ForgotPasswordPage;
