export type ChatResponse = {
  messageId: string;
  conversationId: string;
  response: string;
};

export type ChatRequest = {
  conversationId?: string;
  request: string;
};
