'use client';

import Image from 'next/image';
import { MouseEvent, useCallback, useMemo } from 'react';
import { v4 as uuidv4 } from 'uuid';

import { Button } from '@/components/ui/button';
import { MailboxConnectionStatus, MailboxType } from '@/constants/mailbox.constants';
import { useAuth } from '@/context/AuthContext';
import {
  useAddGmailAuthCode,
  useAddOutlookAuthCode,
  useDisconnectMailbox,
  useGetConnectedMailboxes,
} from '@/hooks/useMailbox';
import { cn } from '@/lib/utils/helpers';
import { Mailbox } from '@/types/mailbox.types';
import { getAzureAuthUrl, getGmailAuthUrl } from './authConfig';
import { SmtpConnectModal } from '@/components/mailboxes/SmtpConnectModal';
import { Info, LogOut, LucideIcon, MailIcon, RefreshCw } from 'lucide-react';
import { useModal } from '@/hooks/useModal';
import { SesConnectModal } from '@/components/mailboxes/SesConnectModal';
import useToggle from '@/hooks/useToggle';
import { toast } from 'sonner';
import { AxiosError } from 'axios';
import { UserRoles } from '@/constants/user.constants';
import { handleOAuthPopup } from '@/lib/utils/oauthPopup';

type Provider = {
  type: MailboxType;
  label: string;
  logo?: string;
  description: string;
  icon?: LucideIcon;
};

const providers: Provider[] = [
  {
    type: MailboxType.OUTLOOK,
    label: 'Microsoft Outlook',
    logo: '/outlook.png',
    description: 'Connect your Microsoft Outlook or Office 365 account.',
  },
  {
    type: MailboxType.GMAIL,
    label: 'Gmail',
    logo: '/gmail.svg',
    description: 'Connect your Gmail account.',
  },
];

const adminAccessProviders: Provider[] = [
  {
    type: MailboxType.AMAZON_SES,
    label: 'Amazon SES',
    logo: '/aws-ses.svg',
    description: 'Connect your Amazon SES account.',
  },
  {
    type: MailboxType.SMTP,
    label: 'SMTP',
    icon: MailIcon,
    description: 'Connect your SMTP account.',
  },
];

export default function Communications() {
  const { renderModal } = useModal();
  const { authenticatedUserDetails, loggedInUser } = useAuth();
  const workspaceId = authenticatedUserDetails?.workspaceId || '';
  const tenantId = authenticatedUserDetails?.tenantId || '';

  const { mutate: connectOutlook } = useAddOutlookAuthCode();
  const { mutate: connectGmail } = useAddGmailAuthCode();
  const { mutate: disconnectMailbox } = useDisconnectMailbox();

  const { data } = useGetConnectedMailboxes({ workspaceId, tenantId });

  const { value: isSmtpModalOpen, toggle: toggleSmtpModal } = useToggle(false);
  const { value: isSesModalOpen, toggle: toggleSesModal } = useToggle(false);

  const emailProviders = useMemo(() => {
    if (loggedInUser?.roles?.includes(UserRoles.ADMIN)) {
      return [...providers, ...adminAccessProviders];
    }
    return providers;
  }, [loggedInUser]);

  const cleanupAndClose = useCallback((popup: Window, checkPopup: NodeJS.Timeout) => {
    popup.close();
    clearInterval(checkPopup);
    sessionStorage.removeItem('oauth_state');
  }, []);

  const handleOutlookConnect = useCallback(() => {
    const expectedState = uuidv4();

    handleOAuthPopup({
      authUrl: `${getAzureAuthUrl()}&state=${encodeURIComponent(expectedState)}`,
      windowTitle: 'Azure OAuth',
      state: expectedState,
      cleanupAndClose,
      onConnect: (code) => {
        connectOutlook(
          { code, workspaceId, tenantId, redirectUri: window.location.href },
          {
            onSuccess: () => toast.success('Outlook connected successfully'),
            onError: (error: AxiosError) =>
              toast.error(error?.message || 'Outlook connection failed'),
          }
        );
      },
    });
  }, [connectOutlook, workspaceId, tenantId, cleanupAndClose]);

  const handleGmailConnect = useCallback(() => {
    handleOAuthPopup({
      authUrl: getGmailAuthUrl(),
      windowTitle: 'Gmail OAuth',
      cleanupAndClose,
      onConnect: (code) => {
        connectGmail(
          { code, workspaceId, tenantId, redirectUri: window.location.href },
          {
            onSuccess: () => toast.success('Gmail connected successfully'),
            onError: (error: AxiosError) =>
              toast.error(error?.message || 'Gmail connection failed'),
          }
        );
      },
    });
  }, [connectGmail, workspaceId, tenantId, cleanupAndClose]);

  const handleOpenConnect = useCallback(
    (provider: MailboxType) => {
      if (provider === MailboxType.OUTLOOK) {
        handleOutlookConnect();
      } else if (provider === MailboxType.AMAZON_SES) {
        toggleSesModal();
      } else if (provider === MailboxType.SMTP) {
        toggleSmtpModal();
      } else if (provider === MailboxType.GMAIL) {
        handleGmailConnect();
      }
    },
    [handleOutlookConnect, toggleSesModal, toggleSmtpModal, handleGmailConnect]
  );

  const handleDisconnectClick = useCallback(
    (e: MouseEvent, account?: Mailbox) => {
      e.stopPropagation();
      renderModal({
        type: 'error',
        title: `Disconnect ${account?.type}`,
        message: `You will not be able to send emails or receive responses if you disconnect your email from the platform.`,
        onConfirm: () => {
          if (account) {
            disconnectMailbox(
              { mailboxId: account.id, workspaceId, tenantId },
              {
                onSuccess: () => {
                  toast.success(`${account.type} disconnected successfully`);
                },
                onError: (error: AxiosError) => {
                  toast.error(error?.message || `${account.type} disconnection failed`);
                },
              }
            );
          }
        },
      });
    },
    [renderModal, disconnectMailbox, workspaceId, tenantId]
  );

  return (
    <div className="animate-in fade-in slide-in-from-right-4 mx-auto max-w-6xl space-y-8 p-8 duration-500">
      <header className="space-y-2">
        <h1 className="text-foreground text-3xl font-bold">Connect your inbox to Leadplus</h1>
        <p className="text-muted-foreground">Select your email provider</p>
        <div className="flex items-start gap-2 rounded-lg border border-blue-200 bg-blue-50 px-4 py-3 text-sm text-blue-700">
          <Info className="mt-0.5 h-4 w-4 shrink-0" />
          <span>
            Only workspace emails are allowed to connect here. Personal email accounts will not be
            accepted.
          </span>
        </div>
      </header>

      <div className="flex flex-wrap gap-6">
        {emailProviders.map((provider) => {
          const account = data?.find((acc) => acc.type === provider.type);

          const isPending = account?.status === MailboxConnectionStatus.PENDING;
          const isTokenExpired = Boolean(account?.tokenExpired);
          const isConnected = Boolean(account && !isPending && !isTokenExpired);

          const handleCardClick = () => {
            if (!isConnected) handleOpenConnect(provider.type);
          };

          return (
            <div
              key={provider.type}
              onClick={handleCardClick}
              className={cn(
                'group relative flex flex-col items-center justify-center',
                'h-60 w-56 cursor-pointer rounded-xl border-2 bg-white',
                'shadow-sm transition-all duration-200 hover:shadow-md',
                isTokenExpired
                  ? 'border-amber-400 bg-amber-50/20'
                  : isConnected
                    ? 'border-sky-500 bg-sky-50/10'
                    : 'border-slate-100 hover:border-slate-200 hover:bg-slate-50/50'
              )}
            >
              <div className="relative mb-6 h-16 w-16">
                {provider.logo && (
                  <Image src={provider.logo} alt={provider.label} fill className="object-contain" />
                )}
                {provider.icon && (
                  <div className="flex h-14 w-14 items-center justify-center rounded-full bg-blue-50 text-blue-500">
                    {provider.icon && <provider.icon className="h-7 w-7" />}
                  </div>
                )}
              </div>

              <h3 className="mb-4 text-sm font-semibold text-slate-900">{provider.label}</h3>

              {isPending && (
                <div className="h-8">
                  <span className="rounded-sm bg-yellow-100 px-3 py-1 text-xs font-medium text-yellow-600">
                    Pending
                  </span>
                </div>
              )}

              {isTokenExpired && (
                <div className="animate-in fade-in zoom-in-95 flex w-full flex-col items-center gap-3 duration-200">
                  <span className="rounded-full bg-amber-100 px-3 py-1 text-xs font-medium text-amber-700 transition-opacity duration-200 group-hover:opacity-0">
                    Connection Expired
                  </span>
                  <Button
                    variant="none"
                    className={cn(
                      'absolute bottom-6 flex items-center gap-1.5 text-xs font-medium',
                      'text-amber-600 hover:text-amber-700',
                      'translate-y-2 opacity-0 transition-all duration-200',
                      'group-hover:translate-y-0 group-hover:opacity-100'
                    )}
                  >
                    <RefreshCw className="h-3 w-3" />
                    Reconnect
                  </Button>
                </div>
              )}

              {isConnected && !isPending && (
                <div className="animate-in fade-in zoom-in-95 flex w-full flex-col items-center gap-3 duration-200">
                  <span className="rounded-full bg-green-100 px-3 py-1 text-xs font-medium text-green-700 transition-opacity duration-200 group-hover:opacity-0">
                    Connected
                  </span>

                  <Button
                    variant="none"
                    onClick={(e) => handleDisconnectClick(e, account)}
                    className={cn(
                      'absolute bottom-6 flex items-center gap-1.5 text-xs font-medium',
                      'text-red-500 hover:text-red-600',
                      'translate-y-2 opacity-0 transition-all duration-200',
                      'group-hover:translate-y-0 group-hover:opacity-100'
                    )}
                  >
                    <LogOut className="h-3 w-3" />
                    Disconnect
                  </Button>
                </div>
              )}

              {!account && (
                <div className="h-8">
                  <span className="text-xs font-medium text-sky-500">Click to connect</span>
                </div>
              )}
            </div>
          );
        })}
      </div>
      <SmtpConnectModal open={isSmtpModalOpen} onOpenChange={toggleSmtpModal} />
      <SesConnectModal open={isSesModalOpen} onOpenChange={toggleSesModal} />
    </div>
  );
}
