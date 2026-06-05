import { Loader2 } from 'lucide-react';

const DataLoader = () => {
  return (
    <div className="absolute inset-0 flex w-full flex-col items-center justify-center">
      <Loader2 className="text-primary mb-3 animate-spin" size={40} />
      <span className="text-muted-foreground text-base font-medium">Loading...</span>
    </div>
  );
};

export { DataLoader };
