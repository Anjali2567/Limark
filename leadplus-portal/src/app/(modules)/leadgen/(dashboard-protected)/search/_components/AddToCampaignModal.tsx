'use client';

import { useState } from 'react';
import { toast } from 'sonner';
import { Loader2, Search } from 'lucide-react';

import { Button } from '@/components/ui/button';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from '@/components/ui/dialog';
import { Input } from '@/components/ui/input';
import { CampaignStatus } from '@/constants/Campaign';
import { useAddContactsToCampaign, useGetAllCampaigns } from '@/hooks/useCampaign';
import { useDebounce } from '@/hooks/useDebounce';
import { defaultPaginationParams } from '@/types/paginated-params';

const ALLOWED_STATUSES = [
  CampaignStatus.PAUSED,
  CampaignStatus.DRAFT,
  CampaignStatus.PENDING_APPROVAL,
];

const STATUS_LABEL: Partial<Record<CampaignStatus, string>> = {
  [CampaignStatus.PAUSED]: 'Paused',
  [CampaignStatus.DRAFT]: 'Draft',
  [CampaignStatus.PENDING_APPROVAL]: 'Pending Approval',
};

type AddToCampaignModalProps = {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  contactIds: string[];
  tenantId: string;
  workspaceId: string;
};

const AddToCampaignModal = ({
  open,
  onOpenChange,
  contactIds,
  tenantId,
  workspaceId,
}: AddToCampaignModalProps) => {
  const [searchQuery, setSearchQuery] = useState('');
  const [debouncedSearch, setDebouncedSearch] = useState('');
  const [selectedCampaignId, setSelectedCampaignId] = useState<string | null>(null);

  useDebounce(searchQuery, 300, (val) => setDebouncedSearch(val));

  const { data, isLoading } = useGetAllCampaigns(
    {
      tenantId,
      workspaceId,
      params: {
        ...defaultPaginationParams,
        query: debouncedSearch,
        campaignStatuses: ALLOWED_STATUSES,
        size: 100,
      },
    },
    { enabled: open }
  );

  const { mutate: addContacts, isPending } = useAddContactsToCampaign();

  const campaigns = data?.content ?? [];
  const selectedCampaign = campaigns.find((c) => c.id === selectedCampaignId);

  const handleAdd = () => {
    if (!selectedCampaignId || !selectedCampaign) return;
    addContacts(
      { tenantId, workspaceId, campaignId: selectedCampaignId, contactIds },
      {
        onSuccess: () => {
          const isPaused = selectedCampaign.status === CampaignStatus.PAUSED;
          const contactCount = contactIds.length;
          const plural = contactCount !== 1 ? 's' : '';

          toast.success(
            `${contactCount} contact${plural} added to "${selectedCampaign.name}". They will start the sequence when the campaign ${isPaused ? 'resumes' : 'launches'}.`
          );
          onOpenChange(false);
        },
        onError: () => {
          toast.error('Failed to add contacts to campaign.');
        },
      }
    );
  };

  const handleOpenChange = (next: boolean) => {
    if (!next) {
      setSearchQuery('');
      setSelectedCampaignId(null);
    }
    onOpenChange(next);
  };

  return (
    <Dialog open={open} onOpenChange={handleOpenChange}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>Add to Campaign</DialogTitle>
        </DialogHeader>

        <div className="relative">
          <Search className="text-muted-foreground absolute top-1/2 left-3 h-4 w-4 -translate-y-1/2" />
          <Input
            placeholder="Search campaigns..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="h-9 pl-10"
          />
        </div>

        <fieldset className="max-h-72 overflow-y-auto rounded-md border">
          <legend className="sr-only">Select a campaign</legend>
          {isLoading ? (
            <div className="flex h-20 items-center justify-center">
              <Loader2 className="text-muted-foreground h-5 w-5 animate-spin" />
            </div>
          ) : campaigns.length === 0 ? (
            <p className="text-muted-foreground px-4 py-6 text-center text-sm">
              {searchQuery ? 'No campaigns match your search.' : 'No eligible campaigns available.'}
            </p>
          ) : (
            campaigns.map((campaign) => (
              <label
                key={campaign.id}
                htmlFor={`campaign-option-${campaign.id}`}
                className={`hover:bg-muted flex w-full cursor-pointer items-center justify-between px-4 py-3 transition-colors ${
                  selectedCampaignId === campaign.id ? 'bg-muted' : ''
                }`}
              >
                <div className="flex items-center gap-3">
                  <input
                    type="radio"
                    id={`campaign-option-${campaign.id}`}
                    name="campaign"
                    value={campaign.id}
                    checked={selectedCampaignId === campaign.id}
                    onChange={() => setSelectedCampaignId(campaign.id)}
                    className="h-4 w-4"
                  />
                  <span className="text-foreground text-sm font-medium">{campaign.name}</span>
                </div>
                <div className="flex items-center gap-2">
                  <span className="bg-muted text-muted-foreground rounded-md px-2 py-0.5 text-xs">
                    {STATUS_LABEL[campaign.status] ?? campaign.status}
                  </span>
                  <span className="text-muted-foreground text-xs">{campaign.totalContacts}</span>
                </div>
              </label>
            ))
          )}
        </fieldset>

        <DialogFooter>
          <Button variant="outline" onClick={() => handleOpenChange(false)} disabled={isPending}>
            Cancel
          </Button>
          <Button disabled={!selectedCampaign || isPending} onClick={handleAdd}>
            {isPending && <Loader2 className="mr-2 h-3.5 w-3.5 animate-spin" />}
            Add to Campaign
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
};

export { AddToCampaignModal };
