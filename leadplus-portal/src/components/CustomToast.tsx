'use client';

import { Info } from 'lucide-react';
import { toast } from 'sonner';

import { Button } from '@/components/ui/button';

type CustomToastProps = {
  message: string;
  toastId: string | number;
};

const CustomToast = ({ message, toastId }: CustomToastProps) => (
  <div className="border-border max-w-md rounded-lg border bg-white p-4 shadow-lg">
    <div className="mb-3 flex items-start gap-3">
      <Info className="text-primary mt-0.5 h-6 w-6 shrink-0" />
      <div className="flex-1">
        <h3 className="text-foreground mb-1 text-lg font-semibold">Coming Soon!</h3>
        <p className="text-muted-foreground text-sm">{message}</p>
      </div>
    </div>
    <div className="flex justify-end">
      <Button
        className="bg-primary text-white hover:bg-sky-600"
        onClick={() => toast.dismiss(toastId)}
      >
        Dismiss
      </Button>
    </div>
  </div>
);

export const showComingSoonToast = (message: string) => {
  toast.custom((t) => <CustomToast message={message} toastId={t} />, { duration: 5000 });
};
