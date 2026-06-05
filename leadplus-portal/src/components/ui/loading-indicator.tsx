import { FC } from "react";

interface LoadingIndicatorProps {
  isLoading?: boolean;
}

export const LoadingIndicator: FC<LoadingIndicatorProps> = ({
  isLoading = false,
}) => {
  if (!isLoading) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/10 backdrop-blur-xs">
      <div>
        <div className="relative h-16 w-16">
          <div className="absolute inset-0 rounded-full border-4 border-gray-200"></div>
          <div className="absolute inset-0 animate-spin rounded-full border-4 border-primary border-t-transparent"></div>
        </div>
      </div>
    </div>
  );
};
