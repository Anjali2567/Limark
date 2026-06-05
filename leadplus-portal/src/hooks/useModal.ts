"use client";

import { useContext } from "react";
import { ActionModalContext } from "@/context/ActionConfirmationContext";

export function useModal() {
  const context = useContext(ActionModalContext);
  if (!context) {
    throw new Error("useModal must be used within a ActionModalProvider");
  }
  return context;
}
