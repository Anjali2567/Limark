export type Specification = {
  id: number;
  name: string;
  type: string;
  icon: string;
  specificationCategoryId: number;
  disabled: boolean;
  active: boolean;
  createdBy: string;
  createdAt: Date;
  updatedBy: string;
  updatedAt: Date;
};

export type SpecificationParams = {
  specificationCategoryId?: number;
  serviceId?: number;
};
