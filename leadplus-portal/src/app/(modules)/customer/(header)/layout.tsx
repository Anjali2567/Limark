'use client';

import { PropsWithChildren } from 'react';

import { Module } from '@/constants/modules.constants';
import PublicHeaderLayout from '@/components/PublicHeaderLayout';

const CustomerHeaderLayout = ({ children }: PropsWithChildren) => {
  return <PublicHeaderLayout userType={Module.CUSTOMER}>{children}</PublicHeaderLayout>;
};

export default CustomerHeaderLayout;
