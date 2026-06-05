import { Button } from '@/components/ui/button';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import { Agreement } from '@/types/agreements.types';
import { Edit, MoreHorizontal } from 'lucide-react';
import { useRouter, useSearchParams } from 'next/navigation';

type DocumentTableActionsProps = {
  data: Agreement;
};

const DocumentTableActions = ({ data }: DocumentTableActionsProps) => {
  const router = useRouter();
  const searchParams = useSearchParams();

  const handleEdit = () => {
    const params = new URLSearchParams(searchParams.toString());
    params.set('id', data.id);
    router.push(`?${params.toString()}`);
  };

  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild disabled={!data.latest}>
        <Button variant="ghost" className="text-foreground hover:text-foreground h-8 w-8 p-0">
          <span className="sr-only">Open menu</span>
          <MoreHorizontal className="h-4 w-4" />
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent align="end">
        <DropdownMenuItem onClick={handleEdit}>
          <Edit className="mr-2 h-4 w-4" /> Edit
        </DropdownMenuItem>
      </DropdownMenuContent>
    </DropdownMenu>
  );
};

export { DocumentTableActions };
