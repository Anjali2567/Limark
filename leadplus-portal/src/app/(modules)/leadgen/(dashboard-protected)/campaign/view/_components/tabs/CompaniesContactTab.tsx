import { useSearchParams } from "next/navigation";
import { useMemo, useRef, useState } from "react";

import { SearchBar } from "@/components/SearchBar";
import { Pagination } from "@/components/tables/Pagination";
import { useAuth } from "@/context/AuthContext";
import { useGetCampaignById } from "@/hooks/useCampaign";
import { useScrollToTop } from "@/hooks/useScrollToTop";
import { CompaniesContactCard } from "../CompaniesContactCard";
import { TabSkeleton } from "@/components/Skeleton";
import { CampaignCompanyResponse } from "@/types/campaign.types";

const PAGE_SIZE = 10;

const CompaniesContactTab = () => {
  const searchParams = useSearchParams();
  const { authenticatedUserDetails } = useAuth();

  const campaignId = searchParams.get("id") || "";

  const { data: campaign, isLoading } = useGetCampaignById({
    tenantId: authenticatedUserDetails?.tenantId || "",
    workspaceId: authenticatedUserDetails?.workspaceId || "",
    campaignId,
  });

  const [search, setSearch] = useState<string>("");
  const [pageNumber, setPageNumber] = useState<number>(0);
  const topRef = useRef<HTMLDivElement>(null);

  const { scrollToTop } = useScrollToTop(topRef);

  const filteredResults = useMemo(() => {
    const companies = campaign?.participatingCompanies ?? [];

    if (!search.trim()) return companies;
    const q = search.toLowerCase();

    return companies
      .map((company) => {
        const companyNameMatches =
          company.name.toLowerCase().includes(q) ||
          company.industry?.toLowerCase().includes(q) ||
          company.hqState?.toLowerCase().includes(q) ||
          company.hqCity?.toLowerCase().includes(q);

        if (companyNameMatches) return company;

        const matchingContacts = (company.campaignContacts ?? []).filter((contact) =>
          [contact.fullName, contact.email, contact.title]
            .filter(Boolean)
            .some((v) => v!.toLowerCase().includes(q))
        );

        if (matchingContacts.length === 0) return null;

        return {
          ...company,
          campaignContacts: matchingContacts,
        } as CampaignCompanyResponse;
      })
      .filter(Boolean) as CampaignCompanyResponse[];
  }, [campaign?.participatingCompanies, search]);

  const page = useMemo(() => {
    const totalElements = filteredResults.length;
    const totalPages = Math.ceil(totalElements / PAGE_SIZE);

    return {
      size: PAGE_SIZE,
      number: pageNumber,
      totalElements,
      totalPages,
    };
  }, [filteredResults, pageNumber]);

  const paginatedResults = useMemo(() => {
    const start = pageNumber * PAGE_SIZE;
    const end = start + PAGE_SIZE;
    return filteredResults.slice(start, end);
  }, [filteredResults, pageNumber]);

  return (
    <div ref={topRef} className="space-y-3">
      <SearchBar
        placeholder="Search companies, contacts, industries, locations..."
        inputClassName="w-full bg-input-background border-input pl-10 pr-3 py-2 rounded-lg"
        onChange={(value) => {
          setSearch(value);
          setPageNumber(0);
        }}
      />
      {isLoading ? (
        Array.from({ length: 3 }).map((_, index) => <TabSkeleton key={index} />)
      ) : (
        <>
          {paginatedResults.length > 0 ? (
            <>
              {paginatedResults.map((company) => (
                <CompaniesContactCard key={company.id} company={company} />
              ))}
              <div className="mt-6">
                <Pagination
                  params={page}
                  onPageChange={(nextPage: number) => {
                    setPageNumber(nextPage);
                    scrollToTop();
                  }}
                />
              </div>
            </>
          ) : (
            <div className="flex items-center justify-center italic mt-10 text-sm text-gray-400">
              <p>No results found</p>
            </div>
          )}
        </>
      )}
    </div>
  );
};

export { CompaniesContactTab };
