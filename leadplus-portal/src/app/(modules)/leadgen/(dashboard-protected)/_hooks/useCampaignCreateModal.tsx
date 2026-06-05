'use client';

import { useModal } from '@/hooks/useModal';
import { LeadContactData } from '@/types/leadSearch.types';
import { InfoIcon } from 'lucide-react';
import { useCallback } from 'react';

export function useCampaignCreateModal(
  selectedRows: LeadContactData[],
  onConfirm: () => void,
  totalCount?: number
) {
  const { renderModal } = useModal();

  const getModalContent = useCallback(() => {
    const warnings: string[] = [];
    const contactCount = totalCount ?? selectedRows.length;

    const crmLeads = selectedRows.some((lead) => lead.zohoExisting || lead.hubspotExisting);
    const hasActiveCampaigns = selectedRows.some(
      (lead) => lead.currentCampaigns && lead.currentCampaigns.length > 0
    );
    const hasMoreThan30Contacts = contactCount > 30;

    if (crmLeads) {
      warnings.push(
        'Existing contacts detected: Some of these contacts already exist in your CRM.'
      );
    }

    if (hasActiveCampaigns) {
      warnings.push(
        'Active campaigns found: Some contacts are currently enrolled in other campaigns and may receive multiple outreach sequences.'
      );
    }

    if (hasMoreThan30Contacts) {
      warnings.push(
        `Large selection: You've selected ${contactCount} contacts. Consider breaking this into smaller campaigns for better deliverability and engagement tracking.`
      );
    }

    return {
      title: warnings.length ? 'Confirm Campaign Creation' : 'Create Campaign',
      icon:
        warnings.length > 0 ? (
          <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-amber-100">
            <InfoIcon className="h-5 w-5 text-amber-600" />
          </div>
        ) : undefined,
      message: (
        <div className="space-y-3">
          <div className="text-base">
            {warnings.length > 0
              ? `You're about to create a campaign with ${contactCount} contact${contactCount > 1 ? 's' : ''}. Please review the following:`
              : `You're about to create a campaign with ${contactCount} contact${contactCount > 1 ? 's' : ''}. Do you want to proceed?`}
          </div>

          {warnings.length > 0 && (
            <p className="space-y-2 rounded-md bg-amber-50 p-3">
              {warnings.map((warning, index) => (
                <span key={index} className="flex gap-2 text-sm text-amber-900">
                  <span className="font-semibold">•</span>
                  <span>{warning}</span>
                </span>
              ))}
            </p>
          )}
        </div>
      ),
    };
  }, [selectedRows, totalCount]);

  const handleCampaignCreate = useCallback(() => {
    const content = getModalContent();
    renderModal({
      type: 'confirm',
      icon: content.icon,
      title: content.title,
      message: content.message,
      onConfirm,
      submitButtonText: 'Continue Anyway',
    });
  }, [getModalContent, onConfirm, renderModal]);

  return { handleCampaignCreate };
}
