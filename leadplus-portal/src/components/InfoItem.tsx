import { Label } from './ui/label';

const InfoItem = ({
  label,
  icon: Icon,
  value,
  valueClassName = '',
  iconClassName = 'text-muted-foreground',
}: {
  label: string;
  icon: React.ElementType;
  value: React.ReactNode;
  valueClassName?: string;
  iconClassName?: string;
}) => (
  <div>
    <Label className="text-muted-foreground mb-2 block text-sm">{label}</Label>
    <div className="flex items-center gap-2">
      <Icon className={`h-4 w-4 ${iconClassName}`} />
      <span className={`text-sm font-medium ${valueClassName}`}>{value}</span>
    </div>
  </div>
);

export { InfoItem };
