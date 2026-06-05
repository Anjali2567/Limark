'use client';

import { ReactNode } from 'react';
import { usePathname, useRouter, useSearchParams } from 'next/navigation';
import { Button } from '@/components/ui/button';
import { Card, CardFooter } from '@/components/ui/card';
import { appRoutes } from '@/config/routes';
import { normalizePath } from '@/lib/utils/formatter';

const Background = () => (
  <div className="absolute inset-0 z-0">
    <div className="absolute inset-0 h-full w-full bg-[linear-gradient(to_right,#FFFFFF20_1px,transparent_1px),linear-gradient(to_bottom,#FFFFFF20_1px,transparent_1px)] mask-[radial-gradient(ellipse_60%_50%_at_50%_0%,#000_70%,transparent_100%)] bg-size-[24px_24px]"></div>
    <div className="absolute top-0 right-0 left-0 h-125 bg-linear-to-b from-white/10 to-transparent"></div>
  </div>
);

interface AuthLayoutContentProps {
  children: ReactNode;
  loginPath?: string;
  signupPath?: string;
  hideSignup?: boolean;
}

export const AuthLayoutContent = ({
  children,
  hideSignup = false,
  loginPath = appRoutes.leadgen.auth.login,
  signupPath = appRoutes.leadgen.auth.signup,
}: AuthLayoutContentProps) => {
  const router = useRouter();
  const pathname = usePathname();
  const searchParams = useSearchParams();

  const isLoginPage = normalizePath(pathname) === normalizePath(loginPath);
  const isSignupPage = normalizePath(pathname) === normalizePath(signupPath);

  const toggleAuthView = () => {
    const userType = searchParams.get('type');
    const targetPath = isLoginPage ? signupPath : loginPath;
    const urlWithType = userType ? `${targetPath}?type=${userType}` : targetPath;
    router.push(urlWithType);
  };

  return (
    <div className="bg-primary relative flex min-h-screen items-center justify-center overflow-hidden p-4">
      <Background />
      <Card className="border-t-primary z-10 w-full max-w-md rounded-lg border-t-4 bg-white/95 shadow-2xl backdrop-blur-sm transition-all duration-300 ease-in-out">
        {children}
        {!hideSignup && (isLoginPage || isSignupPage) && (
          <CardFooter className="bg-secondary/50 border-border flex items-center justify-center gap-1 rounded-b-lg border-t p-6">
            <span className="text-muted-foreground text-sm">
              {isLoginPage ? "Don't have an account?" : 'Already have an account?'}
            </span>
            <Button
              variant="none"
              onClick={toggleAuthView}
              className="border-0 bg-transparent p-0 font-semibold text-sky-500 hover:underline"
            >
              {isLoginPage ? 'Sign Up' : 'Login'}
            </Button>
          </CardFooter>
        )}
      </Card>
      <div className="absolute bottom-4 w-full text-center text-xs text-white/60">
        &copy; {new Date().getFullYear()} LeadPlus AI Solutions. All rights reserved.
      </div>
    </div>
  );
};
