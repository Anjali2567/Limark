'use client';

import { useState } from 'react';

import { Button } from '@/components/ui/button';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { Textarea } from '@/components/ui/textarea';
import { useAuth } from '@/context/AuthContext';
import { useGenerateLeadEmail } from '@/hooks/useLeadEmail';
import { LeadEmailGenerateResponse } from '@/types/lead-email.types';

import { Loader2, Sparkles } from 'lucide-react';

type AIEmailComposeDialogProps = {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onGenerated: (result: LeadEmailGenerateResponse) => void;
};

export const AIEmailComposeDialog = ({
  open,
  onOpenChange,
  onGenerated,
}: AIEmailComposeDialogProps) => {
  const { authenticatedUserDetails } = useAuth();
  const [prompt, setPrompt] = useState('');

  const { mutate: generateEmail, isPending } = useGenerateLeadEmail();

  const handleGenerate = async () => {
    if (
      !prompt.trim() ||
      !authenticatedUserDetails?.tenantId ||
      !authenticatedUserDetails?.workspaceId
    )
      return;

    generateEmail(
      {
        params: {
          tenantId: authenticatedUserDetails.tenantId,
          workspaceId: authenticatedUserDetails.workspaceId,
        },
        payload: { prompt: prompt.trim() },
      },
      {
        onSuccess: (data) => {
          onGenerated(data);
          setPrompt('');
          onOpenChange(false);
        },
      }
    );
  };

  const handleOpenChange = (open: boolean) => {
    if (!open && !isPending) {
      setPrompt('');
    }
    onOpenChange(open);
  };

  return (
    <Dialog open={open} onOpenChange={handleOpenChange}>
      <DialogContent className="sm:max-w-lg">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            <Sparkles className="text-primary h-5 w-5" />
            AI Email Composer
          </DialogTitle>
          <DialogDescription>
            Describe what you want to communicate and AI will draft the subject and body for you.
          </DialogDescription>
        </DialogHeader>

        <Textarea
          value={prompt}
          onChange={(e) => setPrompt(e.target.value)}
          placeholder="e.g. Write a professional outreach email introducing our SaaS platform to a potential B2B client, highlighting key benefits and a clear call-to-action to schedule a demo."
          className="border-border bg-background min-h-32 resize-none"
          disabled={isPending}
        />

        <DialogFooter>
          <Button variant="outline" onClick={() => handleOpenChange(false)} disabled={isPending}>
            Cancel
          </Button>
          <Button onClick={handleGenerate} disabled={!prompt.trim() || isPending}>
            {isPending ? (
              <>
                <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                Generating...
              </>
            ) : (
              <>
                <Sparkles className="mr-2 h-4 w-4" />
                Generate
              </>
            )}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
};
