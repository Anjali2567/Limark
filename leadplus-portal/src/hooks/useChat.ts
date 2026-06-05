import { sendChatMessage } from '@/lib/api/chat.api';
import { ChatRequest, ChatResponse } from '@/types/chat.types';
import { useMutation, UseMutationResult } from '@tanstack/react-query';
import { AxiosError } from 'axios';

export const useChat = (): UseMutationResult<ChatResponse, AxiosError, ChatRequest> => {
  return useMutation({
    mutationFn: sendChatMessage,
  });
};
