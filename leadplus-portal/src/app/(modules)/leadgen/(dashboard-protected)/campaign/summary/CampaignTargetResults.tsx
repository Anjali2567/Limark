"use client";

import { UIEvent, useState } from "react";

import { Button } from "@/components/ui/button";
import {
  Sheet,
  SheetContent,
  SheetDescription,
  SheetHeader,
  SheetTitle,
  SheetTrigger,
} from "@/components/ui/sheet";

import { Loader2, Search } from "lucide-react";
import { CampaignCompanyResponse } from "@/types/campaign.types";
import { SearchBar } from "@/components/SearchBar";
import { CampaignTargetResultCompanyCard } from "./CampaignTargetResultCompanyCard";
import { useUpdateCampaignTargets } from "@/hooks/useCampaign";

type CampaignTargetResultsProps = {
  result: CampaignCompanyResponse[];
  campaignId: string;
  workspaceId: string;
  tenantId: string;
  open: boolean;
  toggle: () => void;
  hideTrigger?: boolean;
  isViewMode?: boolean;
  isLoading?: boolean;
};

const CampaignTargetResults = ({
  result = [],
  campaignId,
  tenantId,
  workspaceId,
  open,
  toggle,
  hideTrigger = false,
  isViewMode = false,
  isLoading = false,
}: CampaignTargetResultsProps) => {
  const COMPANY_BATCH_SIZE = 20;
  const [initialResults, setInitialResults] = useState(result);
  const [filteredResults, setFilteredResults] = useState(result);
  const [modifiedResults, setModifiedResults] =
    useState<CampaignCompanyResponse[]>(result);
  const [visibleCompanyCount, setVisibleCompanyCount] =
    useState(COMPANY_BATCH_SIZE);
  const [hasUnsavedChanges, setHasUnsavedChanges] = useState(false);

  const { mutate, isPending } = useUpdateCampaignTargets();

  const handleResultsScroll = (event: UIEvent<HTMLDivElement>) => {
    const element = event.currentTarget;
    const nearBottom =
      element.scrollTop + element.clientHeight >= element.scrollHeight - 120;
    if (nearBottom && visibleCompanyCount < filteredResults.length) {
      setVisibleCompanyCount((prev) =>
        Math.min(prev + COMPANY_BATCH_SIZE, filteredResults.length)
      );
    }
  };

  const handleOnSearch = (search: string) => {
    if (!search) {
      setFilteredResults(result);
      setVisibleCompanyCount(COMPANY_BATCH_SIZE);
      return;
    }

    const searchTerm = search.toLowerCase();

    const filtered = result
      .map((company) => {
        const companyNameMatches = company.name
          .toLowerCase()
          .includes(searchTerm);

        if (companyNameMatches) return company;

        const matchingContacts = (company.campaignContacts ?? []).filter((contact) =>
          [contact.fullName, contact.email, contact.title]
            .filter(Boolean)
            .some((v) => v!.toLowerCase().includes(searchTerm))
        );

        if (matchingContacts.length === 0) return null;

        return {
          ...company,
          campaignContacts: matchingContacts,
        } as CampaignCompanyResponse;
      })
      .filter(Boolean) as CampaignCompanyResponse[];
    setFilteredResults(filtered);
    setVisibleCompanyCount(COMPANY_BATCH_SIZE);
  };

  const handleToggleContact = (companyId: string, contactId: string) => {
    setModifiedResults((prev) =>
      prev.map((company) => {
        if (company.id === companyId) {
          return {
            ...company,
            campaignContacts: (company.campaignContacts ?? []).map((contact) => {
              if (contact.id === contactId) {
                return {
                  ...contact,
                  participating: !contact.participating,
                };
              }
              return contact;
            }),
          };
        }
        return company;
      })
    );
    setHasUnsavedChanges(true);
  };

  const handleToggleCompany = (companyId: string) => {
    setModifiedResults((prev) =>
      prev.map((company) => {
        if (company.id === companyId) {
          const anyParticipating = (company.campaignContacts ?? []).some(
            (contact) => contact.participating
          );
          return {
            ...company,
            campaignContacts: (company.campaignContacts ?? []).map((contact) => ({
              ...contact,
              participating: !anyParticipating,
            })),
          };
        }
        return company;
      })
    );
    setHasUnsavedChanges(true);
  };

  const handleSubmit = () => {
    const newlySelectedIds: string[] = [];
    const excludedIds: string[] = [];

    modifiedResults.forEach((modifiedCompany) => {
      const originalCompany = initialResults.find(
        (c) => c.id === modifiedCompany.id
      );
      if (!originalCompany) return;

      (modifiedCompany.campaignContacts ?? []).forEach((modifiedContact) => {
        const originalContact = (originalCompany.campaignContacts ?? []).find(
          (c) => c.id === modifiedContact.id
        );
        if (!originalContact) return;

        if (!originalContact.participating && modifiedContact.participating) {
          newlySelectedIds.push(modifiedContact.id);
        } else if (
          originalContact.participating &&
          !modifiedContact.participating
        ) {
          excludedIds.push(modifiedContact.id);
        }
      });
    });

    mutate(
      {
        tenantId,
        workspaceId,
        campaignId,
        requestBody: {
          selectedCampaignContactIds: newlySelectedIds,
          excludedCampaignContactIds: excludedIds,
        },
      },
      {
        onSuccess: () => {
          setHasUnsavedChanges(false);
          toggle();
        },
      }
    );
  };

  const handleOpenChange = () => {
    toggle();
    setInitialResults(result);
    setModifiedResults(result);
    setFilteredResults(result);
    setVisibleCompanyCount(COMPANY_BATCH_SIZE);
    setHasUnsavedChanges(false);
  };

  return (
    <div className="flex justify-center">
      <Sheet open={open} onOpenChange={() => handleOpenChange()}>
        {!hideTrigger && (
          <SheetTrigger asChild>
            <Button
              variant="ghost"
              className="text-sky-500 hover:text-sky-600 hover:bg-sky-50 font-medium"
            >
              <Search className="mr-2 h-4 w-4" />
              View & Edit Full List
            </Button>
          </SheetTrigger>
        )}
        <SheetContent
          className="w-full sm:max-w-xl flex flex-col gap-0 overflow-hidden p-0"
          side="right"
        >
          <SheetHeader className="mb-2 shrink-0 p-6 pb-0">
            <SheetTitle className="text-2xl">
              Leads List ({result.length})
            </SheetTitle>
            <SheetDescription>
              Review and select companies for your campaign.
            </SheetDescription>
          </SheetHeader>

          {isLoading ? (
            <div className="flex h-full items-center justify-center">
              <Loader2 className="h-6 w-6 animate-spin text-muted-foreground" />
            </div>
          ) : (
            <>
              <div
                className="flex-1 overflow-y-auto overflow-x-hidden px-6 pb-4 space-y-6"
                onScroll={handleResultsScroll}
              >
                <div className="sticky top-0 z-10 bg-background/95 pb-2 backdrop-blur supports-[backdrop-filter]:bg-background/80">
                  <SearchBar onChange={handleOnSearch} />
                </div>

                {filteredResults.length > 0 ? (
                  filteredResults.slice(0, visibleCompanyCount).map((company) => {
                    const modifiedCompany = modifiedResults.find(
                      (c) => c.id === company.id
                    );
                    const displayCompany = modifiedCompany
                      ? {
                          ...company,
                          campaignContacts: (company.campaignContacts ?? []).map(
                            (contact) => {
                              const modContact =
                                (modifiedCompany.campaignContacts ?? []).find(
                                  (c) => c.id === contact.id
                                );
                              return modContact
                                ? {
                                    ...contact,
                                    participating: modContact.participating,
                                  }
                                : contact;
                            }
                          ),
                        }
                      : company;
                    return (
                      <CampaignTargetResultCompanyCard
                        key={company.id}
                        company={displayCompany}
                        onToggleCompany={handleToggleCompany}
                        onToggleContact={handleToggleContact}
                        isViewMode={isViewMode}
                      />
                    );
                  })
                ) : (
                  <span className="flex text-center text-sm text-slate-400 justify-center">
                    No results found
                  </span>
                )}
                {visibleCompanyCount < filteredResults.length && (
                  <div className="flex items-center justify-center py-2 text-xs text-muted-foreground">
                    Scroll to load more
                  </div>
                )}
              </div>
              {!isViewMode && (
                <div className="shrink-0 p-6 bg-card border-t border-border mt-auto grid grid-cols-2 gap-3">
                  <Button variant="outline" onClick={toggle}>
                    Cancel
                  </Button>
                  <Button
                    className="bg-sky-500 hover:bg-sky-600 text-primary-foreground"
                    disabled={!hasUnsavedChanges || isPending}
                    onClick={handleSubmit}
                  >
                    Save Changes
                  </Button>
                </div>
              )}
            </>
          )}
        </SheetContent>
      </Sheet>
    </div>
  );
};

export { CampaignTargetResults };
