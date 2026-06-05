'use client';

import { ReactNode } from 'react';

type EmptyStateCardProps = {
  icon: ReactNode;
  message: string;
};

export const EmptyStateCard = ({ icon, message }: EmptyStateCardProps) => (
  <div className="bg-card border border-border rounded-lg p-12">
    <div className="flex flex-col items-center gap-3 text-center">
      {icon}
      <p className="text-muted-foreground text-base">{message}</p>
    </div>
  </div>
);
