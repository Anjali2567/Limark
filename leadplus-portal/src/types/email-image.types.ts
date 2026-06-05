import { EmailImageSourceType } from '@/constants/email-image.constant';

export type EmailImageResponse = {
  id: string;
  sourceId: string;
  sourceType: EmailImageSourceType;
  resourceUrl: string;
  createdAt: string;
};

export type EmailImageRequest = {
  sourceId: string;
  sourceType: EmailImageSourceType;
  file: File;
};
