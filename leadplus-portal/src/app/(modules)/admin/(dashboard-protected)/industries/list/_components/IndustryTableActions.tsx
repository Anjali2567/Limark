import { Button } from '@/components/ui/button';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import { Industry } from '@/types/industry.types';
import { CheckCircle, Edit, MoreHorizontal, XCircle } from 'lucide-react';
import { useCallback } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import { useModal } from '@/hooks/useModal';
import { useToggleIndustryStatus } from '@/hooks/useIndustry';
import { toast } from 'sonner';

type IndustryTableActionsProps = {
  data: Industry;
};
const IndustryTableActions = ({ data }: IndustryTableActionsProps) => {
  const router = useRouter();
  const searchParams = useSearchParams();

  const { renderModal } = useModal();
  const { mutate: toggleIndustryStatus } = useToggleIndustryStatus();

  const handleEditClick = useCallback(() => {
    const params = new URLSearchParams(searchParams.toString());
    params.set('id', data.id);
    router.push(`?${params.toString()}`);
  }, [data, router, searchParams]);

  const handleToggleStatus = useCallback(() => {
    const status = data.disabled;
    const industry = data;
    renderModal({
      type: status ? 'success' : 'error',
      title: status ? 'Enable Industry' : 'Disable Industry',
      message: `Are you sure you want to ${status ? 'enable' : 'disable'} this Industry?`,
      onConfirm: () => {
        toggleIndustryStatus(
          {
            id: industry.id,
            active: status,
          },
          {
            onSuccess: () => {
              toast.success(`Industry ${status ? 'enabled' : 'disabled'} successfully`);
            },
          }
        );
      },
    });
  }, [data, renderModal, toggleIndustryStatus]);

  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button variant="ghost" className="text-foreground hover:text-foreground h-8 w-8 p-0">
          <span className="sr-only">Open menu</span>
          <MoreHorizontal className="h-4 w-4" />
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent align="end">
        <DropdownMenuItem onClick={handleEditClick}>
          <Edit className="mr-2 h-4 w-4" /> Edit
        </DropdownMenuItem>
        <DropdownMenuItem onClick={handleToggleStatus}>
          {!data.disabled ? (
            <>
              <XCircle className="text-destructive mr-2 h-4 w-4" /> Deactivate
            </>
          ) : (
            <>
              <CheckCircle className="text-success mr-2 h-4 w-4" /> Activate
            </>
          )}
        </DropdownMenuItem>
      </DropdownMenuContent>
    </DropdownMenu>
  );
};

export { IndustryTableActions };
