import { AgreementType } from '@/constants/agreement.constant';

export type VendorAgreementParams = {
  vendorId: string;
  agreementType: AgreementType;
};

export type VendorAgreement = {
  vendorId: string;
  agreementType: AgreementType;
  version: number;
  agreementText: string;
  signed: boolean;
  signedAt: Date;
  createdAt: Date;
  updatedAt: Date;
};

export type VendorAgreementVerifyOtpRequest = {
  vendorId: string;
  agreementType: AgreementType;
  signedBy: string;
  name: string;
  title: string;
  otp: string;
};
