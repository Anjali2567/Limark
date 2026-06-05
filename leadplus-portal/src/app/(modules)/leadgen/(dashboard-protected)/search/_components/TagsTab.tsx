import { Badge } from '@/components/ui/badge';
import { formatString } from '@/lib/utils/formatter';

type Props = {
  items: string[];
  emptyMessage: string;
};

export const TagsTab = ({ items, emptyMessage }: Props) => {
  if (!items || items.length === 0) {
    return <p className="text-muted-foreground text-sm">{emptyMessage}</p>;
  }

  return (
    <div className="flex flex-wrap gap-1.5">
      {items.map((item, i) => (
        <Badge key={i} variant="secondary" className="rounded-full text-xs">
          {formatString(item)}
        </Badge>
      ))}
    </div>
  );
};
