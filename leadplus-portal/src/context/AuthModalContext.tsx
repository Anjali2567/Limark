'use client';

import { createContext, ReactNode, useContext } from 'react';

import useToggle from '@/hooks/useToggle';

type AuthModalContextType = {
  isOpen: boolean;
  showAuthModal: () => void;
  closeAuthModal: () => void;
};

const AuthModalContext = createContext<AuthModalContextType | null>(null);

export const AuthModalProvider = ({ children }: { children: ReactNode }) => {
  const { value: isOpen, setValue: setIsOpen } = useToggle(false);

  const showAuthModal = () => {
    setIsOpen(true);
  };

  const closeAuthModal = () => {
    setIsOpen(false);
  };

  return (
    <AuthModalContext.Provider value={{ isOpen, showAuthModal, closeAuthModal }}>
      {children}
    </AuthModalContext.Provider>
  );
};

export const useAuthModal = () => {
  const context = useContext(AuthModalContext);
  if (!context) {
    throw new Error('useAuthModal must be used within AuthModalProvider');
  }
  return context;
};
