'use client';

import { useRouter } from 'next/navigation';
import { PropsWithChildren, ReactNode } from 'react';

import { publicNavLinks } from '@/app/_config/navlinks';
import { Header } from '@/components/Header';
import { appRoutes } from '@/config/routes';
import { Module } from '@/constants/modules.constants';
import { useAuth } from '@/context/AuthContext';

import { ClipboardList, FileText } from 'lucide-react';

type PublicHeaderLayoutProps = {
  userType: Module;
  otherOptions?: ReactNode;
};

const PublicHeaderLayout = ({
  children,
  userType,
  otherOptions,
}: PropsWithChildren<PublicHeaderLayoutProps>) => {
  const router = useRouter();
  const { loggedInUser } = useAuth();

  const menuOptions = loggedInUser
    ? [
        {
          label: 'My RFQs',
          icon: <FileText className="h-4 w-4" />,
          onClick: () => router.push(appRoutes.customer.rfq.root),
          className: 'text-slate-700 hover:text-sky-600',
        },
        {
          label: 'My RFPs',
          icon: <ClipboardList className="h-4 w-4" />,
          onClick: () => router.push(appRoutes.customer.rfp.root),
          className: 'text-slate-700 hover:text-sky-600',
        },
      ]
    : [];

  return (
    <>
      <Header
        navLinks={publicNavLinks}
        nonAuthState={!loggedInUser}
        userType={userType}
        customMenuOptions={menuOptions}
        otherOptions={otherOptions}
      />
      <main className="flex-1">{children}</main>
    </>
  );
};

export default PublicHeaderLayout;
