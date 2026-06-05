export type Service = {
  id: number;
  name: string;
  slug: string;
  serviceCategoryId: number;
  disabled: boolean;
  active: boolean;
  createdBy: string;
  createdAt: Date;
  updatedBy: string;
  updatedAt: Date;
};

export type ServiceParams = {
  serviceCategoryId?: number;
  industryId?: number;
};

export type ServiceCategory = {
  id: number;
  name: string;
};
