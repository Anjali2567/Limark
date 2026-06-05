'use client';

import { CheckCircle } from 'lucide-react';

type CertificationsSidebarCardProps = {
  certifications: string[];
};

const CertificationsSidebarCard = ({ certifications }: CertificationsSidebarCardProps) => {
  if (!certifications || certifications.length === 0) return null;

  return (
    <div className="bg-card border-border rounded-xl border p-6">
      <h3 className="text-muted-foreground mb-4 text-sm font-semibold tracking-wider uppercase">
        Certifications
      </h3>
      <div className="space-y-3">
        {certifications.map((cert, idx) => (
          <div key={idx} className="flex items-start gap-2">
            <div className="mt-0.5 flex h-5 w-5 shrink-0 items-center justify-center rounded-full bg-green-100">
              <CheckCircle className="h-3 w-3 text-green-600" />
            </div>
            <div className="flex-1">
              <p className="text-foreground text-sm font-medium">{cert}</p>
              <p className="text-muted-foreground text-xs">Certified</p>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export { CertificationsSidebarCard };
