export enum PromptSpecificationType {
  CAMPAIGN_EMAIL_GENERATOR = "CAMPAIGN_EMAIL_GENERATOR",
  CAMPAIGN_GENERATOR = "CAMPAIGN_GENERATOR",
  CAMPAIGN_COPILOT = "CAMPAIGN_COPILOT",
}

export const promptSpecificationTypeList = [
  {
    value: PromptSpecificationType.CAMPAIGN_EMAIL_GENERATOR,
    label: "Campaign Email Generator",
  },
  {
    value: PromptSpecificationType.CAMPAIGN_GENERATOR,
    label: "Campaign Generator",
  },
  {
    value: PromptSpecificationType.CAMPAIGN_COPILOT,
    label: "Campaign Co-pilot",
  },
];
