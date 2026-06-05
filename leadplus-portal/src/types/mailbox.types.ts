import {
  MailboxConnectionStatus,
  MailboxType,
} from "@/constants/mailbox.constants";
import { BasicParams } from "./user.types";

export type Mailbox = {
  id: string;
  userId: string;
  tenantId: string;
  workspaceId: string;
  emailAddress: string;
  type: MailboxType;
  status: MailboxConnectionStatus;
  tokenExpired: boolean;
};

export type OutlookConnectParams = {
  code: string;
  redirectUri: string;
} & BasicParams;

export type GmailConnectParams = {
  code: string;
  redirectUri: string;
} & BasicParams;

export type SmtpConnectParams = {
  email: string;
  appPassword: string;
} & BasicParams;

export type SesConnectParams = {
  email: string;
} & BasicParams;

export type MailboxDisconnectParams = {
  mailboxId: string;
} & BasicParams;
