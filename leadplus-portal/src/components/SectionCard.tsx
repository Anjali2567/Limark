import { ReactNode } from 'react';

interface SectionCardProps {
  title: string;
  children: ReactNode;
  className?: string;
}

const SectionCard = ({ title, children, className = '' }: SectionCardProps) => {
  return (
    <div
      className={`bg-card border-border overflow-hidden rounded-lg border shadow-sm ${className}`}
    >
      <div className="border-border bg-secondary/30 border-b px-8 py-6">
        <h2 className="text-foreground text-xl font-bold">{title}</h2>
      </div>
      <div className="px-8 py-2">
        <div className="grid grid-cols-2 gap-x-12">{children}</div>
      </div>
    </div>
  );
};

export { SectionCard };
