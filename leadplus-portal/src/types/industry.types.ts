export type Industry = {
  id: string;
  name: string;
  slug: string;
  description: string;
  image: string;
  disabled: boolean;
  active: boolean;
  createdBy: string;
  createdAt: string;
  updatedBy: string;
  updatedAt: string;
};

export type IndustryPayload = {
  name: string;
  slug: string;
  description: string;
};

export type IndustryRequest = {
  id: string;
} & IndustryPayload;

export type IndustryRequestWithImage = IndustryRequest & {
  imageFile: File | string;
};
