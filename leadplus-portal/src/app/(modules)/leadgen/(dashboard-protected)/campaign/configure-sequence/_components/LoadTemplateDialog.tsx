'use client';

import { useState } from 'react';
import { toast } from 'sonner';

import { Button } from '@/components/ui/button';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { Input } from '@/components/ui/input';
import { ScrollArea } from '@/components/ui/scroll-area';
import {
  useImportEmailSequenceTemplate,
  useListEmailSequenceTemplates,
} from '@/hooks/useEmailSequenceTemplates';
import { formatDate } from '@/lib/utils/formatter';
import { cn } from '@/lib/utils/helpers';
import { EmailSequenceTemplate } from '@/types/email-sequence-template.types';

import { Download, Search } from 'lucide-react';

type LoadTemplateDialogProps = {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  tenantId: string;
  workspaceId: string;
  campaignId: string;
  onImportSuccess?: () => void;
};

const LoadTemplateDialog = ({
  open,
  onOpenChange,
  tenantId,
  workspaceId,
  campaignId,
  onImportSuccess,
}: LoadTemplateDialogProps) => {
  const [selectedTemplateId, setSelectedTemplateId] = useState<string | null>(null);
  const [search, setSearch] = useState('');

  const { data: templates = [], isLoading } = useListEmailSequenceTemplates({ tenantId }, open);
  const { mutate: importTemplate, isPending } = useImportEmailSequenceTemplate();

  const filteredTemplates = search.trim()
    ? templates.filter((t) => t.name.toLowerCase().includes(search.toLowerCase()))
    : templates;

  const handleImport = () => {
    if (!selectedTemplateId) return;
    importTemplate(
      { tenantId, workspaceId, campaignId, templateId: selectedTemplateId },
      {
        onSuccess: () => {
          toast.success('Template imported');
          setSelectedTemplateId(null);
          onOpenChange(false);
          onImportSuccess?.();
        },
        onError: (error) => {
          toast.error(error?.message || 'Failed to import template');
        },
      }
    );
  };

  const handleOpenChange = (open: boolean) => {
    if (!open) {
      setSelectedTemplateId(null);
      setSearch('');
    }
    onOpenChange(open);
  };

  return (
    <Dialog open={open} onOpenChange={handleOpenChange}>
      <DialogContent className="sm:max-w-lg">
        <DialogHeader>
          <DialogTitle>Import Template</DialogTitle>
          <DialogDescription>
            Select a saved template to import into your sequence.
          </DialogDescription>
        </DialogHeader>

        <div className="relative">
          <Search className="text-muted-foreground absolute top-1/2 left-3 h-4 w-4 -translate-y-1/2" />
          <Input
            placeholder="Search templates..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="pl-9"
          />
        </div>

        <ScrollArea className="h-80">
          <div className="space-y-3 py-1 pr-3">
            {isLoading && (
              <div className="flex justify-center py-8">
                <div className="h-8 w-8 animate-spin rounded-full border-2 border-sky-500 border-t-transparent" />
              </div>
            )}

            {!isLoading && templates.length === 0 && (
              <div className="text-muted-foreground py-8 text-center text-sm">
                No templates saved yet. Save a sequence as a template to get started.
              </div>
            )}

            {!isLoading && templates.length > 0 && filteredTemplates.length === 0 && (
              <div className="text-muted-foreground py-8 text-center text-sm">
                No templates match your search.
              </div>
            )}

            {!isLoading &&
              filteredTemplates.map((template) => (
                <TemplateCard
                  key={template.id}
                  template={template}
                  selected={selectedTemplateId === template.id}
                  onSelect={() => setSelectedTemplateId(template.id)}
                />
              ))}
          </div>
        </ScrollArea>

        <DialogFooter>
          <Button variant="outline" onClick={() => handleOpenChange(false)} disabled={isPending}>
            Cancel
          </Button>
          <Button
            onClick={handleImport}
            disabled={!selectedTemplateId || isPending}
            className="bg-sky-500 text-white hover:bg-sky-600"
          >
            <Download className="mr-2 h-4 w-4" />
            Import Template
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
};

type TemplateCardProps = {
  template: EmailSequenceTemplate;
  selected: boolean;
  onSelect: () => void;
};

const TemplateCard = ({ template, selected, onSelect }: TemplateCardProps) => {
  return (
    <Button
      type="button"
      onClick={onSelect}
      variant="outline"
      aria-pressed={selected}
      className={cn(
        'h-auto w-full justify-start p-4 text-left',
        selected && 'border-sky-500 bg-sky-50 dark:bg-sky-950/20'
      )}
    >
      <div className="flex w-full items-start justify-between gap-3">
        <div className="min-w-0 flex-1 space-y-1">
          <div className="flex items-center gap-2">
            <span className="font-semibold">{template.name}</span>
            <span className="bg-muted text-muted-foreground rounded-full px-2 py-0.5 text-xs">
              {template.stepCount} {template.stepCount === 1 ? 'email' : 'emails'}
            </span>
          </div>
          {template.description && (
            <p className="text-muted-foreground truncate text-sm">{template.description}</p>
          )}
          <p className="text-muted-foreground text-xs">
            Created: {template.createdAt ? formatDate(template.createdAt) : 'Unknown'}
          </p>
        </div>
        <div
          className={cn(
            'mt-0.5 h-4 w-4 shrink-0 rounded-full border-2',
            selected ? 'border-sky-500 bg-sky-500' : 'border-muted-foreground'
          )}
        />
      </div>
    </Button>
  );
};

export { LoadTemplateDialog };
