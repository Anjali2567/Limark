'use client';

import Image from 'next/image';
import { useRouter } from 'next/navigation';
import { useEffect, useState } from 'react';

import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { appRoutes } from '@/config/routes';
import { useAuth } from '@/context/AuthContext';

import { ArrowLeft, Loader2, Mail } from 'lucide-react';
import { useResendEmail } from '@/hooks/useAuthentication';

const RESEND_DELAY = 0;

const EmailVerification = () => {
  const router = useRouter();
  const { loggedInUser } = useAuth();
  const userEmail = loggedInUser?.email;

  useEffect(() => {
    if (loggedInUser?.verified) {
      router.push(appRoutes.leadgen.profileOverview.root);
    }
  }, [loggedInUser, router]);

  const { mutate: resendEmail, isPending } = useResendEmail();

  const [canResend, setCanResend] = useState(false);
  const [countdown, setCountdown] = useState(RESEND_DELAY);

  const handleResendCode = () => {
    resendEmail({});
    setCanResend(false);
    setCountdown(RESEND_DELAY);
  };

  useEffect(() => {
    if (canResend) return;
    const interval = setInterval(() => {
      setCountdown((prev) => {
        if (prev <= 1) {
          clearInterval(interval);
          setCanResend(true);
          return RESEND_DELAY;
        }
        return prev - 1;
      });
    }, 1000);
    return () => clearInterval(interval);
  }, [canResend]);

  const onBackToSignup = () => {
    router.push(appRoutes.leadgen.auth.signup);
  };

  return (
    <div className="flex min-h-screen flex-col items-center justify-center from-sky-50 via-white to-blue-50 p-4">
      <div className="w-full max-w-md space-y-6">
        <Image
          src="/leadplus-logo.png"
          alt="LeadPlus Logo"
          width={140}
          height={32}
          className="mx-auto object-contain"
        />
        <Card className="border-border shadow-lg">
          <CardHeader className="space-y-2 pb-4 text-center">
            <div className="bg-primary/10 mx-auto mb-2 flex h-16 w-16 items-center justify-center rounded-full">
              <Mail className="text-primary h-8 w-8" />
            </div>
            <CardTitle className="text-2xl">Check Your Email</CardTitle>
            <CardDescription className="text-base">We&apos;ve sent a login link to</CardDescription>
            <p className="text-foreground font-medium">{userEmail}</p>
          </CardHeader>
          <CardContent className="space-y-6">
            <div className="space-y-3 text-center">
              <p className="text-muted-foreground text-sm">Didn&apos;t receive the email?</p>
              {canResend ? (
                <Button
                  variant="outline"
                  onClick={handleResendCode}
                  disabled={isPending}
                  className="w-full"
                >
                  {isPending ? (
                    <>
                      <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                      Sending...
                    </>
                  ) : (
                    'Resend Link'
                  )}
                </Button>
              ) : (
                <p className="text-muted-foreground text-sm">
                  Resend link in <span className="text-foreground font-medium">{countdown}s</span>
                </p>
              )}
            </div>
            <div className="bg-muted/50 space-y-2 rounded-lg p-4">
              <p className="text-foreground text-sm font-medium">Tips:</p>
              <ul className="text-muted-foreground space-y-1 text-sm">
                <li>• Check your spam or junk folder</li>
              </ul>
            </div>
            <div className="pt-2 text-center">
              <Button variant="ghost" onClick={onBackToSignup} className="text-sm">
                <ArrowLeft className="mr-2 h-4 w-4" />
                Back to Sign Up
              </Button>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
};

export default EmailVerification;
