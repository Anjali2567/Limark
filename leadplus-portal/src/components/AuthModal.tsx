'use client';

import { useState } from 'react';
import { toast } from 'sonner';
import { AxiosError } from 'axios';

import { useAuth } from '@/context/AuthContext';
import { useAuthModal } from '@/context/AuthModalContext';
import { useGoogleAuth } from '@/hooks/useAuthentication';
import { TokenResponse } from '@/types/auth.types';
import { SignInForm } from './auth/SignInForm';
import { SignUpForm } from './auth/SignUpForm';
import { Dialog, DialogContent, DialogTitle } from './ui/dialog';
import { ScrollArea } from './ui/scroll-area';
import { AuthType } from '@/constants/Auth';

import { CheckCircle2, LogIn } from 'lucide-react';

export function AuthModal() {
  const { isOpen, closeAuthModal } = useAuthModal();
  const { login: saveAuth } = useAuth();

  const [mode, setMode] = useState<AuthType.SIGN_IN | AuthType.SIGN_UP>(AuthType.SIGN_IN);

  const { mutate: googleAuthMutate } = useGoogleAuth();

  const handleGoogleAuth = (token: string) => {
    googleAuthMutate(
      { token },
      {
        onSuccess: (data) => {
          saveAuth(data);
          closeAuthModal();
          toast.success('Signed in with Google successfully!');
        },
        onError: (error: AxiosError) => {
          toast.error(error.message || 'Failed to sign in with Google. Please try again.');
        },
      }
    );
  };

  const handleAuthSuccess = (data: TokenResponse) => {
    saveAuth(data);
    closeAuthModal();
  };

  const currentTitle = mode === AuthType.SIGN_IN ? 'Welcome Back' : 'Create Your Account';
  const currentSubtitle =
    mode === AuthType.SIGN_IN ? 'Sign in to continue' : 'Start your journey • Fast & Free';

  return (
    <Dialog open={isOpen} onOpenChange={closeAuthModal}>
      <DialogTitle className="sr-only">Authentication</DialogTitle>
      <DialogContent className="max-h-[90vh] max-w-115 overflow-hidden p-0">
        <div className="border-border border-b py-4 text-center">
          <div className="mx-auto flex h-12 w-12 items-center justify-center rounded-full bg-sky-500/10">
            {mode === AuthType.SIGN_IN ? (
              <LogIn className="text-primary h-6 w-6" />
            ) : (
              <CheckCircle2 className="text-primary h-6 w-6" />
            )}
          </div>
          <h2 className="text-foreground mb-2 font-sans text-2xl font-bold">{currentTitle}</h2>
          <p className="text-muted-foreground font-sans text-sm">{currentSubtitle}</p>
        </div>
        <ScrollArea className="max-h-[calc(90vh-140px)]">
          <div className="space-y-5 px-8 py-6">
            {mode === AuthType.SIGN_IN ? (
              <SignInForm
                onSuccess={handleAuthSuccess}
                onGoogleAuth={handleGoogleAuth}
                onSwitchToSignUp={() => setMode(AuthType.SIGN_UP)}
              />
            ) : (
              <SignUpForm
                onSuccess={handleAuthSuccess}
                onGoogleAuth={handleGoogleAuth}
                onSwitchToSignIn={() => setMode(AuthType.SIGN_IN)}
              />
            )}
          </div>
        </ScrollArea>
      </DialogContent>
    </Dialog>
  );
}
