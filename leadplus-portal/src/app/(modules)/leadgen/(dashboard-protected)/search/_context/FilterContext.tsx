'use client';

import {
  createContext,
  useContext,
  ReactNode,
  useEffect,
  useState,
  Dispatch,
  SetStateAction,
} from 'react';
import { LeadCompanyData, LeadSearchPayload } from '@/types/leadSearch.types';
import { useFilterValues } from '../_hooks/useFilterValues';

interface FilterContextType {
  filters: LeadSearchPayload | null;
  debouncedFilters: LeadSearchPayload | null;
  updateFilters: (newFilters: React.SetStateAction<LeadSearchPayload | null>) => void;
  clearFilters: () => void;
  resetFiltersToDefault: () => void;
  initialFilters: LeadSearchPayload | null;
  isChatMode: boolean;
  setChatMode: (isChatMode: boolean) => void;
  activeConversationId?: number;
  setActiveConversationId: (conversationId?: number) => void;
  selectedCompanyRows: LeadCompanyData[];
  setSelectedCompanyRows: Dispatch<SetStateAction<LeadCompanyData[]>>;
}

const FilterContext = createContext<FilterContextType | undefined>(undefined);

export const FilterProvider = ({ children }: { children: ReactNode }) => {
  const {
    filters,
    debouncedFilters,
    updateFilters,
    clearFilters,
    resetFiltersToDefault,
    initialFilters,
  } = useFilterValues();
  const [isChatMode, setChatMode] = useState(() => {
    if (typeof window === 'undefined') return false;
    return sessionStorage.getItem('leadChatMode') === 'true';
  });
  const [activeConversationId, setActiveConversationId] = useState<number | undefined>(() => {
    if (typeof window === 'undefined') return undefined;
    const stored = sessionStorage.getItem('leadChatConversationId');
    if (!stored) return undefined;
    const parsed = Number(stored);
    return Number.isFinite(parsed) ? parsed : undefined;
  });
  const [selectedCompanyRows, setSelectedCompanyRows] = useState<LeadCompanyData[]>(() => {
    if (typeof window === 'undefined') return [];
    const stored = sessionStorage.getItem('selectedCompanyRows');
    if (!stored) return [];

    try {
      const rows = JSON.parse(stored);
      return Array.isArray(rows) ? rows : [];
    } catch (error) {
      console.error('Failed to restore selectedCompanyRows from sessionStorage', error);
      return [];
    }
  });

  useEffect(() => {
    if (typeof window === 'undefined') return;

    if (selectedCompanyRows.length > 0) {
      sessionStorage.setItem('selectedCompanyRows', JSON.stringify(selectedCompanyRows));
    } else {
      sessionStorage.removeItem('selectedCompanyRows');
    }
  }, [selectedCompanyRows]);

  useEffect(() => {
    if (typeof window === 'undefined') return;
    sessionStorage.setItem('leadChatMode', String(isChatMode));
  }, [isChatMode]);

  useEffect(() => {
    if (typeof window === 'undefined') return;
    if (activeConversationId !== undefined) {
      sessionStorage.setItem('leadChatConversationId', String(activeConversationId));
    } else {
      sessionStorage.removeItem('leadChatConversationId');
    }
  }, [activeConversationId]);

  return (
    <FilterContext.Provider
      value={{
        filters,
        debouncedFilters,
        updateFilters,
        clearFilters,
        resetFiltersToDefault,
        initialFilters,
        isChatMode,
        setChatMode,
        activeConversationId,
        setActiveConversationId,
        selectedCompanyRows,
        setSelectedCompanyRows,
      }}
    >
      {children}
    </FilterContext.Provider>
  );
};

export const useFilterContext = () => {
  const context = useContext(FilterContext);
  if (context === undefined) {
    throw new Error('useFilterContext must be used within FilterProvider');
  }
  return context;
};
