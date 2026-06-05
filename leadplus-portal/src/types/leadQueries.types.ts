import { LeadQueryType } from '@/constants/leadQueries.constant';
import { PaginationParams } from './paginated-params';

export type LeadQuery = {
  value: string;
  type: LeadQueryType;
};

export type LeadQueryRequest = {
  types: LeadQueryType[];
} & PaginationParams;
