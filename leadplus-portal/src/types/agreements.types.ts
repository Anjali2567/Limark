import { AgreementType } from '@/constants/agreement.constant';
import { UserIdentity } from './user.types';
import { PaginationParams } from './paginated-params';

export type Agreement = {
  id: string;
  version: number;
  content: string;
  agreementType: AgreementType;
  latest: boolean;
  createdAt: string;
  createdBy: UserIdentity;
};

export type AgreementParams = {
  agreementType?: AgreementType;
} & PaginationParams;

export type AgreementPayload = {
  content: string;
};

export type AgreementRequest = {
  agreementType: AgreementType;
} & AgreementPayload;
