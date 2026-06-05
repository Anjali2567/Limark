import { PromptSpecificationType } from "@/constants/prompt-specification.constants";

export type PromptSpecification = {
  id: string;
  type: PromptSpecificationType;
  promptTemplate: string;
  createdBy: string;
  createdAt: Date;
};

export type PromptSpecificationRequest = {
  type: PromptSpecificationType;
  promptTemplate: string;
};

export type PromptSpecificationParams = {
  type: PromptSpecificationType;
};
