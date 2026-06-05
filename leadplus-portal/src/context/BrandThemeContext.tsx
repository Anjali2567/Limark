'use client';

import { createContext, useContext, useEffect, useState, ReactNode } from 'react';

export type BrandTheme = 'blue' | 'gold';

interface BrandThemeContextType {
  brandTheme: BrandTheme;
  setBrandTheme: (theme: BrandTheme) => void;
}

const BrandThemeContext = createContext<BrandThemeContextType>({
  brandTheme: 'blue',
  setBrandTheme: () => {},
});

export function BrandThemeProvider({ children }: { children: ReactNode }) {
  const [brandTheme, setBrandThemeState] = useState<BrandTheme>('blue');

  // Apply class to <html> whenever the theme changes
  useEffect(() => {
    const root = document.documentElement;
    root.classList.remove('theme-blue', 'theme-gold');
    if (brandTheme === 'gold') {
      root.classList.add('theme-gold');
    }
  }, [brandTheme]);

  const setBrandTheme = (theme: BrandTheme) => {
    setBrandThemeState(theme);
  };

  return (
    <BrandThemeContext.Provider value={{ brandTheme, setBrandTheme }}>
      {children}
    </BrandThemeContext.Provider>
  );
}

export function useBrandTheme() {
  return useContext(BrandThemeContext);
}
