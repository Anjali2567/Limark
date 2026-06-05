'use client';

import Image from 'next/image';
import { useRouter } from 'next/navigation';
import { useForm } from 'react-hook-form';
import { useEffect, useState } from 'react';
import * as z from 'zod';

import { GoogleAuthButton } from '@/components/GoogleAuthButton';
import { Button } from '@/components/ui/button';
import { CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { FormInput } from '@/components/ui/form-input';
import { appRoutes } from '@/config/routes';
import { useAuth } from '@/context/AuthContext';
import { useLogin } from '@/hooks/useAuthentication';
import { usePostLoginRedirect } from '@/hooks/usePostLoginRedirect';
import { jwtDecoder } from '@/lib/utils/jwtDecode';
import { emailValidation, passwordValidation } from '@/lib/utils/validations';
import { zodResolver } from '@hookform/resolvers/zod';

import { ArrowRight, Loader2, Lock, User } from 'lucide-react';

type LoginFormValues = z.infer<typeof loginSchema>;

const loginSchema = z.object({
  email: emailValidation(),
  password: passwordValidation(),
});

const LoginPage = () => {
  const router = useRouter();
  const { login, logout } = useAuth();
  const { mutate, isPending, isError, reset } = useLogin();
  const [isRedirecting, setIsRedirecting] = useState(false);
  const { shouldRedirect, redirectPath, hasNoModules, isLoading } = usePostLoginRedirect();

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<LoginFormValues>({
    resolver: zodResolver(loginSchema),
    defaultValues: {
      email: '',
      password: '',
    },
  });

  const onSubmit = (values: LoginFormValues) => {
    reset();
    mutate(values, {
      onSuccess: (data) => {
        login(data);
        if (data.accessToken) {
          const decoded = jwtDecoder(data.accessToken);
          if (decoded?.verified) {
            setIsRedirecting(true);
          } else {
            router.replace(appRoutes.emailVerification.root);
          }
        }
      },
    });
  };

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

  const onForgotPassword = () => {
    router.push(appRoutes.auth.forgotPassword);
  };

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
          <CardTitle className="text-2xl font-bold text-slate-900">Welcome Back</CardTitle>
          <CardDescription className="mt-2 text-slate-600">
            Sign in to LeadPlus Campaign Manager
          </CardDescription>
        </div>
      </CardHeader>

      <CardContent className="space-y-4 p-8 pt-0">
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <FormInput
            id="email"
            type="email"
            label="Email*"
            placeholder="Enter your email"
            icon={User}
            {...register('email')}
            error={!!errors.email || isError}
            errorMessage={isError ? 'Invalid credentials' : errors.email?.message}
          />

          <FormInput
            id="password"
            type="password"
            label="Password*"
            placeholder="Enter your password"
            icon={Lock}
            {...register('password')}
            error={!!errors.password || isError}
            errorMessage={isError ? 'Invalid credentials' : errors.password?.message}
            rightElement={
              <Button
                variant="none"
                type="button"
                onClick={onForgotPassword}
                className="text-primary h-fit p-0! text-sm font-medium hover:underline focus:outline-none"
              >
                Forgot password?
              </Button>
            }
          />
          <Button
            type="submit"
            className="bg-primary mt-4 h-11.5 w-full font-semibold text-white shadow-md transition-all hover:shadow-lg"
            disabled={isPending || isRedirecting}
          >
            {isPending || isRedirecting ? (
              <>
                <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                {isRedirecting ? 'Loading...' : 'Signing in...'}
              </>
            ) : (
              <>
                Login
                <ArrowRight className="ml-2 h-4 w-4" />
              </>
            )}
          </Button>
        </form>
        <GoogleAuthButton />
      </CardContent>
    </>
  );
};

export default LoginPage;
