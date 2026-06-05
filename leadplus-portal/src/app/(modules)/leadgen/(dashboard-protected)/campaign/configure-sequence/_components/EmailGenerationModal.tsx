import { AxiosError } from "axios";
import { useCallback, useState } from "react";
import { toast } from "sonner";

import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { useGenerateCampaignEmail } from "@/hooks/useCampaignEmail";
import { GenerateCampaignEmailResponse } from "@/types/campaign-email.types";

type EmailGenerationModalProps = {
  isOpen: boolean;
  toggle: () => void;
  tenantId: string;
  campaignId: string;
  workspaceId: string;
  onSubmit: (response: GenerateCampaignEmailResponse[]) => void;
};

const DEFAULT_PROMPT =
  "Generate a sequence of campaign emails to introduce your product or service and follow up with selected leads.";

const EmailGenerationModal = ({
  isOpen,
  toggle,
  campaignId,
  tenantId,
  workspaceId,
  onSubmit,
}: EmailGenerationModalProps) => {
  const { mutate, isPending } = useGenerateCampaignEmail();
  const [userPrompt, setUserPrompt] = useState<string>(DEFAULT_PROMPT);

  const handleSubmit = useCallback(() => {
    mutate(
      {
        tenantId,
        workspaceId,
        campaignId,
        requestBody: {
          userPrompt,
        },
      },
      {
        onSuccess: (data) => {
          onSubmit(data);
          setUserPrompt(DEFAULT_PROMPT);
          toggle();
        },
        onError: (error: AxiosError) => {
          toast.error(
            error.message ||
              "An error occurred while generating the emails. Please try again."
          );
        },
      }
    );
  }, [mutate, tenantId, workspaceId, campaignId, userPrompt, onSubmit, toggle]);

  return (
    <Dialog open={isOpen} onOpenChange={() => !isPending && toggle()}>
      <DialogContent className="sm:max-w-125">
        <DialogHeader>
          <DialogTitle>Generate Email Sequence</DialogTitle>
        </DialogHeader>

        <div className="space-y-3">
          <Label className="text-xs font-semibold uppercase text-muted-foreground">
            Prompt
          </Label>
          <Textarea
            placeholder="Describe the email sequence you want to generate..."
            value={userPrompt}
            onChange={(e) => setUserPrompt(e.target.value)}
            className="min-h-28 resize-y"
          />
        </div>

        <DialogFooter className="gap-2">
          <Button variant="outline" onClick={toggle}>
            Cancel
          </Button>
          <Button
            onClick={handleSubmit}
            disabled={!userPrompt.trim() || isPending}
          >
            {isPending ? "Generating..." : "Generate"}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
};

export { EmailGenerationModal };
