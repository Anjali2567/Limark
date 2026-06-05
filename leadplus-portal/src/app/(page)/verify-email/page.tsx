'use client';

import { useRouter, useSearchParams } from 'next/navigation';
import { Suspense, useEffect, useRef } from 'react';
import { toast } from 'sonner';

import { appRoutes } from '@/config/routes';
import { useGetAccessToken, useVerifyEmail } from '@/hooks/useAuthentication';
import { useAuth } from '@/context/AuthContext';
import { getRefreshToken } from '@/lib/auth/tokenStorage';
import { AuthToken } from '@/constants/Auth';

import { Loader2 } from 'lucide-react';

const VerifyEmailContent = () => {
  const router = useRouter();
  const searchParams = useSearchParams();
  const { mutate: verifyEmail } = useVerifyEmail();
  const { mutate: getAccessToken } = useGetAccessToken();
  const { login } = useAuth();
  const hasVerified = useRef(false);

  useEffect(() => {
    if (hasVerified.current) return;
    const token = searchParams.get('token');

    if (!token) {
      toast.error('Invalid or missing verification token');
      router.replace(appRoutes.emailVerification.root);
      return;
    }

    hasVerified.current = true;
    verifyEmail(
      { token },
      {
        onSuccess: () => {
          toast.success('Email Verification Successful');
          const storedRefreshToken = getRefreshToken(AuthToken.REFRESH_TOKEN);
          if (storedRefreshToken) {
            getAccessToken(storedRefreshToken, {
              onSuccess: (data) => {
                login(data);
                router.replace(appRoutes.leadgen.search.companySearch);
              },
              onError: () => {
                router.replace(appRoutes.leadgen.auth.login);
              },
            });
          } else {
            router.replace(appRoutes.leadgen.auth.login);
          }
        },
        onError: () => {
          toast.error('Email Verification Failed');
          router.replace(appRoutes.emailVerification.root);
        },
      }
    );
  }, [router, searchParams, verifyEmail, getAccessToken, login]);

  return null;
};

const VerifyEmail = () => {
  return (
    <Suspense
      fallback={
        <div className="flex h-screen w-screen items-center justify-center">
          <Loader2 className="text-muted-foreground h-6 w-6 animate-spin" />
        </div>
      }
    >
      <VerifyEmailContent />
    </Suspense>
  );
};

export default VerifyEmail;
