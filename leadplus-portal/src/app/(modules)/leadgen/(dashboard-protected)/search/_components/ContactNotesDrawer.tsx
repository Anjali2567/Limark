'use client';

import { useState } from 'react';

import { Button } from '@/components/ui/button';
import {
  Sheet,
  SheetContent,
  SheetDescription,
  SheetHeader,
  SheetTitle,
  SheetTrigger,
} from '@/components/ui/sheet';
import { Textarea } from '@/components/ui/textarea';
import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from '@/components/ui/tooltip';
import { LeadType } from '@/constants/leadSearch.constants';
import { useAuth } from '@/context/AuthContext';
import {
  useCreateLeadNote,
  useDeleteLeadNote,
  useGetLeadNotesBySourceId,
  useUpdateLeadNote,
} from '@/hooks/useLeadNote';
import useToggle from '@/hooks/useToggle';
import { getAvatarColorClasses, getInitials } from '@/lib/utils/helpers';
import { convertDate } from '@/lib/utils/timeConversion';
import { LeadNote } from '@/types/lead-notes.types';

import { ChevronDown, Loader2, Pencil, Send, StickyNote, Trash2 } from 'lucide-react';
import { defaultPaginationParams } from '@/types/paginated-params';

type ContactNotesDrawerProps = {
  entityId: string;
  entityName: string;
  tenantId: string;
  workspaceId: string;
  leadType: LeadType;
};

const ContactNotesDrawer = ({
  entityId,
  entityName,
  tenantId,
  workspaceId,
  leadType,
}: ContactNotesDrawerProps) => {
  const { authenticatedUser } = useAuth();
  const currentUserId = authenticatedUser?.id ?? '';
  const currentUserName = authenticatedUser?.name ?? 'Unknown User';

  const createdBy = { id: currentUserId, name: currentUserName };
  const { value: isOpen, setValue: setIsOpen } = useToggle();

  const [editingText, setEditingText] = useState<string>('');
  const [newNoteText, setNewNoteText] = useState<string>('');

  const [editingNoteId, setEditingNoteId] = useState<string | null>(null);
  const [deletingNoteId, setDeletingNoteId] = useState<string | null>(null);

  const { data, fetchNextPage, hasNextPage, isFetchingNextPage } = useGetLeadNotesBySourceId({
    tenantId,
    workspaceId,
    sourceId: entityId,
    params: { ...defaultPaginationParams },
  });
  const { mutate: createNote, isPending: isCreating } = useCreateLeadNote(createdBy);
  const { mutate: updateNote, isPending: isUpdating } = useUpdateLeadNote(createdBy);
  const { mutate: deleteNote } = useDeleteLeadNote(entityId);

  const notes: LeadNote[] = data?.pages.flatMap((p) => p.content) ?? [];
  const hasNotes = notes.length > 0;

  const handleAddNote = () => {
    if (!newNoteText.trim()) return;
    createNote(
      {
        params: { tenantId, workspaceId },
        payload: { note: newNoteText.trim(), type: leadType, sourceId: entityId },
      },
      {
        onSuccess: () => setNewNoteText(''),
      }
    );
  };

  const handleSaveEdit = () => {
    if (!editingNoteId || !editingText.trim()) return;
    updateNote(
      {
        params: { tenantId, workspaceId, leadNoteId: editingNoteId },
        note: editingText.trim(),
        sourceId: entityId,
      },
      {
        onSuccess: () => {
          setEditingNoteId(null);
          setEditingText('');
        },
      }
    );
  };

  const handleCancelEdit = () => {
    setEditingNoteId(null);
    setEditingText('');
  };

  const handleStartEdit = (note: LeadNote) => {
    setEditingNoteId(note.id);
    setEditingText(note.note);
  };

  const handleDeleteNote = (noteId: string) => {
    deleteNote({ tenantId, workspaceId, leadNoteId: noteId });
    setDeletingNoteId(null);
  };

  const onOpenChange = (open: boolean) => {
    setIsOpen(open);
    if (!open) {
      setEditingNoteId(null);
      setEditingText('');
      setDeletingNoteId(null);
    }
  };

  return (
    <Sheet open={isOpen} onOpenChange={onOpenChange}>
      <TooltipProvider delayDuration={300}>
        <Tooltip>
          <TooltipTrigger asChild>
            <SheetTrigger asChild>
              <Button
                variant="ghost"
                size="icon"
                className={`hover:bg-primary/10 relative h-8 w-8 ${hasNotes ? 'text-primary' : 'text-muted-foreground hover:text-primary'}`}
              >
                <StickyNote className="h-4 w-4" />
                {hasNotes && (
                  <span className="bg-primary text-primary-foreground absolute -top-0.5 -right-0.5 flex h-4.5 w-4.5 items-center justify-center rounded-full text-[8px] font-semibold">
                    {notes.length > 9 ? '9+' : notes.length}
                  </span>
                )}
              </Button>
            </SheetTrigger>
          </TooltipTrigger>
          <TooltipContent>
            <p>Notes</p>
          </TooltipContent>
        </Tooltip>
      </TooltipProvider>

      <SheetContent side="right" className="flex w-full flex-col gap-0 p-0 sm:max-w-md">
        <SheetHeader className="bg-secondary border-border shrink-0 border-b px-8 py-5">
          <div className="flex items-center gap-3">
            <div className="bg-primary text-primary-foreground flex h-8 w-8 items-center justify-center rounded">
              <StickyNote className="h-4 w-4" />
            </div>
            <div>
              <SheetTitle className="text-foreground text-base font-semibold">Notes</SheetTitle>
              <p className="text-muted-foreground max-w-55 truncate text-sm">{entityName}</p>
            </div>
          </div>
          <SheetDescription className="sr-only">
            View and manage notes for {entityName}
          </SheetDescription>
        </SheetHeader>
        <div className="flex-1 overflow-y-auto">
          {hasNotes ? (
            <div className="divide-border divide-y">
              {notes.map((note) => {
                const authorName = note.createdBy?.name ?? currentUserName;
                const isCurrentUser = note.createdBy?.id === currentUserId;
                return (
                  <div
                    key={note.id}
                    className="group hover:bg-muted/30 px-8 py-4 transition-colors"
                  >
                    {deletingNoteId === note.id ? (
                      <div className="bg-muted flex flex-col gap-2 rounded px-3 py-2">
                        <div className="flex items-center gap-2">
                          <Trash2 className="text-destructive h-3.5 w-3.5 shrink-0" />
                          <span className="text-foreground text-sm font-medium">
                            Delete this note?
                          </span>
                        </div>
                        <p className="text-muted-foreground line-clamp-2 pl-5.5 text-sm">
                          {note.note}
                        </p>
                        <div className="flex items-center justify-end gap-1.5">
                          <Button
                            variant="ghost"
                            size="sm"
                            className="text-muted-foreground h-7 text-xs"
                            onClick={() => setDeletingNoteId(null)}
                          >
                            Cancel
                          </Button>
                          <Button
                            variant="destructive"
                            size="sm"
                            className="h-7 text-xs"
                            onClick={() => handleDeleteNote(note.id)}
                          >
                            Delete
                          </Button>
                        </div>
                      </div>
                    ) : editingNoteId === note.id ? (
                      <div>
                        <Textarea
                          value={editingText}
                          onChange={(e) => setEditingText(e.target.value)}
                          className="min-h-20 resize-none text-sm"
                          autoFocus
                          onKeyDown={(e) => {
                            if (e.key === 'Enter' && (e.metaKey || e.ctrlKey)) handleSaveEdit();
                            if (e.key === 'Escape') handleCancelEdit();
                          }}
                        />
                        <div className="mt-2 flex items-center justify-between">
                          <span className="text-muted-foreground text-xs">Ctrl+Enter to save</span>
                          <div className="flex justify-end gap-1.5">
                            <Button
                              variant="ghost"
                              size="sm"
                              className="text-muted-foreground h-7 text-xs"
                              onClick={handleCancelEdit}
                            >
                              Cancel
                            </Button>
                            <Button
                              size="sm"
                              className="h-7 text-xs"
                              onClick={handleSaveEdit}
                              disabled={!editingText.trim() || isUpdating}
                            >
                              Save
                            </Button>
                          </div>
                        </div>
                      </div>
                    ) : (
                      <div>
                        <div className="mb-2 flex items-center gap-2">
                          <span
                            className={`flex h-6 w-6 shrink-0 items-center justify-center rounded-full text-[10px] font-semibold ${getAvatarColorClasses(authorName)}`}
                          >
                            {getInitials(authorName)}
                          </span>
                          <span className="text-foreground text-sm font-medium">{authorName}</span>
                          {isCurrentUser && (
                            <span className="text-muted-foreground text-xs">(you)</span>
                          )}
                          <span className="text-muted-foreground ml-auto text-xs">
                            {convertDate(new Date(note.createdAt))}
                          </span>
                        </div>
                        <p className="text-foreground pl-8 text-sm font-normal wrap-break-word whitespace-pre-wrap">
                          {note.note}
                        </p>
                        {isCurrentUser && (
                          <div className="mt-2 flex items-center justify-end">
                            <div className="flex items-center gap-0.5 opacity-0 transition-opacity group-hover:opacity-100">
                              <Button
                                variant="ghost"
                                size="icon"
                                className="text-muted-foreground hover:text-foreground h-7 w-7"
                                onClick={() => handleStartEdit(note)}
                              >
                                <Pencil className="h-3.5 w-3.5" />
                              </Button>
                              <Button
                                variant="ghost"
                                size="icon"
                                className="text-muted-foreground hover:text-destructive h-7 w-7"
                                onClick={() => setDeletingNoteId(note.id)}
                              >
                                <Trash2 className="h-3.5 w-3.5" />
                              </Button>
                            </div>
                          </div>
                        )}
                      </div>
                    )}
                  </div>
                );
              })}
              {hasNextPage && (
                <div className="flex justify-center px-8 py-3">
                  <Button
                    variant="ghost"
                    size="sm"
                    className="text-muted-foreground hover:text-foreground h-7 gap-1.5 text-xs"
                    onClick={() => fetchNextPage()}
                    disabled={isFetchingNextPage}
                  >
                    {isFetchingNextPage ? (
                      <Loader2 className="h-3.5 w-3.5 animate-spin" />
                    ) : (
                      <ChevronDown className="h-3.5 w-3.5" />
                    )}
                    Load more
                  </Button>
                </div>
              )}
            </div>
          ) : (
            <div className="flex flex-col items-center justify-center px-8 py-16 text-center">
              <div className="bg-muted mb-3 flex h-12 w-12 items-center justify-center rounded-full">
                <StickyNote className="text-muted-foreground h-6 w-6" />
              </div>
              <p className="text-foreground mb-1 text-sm font-medium">No notes yet</p>
              <p className="text-muted-foreground text-sm">Add a note below to get started</p>
            </div>
          )}
        </div>
        <div className="bg-secondary border-border shrink-0 border-t px-8 py-4">
          <div className="mb-2 flex items-center gap-2">
            <span
              className={`flex h-6 w-6 shrink-0 items-center justify-center rounded-full text-[10px] font-semibold ${getAvatarColorClasses(currentUserName)}`}
            >
              {getInitials(currentUserName)}
            </span>
            <span className="text-foreground text-sm font-medium">{currentUserName}</span>
          </div>
          <Textarea
            value={newNoteText}
            onChange={(e) => setNewNoteText(e.target.value)}
            placeholder="Write a note..."
            className="bg-card min-h-20 resize-none text-sm"
            onKeyDown={(e) => {
              if (e.key === 'Enter' && (e.metaKey || e.ctrlKey)) handleAddNote();
            }}
          />
          <div className="mt-2 flex items-center justify-between">
            <span className="text-muted-foreground text-xs">Ctrl+Enter to save</span>
            <Button
              size="sm"
              className="h-8 gap-1.5"
              onClick={handleAddNote}
              disabled={!newNoteText.trim() || isCreating}
            >
              <Send className="h-3.5 w-3.5" />
              <span className="text-sm font-medium">Add Note</span>
            </Button>
          </div>
        </div>
      </SheetContent>
    </Sheet>
  );
};

export default ContactNotesDrawer;
