import { Module } from '@/constants/modules.constants';

export const isValidAuthModule = (type: string | null): type is Module => {
  if (!type) return false;
  const upperType = type.toUpperCase();
  return upperType === Module.CUSTOMER || upperType === Module.VENDOR;
};

export const getAuthModuleFromUrl = (searchParams: URLSearchParams): Module => {
  const type = searchParams.get('type');
  if (type) {
    const upperType = type.toUpperCase();
    if (upperType === Module.CUSTOMER || upperType === Module.VENDOR) {
      return upperType as Module;
    }
  }
  return Module.CUSTOMER;
};
