import { JSX } from 'react';
import { MessageAgentType, MessageType } from '@/constants/campaign-agent.constant';
import { PaginationParams } from './paginated-params';
import { BasicParams } from './user.types';
import { EmployeeRange } from '@/constants/Campaign';
import { LocationFilterDto } from './campaign.types';

export type SearchQuery = {
  segments: string[];
  employeeCount: string;
  states: string[];
  cities: string[];
  countries: string[];
  regions: string[];
};

export type CampaignState = {
  conversationId: string;
  searchQuery: string;
  employeeRanges?: EmployeeRange[] | null;
  companyLocationFilter?: LocationFilterDto;
  companyExcludeLocationFilter?: LocationFilterDto;
  contactLocationFilter?: LocationFilterDto;
  companyIds?: string[] | null;
  contactIds: string[] | null;
  jobTitles?: string[] | null;
  totalCompanies?: number;
  totalContacts?: number;
};

export type MessageRequest = {
  conversationId: string | null;
  request: string;
} & BasicParams;

export type TargetingCriteriaCount = {
  employeeRanges?: EmployeeRange[] | null;
  companyLocationFilter?: LocationFilterDto | null;
  companyExcludeLocationFilter?: LocationFilterDto | null;
  noOfCompanies: number | null;
  noOfContacts: number | null;
  contactLocationFilter?: LocationFilterDto | null;
  jobTitles?: string[] | null;
} | null;

export type ChatMessage = {
  owner: MessageType;
  message: string | JSX.Element;
  campaignId?: string;
  campaignState?: TargetingCriteriaCount;
  searchPerformed?: boolean;
  createdAt?: Date;
};

export type ChatMemoryDetails = {
  chatMemoryId: string;
} & BasicParams;

export type UpdateTargetCriteriaRequest = {
  chatMemoryId: string;
  payload: {
    employeeRanges: EmployeeRange[] | null;
    titles: string[] | null;
  };
} & BasicParams;

// Agent Response
export type Message = {
  userId: number;
  messageId: number;
  conversationId: string;
  response: string;
  campaignId: number;
  campaignState?: CampaignState;
  searchPerformed: boolean;
  createdAt: string;
};

// History Messages
export type LeadAgentConversations = {
  id: string;
  conversationId: string;
  userId: string;
  type: MessageAgentType;
  request: string;
  response: string;
  createdAt: Date;
  targetingCriteriaCount: TargetingCriteriaCount;
  searchPerformed: boolean;
};

export type LeadAgentConversationRequest = {
  conversationId: string;
} & BasicParams;

export type ConversationsParams = BasicParams & PaginationParams;
