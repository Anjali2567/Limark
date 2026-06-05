'use client';

import { EmptyStateCard } from './EmptyStateCard';

import { Award, CheckCircle } from 'lucide-react';

type CertificationsTabProps = {
  certifications: string[];
};

export const CertificationsTab = ({ certifications }: CertificationsTabProps) => {
  if (!certifications || certifications.length === 0) {
    return (
      <EmptyStateCard
        icon={<Award className="text-muted-foreground h-12 w-12" />}
        message="No certifications listed"
      />
    );
  }

  return (
    <div>
      <h2 className="text-foreground mb-6 text-[24px] font-bold">Certifications</h2>
      <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
        {certifications.map((cert, idx) => (
          <div
            key={idx}
            className="bg-card border-border flex items-start gap-3 rounded-lg border p-5"
          >
            <div className="flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-green-100">
              <CheckCircle className="h-4 w-4 text-green-600" />
            </div>
            <div>
              <p className="text-foreground font-semibold">{cert}</p>
              <p className="text-muted-foreground text-sm">Certified</p>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};
