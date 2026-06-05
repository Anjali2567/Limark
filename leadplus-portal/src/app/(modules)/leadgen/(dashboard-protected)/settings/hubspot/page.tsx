'use client';

import Image from 'next/image';
import { useRouter, useSearchParams } from 'next/navigation';
import { useCallback, useEffect, useRef } from 'react';
import { toast } from 'sonner';

import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { useAuth } from '@/context/AuthContext';
import {
  useConnectHubSpotCRM,
  useDisconnectHubSpotCRM,
  useGetTenantDetails,
  useSyncHubSpotRecords,
} from '@/hooks/useTenant';
import { formatDateTimeToReadable } from '@/lib/utils/timeConversion';

import { CheckCircle2, CloudSync, Loader2, LogOut } from 'lucide-react';

const features = [
  {
    title: 'Customer Sync',
    description: 'View and access your HubSpot CRM contacts within LeadPlus.',
  },
];

const scope = 'crm.objects.contacts.read crm.objects.companies.read oauth';

const HubSpotSettingsPage = () => {
  const router = useRouter();
  const searchParams = useSearchParams();
  const { authenticatedUserDetails } = useAuth();
  const authorizationCode = searchParams.get('code');

  const initConnect = useRef(false);

  const { data: tenantDetails, isLoading } = useGetTenantDetails({
    tenantId: authenticatedUserDetails?.tenantId || '',
  });

  const { mutate: connectHubSpotAccount, isPending: isConnecting } = useConnectHubSpotCRM();
  const { mutate: disconnectHubSpotAccount } = useDisconnectHubSpotCRM();
  const { mutate: syncHubSpotRecords, isPending } = useSyncHubSpotRecords();

  const handleDisconnect = useCallback(() => {
    if (!authenticatedUserDetails) return;
    disconnectHubSpotAccount(
      {
        tenantId: authenticatedUserDetails.tenantId,
      },
      {
        onSuccess: () => {
          toast.success('HubSpot CRM account disconnected successfully');
        },
        onError: () => {
          toast.error('Failed to disconnect HubSpot CRM account. Please try again.');
        },
      }
    );
  }, [authenticatedUserDetails, disconnectHubSpotAccount]);

  const handleSyncHubSpotContacts = useCallback(() => {
    if (!authenticatedUserDetails) return;
    syncHubSpotRecords(
      {
        tenantId: authenticatedUserDetails.tenantId,
      },
      {
        onSuccess: () => {
          toast.success('HubSpot records synced successfully');
        },
        onError: () => {
          toast.error('Failed to sync HubSpot records. Please try again.');
        },
      }
    );
  }, [authenticatedUserDetails, syncHubSpotRecords]);

  const handleConnect = useCallback(() => {
    const clientId = process.env.NEXT_PUBLIC_HUBSPOT_CLIENT_ID || '';
    if (!clientId) return;

    const redirectUri = window.location.origin + window.location.pathname;
    const authUrl = new URL('https://app.hubspot.com/oauth/authorize');
    authUrl.searchParams.append('response_type', 'code');
    authUrl.searchParams.append('client_id', clientId);
    authUrl.searchParams.append('scope', scope);
    authUrl.searchParams.append('redirect_uri', redirectUri);
    window.location.href = authUrl.toString();
  }, []);

  useEffect(() => {
    if (
      authorizationCode &&
      authenticatedUserDetails &&
      !isConnecting &&
      !tenantDetails?.hubspotConnected &&
      !initConnect.current
    ) {
      initConnect.current = true;
      connectHubSpotAccount(
        {
          tenantId: authenticatedUserDetails.tenantId,
          payload: {
            code: authorizationCode,
            redirectUri: window.location.origin + window.location.pathname,
          },
        },
        {
          onSettled: () => {
            router.replace(window.location.pathname);
            initConnect.current = false;
          },
        }
      );
    }
  }, [
    authenticatedUserDetails,
    authorizationCode,
    connectHubSpotAccount,
    isConnecting,
    router,
    tenantDetails,
  ]);

  return (
    <div className="animate-in fade-in slide-in-from-right-4 mx-auto max-w-6xl space-y-8 p-8 duration-500">
      <div>
        <h1 className="text-foreground text-3xl font-bold">HubSpot CRM Integration</h1>
        <p className="text-muted-foreground mt-1 text-lg">
          Connect your HubSpot CRM account to sync companies and contacts
        </p>
      </div>
      <Card className="border-border shadow-sm">
        {isLoading ? (
          <div className="flex h-40 items-center justify-center">
            <Loader2 className="mr-2 h-4 w-4 animate-spin" />
          </div>
        ) : (
          <CardContent className="p-8">
            <div className="flex flex-col items-center justify-center space-y-6">
              <Image
                src="/hubspot-crm.png"
                alt="HubSpot CRM Logo"
                width={192}
                height={40}
                className="object-contain"
              />
              {isConnecting && (
                <div className="text-center">
                  <p className="text-foreground text-lg font-medium">
                    Connecting to HubSpot CRM...
                  </p>
                </div>
              )}
              {tenantDetails?.hubspotConnected ? (
                <div className="w-full max-w-md space-y-4 text-center">
                  <div className="text-success flex items-center justify-center gap-2">
                    <CheckCircle2 className="h-6 w-6" />
                    <h3 className="text-xl font-semibold">Connected</h3>
                  </div>
                  <p className="text-muted-foreground">
                    Your HubSpot CRM account is successfully connected to LeadPlus.
                  </p>
                  {tenantDetails && (
                    <Card className="bg-secondary/30 border-border mt-6 text-left">
                      <CardContent className="space-y-3 p-4">
                        <div className="flex items-start justify-between">
                          <div className="text-muted-foreground text-sm">Account</div>
                          <div className="text-foreground text-right text-sm font-medium">
                            {tenantDetails?.hubspotEmail}
                          </div>
                        </div>
                        <div className="flex items-start justify-between">
                          <div className="text-muted-foreground text-sm">Connected</div>
                          <div className="text-foreground text-right text-sm font-medium">
                            {formatDateTimeToReadable(new Date(tenantDetails.updatedAt))}
                          </div>
                        </div>
                      </CardContent>
                    </Card>
                  )}
                  <div className="flex h-full w-full items-center justify-center gap-4">
                    <Button onClick={handleSyncHubSpotContacts} disabled={isPending}>
                      {isPending ? (
                        <>
                          <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                          <span>Syncing...</span>
                        </>
                      ) : (
                        <>
                          <CloudSync className="mr-2 h-4 w-4" />
                          <span>Sync Companies & Contacts</span>
                        </>
                      )}
                    </Button>
                    <Button
                      onClick={handleDisconnect}
                      variant="outline"
                      className="border-destructive text-destructive hover:bg-destructive hover:text-white"
                    >
                      <LogOut className="mr-2 h-4 w-4" />
                      Disconnect Account
                    </Button>
                  </div>
                </div>
              ) : (
                <div className="space-y-4 text-center">
                  <Button
                    onClick={handleConnect}
                    className="text-primary-foreground bg-sky-500 px-8 py-6 text-base font-semibold shadow-md transition-all hover:bg-sky-600 hover:shadow-lg"
                    size="lg"
                  >
                    Connect HubSpot CRM
                  </Button>
                </div>
              )}
            </div>
          </CardContent>
        )}
      </Card>
      <Card className="border-border shadow-sm">
        <CardHeader>
          <CardTitle>Integration Features</CardTitle>
          <CardDescription>What you get when you connect HubSpot CRM</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
            {features.map((feature) => (
              <div key={feature.title} className="flex items-start gap-3">
                <div className="mt-0.5 flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-sky-100">
                  <CheckCircle2 className="h-4 w-4 text-sky-500" />
                </div>
                <div>
                  <h4 className="text-foreground font-semibold">{feature.title}</h4>
                  <p className="text-muted-foreground text-sm">{feature.description}</p>
                </div>
              </div>
            ))}
          </div>
        </CardContent>
      </Card>
    </div>
  );
};

export default HubSpotSettingsPage;
