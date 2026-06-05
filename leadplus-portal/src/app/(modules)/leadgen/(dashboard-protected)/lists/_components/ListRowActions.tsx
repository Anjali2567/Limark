import { Button } from '@/components/ui/button';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import { LeadList } from '@/types/lead-list.types';

import { ExternalLink, MoreVertical, Trash2 } from 'lucide-react';

type ListRowActionsProps = {
  list: LeadList;
  onView: (id: string) => void;
  onDelete: (list: LeadList) => void;
};

const ListRowActions = ({ list, onView, onDelete }: ListRowActionsProps) => {
  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button
          variant="ghost"
          size="icon"
          className="h-8 w-8"
          onClick={(e) => e.stopPropagation()}
        >
          <span className="sr-only">Open menu</span>
          <MoreVertical className="h-4 w-4" />
        </Button>
      </DropdownMenuTrigger>

      <DropdownMenuContent align="end">
        <DropdownMenuItem
          className="cursor-pointer"
          onClick={(e) => {
            e.stopPropagation();
            onView(list.id);
          }}
        >
          <ExternalLink className="mr-2 h-4 w-4" />
          View list
        </DropdownMenuItem>

        <DropdownMenuItem
          className="text-destructive focus:text-destructive cursor-pointer"
          onClick={(e) => {
            e.stopPropagation();
            onDelete(list);
          }}
        >
          <Trash2 className="mr-2 h-4 w-4" />
          Delete list
        </DropdownMenuItem>
      </DropdownMenuContent>
    </DropdownMenu>
  );
};

export { ListRowActions };
