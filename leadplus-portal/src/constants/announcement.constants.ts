export enum TenantAnnouncementStatus {
  DRAFT = 'DRAFT',
  IN_PROGRESS = 'IN_PROGRESS',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED',
}

export enum TenantAnnouncementContactStatus {
  PENDING = 'PENDING',
  SENT = 'SENT',
  BOUNCED = 'BOUNCED',
  FAILED = 'FAILED',
}

export enum TenantAnnouncementContactSource {
  LEAD = 'LEAD',
  CRM = 'CRM',
}
