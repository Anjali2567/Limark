'use client';

import { useRouter } from 'next/navigation';
import { useEffect } from 'react';

import { appRoutes } from '@/config/routes';

const LeadGenRootPage = () => {
  const router = useRouter();

  useEffect(() => {
    router.replace(appRoutes.leadgen.auth.login);
  }, [router]);

  return null;
};

export default LeadGenRootPage;
