import { createEmailImage } from '@/lib/api/email-image.api';
import { EmailImageSourceType } from '@/constants/email-image.constant';
import { EmailImageRequest, EmailImageResponse } from '@/types/email-image.types';
import { useMutation, UseMutationResult } from '@tanstack/react-query';
import { AxiosError } from 'axios';
import { useCallback } from 'react';

export const useCreateEmailImage = (): UseMutationResult<
  EmailImageResponse,
  AxiosError,
  EmailImageRequest
> => {
  return useMutation({
    mutationFn: createEmailImage,
  });
};

export const useRichTextImageUpload = (params: {
  sourceId: string;
  sourceType: EmailImageSourceType;
}) => {
  const { mutateAsync } = useCreateEmailImage();

  const onImageUpload = useCallback(
    async (file: File): Promise<string | null> => {
      try {
        const response = await mutateAsync({ ...params, file });
        const url = response.resourceUrl.startsWith('http')
          ? response.resourceUrl
          : `${process.env.NEXT_PUBLIC_BASE_URL}${response.resourceUrl}`;
        return url;
      } catch {
        return null;
      }
    },
    [mutateAsync, params]
  );

  return { onImageUpload };
};
