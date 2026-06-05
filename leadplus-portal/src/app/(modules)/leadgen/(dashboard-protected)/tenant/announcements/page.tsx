'use client';

import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import useToggle from '@/hooks/useToggle';
import { AnnouncementTable } from './_components/AnnouncementTable';
import { CreateAnnouncementDialog } from './_components/CreateAnnouncementDialog';

import { Plus } from 'lucide-react';

const TenantAnnouncementsPage = () => {
  const { value: dialogOpen, toggle: toggleDialog } = useToggle(false);

  return (
    <div className="animate-in fade-in slide-in-from-bottom-4 space-y-6 p-8 duration-500">
      <div className="flex flex-col justify-between md:flex-row md:items-start">
        <div>
          <div className="flex items-center gap-3">
            <h1 className="text-foreground text-3xl font-bold">Announcements</h1>
            <Badge variant="outline" className="border-amber-200 bg-amber-50 text-amber-700">
              Mass Broadcast
            </Badge>
          </div>
          <p className="text-muted-foreground">
            Send mass broadcast emails to targeted contacts with no daily sending limits.
          </p>
        </div>
        <Button onClick={toggleDialog} className="mt-4 shrink-0 md:mt-0">
          <Plus className="h-4 w-4" />
          New Announcement
        </Button>
      </div>
      <AnnouncementTable />
      <CreateAnnouncementDialog open={dialogOpen} onOpenChange={toggleDialog} />
    </div>
  );
};

export default TenantAnnouncementsPage;
