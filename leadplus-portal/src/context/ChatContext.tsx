"use client";

import React, { createContext, useContext, useState, ReactNode } from "react";

interface ChatContextType {
  generatorConversationId: string;
  setGeneratorConversationId: (id: string) => void;
  generatorPrompt: string;
  setGeneratorPrompt: (prompt: string) => void;
  resetGenerator: () => void;
}

const ChatContext = createContext<ChatContextType | undefined>(undefined);

export const ChatProvider = ({ children }: { children: ReactNode }) => {
  const [generatorConversationId, setGeneratorConversationId] = useState("");
  const [generatorPrompt, setGeneratorPrompt] = useState("");

  const resetGenerator = () => {
    setGeneratorConversationId("");
    setGeneratorPrompt("");
  };

  return (
    <ChatContext.Provider
      value={{
        generatorConversationId,
        setGeneratorConversationId,
        generatorPrompt,
        setGeneratorPrompt,
        resetGenerator,
      }}
    >
      {children}
    </ChatContext.Provider>
  );
};

export const useChat = () => {
  const context = useContext(ChatContext);
  if (context === undefined) {
    throw new Error("useChat must be used within a ChatProvider");
  }
  return context;
};
