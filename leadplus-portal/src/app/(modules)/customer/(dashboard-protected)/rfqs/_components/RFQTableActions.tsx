import { Button } from '@/components/ui/button';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import { RequestForQuoteWithQuoteCount } from '@/types/request-for-quote.types';
import { Eye, MoreVertical, Trash2 } from 'lucide-react';
import { useDeleteRequestForQuote } from '@/hooks/useRequestForQuote';
import { toast } from 'sonner';
import { useModal } from '@/hooks/useModal';
import { MouseEvent } from 'react';

type RFQTableActionsProps = {
  data: RequestForQuoteWithQuoteCount;
  onView: (rfq: RequestForQuoteWithQuoteCount) => void;
};

const RFQTableActions = ({ data, onView }: RFQTableActionsProps) => {
  const { renderModal: showConfirmation } = useModal();
  const { mutate: deleteRFQ, isPending } = useDeleteRequestForQuote();

  const handleDelete = (e: MouseEvent) => {
    e.stopPropagation();
    showConfirmation({
      title: 'Delete RFQ',
      message: `Are you sure you want to delete "${data.title}"? This action cannot be undone.`,
      submitButtonText: 'Delete',
      cancelButtonText: 'Cancel',
      type: 'error',
      onConfirm: () => {
        deleteRFQ(data.id, {
          onSuccess: () => {
            toast.success('RFQ deleted successfully');
          },
          onError: (error) => {
            toast.error(`Failed to delete RFQ: ${error.message}`);
          },
        });
      },
    });
  };

  const handleView = (e: React.MouseEvent) => {
    e.stopPropagation();
    onView(data);
  };

  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button
          variant="ghost"
          className="h-8 w-8 p-0"
          disabled={isPending}
          onClick={(e) => e.stopPropagation()}
        >
          <span className="sr-only">Open menu</span>
          <MoreVertical className="h-4 w-4" />
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent align="end" className="w-40">
        <DropdownMenuItem onClick={handleView} className="cursor-pointer">
          <Eye className="mr-2 h-4 w-4" />
          View Details
        </DropdownMenuItem>
        <DropdownMenuItem
          onClick={handleDelete}
          className="text-destructive focus:text-destructive cursor-pointer"
          disabled={isPending}
        >
          <Trash2 className="mr-2 h-4 w-4" />
          Delete
        </DropdownMenuItem>
      </DropdownMenuContent>
    </DropdownMenu>
  );
};

export { RFQTableActions };
