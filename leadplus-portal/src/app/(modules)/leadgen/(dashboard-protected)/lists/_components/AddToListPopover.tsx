'use client';

import { ReactNode, useState } from 'react';
import { useForm } from 'react-hook-form';
import { toast } from 'sonner';
import { z } from 'zod';

import { Button } from '@/components/ui/button';
import { FormInput } from '@/components/ui/form-input';
import { Input } from '@/components/ui/input';
import { Popover, PopoverContent, PopoverTrigger } from '@/components/ui/popover';
import { LeadType } from '@/constants/leadSearch.constants';
import { useCreateLeadList, useGetLeadLists, useUpdateLeadList } from '@/hooks/useLeadList';
import useToggle from '@/hooks/useToggle';
import { stringValidation } from '@/lib/utils/validations';
import { LeadListSearch } from '@/types/lead-list.types';
import { defaultPaginationParams } from '@/types/paginated-params';
import { zodResolver } from '@hookform/resolvers/zod';

import { Tooltip, TooltipContent, TooltipTrigger } from '@/components/ui/tooltip';
import { ArrowLeft, Loader2, Plus, Search } from 'lucide-react';
import { useDebounce } from '@/hooks/useDebounce';

const SEARCH_DEBOUNCE_DELAY = 300;

const newListSchema = z.object({
  name: stringValidation({ fieldName: 'List name' }),
});

type NewListFormValues = z.infer<typeof newListSchema>;

type BulkModeProps = {
  rowIds: string[];
  disabled?: boolean;
  sourceId?: never;
  trigger?: ReactNode;
};

type SingleRowModeProps = {
  sourceId: string;
  trigger: ReactNode;
  rowIds?: never;
  disabled?: never;
};

type AddToListPopoverProps = {
  tenantId: string;
  workspaceId: string;
  type: LeadType;
  tooltip?: string;
} & (BulkModeProps | SingleRowModeProps);

type ListPopoverContentProps = {
  snapshot: LeadListSearch[];
  lists: LeadListSearch[];
  searchQuery: string;
  onSearchChange: (q: string) => void;
  sourceId?: string;
  rowIds?: string[];
  isSingleRow: boolean;
  tenantId: string;
  workspaceId: string;
  type: LeadType;
  onClose: () => void;
  isLoading: boolean;
};

const ListPopoverContent = ({
  snapshot,
  lists,
  searchQuery,
  onSearchChange,
  sourceId,
  rowIds,
  isSingleRow,
  tenantId,
  workspaceId,
  type,
  onClose,
  isLoading,
}: ListPopoverContentProps) => {
  const [isCreateView, setIsCreateView] = useState(false);
  const [newlyCreatedLists, setNewlyCreatedLists] = useState<LeadListSearch[]>([]);
  const [selectedListIds, setSelectedListIds] = useState<Set<string>>(() =>
    isSingleRow && sourceId
      ? new Set(snapshot.filter((l) => l.sourceIds.includes(sourceId)).map((l) => l.id))
      : new Set()
  );

  const { mutate: createList, isPending: isCreating } = useCreateLeadList();
  const { mutate: updateList } = useUpdateLeadList();

  const {
    register,
    handleSubmit,
    reset: resetCreateForm,
    formState: { errors },
  } = useForm<NewListFormValues>({
    resolver: zodResolver(newListSchema),
    defaultValues: { name: '' },
  });

  const toggleList = (id: string) => {
    setSelectedListIds((prev) => {
      const next = new Set(prev);
      if (next.has(id)) next.delete(id);
      else next.add(id);
      return next;
    });
  };

  const handleAdd = () => {
    if (isSingleRow && sourceId) {
      const allForDiff = [...snapshot, ...newlyCreatedLists];
      const changed = allForDiff.filter((list) => {
        const had = list.sourceIds.includes(sourceId);
        const has = selectedListIds.has(list.id);
        return had !== has;
      });

      if (changed.length > 0) {
        changed.forEach((list) => {
          const isAdding = selectedListIds.has(list.id);
          updateList(
            {
              params: { tenantId, workspaceId, listId: list.id },
              payload: {
                name: list.name,
                type: list.type,
                sourceIds: isAdding
                  ? [...list.sourceIds, sourceId]
                  : list.sourceIds.filter((id) => id !== sourceId),
              },
            },
            { onError: () => toast.error(`Failed to update "${list.name}"`) }
          );
        });
        toast.success('Lists updated');
      }

      onClose();
    } else {
      const selectedLists = [...snapshot, ...newlyCreatedLists].filter((l) =>
        selectedListIds.has(l.id)
      );
      if (selectedLists.length > 0 && rowIds && rowIds.length > 0) {
        selectedLists.forEach((list) => {
          const mergedIds = Array.from(new Set([...list.sourceIds, ...rowIds]));
          updateList(
            {
              params: { tenantId, workspaceId, listId: list.id },
              payload: { name: list.name, type: list.type, sourceIds: mergedIds },
            },
            { onError: () => toast.error(`Failed to update "${list.name}"`) }
          );
        });
        toast.success(
          `Added ${rowIds.length} record${rowIds.length !== 1 ? 's' : ''} to ${selectedLists.length} list${selectedLists.length !== 1 ? 's' : ''}`
        );
      }
      onClose();
    }
  };

  const onCreateSubmit = ({ name }: NewListFormValues) => {
    createList(
      {
        params: { tenantId, workspaceId, listId: '' },
        payload: { name, type, sourceIds: sourceId ? [sourceId] : [] },
      },
      {
        onSuccess: (created) => {
          toast.success(`List "${created.name}" created`);
          setIsCreateView(false);
          resetCreateForm();
          if (isSingleRow) {
            const entry: LeadListSearch = { ...created, sourceCount: 1, username: '' };
            setNewlyCreatedLists((prev) => [...prev, entry]);
          }
          setSelectedListIds((prev) => new Set([...prev, created.id]));
        },
        onError: () => toast.error('Failed to create list'),
      }
    );
  };

  if (isCreateView) {
    return (
      <form onSubmit={handleSubmit(onCreateSubmit)} className="flex flex-col">
        <div className="border-border flex items-center gap-3 border-b px-4 py-3">
          <Button
            type="button"
            variant="ghost"
            size="icon"
            className="h-8 w-8"
            onClick={() => setIsCreateView(false)}
          >
            <ArrowLeft className="h-4 w-4" />
          </Button>
          <h3 className="text-foreground text-sm font-semibold">Create new list</h3>
        </div>
        <div className="px-4 py-4">
          <FormInput
            id="new-list-name"
            label="List name"
            placeholder="Enter list name"
            error={!!errors.name}
            errorMessage={errors.name?.message}
            autoFocus
            {...register('name')}
          />
        </div>
        <div className="border-border flex items-center justify-end gap-2 border-t px-4 py-3">
          <Button
            type="button"
            variant="outline"
            size="sm"
            onClick={() => setIsCreateView(false)}
            disabled={isCreating}
          >
            Cancel
          </Button>
          <Button type="submit" size="sm" disabled={isCreating}>
            {isCreating && <Loader2 className="mr-2 h-3.5 w-3.5 animate-spin" />}
            Create list
          </Button>
        </div>
      </form>
    );
  }

  return (
    <>
      <div className="px-4 py-3">
        <div className="relative">
          <Search className="text-muted-foreground absolute top-1/2 left-3 h-4 w-4 -translate-y-1/2" />
          <Input
            type="text"
            placeholder="Search lists..."
            value={searchQuery}
            onChange={(e) => onSearchChange(e.target.value)}
            className="h-9 pl-10"
          />
        </div>
      </div>

      <div className="max-h-64 overflow-y-auto">
        {isLoading ? (
          <div className="flex h-20 items-center justify-center">
            <Loader2 className="text-muted-foreground h-5 w-5 animate-spin" />
          </div>
        ) : lists.length === 0 ? (
          <p className="text-muted-foreground px-4 py-6 text-center text-sm">
            {searchQuery ? 'No lists match your search.' : 'No lists yet.'}
          </p>
        ) : (
          lists.map((list) => {
            const isSelected = selectedListIds.has(list.id);
            return (
              <button
                key={list.id}
                onClick={() => toggleList(list.id)}
                className="hover:bg-muted flex w-full items-center justify-between px-4 py-2.5 text-left transition-colors"
              >
                <div className="flex items-center gap-3">
                  <input
                    type="checkbox"
                    checked={isSelected}
                    readOnly
                    className="border-border h-4 w-4 rounded"
                  />
                  <span className="text-foreground text-sm">{list.name}</span>
                </div>
                <span className="bg-muted text-muted-foreground rounded-md px-2 py-0.5 text-xs">
                  {list.sourceIds.length}
                </span>
              </button>
            );
          })
        )}
      </div>

      <div className="border-border flex items-center justify-between border-t px-4 py-3">
        <Button variant="outline" size="sm" onClick={() => setIsCreateView(true)}>
          Create new list
        </Button>
        <Button size="sm" disabled={selectedListIds.size === 0} onClick={handleAdd}>
          {isSingleRow ? 'Save' : 'Add to list'}
        </Button>
      </div>
    </>
  );
};

const AddToListPopover = ({
  tenantId,
  workspaceId,
  type,
  rowIds,
  disabled,
  sourceId,
  trigger,
  tooltip,
}: AddToListPopoverProps) => {
  const isSingleRow = !!sourceId;
  const [sessionKey, setSessionKey] = useState(0);

  const { value: open, setTrue: openPopover, setFalse: closePopover } = useToggle();
  const [searchQuery, setSearchQuery] = useState<string>('');
  const [debouncedSearchQuery, setDebouncedSearchQuery] = useState<string>('');

  useDebounce(searchQuery, SEARCH_DEBOUNCE_DELAY, (val) => setDebouncedSearchQuery(val));

  const { data, isLoading } = useGetLeadLists(
    {
      params: { tenantId, workspaceId },
      type,
      page: { ...defaultPaginationParams, query: debouncedSearchQuery },
    },
    { enabled: !isSingleRow || open, staleTime: 2 * 60 * 1000 }
  );

  const lists = (data?.content ?? []).filter((list) =>
    list.name.toLowerCase().includes(searchQuery.toLowerCase())
  );

  const handleOpenChange = (next: boolean) => {
    if (next) {
      setSearchQuery('');
      setSessionKey((k) => k + 1);
      openPopover();
    } else {
      closePopover();
    }
  };

  const bulkCount = (rowIds ?? []).length;
  const defaultTrigger = (
    <Button variant="outline" disabled={disabled || bulkCount === 0} className="shrink-0">
      <Plus className="mr-2 h-4 w-4" />
      Add to List
      {bulkCount > 0 && (
        <span className="ml-2 rounded-full bg-white/20 px-2 py-0.5 text-xs">{bulkCount}</span>
      )}
    </Button>
  );

  const popoverTrigger = <PopoverTrigger asChild>{trigger ?? defaultTrigger}</PopoverTrigger>;

  return (
    <Popover open={open} onOpenChange={handleOpenChange}>
      {tooltip ? (
        <Tooltip>
          <TooltipTrigger asChild>{popoverTrigger}</TooltipTrigger>
          <TooltipContent>{tooltip}</TooltipContent>
        </Tooltip>
      ) : (
        popoverTrigger
      )}
      <PopoverContent align="end" className="w-96 p-0">
        <div className="flex flex-col">
          <ListPopoverContent
            key={sessionKey}
            snapshot={data?.content ?? []}
            lists={lists}
            searchQuery={searchQuery}
            onSearchChange={setSearchQuery}
            sourceId={sourceId}
            rowIds={rowIds}
            isSingleRow={isSingleRow}
            tenantId={tenantId}
            workspaceId={workspaceId}
            type={type}
            onClose={closePopover}
            isLoading={isLoading}
          />
        </div>
      </PopoverContent>
    </Popover>
  );
};

export { AddToListPopover };
