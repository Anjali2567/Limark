'use client';

import { useRouter, useSearchParams } from 'next/navigation';
import { useCallback, useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import { toast } from 'sonner';
import { z } from 'zod';

import { Button } from '@/components/ui/button';
import {
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from '@/components/ui/card';
import { FormInput } from '@/components/ui/form-input';
import { appRoutes } from '@/config/routes';
import { useAuth } from '@/context/AuthContext';
import { useResetPassword } from '@/hooks/useAuthentication';
import { usePostLoginRedirect } from '@/hooks/usePostLoginRedirect';
import { passwordValidation } from '@/lib/utils/validations';
import { zodResolver } from '@hookform/resolvers/zod';

import { ArrowLeft, ArrowRight, Loader2, Lock } from 'lucide-react';

const resetPasswordSchema = z.object({
  password: passwordValidation(),
});

type ResetPasswordFormValues = z.infer<typeof resetPasswordSchema>;

const ResetPasswordPage = () => {
  const router = useRouter();
  const searchParams = useSearchParams();
  const { login, logout } = useAuth();
  const { mutate, isPending } = useResetPassword();
  const [isRedirecting, setIsRedirecting] = useState(false);
  const { shouldRedirect, redirectPath, hasNoModules, isLoading } = usePostLoginRedirect();

  const token = searchParams.get('token');

  const {
    handleSubmit,
    formState: { errors },
    register,
  } = useForm<ResetPasswordFormValues>({
    resolver: zodResolver(resetPasswordSchema),
    defaultValues: {
      password: '',
    },
  });

  const onSubmit = useCallback(
    (values: ResetPasswordFormValues) => {
      if (!token) {
        toast.error('The password reset link is invalid or missing the required token.');
        return;
      }

      mutate(
        {
          token,
          password: values.password,
        },
        {
          onSuccess: (data) => {
            login(data);
            setIsRedirecting(true);
          },
          onError: () => {
            toast.error('Failed to reset password. Please try again.');
          },
        }
      );
    },
    [token, mutate, login]
  );

  useEffect(() => {
    if (!isRedirecting || isLoading) return;

    if (hasNoModules) {
      logout();
      return;
    }

    if (shouldRedirect && redirectPath) {
      router.replace(redirectPath);
    } else {
      router.replace(appRoutes.dashboard.root);
    }
  }, [isRedirecting, shouldRedirect, redirectPath, hasNoModules, router, logout, isLoading]);

  const onBackToLogin = () => {
    router.push(appRoutes.auth.login);
  };

  return (
    <>
      <CardHeader className="flex flex-col items-center space-y-4 p-8 pb-2 text-center">
        <div>
          <CardTitle className="text-foreground text-2xl font-bold">Reset Password</CardTitle>
          <CardDescription className="text-muted-foreground mt-2">
            Enter a new password for your account.
          </CardDescription>
        </div>
      </CardHeader>

      <CardContent className="p-8 pt-0">
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div className="space-y-2">
            <FormInput
              id="signup-password"
              type="password"
              label="Password"
              placeholder="Create a password"
              icon={Lock}
              {...register('password')}
              error={!!errors.password}
              errorMessage={errors.password?.message}
              required
              disabled={isPending}
            />
          </div>
          <Button
            type="submit"
            className="text-primary-foreground mt-4 h-11.5 w-full bg-sky-500 font-semibold shadow-md transition-all hover:bg-sky-600 hover:shadow-lg"
            disabled={isPending}
          >
            {isPending ? (
              <>
                <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                Resetting Password...
              </>
            ) : (
              <>
                Reset Password
                <ArrowRight className="ml-2 h-4 w-4" />
              </>
            )}
          </Button>
        </form>
      </CardContent>
      <CardFooter className="bg-secondary/50 border-border flex flex-col space-y-4 rounded-b-lg border-t p-8">
        <Button
          variant="ghost"
          onClick={onBackToLogin}
          className="text-muted-foreground hover:text-foreground hover:bg-secondary w-full"
        >
          <ArrowLeft className="mr-2 h-4 w-4" />
          Back to Login
        </Button>
      </CardFooter>
    </>
  );
};

export default ResetPasswordPage;
