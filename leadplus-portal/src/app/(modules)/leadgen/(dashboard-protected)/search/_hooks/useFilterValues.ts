import { SetStateAction, useCallback, useEffect, useState } from 'react';
import { LeadSearchPayload } from '@/types/leadSearch.types';

const FILTERS_SESSION_KEY = 'leadSearchFilters';

export const useFilterValues = () => {
  const [filters, setFilters] = useState<LeadSearchPayload | null>(() => {
    if (typeof window === 'undefined') return null;

    try {
      const stored = sessionStorage.getItem(FILTERS_SESSION_KEY);
      return stored ? (JSON.parse(stored) as LeadSearchPayload) : null;
    } catch (e) {
      console.error('Failed to parse filters from sessionStorage', e);
      return null;
    }
  });

  // Persist filters to sessionStorage whenever they change
  useEffect(() => {
    if (typeof window === 'undefined') return;
    try {
      if (filters) {
        sessionStorage.setItem(FILTERS_SESSION_KEY, JSON.stringify(filters));
      } else {
        sessionStorage.removeItem(FILTERS_SESSION_KEY);
      }
    } catch (e) {
      console.error('Failed to save filters to sessionStorage', e);
    }
  }, [filters]);

  const updateFilters = useCallback((newFilters: SetStateAction<LeadSearchPayload | null>) => {
    setFilters(newFilters);
  }, []);

  const clearFilters = useCallback(() => {
    setFilters(null);
    if (typeof window !== 'undefined') {
      sessionStorage.removeItem(FILTERS_SESSION_KEY);
    }
  }, []);

  const resetFiltersToDefault = useCallback(() => {
    setFilters({ page: '0' });
  }, []);

  return {
    initialFilters: filters,
    filters,
    debouncedFilters: filters,
    updateFilters,
    clearFilters,
    resetFiltersToDefault,
  };
};
