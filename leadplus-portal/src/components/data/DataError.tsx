import { AlertTriangle } from 'lucide-react';
import { Button } from '../ui/button';
import { useRouter } from 'next/navigation';

type DataErrorProps = {
  message?: string;
};

const DataError = ({ message }: DataErrorProps) => {
  const router = useRouter();

  const onGoBack = () => {
    router.back();
  };
  return (
    <div className="absolute inset-0 flex w-full flex-col items-center justify-center space-y-3">
      <AlertTriangle className="mb-3 text-red-600" size={40} />
      <span className="mb-2 text-lg font-semibold text-red-600">
        {message || 'Error loading data'}
      </span>
      <span className="text-muted-foreground text-sm">Please try again later.</span>
      <Button variant="link" onClick={onGoBack}>
        Go Back
      </Button>
    </div>
  );
};

export { DataError };
