import { BasicParams } from './user.types';
import { Recipient } from './workspace.types';

export type LeadEmailPayload = {
  mailboxId: string;
  toRecipients: Recipient[];
  ccRecipients: Recipient[];
  bccRecipients: Recipient[];
  subject: string;
  body: string;
  attachmentIds: string[];
};

export type LeadEmailRequest = {
  contactId: string;
  payload: LeadEmailPayload;
} & BasicParams;

export type LeadEmailGeneratePayload = {
  prompt: string;
}

export type LeadEmailGenerateRequest = {
  params: BasicParams;
  payload: LeadEmailGeneratePayload;
}

export type LeadEmailGenerateResponse = {
  subject: string;
  body: string;
}