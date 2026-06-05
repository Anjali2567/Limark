'use client';

import { useRouter, useSearchParams } from 'next/navigation';
import { Suspense, useEffect, useRef } from 'react';

import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { appRoutes } from '@/config/routes';
import { Module } from '@/constants/modules.constants';
import { useAcceptWorkspaceInvitation } from '@/hooks/useWorkspace';

import { Loader2 } from 'lucide-react';

const WorkspaceInviteAcceptPage = () => {
  const searchParams = useSearchParams();
  const router = useRouter();

  const token = searchParams.get('token');
  const userExists = searchParams.get('userExists');

  const { mutate, isPending, isSuccess, isError } = useAcceptWorkspaceInvitation();
  const hasAcceptedRef = useRef(false);

  useEffect(() => {
    if (!token || hasAcceptedRef.current) return;
    hasAcceptedRef.current = true;

    mutate(token, {
      onSettled: () => {
        window.setTimeout(() => {
          if (userExists === 'false') {
            router.push(`${appRoutes.auth.signup}?type=${Module.VENDOR.toLowerCase()}`);
            return;
          }
          router.push(appRoutes.leadgen.profileOverview.root);
        }, 1000);
      },
    });
  }, [token, mutate, router, userExists]);

  return (
    <div className="flex min-h-screen items-center justify-center bg-slate-50">
      <Card className="w-full max-w-md">
        <CardHeader className="text-center">
          <CardTitle className="text-2xl">Workspace Invitation</CardTitle>
          <CardDescription>
            {isPending
              ? 'Accepting your invitation...'
              : isSuccess
                ? 'Invitation accepted successfully!'
                : isError
                  ? 'Failed to accept invitation.'
                  : 'Confirming your invitation token.'}
          </CardDescription>
        </CardHeader>

        <CardContent className="flex flex-col items-center gap-4">
          {isPending && (
            <div className="flex items-center gap-2">
              <Loader2 className="h-5 w-5 animate-spin" />
              <span className="text-sm font-medium">Processing...</span>
            </div>
          )}

          {isSuccess && (
            <div className="font-medium text-green-600">Redirecting to your profile shortly...</div>
          )}

          {isError && (
            <div className="text-center text-red-600">
              Something went wrong. Please try again or contact support.
            </div>
          )}

          {!token && (
            <>
              <div className="text-center">
                <div className="font-medium text-red-600">No token found.</div>
                <div className="text-sm text-slate-500">Please use the invite link again.</div>
              </div>
              <div className="pt-2">
                <Button
                  variant="outline"
                  onClick={() => router.push(appRoutes.leadgen.profileOverview.root)}
                  disabled={isPending}
                >
                  Go to Profile
                </Button>
              </div>
            </>
          )}
        </CardContent>
      </Card>
    </div>
  );
};

const WorkspaceInvitePage = () => {
  return (
    <Suspense
      fallback={
        <div className="flex min-h-screen items-center justify-center bg-slate-50">
          <Card className="w-full max-w-md">
            <CardHeader className="text-center">
              <CardTitle className="text-2xl">Workspace Invitation</CardTitle>
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
      <WorkspaceInviteAcceptPage />
    </Suspense>
  );
};

export default WorkspaceInvitePage;
