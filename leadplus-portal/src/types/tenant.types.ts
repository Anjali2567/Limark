import {
  TenantAnnouncementContactSource,
  TenantAnnouncementContactStatus,
  TenantAnnouncementStatus,
} from '@/constants/announcement.constants';
import { MailboxConnectionStatus, MailboxType } from '@/constants/mailbox.constants';
import { Module } from '@/constants/modules.constants';
import { PaginationParams } from './paginated-params';
import { Recipient } from './workspace.types';

export type TenantModules = {
  modules: Module[];
};

export type TenantRecipients = {
  ccRecipients?: Recipient[] | null;
  bccRecipients?: Recipient[] | null;
};

export type Tenant = {
  id: string;
  name: string;
  ownerId: string;
  zohoConnected: boolean;
  zohoEmail?: string;
  hubspotConnected: boolean;
  hubspotEmail?: string;
  announcementFromEmail?: string;
  announcementSenderName?: string;
  announcementType?: MailboxType;
  announcementStatus?: MailboxConnectionStatus;
  createdAt: Date;
  updatedAt: Date;
} & TenantRecipients;

export type TenantAnnouncementSmtpRequest = TenantRequest & {
  payload: {
    fromEmail: string;
    senderName: string;
    appPassword: string;
  };
};

export type TenantRequest = {
  tenantId: string;
};

export type IdentityAuthRequest = {
  code: string;
  redirectUri: string;
};

export type ZohoIdentityAuthRequest = {
  payload: IdentityAuthRequest;
  tenantId: string;
};

export type HubSpotIdentityAuthRequest = {
  payload: IdentityAuthRequest;
  tenantId: string;
};

export type TenantWorkspaceResponse = {
  id: string;
  workspaceName: string;
  ownerId: string;
  ownerName: string;
  totalMembers: number;
  createdAt: Date;
};

export type TenantWorkspaceSearch = {
  tenantId: string;
  params: PaginationParams;
};

export type TenantUserResponse = {
  id: string;
  name: string;
  email: string;
  workspacesCount: number;
  createdAt: Date;
};

export type TenantUserSearch = {
  tenantId: string;
  params: PaginationParams;
};

export type TenantRecipientRequest = TenantRequest & {
  payload: TenantRecipients;
};

export type TenantAnnouncement = {
  id: string;
  tenantId: string;
  name: string;
  subject: string;
  body?: string;
  status: TenantAnnouncementStatus;
  recipientCount: number;
  attachmentIds?: string[];
  launchedAt?: string;
  createdAt: string;
  updatedAt: string;
} & TenantRecipients;

export type TenantAnnouncementContact = {
  id: string;
  announcementId: string;
  sourceId: string;
  sourceType: TenantAnnouncementContactSource;
  email: string;
  firstName: string;
  status: TenantAnnouncementContactStatus;
  sentAt?: string;
};

export type CreateAnnouncementPayload = {
  name: string;
  subject: string;
  body: string;
  ccRecipients?: Recipient[];
  bccRecipients?: Recipient[];
  attachmentIds?: string[];
};

export type UpdateAnnouncementPayload = Partial<CreateAnnouncementPayload>;

export type TenantAnnouncementSearch = {
  tenantId: string;
  params: PaginationParams;
};

export type TenantContact = {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  companyName?: string;
  sourceType: 'HUBSPOT' | 'ZOHO';
};

export type TenantListing = {
  id: string;
  name: string;
  domain: string;
  modules: Module[];
  ownerName: string | null;
  createdAt: string;
};

export type TenantAnalytics = {
  tenantName: string;
  totalCampaigns: number;
  activeCampaigns: number;
  emailsSent: number;
  individualEmails: number;
  workspaceUsers: number;
  lastActivity: string | null;
};

export type TenantActivityReport = {
  tenantId: string;
  sessionCount?: number | null;
  activeHours?: number | null;
  engagementRate?: number | null;
  engagedSessionCount?: number | null;
  engagementLabel?: string | null;
};
