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
import { Label } from '@/components/ui/label';
import { useCreateEmailSequenceTemplate } from '@/hooks/useEmailSequenceTemplates';
import { CampaignEmail } from '@/types/campaign-email.types';

import { Save } from 'lucide-react';

type SaveTemplateDialogProps = {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  tenantId: string;
  campaignEmails: CampaignEmail[];
};

const SaveTemplateDialog = ({
  open,
  onOpenChange,
  tenantId,
  campaignEmails,
}: SaveTemplateDialogProps) => {
  const [templateName, setTemplateName] = useState<string>('');

  const { mutate: createTemplate, isPending } = useCreateEmailSequenceTemplate();

  const totalDelays = campaignEmails.reduce((sum, email) => sum + (email.delayDays || 0), 0);

  const handleSave = () => {
    if (!templateName.trim()) return;

    const steps = campaignEmails.map((email) => ({
      stepNumber: email.stepNumber,
      subject: email.subject || '',
      bodyTemplate: email.bodyTemplate || '',
      delayDays: email.delayDays || 0,
    }));

    createTemplate(
      {
        tenantId,
        requestBody: { name: templateName.trim(), steps },
      },
      {
        onSuccess: () => {
          toast.success('Template saved successfully');
          setTemplateName('');
          onOpenChange(false);
        },
        onError: () => {
          toast.error('Failed to save template');
        },
      }
    );
  };

  const handleOpenChange = (open: boolean) => {
    if (!open) setTemplateName('');
    onOpenChange(open);
  };

  return (
    <Dialog open={open} onOpenChange={handleOpenChange}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>Save as Template</DialogTitle>
          <DialogDescription>Save this sequence as a template to reuse later.</DialogDescription>
        </DialogHeader>

        <div className="space-y-4 py-2">
          <div className="space-y-2">
            <Label htmlFor="template-name">Template Name</Label>
            <Input
              id="template-name"
              placeholder="Enter template name..."
              value={templateName}
              onChange={(e) => setTemplateName(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && !isPending && handleSave()}
            />
          </div>

          <div className="bg-muted space-y-2 rounded-md p-4 text-sm">
            <div className="flex justify-between">
              <span className="text-muted-foreground">Email Steps:</span>
              <span className="font-semibold">{campaignEmails.length}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-muted-foreground">Total Delays:</span>
              <span className="font-semibold">{totalDelays} days</span>
            </div>
          </div>
        </div>

        <DialogFooter>
          <Button variant="outline" onClick={() => handleOpenChange(false)} disabled={isPending}>
            Cancel
          </Button>
          <Button
            onClick={handleSave}
            disabled={!templateName.trim() || isPending}
            className="bg-sky-500 text-white hover:bg-sky-600"
          >
            <Save className="mr-2 h-4 w-4" />
            Save Template
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
};

export { SaveTemplateDialog };
