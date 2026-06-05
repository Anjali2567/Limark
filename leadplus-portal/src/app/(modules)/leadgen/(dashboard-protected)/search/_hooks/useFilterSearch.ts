import { useState } from 'react';

import { useDebounce } from '@/hooks/useDebounce';

type SearchState = {
  industry: string;
  region: string;
  title: string;
  seniority: string;
  companyLocation: string;
  companyName: string;
  [key: string]: string;
};

type UseFilterSearchProps = {
  delay?: number;
  onSearchChange?: (id: string, value: string) => void;
};

export const useFilterSearch = ({ delay = 500, onSearchChange }: UseFilterSearchProps = {}) => {
  const [searchState, setSearchState] = useState<SearchState>({
    industry: '',
    region: '',
    title: '',
    seniority: '',
    location: '',
    companyLocation: '',
    companyName: '',
  });

  const [activeSearchState, setActiveSearchState] = useState<SearchState>({
    industry: '',
    region: '',
    title: '',
    seniority: '',
    location: '',
    companyLocation: '',
    companyName: '',
  });

  const updateSearchField = (field: keyof SearchState, value: string) => {
    setSearchState((prev) => ({ ...prev, [field]: value }));
  };

  const emptyState: SearchState = {
    industry: '',
    region: '',
    title: '',
    seniority: '',
    location: '',
    companyLocation: '',
    companyName: '',
  };

  const resetSearch = () => {
    setSearchState(emptyState);
    setActiveSearchState(emptyState);
  };

  useDebounce(searchState.industry, delay, (value) => {
    setActiveSearchState((prev) => ({ ...prev, industry: value }));
    onSearchChange?.('industries', value);
  });

  useDebounce(searchState.region, delay, (value) => {
    setActiveSearchState((prev) => ({ ...prev, region: value }));
    onSearchChange?.('regions', value);
  });

  useDebounce(searchState.title, delay, (value) => {
    setActiveSearchState((prev) => ({ ...prev, title: value }));
    onSearchChange?.('titles', value);
  });

  useDebounce(searchState.seniority, delay, (value) => {
    setActiveSearchState((prev) => ({ ...prev, seniority: value }));
    onSearchChange?.('seniority', value);
  });

  useDebounce(searchState.location, delay, (value) => {
    setActiveSearchState((prev) => ({ ...prev, location: value }));
    onSearchChange?.('locations', value);
  });

  useDebounce(searchState.companyLocation, delay, (value) => {
    setActiveSearchState((prev) => ({ ...prev, companyLocation: value }));
    onSearchChange?.('companyLocations', value);
  });

  useDebounce(searchState.companyName, delay, (value) => {
    setActiveSearchState((prev) => ({ ...prev, companyName: value }));
    onSearchChange?.('companyNames', value);
  });

  return {
    searchState,
    activeSearchState,
    updateSearchField,
    setActiveSearchState,
    resetSearch,
  };
};
