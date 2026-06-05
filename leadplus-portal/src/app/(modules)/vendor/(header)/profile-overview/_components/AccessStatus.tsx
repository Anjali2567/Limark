import { ReactNode } from 'react';

import { Mail, LucideIcon } from 'lucide-react';

interface AccessStatusProps {
  type: 'pending' | 'rejected';
  title: string;
  subtitle: string;
  icon: LucideIcon;
  iconBgColor: string;
  iconColor: string;
  description: ReactNode;
  actions?: ReactNode;
  nextSteps: {
    title: string;
    items: string[];
    borderColor: string;
    bgColor: string;
  };
}

const AccessStatus = ({
  title,
  subtitle,
  icon: Icon,
  iconBgColor,
  iconColor,
  description,
  actions,
  nextSteps,
}: AccessStatusProps) => {
  return (
    <div className="flex w-full max-w-2xl flex-col items-center justify-center space-y-6">
      <div>
        <h1 className="text-foreground mb-2 text-4xl font-bold">{title}</h1>
        <h2 className="text-muted-foreground text-2xl font-semibold">{subtitle}</h2>
      </div>

      <div className="bg-card border-border space-y-6 rounded-lg border p-8 text-left shadow-lg">
        <div className="flex justify-center">
          <div className={`${iconBgColor} flex h-20 w-20 items-center justify-center rounded-full`}>
            <Icon className={`${iconColor} h-10 w-10`} aria-hidden="true" />
          </div>
        </div>

        <div className="bg-secondary/50 border-border space-y-4 rounded-lg border p-6">
          <div className="flex items-start gap-3">
            <Mail className="mt-0.5 h-5 w-5 shrink-0 text-sky-500" aria-hidden="true" />
            <div className="space-y-2">{description}</div>
          </div>
        </div>

        <div className="flex flex-col space-y-4">
          <div className={`border-l-4 p-4 ${nextSteps.borderColor} ${nextSteps.bgColor}`}>
            <h3 className="text-foreground mb-1 text-sm font-semibold">{nextSteps.title}</h3>
            <ul className="text-muted-foreground ml-4 list-disc space-y-1 text-sm">
              {nextSteps.items.map((item, index) => (
                <li key={index}>{item}</li>
              ))}
            </ul>
          </div>
          {actions}
        </div>
      </div>

      <p className="text-muted-foreground mt-6 text-center text-sm">
        Questions or concerns? Reach out to{' '}
        <a
          href="mailto:support@leadplus.com"
          className="font-medium text-sky-500 hover:text-sky-600"
        >
          support@leadplus.com
        </a>
      </p>
    </div>
  );
};

export { AccessStatus };
