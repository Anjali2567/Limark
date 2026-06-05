'use client';

import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ReactNode, useEffect, useState } from 'react';
import ReactGA from 'react-ga4';

import ActionConfirmationModal from '@/components/ActionConfirmationModal';
import { AuthModal } from '@/components/AuthModal';
import { ThemeProvider } from '@/components/theme-provider';
import { Toaster } from '@/components/ui/sonner';
import { ModalProvider } from '@/context/ActionConfirmationContext';
import { AuthModalProvider } from '@/context/AuthModalContext';
import { AuthProvider } from '@/context/AuthContext';
import { BrandThemeProvider } from '@/context/BrandThemeContext';
import { ChatProvider } from '@/context/ChatContext';
import { ModuleProvider } from '@/context/ModuleContext';
import { GoogleOAuthProvider } from '@react-oauth/google';

export function Providers({ children }: { children: ReactNode }) {
  const GOOGLE_CLIENT_ID = process.env.NEXT_PUBLIC_GOOGLE_CLIENT_ID || '';

  useEffect(() => {
    const measurementId = process.env.NEXT_PUBLIC_GA_MEASUREMENT_ID;
    if (measurementId) ReactGA.initialize(measurementId);
  }, []);

  const [queryClient] = useState(
    () =>
      new QueryClient({
        defaultOptions: {
          queries: {
            throwOnError: false,
            refetchOnWindowFocus: false,
            refetchOnMount: false,
            retry: 1,
          },
        },
      })
  );

  return (
    <BrandThemeProvider>
      <ThemeProvider attribute="class" defaultTheme="light" enableSystem={false}>
        <QueryClientProvider client={queryClient}>
          <GoogleOAuthProvider clientId={GOOGLE_CLIENT_ID}>
            <ModalProvider>
              <AuthProvider>
                <AuthModalProvider>
                  <ModuleProvider>
                    <ChatProvider>
                      {children}
                      <AuthModal />
                    </ChatProvider>
                  </ModuleProvider>
                </AuthModalProvider>
              </AuthProvider>
              <ActionConfirmationModal />
              <Toaster position="top-center" />
            </ModalProvider>
          </GoogleOAuthProvider>
        </QueryClientProvider>
      </ThemeProvider>
    </BrandThemeProvider>
  );
}
