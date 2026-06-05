import { toast } from 'sonner';

import { Button } from '@/components/ui/button';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import { useAuth } from '@/context/AuthContext';
import { useModal } from '@/hooks/useModal';
import { useDeleteAnnouncement } from '@/hooks/useTenant';
import { TenantAnnouncement } from '@/types/tenant.types';
import { CreateAnnouncementDialog } from './CreateAnnouncementDialog';
import useToggle from '@/hooks/useToggle';

import { MoreHorizontal, Pencil, Trash2 } from 'lucide-react';

type AnnouncementTableActionsProps = {
  announcement: TenantAnnouncement;
};

const AnnouncementTableActions = ({ announcement }: AnnouncementTableActionsProps) => {
  const { authenticatedUserDetails } = useAuth();
  const tenantId = authenticatedUserDetails?.tenantId || '';

  const { renderModal } = useModal();
  const { mutate: deleteAnnouncement } = useDeleteAnnouncement();

  const { value: editOpen, setValue: setEditOpen } = useToggle(false);

  const handleDelete = () => {
    renderModal({
      type: 'error',
      title: 'Delete Announcement',
      message: `Are you sure you want to delete "${announcement.name}"? This action cannot be undone.`,
      submitButtonText: 'Delete',
      onConfirm: () => {
        deleteAnnouncement(
          { tenantId, announcementId: announcement.id },
          {
            onSuccess: () => toast.success('Announcement deleted'),
            onError: () => toast.error('Failed to delete announcement'),
          }
        );
      },
    });
  };

  return (
    <div onClick={(e) => e.stopPropagation()}>
      <DropdownMenu>
        <DropdownMenuTrigger asChild>
          <Button
            variant="ghost"
            className="text-muted-foreground hover:text-foreground h-8 w-8 p-0"
          >
            <span className="sr-only">Open menu</span>
            <MoreHorizontal className="h-4 w-4" />
          </Button>
        </DropdownMenuTrigger>
        <DropdownMenuContent align="end">
          <DropdownMenuItem onClick={() => setEditOpen(true)}>
            <Pencil className="mr-2 h-4 w-4" />
            Edit
          </DropdownMenuItem>
          <DropdownMenuItem
            className="text-destructive focus:text-destructive"
            onClick={handleDelete}
          >
            <Trash2 className="mr-2 h-4 w-4" />
            Delete
          </DropdownMenuItem>
        </DropdownMenuContent>
      </DropdownMenu>
      <CreateAnnouncementDialog
        open={editOpen}
        onOpenChange={setEditOpen}
        editAnnouncement={announcement}
      />
    </div>
  );
};

export { AnnouncementTableActions };
