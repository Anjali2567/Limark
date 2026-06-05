import { useRouter } from 'next/navigation';

import { Button } from '@/components/ui/button';
import {
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from '@/components/ui/card';
import { appRoutes } from '@/config/routes';

import { CheckCircle } from 'lucide-react';

type PasswordResetSentViewProps = {
  forgotEmail: string;
  onBack: () => void;
};

const PasswordResetSentView = ({ forgotEmail, onBack }: PasswordResetSentViewProps) => {
  const router = useRouter();

  const onBackToLogin = () => {
    router.push(appRoutes.auth.login);
  };
  return (
    <>
      <CardHeader className="flex flex-col items-center space-y-4 p-8 pb-2 text-center">
        <div className="mb-2 flex h-16 w-16 items-center justify-center rounded-full bg-green-100">
          <CheckCircle className="h-8 w-8 text-green-600" />
        </div>
        <div>
          <CardTitle className="text-2xl font-bold text-slate-900">Check Your Email</CardTitle>
          <CardDescription className="mt-2 text-slate-600">
            We&apos;ve sent a password reset link to{' '}
            <span className="font-semibold text-slate-900">{forgotEmail}</span>.
          </CardDescription>
        </div>
      </CardHeader>
      <CardContent className="space-y-4 p-8 pt-0 text-center">
        <p className="text-sm text-slate-500">
          Did not receive the email? Check your spam folder or try another email address.
        </p>
      </CardContent>
      <CardFooter className="flex flex-col space-y-4 rounded-b-lg border-t border-slate-100 bg-slate-50/50 p-8">
        <Button
          className="bg-primary h-11.5 w-full font-semibold text-white shadow-md transition-all hover:shadow-lg"
          onClick={onBackToLogin}
        >
          Back to Login
        </Button>
        <Button
          variant="ghost"
          onClick={onBack}
          className="hover:text-primary w-full text-sm text-slate-500"
        >
          Try a different email
        </Button>
      </CardFooter>
    </>
  );
};

export { PasswordResetSentView };
