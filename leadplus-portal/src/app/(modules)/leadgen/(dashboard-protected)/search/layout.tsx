import { ReactNode, Suspense } from 'react';
import { FilterProvider } from './_context/FilterContext';

export default function SearchLayout({ children }: { children: ReactNode }) {
  return (
    <FilterProvider>
      <Suspense>{children}</Suspense>
    </FilterProvider>
  );
}
