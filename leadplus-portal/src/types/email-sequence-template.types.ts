export type EmailSequenceTemplateStep = {
  stepNumber: number;
  subject: string;
  bodyTemplate: string;
  delayDays: number;
};

export type EmailSequenceTemplate = {
  id: string;
  name: string;
  description?: string;
  stepCount: number;
  steps?: EmailSequenceTemplateStep[];
  createdAt: string;
  updatedAt: string;
};

export type CreateEmailSequenceTemplateRequest = {
  name: string;
  description?: string;
  steps: EmailSequenceTemplateStep[];
};

export type EmailSequenceTemplateParams = {
  tenantId: string;
};

export type GetEmailSequenceTemplateParams = EmailSequenceTemplateParams & {
  templateId: string;
};

export type CreateEmailSequenceTemplateParams = EmailSequenceTemplateParams & {
  requestBody: CreateEmailSequenceTemplateRequest;
};

export type ImportEmailSequenceTemplateParams = {
  tenantId: string;
  workspaceId: string;
  campaignId: string;
  templateId: string;
};
