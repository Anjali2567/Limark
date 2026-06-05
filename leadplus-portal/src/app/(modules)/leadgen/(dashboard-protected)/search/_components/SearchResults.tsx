import { Dispatch, SetStateAction, useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { useRouter } from 'next/navigation';
import { toast } from 'sonner';

import { AddToListPopover } from '@/app/(modules)/leadgen/(dashboard-protected)/lists/_components/AddToListPopover';
import DataTable from '@/components/tables/DataTable';
import { SortConfig } from '@/components/tables/SortableHeader';
import { Textarea } from '@/components/ui/textarea';
import { LeadType } from '@/constants/leadSearch.constants';
import { appRoutes } from '@/config/routes';
import { useAuth } from '@/context/AuthContext';
import { useGetLeadChatMessages, useParseQuery } from '@/hooks/useSearchLeads';
import { SimpleMarkdown } from '@/components/chat/SimpleMarkdown';
import {
  LeadChatMessage,
  LeadCompanyData,
  LeadContactData,
  LeadFilterCriteria,
  LeadSearchPayload,
} from '@/types/leadSearch.types';
import { PaginatedResponse } from '@/types/paginated-params';
import { ColumnType } from '@/types/table.types';
import { ScrollArea } from '@radix-ui/react-scroll-area';
import { BookMarked, ChevronDown } from 'lucide-react';
import { useFilterContext } from '../_context/FilterContext';
import { SearchResultsHeader } from './SearchResultsHeader';
import { SelectAllBanner } from './SelectAllBanner';
import { Button } from '@/components/ui/button';

const mergeKeywords = (criteria: LeadFilterCriteria) =>
  Array.from(
    new Map(
      [
        ...(criteria.keywords ?? []),
        ...(criteria.toolsServices ?? []),
        ...(criteria.technologies ?? []),
      ].map((keyword) => [keyword.toLowerCase(), keyword])
    ).values()
  );

const getChatMessageKey = (message: LeadChatMessage) =>
  message.messageId?.toString() ??
  `${message.conversationId ?? ''}:${message.request ?? ''}:${message.response ?? ''}:${message.createdAt ?? ''}`;

const getChatStorageKey = (conversationId?: number) =>
  conversationId ? `leadplus:lead-chat-messages:${conversationId}` : null;

const readStoredChatMessages = (conversationId?: number): LeadChatMessage[] => {
  if (typeof window === 'undefined' || !conversationId) return [];

  const storageKey = getChatStorageKey(conversationId);
  if (!storageKey) return [];

  try {
    const stored = sessionStorage.getItem(storageKey);
    if (!stored) return [];

    const parsed = JSON.parse(stored) as LeadChatMessage[];
    return Array.isArray(parsed) ? parsed : [];
  } catch {
    return [];
  }
};

const buildChatRequestCriteria = (
  filters: LeadSearchPayload | null | undefined,
  isCompanySearch: boolean
): LeadFilterCriteria | undefined => {
  if (!filters) return undefined;

  const criteria: LeadFilterCriteria = {
    companyIds: filters.companyIds,
    contactNames: filters.contactNames,
    companyNames: filters.companyNames,
    cities: isCompanySearch ? filters.companyCities : filters.cities,
    states: isCompanySearch ? filters.companyStates : filters.states,
    countries: isCompanySearch ? filters.companyCountries : filters.countries,
    companyCities: filters.companyCities,
    companyStates: filters.companyStates,
    companyCountries: filters.companyCountries,
    regions: filters.regions,
    postalCodes: filters.postalCodes,
    keywords: filters.keywords,
    industries: filters.industries,
    employeeRanges: filters.employeeRanges,
    revenueRanges: filters.revenueRanges,
    technologies: filters.technologies,
    toolsServices: filters.toolsServices,
    sicCodes: filters.sicCodes,
    naicsCodes: filters.naicsCodes,
    titles: filters.titles,
    seniority: filters.seniority,
    departments: filters.departments,
    campaignEligibleOnly: filters.campaignEligibleOnly,
    aggregateTechSearch: filters.aggregateTechSearch,
  };

  const hasAtLeastOneCriteria = Object.values(criteria).some(
    (value) => (Array.isArray(value) && value.length > 0) || value === true
  );

  return hasAtLeastOneCriteria ? criteria : undefined;
};

type SearchResultsProps<T extends LeadCompanyData | LeadContactData> = {
  data?: PaginatedResponse<T>;
  columns?: ColumnType<T>[];
  isLoading?: boolean;
  onCollapse?: () => void;
  proceedButtonText?: string;
  onProceed?: (rows: T[]) => void;
  onSendEmail?: (rows: T[]) => void;
  isProceeding?: boolean;
  searchPlaceholder?: string;
  tenantId?: string;
  workspaceId?: string;
  leadType?: LeadType;
  selectedRows: T[];
  onAddContacts?: (rows: T[]) => void;
  setSelectedRows: Dispatch<SetStateAction<T[]>>;
  onFetchAllIds?: () => Promise<string[]>;
  isFetchingIds?: boolean;
};

const SearchResults = <T extends LeadCompanyData | LeadContactData>({
  columns = [],
  data,
  isLoading = false,
  onCollapse = () => {},
  proceedButtonText,
  onProceed,
  onSendEmail,
  isProceeding,
  searchPlaceholder,
  tenantId,
  workspaceId,
  leadType,
  selectedRows,
  setSelectedRows,
  onAddContacts,
  onFetchAllIds,
  isFetchingIds = false,
}: SearchResultsProps<T>) => {
  const router = useRouter();
  const {
    debouncedFilters,
    updateFilters,
    filters,
    clearFilters,
    isChatMode,
    setChatMode,
    activeConversationId,
    setActiveConversationId,
    setSelectedCompanyRows,
  } = useFilterContext();
  const { authenticatedUserDetails } = useAuth();

  const [searchQuery, setSearchQuery] = useState<string>('');
  const [pendingUserMessage, setPendingUserMessage] = useState<string | null>(null);
  const retainedCriteriaRef = useRef<LeadFilterCriteria | undefined>(undefined);
  const [isRedirecting, setIsRedirecting] = useState(false);
  const [isSelectAllActive, setIsSelectAllActive] = useState(false);
  const [showScrollToBottom, setShowScrollToBottom] = useState(false);
  const chatScrollContainerRef = useRef<HTMLDivElement | null>(null);
  const chatBottomRef = useRef<HTMLDivElement | null>(null);
  const chatComposerRef = useRef<HTMLTextAreaElement | null>(null);

  // Reset local select-all flag when search filters change (not page/sort/resultsSearch)
  const searchFilterKey = useMemo(() => {
    if (!debouncedFilters) return null;
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    const { page, sort, resultsSearch, ...searchFilters } = debouncedFilters;
    return JSON.stringify(searchFilters);
  }, [debouncedFilters]);

  const [prevSearchFilterKey, setPrevSearchFilterKey] = useState(searchFilterKey);

  if (searchFilterKey !== prevSearchFilterKey) {
    setPrevSearchFilterKey(searchFilterKey);
    setIsSelectAllActive(false);
  }

  const [sortConfig, setSortConfig] = useState<SortConfig | null>(() => {
    if (filters?.sort) {
      const [key, direction] = filters.sort.split(',');
      return { key, direction: direction as 'asc' | 'desc' };
    }
    return null;
  });

  const { mutate, isPending: isQueryPending } = useParseQuery();
  const { data: chatHistory, isLoading: isChatHistoryLoading } = useGetLeadChatMessages(
    authenticatedUserDetails?.tenantId ?? '',
    isChatMode ? activeConversationId : undefined
  );

  const [chatMessages, setChatMessages] = useState<LeadChatMessage[]>(() =>
    readStoredChatMessages(activeConversationId)
  );

  useEffect(() => {
    const currentCriteria = buildChatRequestCriteria(filters, leadType === LeadType.LEAD_COMPANY);
    if (currentCriteria) {
      retainedCriteriaRef.current = currentCriteria;
    }
  }, [filters, leadType]);

  useEffect(() => {
    if (typeof window === 'undefined') return;

    const storageKey = getChatStorageKey(activeConversationId);
    if (!storageKey) {
      return;
    }

    try {
      sessionStorage.setItem(storageKey, JSON.stringify(chatMessages));
    } catch {
      // Ignore storage write failures.
    }
  }, [activeConversationId, chatMessages]);

  useEffect(() => {
    if (!chatHistory) return;

    setChatMessages((previousMessages) => {
      const previousByKey = new Map(
        previousMessages.map((message) => [getChatMessageKey(message), message])
      );

      const historyKeys = new Set(chatHistory.map((message) => getChatMessageKey(message)));
      const mergedHistory = chatHistory.map(
        (message) => previousByKey.get(getChatMessageKey(message)) ?? message
      );
      const optimisticMessages = previousMessages.filter(
        (message) => !historyKeys.has(getChatMessageKey(message))
      );

      return [...mergedHistory, ...optimisticMessages];
    });
  }, [chatHistory]);

  const updateScrollToBottomVisibility = useCallback(() => {
    const container = chatScrollContainerRef.current;
    if (!container) return;

    const distanceFromBottom =
      container.scrollHeight - container.scrollTop - container.clientHeight;
    setShowScrollToBottom(distanceFromBottom > 96);
  }, []);

  const scrollToChatBottom = useCallback((behavior: ScrollBehavior = 'smooth') => {
    const container = chatScrollContainerRef.current;
    if (!container) return;

    container.scrollTo({
      top: container.scrollHeight,
      behavior,
    });
    setShowScrollToBottom(false);
  }, []);

  useEffect(() => {
    if (!isChatMode || isChatHistoryLoading) {
      return;
    }
    const timeout = window.setTimeout(() => {
      scrollToChatBottom('smooth');
    }, 0);
    return () => window.clearTimeout(timeout);
  }, [chatMessages, isChatHistoryLoading, isChatMode, pendingUserMessage, scrollToChatBottom]);

  useEffect(() => {
    const textarea = chatComposerRef.current;
    if (!textarea) return;
    textarea.style.height = 'auto';
    textarea.style.height = `${Math.min(textarea.scrollHeight, 160)}px`;
  }, [searchQuery]);

  const handlePageChange = useCallback(
    (newPage: number) => {
      updateFilters((prev) => ({ ...(prev || {}), page: newPage.toString() }));
    },
    [updateFilters]
  );

  const handleSortChange = useCallback(
    (newSortConfig: SortConfig | null) => {
      setSortConfig(newSortConfig);
      const sortValue = newSortConfig
        ? `${newSortConfig.key},${newSortConfig.direction}`
        : undefined;
      updateFilters((prev) => ({
        ...(prev || {}),
        page: '0',
        sort: sortValue,
      }));
    },
    [updateFilters]
  );

  const onSearch = useCallback(() => {
    if (!authenticatedUserDetails) return;
    const queryToSend = searchQuery.trim();
    if (!queryToSend || isQueryPending || isRedirecting) return;

    const isCompanySearch = leadType === LeadType.LEAD_COMPANY;
    const currentCriteria =
      buildChatRequestCriteria(filters, isCompanySearch) ?? retainedCriteriaRef.current;
    setPendingUserMessage(queryToSend);
    setSearchQuery('');

    mutate(
      {
        tenantId: authenticatedUserDetails.tenantId,
        query: queryToSend,
        conversationId: activeConversationId,
        criteria: currentCriteria,
      },
      {
        onSuccess: (data) => {
          setPendingUserMessage(null);
          setActiveConversationId(data.conversationId);
          setChatMessages((previousMessages) => {
            const nextMessage: LeadChatMessage = {
              messageId: data.messageId,
              conversationId: data.conversationId,
              request: data.request,
              response: data.response,
              createdAt: new Date().toISOString(),
            };
            if (!nextMessage.messageId) {
              return [...previousMessages, nextMessage];
            }
            const withoutCurrent = previousMessages.filter(
              (message) => message.messageId !== nextMessage.messageId
            );
            return [...withoutCurrent, nextMessage];
          });
          const criteria = data.criteria ?? {};
          const hasPeopleFilters = [criteria.titles, criteria.seniority, criteria.departments].some(
            (field) => (field?.length ?? 0) > 0
          );

          const keywords = mergeKeywords(criteria);

          // Redirect company search -> people search
          if (isCompanySearch && hasPeopleFilters) {
            const peoplePayload: LeadSearchPayload = {
              companyNames: criteria.companyNames,
              cities: criteria.cities,
              states: criteria.states,
              countries: criteria.countries,
              companyCities: criteria.companyCities ?? filters?.companyCities ?? [],
              companyStates: criteria.companyStates ?? filters?.companyStates ?? [],
              companyCountries: criteria.companyCountries ?? filters?.companyCountries ?? [],
              regions: criteria.regions,
              keywords,
              industries: criteria.industries,
              employeeRanges: criteria.employeeRanges,
              revenueRanges: criteria.revenueRanges,
              titles: criteria.titles,
              seniority: criteria.seniority,
              departments: criteria.departments,
              page: '0',
            };

            setIsRedirecting(true);
            updateFilters(peoplePayload);
            router.push(`${appRoutes.leadgen.search.contactSearch}?tabSwitch=true`);
            return;
          }

          const payload: LeadSearchPayload = {
            ...criteria,
            companyIds: criteria.companyIds?.map(String),
            keywords,
            cities: isCompanySearch ? [] : criteria.cities,
            states: isCompanySearch ? [] : criteria.states,
            countries: isCompanySearch ? [] : criteria.countries,
            companyCities: isCompanySearch
              ? (criteria.companyCities?.length ? criteria.companyCities : (criteria.cities ?? []))
              : (criteria.companyCities ?? filters?.companyCities ?? []),
            companyStates: isCompanySearch
              ? (criteria.companyStates?.length ? criteria.companyStates : (criteria.states ?? []))
              : (criteria.companyStates ?? filters?.companyStates ?? []),
            companyCountries: isCompanySearch
              ? (criteria.companyCountries?.length ? criteria.companyCountries : (criteria.countries ?? []))
              : (criteria.companyCountries ?? filters?.companyCountries ?? []),
            toolsServices: [],
            technologies: [],
          };

          updateFilters(payload);
        },
        onError: () => {
          setPendingUserMessage(null);
          setSearchQuery(queryToSend);
        },
      }
    );
  }, [
    activeConversationId,
    authenticatedUserDetails,
    filters,
    isQueryPending,
    isRedirecting,
    mutate,
    searchQuery,
    setActiveConversationId,
    updateFilters,
    leadType,
    router,
  ]);

  const onResultsSearch = useCallback(
    (searchQuery: string) => {
      updateFilters((prev) => ({ ...(prev || {}), resultsSearch: searchQuery, page: '0' }));
    },
    [updateFilters]
  );

  const onSelectRows = (ids: string[]) => {
    if (ids.length === 0) {
      setSelectedRows([]);
      setIsSelectAllActive(false);
      return;
    }
    setSelectedRows((prev) => {
      const currentPageRowIds = data?.content.map((row) => row.id) || [];
      const selectionsFromOtherPages = prev.filter((row) => !currentPageRowIds.includes(row.id));
      const newlySelectedRows = data?.content.filter((row) => ids.includes(row.id)) || [];
      return [...selectionsFromOtherPages, ...newlySelectedRows];
    });
  };

  const isFiltersPending = !!filters && !debouncedFilters;

  const totalElements = data?.page?.totalElements ?? 0;
  const currentPageContent = data?.content ?? [];
  const currentPageCount = data?.content?.length ?? 0;
  const selectedCount = selectedRows.length;
  const selectedRowIds = selectedRows.map((r) => r.id);

  const isCurrentPageAllSelected =
    currentPageContent.length > 0 &&
    currentPageContent.every((row) => selectedRows.some((sr) => sr.id === row.id));

  const showSelectAllBanner =
    isSelectAllActive || (isCurrentPageAllSelected && totalElements > currentPageCount);

  const handleSelectAll = useCallback(() => {
    if (!onFetchAllIds) return;
    onFetchAllIds()
      .then(() => setIsSelectAllActive(true))
      .catch(() => toast.error('Failed to select all results. Please try again.'));
  }, [onFetchAllIds]);

  const handleClearSelection = useCallback(() => {
    setSelectedRows([]);
    setIsSelectAllActive(false);
  }, [setSelectedRows]);

  const defaultColumns = useMemo<ColumnType<T>[]>(() => {
    return columns.slice(0, 5).map((column) => ({
      ...column,
      width: '20%',
    }));
  }, [columns]);

  const handleNewChat = useCallback(() => {
    const storageKey = getChatStorageKey(activeConversationId);
    if (storageKey && typeof window !== 'undefined') {
      sessionStorage.removeItem(storageKey);
    }
    setActiveConversationId(undefined);
    setChatMessages([]);
    setPendingUserMessage(null);
    retainedCriteriaRef.current = undefined;
    setSearchQuery('');
    setSelectedRows([]);
    setSelectedCompanyRows([]);
    clearFilters();
  }, [activeConversationId, clearFilters, setActiveConversationId, setSelectedCompanyRows, setSelectedRows]);

  return (
    <>
      {!isChatMode && (debouncedFilters || filters) && (isLoading || data || isFiltersPending) ? (
        <div className="flex min-h-0 flex-1 flex-col">
          <SearchResultsHeader
            onCollapse={onCollapse}
            selectedCount={selectedCount}
            proceedButtonText={proceedButtonText}
            onProceed={() => onProceed?.(selectedRows)}
            onSendEmail={
              onSendEmail && !isSelectAllActive ? () => onSendEmail(selectedRows) : undefined
            }
            onAddContacts={onAddContacts ? () => onAddContacts(selectedRows) : undefined}
            isProceeding={isProceeding}
            resultsSearch={filters?.resultsSearch || ''}
            searchPlaceholder={searchPlaceholder}
            setResultsSearch={onResultsSearch}
            addToListSlot={
              tenantId && workspaceId && leadType ? (
                <AddToListPopover
                  tenantId={tenantId}
                  workspaceId={workspaceId}
                  type={leadType}
                  rowIds={selectedRowIds}
                  tooltip="Add to list"
                  trigger={
                    <Button
                      variant="outline"
                      aria-label={
                        selectedCount > 0 ? `Add ${selectedCount} selected to list` : 'Add to list'
                      }
                      disabled={selectedCount === 0 || isProceeding}
                    >
                      <BookMarked className="h-4 w-4" />
                      {selectedCount > 0 && (
                        <span
                          className="ml-2 rounded-full bg-white/20 px-2 py-0.5 text-xs"
                          aria-hidden="true"
                        >
                          {selectedCount}
                        </span>
                      )}
                    </Button>
                  }
                />
              ) : undefined
            }
          />
          {showSelectAllBanner && onFetchAllIds && (
            <SelectAllBanner
              pageCount={currentPageCount}
              totalCount={totalElements}
              selectedCount={selectedCount}
              isCurrentPageAllSelected={isCurrentPageAllSelected}
              onSelectAll={handleSelectAll}
              onClearSelection={handleClearSelection}
              isLoading={isFetchingIds}
              entityLabel={leadType === LeadType.LEAD_COMPANY ? 'companies' : 'contacts'}
            />
          )}
          {isLoading || isFiltersPending || (data && data.content.length === 0) ? (
            <div className="flex min-h-0 flex-1 flex-col overflow-hidden px-6 py-4">
              <DataTable
                rowClassName="h-12"
                data={data?.content ?? []}
                pagination={data?.page}
                columns={defaultColumns}
                isLoading={isLoading || isFiltersPending}
                onPageChange={handlePageChange}
                initialPage={Number(filters?.page || '0')}
              />
            </div>
          ) : (
            <ScrollArea className="flex-1 overflow-y-auto">
              <div className="flex min-h-0 flex-1 flex-col overflow-hidden px-6 py-4">
                <DataTable
                  rowClassName="h-12"
                  style="grid"
                  data={data?.content ?? []}
                  pagination={data?.page}
                  columns={columns}
                  isLoading={isLoading}
                  onPageChange={handlePageChange}
                  initialPage={Number(filters?.page || '0')}
                  selectable
                  scrollHorizontal
                  selectedRowIds={selectedRowIds}
                  onSelectionChange={onSelectRows}
                  freezeColumnsCount={1}
                  sortConfig={sortConfig}
                  onSortChange={handleSortChange}
                />
              </div>
            </ScrollArea>
          )}
        </div>
      ) : (
        <div className="flex h-full w-full flex-col bg-slate-50">
          <div className="border-border bg-card flex h-16 items-center justify-between border-b px-6 py-4">
            <h2 className="text-foreground text-lg font-semibold">Lead Assistant Chat</h2>
            <div className="flex items-center gap-2">
              <Button variant="outline" onClick={handleNewChat}>
                New Chat
              </Button>
              <Button onClick={() => setChatMode(false)}>View Results</Button>
            </div>
          </div>
          <div className="relative min-h-0 flex-1">
            <div
              ref={chatScrollContainerRef}
              className="h-full overflow-y-auto"
              onScroll={updateScrollToBottomVisibility}
            >
              <div className="mx-auto flex w-full max-w-4xl flex-col gap-4 p-6">
                {isChatHistoryLoading ? (
                  <p className="text-muted-foreground text-sm">Loading conversation...</p>
                ) : chatMessages.length > 0 ? (
                  chatMessages.map((message) => (
                    <div
                      key={message.messageId ?? `${message.request}-${message.createdAt}`}
                      className="space-y-3"
                    >
                      {message.request && (
                        <div className="flex justify-end">
                          <div className="bg-primary text-primary-foreground max-w-[80%] rounded-2xl px-4 py-3 text-sm">
                            {message.request}
                          </div>
                        </div>
                      )}
                      {message.response && (
                        <div className="flex justify-start">
                          <div className="bg-card border-border max-w-[80%] rounded-2xl border px-4 py-3 text-sm">
                            <SimpleMarkdown
                              text={message.response}
                              className="text-foreground text-sm"
                            />
                          </div>
                        </div>
                      )}
                    </div>
                  ))
                ) : !pendingUserMessage ? (
                  <p className="text-muted-foreground text-sm">
                    Start a new conversation to refine your filters with chat.
                  </p>
                ) : null}
                {pendingUserMessage && (
                  <div className="space-y-3">
                    <div className="flex justify-end">
                      <div className="bg-primary text-primary-foreground max-w-[80%] rounded-2xl px-4 py-3 text-sm">
                        {pendingUserMessage}
                      </div>
                    </div>
                    <div className="flex justify-start">
                      <div className="bg-card border-border max-w-[80%] rounded-2xl border px-4 py-3 text-sm">
                        <div className="flex items-center gap-1">
                          <span className="bg-foreground/50 h-2 w-2 animate-pulse rounded-full" />
                          <span className="bg-foreground/50 h-2 w-2 animate-pulse rounded-full [animation-delay:120ms]" />
                          <span className="bg-foreground/50 h-2 w-2 animate-pulse rounded-full [animation-delay:240ms]" />
                        </div>
                      </div>
                    </div>
                  </div>
                )}
                <div ref={chatBottomRef} />
              </div>
            </div>
            {showScrollToBottom && (
              <Button
                type="button"
                variant="outline"
                size="icon"
                className="bg-card absolute bottom-4 left-1/2 h-9 w-9 -translate-x-1/2 rounded-full shadow-md"
                aria-label="Scroll to bottom"
                onClick={() => scrollToChatBottom()}
              >
                <ChevronDown className="h-4 w-4" />
              </Button>
            )}
          </div>
          <div className="border-border bg-card border-t p-4">
            <div className="mx-auto flex w-full max-w-4xl items-end gap-3">
              <Textarea
                ref={chatComposerRef}
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                onKeyDown={(e) => {
                  if (
                    e.key === 'Enter' &&
                    !e.shiftKey &&
                    searchQuery.trim() &&
                    !isQueryPending &&
                    !isRedirecting
                  ) {
                    e.preventDefault();
                    onSearch();
                  }
                }}
                placeholder="Type a message to refine your search criteria..."
                rows={1}
                className="max-h-40 min-h-11 resize-none"
              />
              <Button
                onClick={onSearch}
                disabled={!searchQuery.trim() || isQueryPending || isRedirecting}
                className="h-11"
              >
                {isQueryPending || isRedirecting ? 'Sending...' : 'Send'}
              </Button>
            </div>
          </div>
        </div>
      )}
    </>
  );
};

export { SearchResults };
