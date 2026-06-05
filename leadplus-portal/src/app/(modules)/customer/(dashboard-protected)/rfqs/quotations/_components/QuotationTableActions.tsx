import { MouseEvent } from 'react';

import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { CustomerQuotation } from '@/types/customer-quotation.types';
import { CheckCircle2, Download } from 'lucide-react';

type QuotationTableActionsProps = {
  data: CustomerQuotation;
  isAccepted?: boolean;
  onAccept?: (id: string, e: MouseEvent) => void;
  onDownload?: (quote: CustomerQuotation, e: MouseEvent) => void;
};

const QuotationTableActions = ({
  data,
  isAccepted = false,
  onAccept,
  onDownload,
}: QuotationTableActionsProps) => {
  return (
    <div className="flex items-center justify-end gap-2">
      {isAccepted ? (
        <Badge variant="default" className="border-0 bg-green-500 text-white hover:bg-green-500">
          <CheckCircle2 className="mr-1.5 h-3.5 w-3.5" />
          Accepted
        </Badge>
      ) : (
        <Button
          size="sm"
          onClick={(e) => onAccept?.(data.id, e)}
          className="bg-primary hover:bg-primary/90 text-primary-foreground"
          style={{ fontSize: '12px' }}
        >
          Accept
        </Button>
      )}
      <Button
        variant="outline"
        size="sm"
        className="border-border"
        onClick={(e) => onDownload?.(data, e)}
      >
        <Download className="h-3.5 w-3.5" />
      </Button>
    </div>
  );
};

export { QuotationTableActions };
