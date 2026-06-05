"use client";

import { FC } from "react";
import { cn } from "@/lib/utils/helpers";

type LoadingIndicatorProps = {
  className?: string;
};

const ChatLoadingIndicator: FC<LoadingIndicatorProps> = ({ className }) => {
  return (
    <div
      className={cn(
        className,
        "flex w-fit items-center space-x-2 rounded-lg bg-white p-2"
      )}
    >
      <p
        className="h-2 w-2 animate-pulse rounded-full bg-blue-500"
        style={{ animationDelay: "-0.6s" }}
      ></p>
      <p
        className="h-2 w-2 animate-pulse rounded-full bg-blue-500"
        style={{ animationDelay: "-0.3s" }}
      ></p>
      <p className="h-2 w-2 animate-pulse rounded-full bg-blue-500"></p>
    </div>
  );
};

export { ChatLoadingIndicator };
