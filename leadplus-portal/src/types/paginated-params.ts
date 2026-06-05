export type PaginationParams = {
  page: string | number;
  size: string | number;
  sort: string;
  query?: string;
};

export type Page = {
  size: number;
  number: number;
  totalElements: number;
  totalPages: number;
};

export const defaultPaginationParams: PaginationParams = {
  page: '0',
  size: '10',
  sort: 'updatedAt,desc',
};

export const singleItemPaginationParams: PaginationParams = {
  ...defaultPaginationParams,
  size: '1',
};

export const bulkItemPaginationParams: PaginationParams = {
  ...defaultPaginationParams,
  size: '100',
};

export interface PaginatedResponse<T> {
  content: T[];
  page: Page;
}
