import { AxiosError } from 'axios';
import { useRouter, useSearchParams } from 'next/navigation';
import { useCallback, useMemo } from 'react';
import { toast } from 'sonner';

import {
  Sheet,
  SheetContent,
  SheetDescription,
  SheetHeader,
  SheetTitle,
} from '@/components/ui/sheet';
import { useGetIndustryById, useUpsertIndustry } from '@/hooks/useIndustry';
import { IndustryForm, IndustryFormValues } from './IndustryForm';

import { Loader2 } from 'lucide-react';

const IndustryDrawer = () => {
  const router = useRouter();
  const searchParams = useSearchParams();
  const industryId = searchParams.get('id');

  const { mutate: upsertIndustry, isPending: isSubmitting } = useUpsertIndustry();
  const { data: industryData, isLoading } = useGetIndustryById(industryId || '');
  const isEditMode = !!industryId;
  const isDrawerOpen = useMemo(() => {
    return !!(industryId || searchParams.get('action') === 'add');
  }, [industryId, searchParams]);

  const handleCloseDrawer = useCallback(() => {
    const params = new URLSearchParams(searchParams.toString());
    params.delete('id');
    params.delete('action');
    router.push(`?${params.toString()}`);
  }, [router, searchParams]);

  const onSubmit = useCallback(
    (payload: IndustryFormValues) => {
      upsertIndustry(
        {
          id: industryId || '',
          ...payload,
          imageFile: payload.imageFile || '',
        },
        {
          onSuccess: () => {
            toast.success(`Industry ${isEditMode ? 'updated' : 'created'} successfully`);
            handleCloseDrawer();
          },
          onError: (error: AxiosError) => {
            toast.error(`Failed to ${isEditMode ? 'update' : 'create'} industry; ${error.message}`);
          },
        }
      );
    },
    [handleCloseDrawer, industryId, isEditMode, upsertIndustry]
  );

  return (
    <div>
      <Sheet
        open={isDrawerOpen}
        onOpenChange={(open) => {
          if (!open) handleCloseDrawer();
        }}
      >
        <SheetContent className="flex w-full flex-col gap-0 sm:max-w-xl" side="right">
          <SheetHeader className="shrink-0">
            <div className="flex items-center justify-between">
              <SheetTitle className="text-lg">
                {isEditMode ? 'Edit Industry' : 'Add New Industry'}
              </SheetTitle>
            </div>
            <SheetDescription>
              <span className="text-muted-foreground text-sm">
                {isEditMode
                  ? 'Update industry details below.'
                  : 'Create a new industry category for organizing vendors and leads.'}
              </span>
            </SheetDescription>
          </SheetHeader>
          {isLoading ? (
            <div className="flex flex-1 items-center justify-center p-4">
              <Loader2 className="h-10 w-10 animate-spin text-sky-400" />
            </div>
          ) : (
            <IndustryForm
              industryData={industryData}
              onSubmit={onSubmit}
              onCancel={handleCloseDrawer}
              isEditMode={isEditMode}
              isSubmitting={isSubmitting}
            />
          )}
        </SheetContent>
      </Sheet>
    </div>
  );
};

export { IndustryDrawer };
