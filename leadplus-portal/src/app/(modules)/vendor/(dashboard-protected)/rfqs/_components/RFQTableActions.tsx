import { MouseEvent } from 'react';

import { Button } from '@/components/ui/button';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import { CustomerQuotationResponse } from '@/types/quotation.types';

import { CheckCircle2, Eye, MoreVertical, XCircle } from 'lucide-react';

type RFQTableActionsProps = {
  data: CustomerQuotationResponse;
  onView: (rfq: CustomerQuotationResponse) => void;
  onAccept: (rfq: string) => void;
  onReject: (rfq: string) => void;
};

const RFQTableActions = ({ data, onView, onAccept, onReject }: RFQTableActionsProps) => {
  const handleView = (e: MouseEvent) => {
    e.stopPropagation();
    onView(data);
  };

  const handleAccept = (e: MouseEvent) => {
    e.stopPropagation();
    onAccept(data.id);
  };

  const handleReject = (e: MouseEvent) => {
    e.stopPropagation();
    onReject(data.id);
  };

  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button variant="ghost" className="h-8 w-8 p-0" onClick={(e) => e.stopPropagation()}>
          <span className="sr-only">Open menu</span>
          <MoreVertical className="h-4 w-4" />
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent align="end" className="w-40">
        <DropdownMenuItem onClick={handleView} className="cursor-pointer">
          <Eye className="mr-2 h-4 w-4" />
          View Details
        </DropdownMenuItem>
        <DropdownMenuSeparator />
        <DropdownMenuItem
          onClick={handleAccept}
          className="text-success focus:text-success cursor-pointer disabled:cursor-not-allowed disabled:opacity-50"
        >
          <CheckCircle2 className="mr-2 h-4 w-4" />
          Accept RFQ
        </DropdownMenuItem>
        <DropdownMenuItem
          onClick={handleReject}
          className="text-destructive focus:text-destructive cursor-pointer disabled:cursor-not-allowed disabled:opacity-50"
        >
          <XCircle className="mr-2 h-4 w-4" />
          Reject RFQ
        </DropdownMenuItem>
      </DropdownMenuContent>
    </DropdownMenu>
  );
};

export { RFQTableActions };
