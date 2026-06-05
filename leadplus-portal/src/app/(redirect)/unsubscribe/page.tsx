'use client';

import { useRouter, useSearchParams } from 'next/navigation';
import { Suspense, useEffect } from 'react';

import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { appRoutes } from '@/config/routes';
import { useUnsubscribeContact } from '@/hooks/useContactUnsubscribe';

import { Loader2 } from 'lucide-react';

const UnsubscribeAcceptPage = () => {
  const searchParams = useSearchParams();
  const router = useRouter();

  const token = searchParams.get('token');
  const { isSuccess, isLoading, isError } = useUnsubscribeContact(token || '');

  useEffect(() => {
    if (!token) return;
    if (!isLoading) {
      window.setTimeout(() => {
        router.push(appRoutes.leadgen.auth.login);
      }, 1000);
    }
  }, [token, isLoading, router]);

  return (
    <div className="flex min-h-screen items-center justify-center bg-slate-50">
      <Card className="w-full max-w-md">
        <CardHeader className="text-center">
          <CardTitle className="text-2xl">Unsubscribe</CardTitle>
          <CardDescription>
            {isLoading
              ? 'Processing your unsubscribe request...'
              : isSuccess
                ? 'You have been unsubscribed successfully!'
                : isError
                  ? 'Failed to unsubscribe. Please try again or contact support.'
                  : 'Confirming your unsubscribe token.'}
          </CardDescription>
        </CardHeader>

        <CardContent className="flex flex-col items-center gap-4">
          {isLoading && (
            <div className="flex items-center gap-2">
              <Loader2 className="h-5 w-5 animate-spin" />
              <span className="text-sm font-medium">Processing...</span>
            </div>
          )}

          {isError && (
            <div className="text-center text-red-600">
              Something went wrong. Please try again or contact support.
            </div>
          )}

          {isSuccess && (
            <div className="text-center text-green-600">
              You have been unsubscribed successfully!
            </div>
          )}

          {!token && (
            <>
              <div className="text-center">
                <div className="font-medium text-red-600">No token found.</div>
                <div className="text-sm text-slate-500">Please use the unsubscribe link again.</div>
              </div>
              <div className="pt-2">
                <Button
                  variant="outline"
                  onClick={() => router.push(appRoutes.leadgen.auth.login)}
                  disabled={isLoading}
                >
                  Go to Login
                </Button>
              </div>
            </>
          )}
        </CardContent>
      </Card>
    </div>
  );
};

const UnsubscribePage = () => {
  return (
    <Suspense
      fallback={
        <div className="flex min-h-screen items-center justify-center bg-slate-50">
          <Card className="w-full max-w-md">
            <CardHeader className="text-center">
              <CardTitle className="text-2xl">Unsubscribe</CardTitle>
              <CardDescription>Loading...</CardDescription>
            </CardHeader>
            <CardContent className="flex flex-col items-center gap-4">
              <div className="flex items-center gap-2">
                <Loader2 className="h-5 w-5 animate-spin" />
                <span className="text-sm font-medium">Processing...</span>
              </div>
            </CardContent>
          </Card>
        </div>
      }
    >
      <UnsubscribeAcceptPage />
    </Suspense>
  );
};

export default UnsubscribePage;
