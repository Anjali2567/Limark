export enum EmployeeRange {
  RANGE_0_500 = "0-500",
  RANGE_501_1000 = "501-1000",
  RANGE_1001_5000 = "1001-5000",
  RANGE_5001_10000 = "5001-10000",
  RANGE_10001_PLUS = "10001+",
}

export enum CampaignStatus {
  DRAFT = "DRAFT",
  PENDING_APPROVAL = "PENDING_APPROVAL",
  APPROVED = "APPROVED",
  RUNNING = "RUNNING",
  PAUSED = "PAUSED",
  COMPLETED = "COMPLETED",
}

export enum CampaignContactStatus {
  PENDING = "PENDING",
  ACTIVE = "ACTIVE",
  PAUSED = "PAUSED",
  COMPLETED = "COMPLETED",
  BOUNCED = "BOUNCED",
  UNSUBSCRIBED = "UNSUBSCRIBED",
}

export enum EmailDeliveryStatus {
  SENT = "SENT",
  OPENED = "OPENED",
  REPLIED = "REPLIED",
  BOUNCED = "BOUNCED",
}

export const editableCampaignStatuses = [
  CampaignStatus.DRAFT,
  CampaignStatus.PAUSED,
  CampaignStatus.RUNNING,
];

export const campaignStatusList = [
  { label: "All", value: null },
  { label: "Draft", value: CampaignStatus.DRAFT },
  { label: "Pending Approval", value: CampaignStatus.PENDING_APPROVAL },
  { label: "Approved", value: CampaignStatus.APPROVED },
  { label: "Running", value: CampaignStatus.RUNNING },
  { label: "Paused", value: CampaignStatus.PAUSED },
  { label: "Completed", value: CampaignStatus.COMPLETED },
];

export enum CampaignEmailStatus {
  PENDING = "PENDING",
  RUNNING = "RUNNING",
  PAUSED = "PAUSED",
  COMPLETED = "COMPLETED",
}
