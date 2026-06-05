import { useMemo } from 'react';

import { Label } from '@/components/ui/label';
import { convertDate } from '@/lib/utils/timeConversion';
import { CustomerQuotationResponse } from '@/types/quotation.types';

import { Building, Calendar, DollarSign, FileCode, Mail, Phone, User } from 'lucide-react';
import { BUDGETS } from '@/constants/request-for-quote.constants';

type RFQProjectDetailsPanelProps = {
  rfq: CustomerQuotationResponse;
};

const ICON_CLASSES = {
  base: 'h-4 w-4',
  textMuted: 'text-muted-foreground h-4 w-4',
  success: 'text-success h-4 w-4',
  destructive: 'text-destructive h-4 w-4',
  primary: 'text-primary h-5 w-5',
} as const;

const InfoItem = ({
  icon: Icon,
  label,
  value,
  iconClassName = ICON_CLASSES.textMuted,
}: {
  icon: React.ElementType;
  label: string;
  value: string | number;
  iconClassName?: string;
}) => (
  <div>
    <p className="text-muted-foreground mb-1 text-sm">{label}</p>
    <p className="text-foreground flex items-center gap-2 text-sm font-medium">
      <Icon className={iconClassName} />
      {value}
    </p>
  </div>
);

const CustomerInfo = ({ rfq }: { rfq: CustomerQuotationResponse }) => {
  const customerDetails = useMemo(
    () => [
      { icon: User, label: 'Customer Name', value: rfq.customerName },
      { icon: Building, label: 'Company', value: rfq.customerCompanyName },
      { icon: Mail, label: 'Email', value: rfq.customerEmail },
      { icon: Phone, label: 'Phone', value: rfq.customerPhoneNumber },
    ],
    [rfq]
  );

  return (
    <div className="border-border border-t pt-4">
      <div className="space-y-3">
        {customerDetails.map(({ icon, label, value }) => (
          <InfoItem key={label} icon={icon} label={label} value={value} />
        ))}
      </div>
    </div>
  );
};

const RFQProjectDetailsPanel = ({ rfq }: RFQProjectDetailsPanelProps) => {
  const formattedDeadline = useMemo(() => convertDate(new Date(rfq.deadline)), [rfq.deadline]);
  return (
    <div className="bg-card border-border sticky top-0 rounded-lg border p-4">
      <h2 className="text-foreground mb-4 flex items-center gap-2 text-xl font-bold">
        <FileCode className={ICON_CLASSES.primary} />
        Project Requirements
      </h2>
      <div className="space-y-6">
        <div>
          <Label className="text-muted-foreground mb-1 block text-sm font-medium">
            Project Title
          </Label>
          <p className="text-foreground text-base font-semibold">{rfq.title}</p>
        </div>
        <CustomerInfo rfq={rfq} />
        <div className="border-border border-t pt-4">
          <Label className="text-muted-foreground mb-2 block text-sm font-medium">
            Project Description
          </Label>
          <p className="text-foreground text-sm leading-relaxed">{rfq.description}</p>
        </div>
        <div className="border-border border-t pt-4">
          <div className="space-y-3">
            <div>
              <Label className="text-muted-foreground mb-1 block text-sm font-medium">
                Budget Range
              </Label>
              <p className="text-foreground flex items-center gap-2 text-sm font-semibold">
                <DollarSign className={ICON_CLASSES.success} />
                {BUDGETS.find((b) => b.value === rfq.budget)?.label ?? 'N/A'}
              </p>
            </div>
            <div>
              <Label className="text-muted-foreground mb-1 block text-sm font-medium">
                Deadline
              </Label>
              <p className="text-foreground flex items-center gap-2 text-sm font-semibold">
                <Calendar className={ICON_CLASSES.destructive} />
                {formattedDeadline}
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export { RFQProjectDetailsPanel };
