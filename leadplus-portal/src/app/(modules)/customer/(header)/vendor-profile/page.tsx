'use client';

import { useRouter, useSearchParams } from 'next/navigation';
import { useEffect } from 'react';

import { useAuth } from '@/context/AuthContext';
import { VendorPublicProfile } from './_components/VendorPublicProfile';

import { Loader2 } from 'lucide-react';

const VendorProfilePage = () => {
  const router = useRouter();
  const { loggedInUser, isLoading } = useAuth();

  const searchParams = useSearchParams();
  const vendorId = searchParams.get('id') ?? '';

  useEffect(() => {
    if (!isLoading && !loggedInUser) {
      router.back();
    }
  }, [isLoading, loggedInUser, router]);

  if (isLoading || !loggedInUser) {
    return (
      <div className="flex h-screen items-center justify-center">
        <Loader2 className="text-primary h-8 w-8 animate-spin" />
      </div>
    );
  }

  return <VendorPublicProfile vendorId={vendorId} />;
};

export default VendorProfilePage;
