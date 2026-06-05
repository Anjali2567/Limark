'use client';

import { useRouter } from 'next/navigation';
import { useEffect } from 'react';

import { appRoutes } from '@/config/routes';

const AdminRootPage = () => {
  const router = useRouter();

  useEffect(() => {
    router.replace(appRoutes.admin.auth.login);
  }, [router]);

  return null;
};

export default AdminRootPage;
