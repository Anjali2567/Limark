import {
  CampaignContactStatus,
  CampaignStatus,
  EmailDeliveryStatus,
  EmployeeRange,
} from '@/constants/Campaign';
import { BasicParams } from './user.types';
import { PaginationParams } from './paginated-params';
import { Recipient } from './workspace.types';
import { LeadFilterCriteria } from './leadSearch.types';

export type LocationFilterDto = {
  cities?: string[] | null;
  states?: string[] | null;
  countries?: string[] | null;
  regions?: string[] | null;
};

export type TargetingCriteriaResponse = {
  searchQuery?: string;
  employeeRanges?: EmployeeRange[];
  companyLocationFilter?: LocationFilterDto;
  companyExcludeLocationFilter?: LocationFilterDto;
  companyIds?: string[];
  contactLocationFilter?: LocationFilterDto;
  jobTitles?: string[];
  contactIds?: string[];
};

export type LeadFilter = {
  companyNames: string[];
  companyCities: string[];
  companyStates: string[];
  companyCountries: string[];
  regions: string[];
  keywords: string[];
  industries: string[];
  employeeRanges: EmployeeRange[];
  revenueRanges: string[];
  technologies: string[];
  titles: string[];
  seniority: string[];
  departments: string[];
  postalCodes: string[];
  sicCodes: string[];
  naicsCodes: string[];
  cities: string[];
  states: string[];
  countries: string[];
};

export type MessageResponse = {
  fromAddress: string;
  toAddresses: string[];
  ccAddresses: string[];
  bccAddresses: string[];
  subject: string;
  body: string;
};

export type EmailDataResponse = {
  sentAt: Date;
  emailDeliveryStatus?: EmailDeliveryStatus;
  messages: MessageResponse[];
};

export type SendingWindow = {
  windowStart: string;
  windowEnd: string;
  sendingDays: string[];
};

export type BasicCampaign = {
  id: string;
  name: string;
  tenantId: string;
  workspaceId: string;
  templateId: string;
  sendingMailboxId?: string | null;
  industry: string;
  approvedBy?: string | null;
  scheduledStart?: Date | null;
  launchedAt?: Date | null;
  ccRecipients: Recipient[] | null;
  bccRecipients: Recipient[] | null;
  status: CampaignStatus;
  targetingCriteria?: TargetingCriteriaResponse;
  leadFilter?: LeadFilterCriteria;
  sendingWindow?: SendingWindow | null;
};

export type Campaign = BasicCampaign & {
  totalCompanies: number;
  totalContacts: number;
  participatingCompaniesCount: number;
  participatingContactsCount: number;
  participatingCompanies: CampaignCompanyResponse[];
  campaignCompanies: CampaignCompanyResponse[];
};

export type CampaignAnalytics = {
  sentEmails: number;
  openedEmails: number;
  repliedEmails: number;
  emailsOpenRate: number;
  emailsReplyRate: number;
  bookedMeetings: number;
};

export type CampaignListResponse = {
  id: string;
  workspaceId: string;
  templateId: string;
  name: string;
  scheduledStart?: Date | null;
  status: CampaignStatus;
  totalCompanies: number;
  totalContacts: number;
  industry: string;
  sentEmails: number;
  openedEmails: number;
  progress: number;
};

export type CampaignCompanyResponse = {
  id: string;
  name: string;
  domain?: string | null;
  websiteUrl?: string | null;
  industry?: string | null;
  employeeCount?: string | null;
  employeeRange?: EmployeeRange | null;
  hqCity?: string | null;
  hqState?: string | null;
  hqCountry?: string | null;
  territory?: string | null;
  region?: string | null;
  accountSummary?: string | null;
  segment?: string | null;
  campaignContacts: CampaignContactResponse[] | null;
};

export type CampaignContactResponse = {
  id: string;
  campaignId: string;
  contactId: string;
  firstName?: string | null;
  lastName?: string | null;
  firstNameNormalized?: string | null;
  lastNameNormalized?: string | null;
  fullName?: string | null;
  title?: string | null;
  email?: string | null;
  phoneE164?: string | null;
  linkedinUrl?: string | null;
  currentStep: number;
  lastSentAt?: Date | null;
  nextSendAt?: Date | null;
  replyReceived?: boolean;
  participating?: boolean;
  zohoExisting?: boolean;
  status: CampaignContactStatus;
  emailData: EmailDataResponse[];
  bdName?: string | null;
  bdEmail?: string | null;
};

export type CampaignChatResponse = {
  messageId: string;
  conversationId: string;
  response: string;
  campaignId: string;
  campaignState: TargetingCriteriaResponse;
  createdAt: Date;
};

export type UpdateCampaignTargetRequest = {
  campaignId: string;
  requestBody: {
    selectedCampaignContactIds: string[];
    excludedCampaignContactIds: string[];
  };
} & BasicParams;

export type UpdateCampaignDetailsRequest = {
  campaignId: string;
  requestBody: {
    name: string;
    sendingWindow?: SendingWindow | null;
  };
} & BasicParams;

export type ChatRequestBody = {
  conversationId: string;
  request: string;
};

export type ChatRequest = {
  requestBody: ChatRequestBody;
} & BasicParams;

export type ChatReply = {
  requestBody: ChatRequestBody;
} & BasicParams;

export type CampaignRequest = {
  campaignId: string;
} & BasicParams;

export type CampaignSearchParams = BasicParams & {
  params: {
    query?: string;
    campaignStatuses?: CampaignStatus[];
    industries?: string[];
  } & PaginationParams;
};

export type CampaignRecipientsRequest = BasicParams & {
  campaignId: string;
  payload: {
    ccRecipients: Recipient[];
    bccRecipients: Recipient[];
  };
};

export type AssignMailboxRequest = {
  campaignId: string;
  mailboxId: string;
} & BasicParams;

export type CreateCampaignForSearchRequest = {
  tenantId: string;
  workspaceId: string;
  requestBody: {
    contactIds: string[];
    leadFilterCriteria?: LeadFilterCriteria;
  };
};

export type AddContactsToCampaignRequest = {
  campaignId: string;
  contactIds: string[];
} & BasicParams;
