'use client';

import { useEffect } from 'react';
import { Button } from '@/components/ui/button';
import { AlertCircle } from 'lucide-react';

export default function Error({ error }: { error: Error & { digest?: string } }) {
  useEffect(() => {
    console.error('Unhandled Error:', error);
  }, [error]);

  return (
    <div className="flex min-h-screen flex-col items-center justify-center p-4 text-center">
      <div className="bg-destructive/10 mb-6 rounded-full p-4">
        <AlertCircle className="text-destructive h-12 w-12" />
      </div>
      <h1 className="mb-2 text-4xl font-bold tracking-tight">Something went wrong!</h1>
      <p className="text-muted-foreground mb-8 max-w-md text-lg">
        We apologize for the inconvenience. An unexpected error occurred.
      </p>
      <Button variant="outline" onClick={() => (window.location.href = '/')}>
        Go to Home
      </Button>
    </div>
  );
}
