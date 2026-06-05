import Image from 'next/image';
import { useGoogleLogin } from '@react-oauth/google';
import { useGoogleAuth } from '@/hooks/useAuthentication';
import { useAuth } from '@/context/AuthContext';
import { useRouter, useSearchParams } from 'next/navigation';
import { appRoutes } from '@/config/routes';
import { Button } from './ui/button';
import { Module } from '@/constants/modules.constants';
import { useCreateVendor } from '@/hooks/useVendor';
import { getAuthModuleFromUrl } from '@/lib/utils/userTypeTracker';

type GoogleAuthButtonProps = {
  onSuccess?: (token: string) => void;
};

const GoogleAuthButton = ({ onSuccess }: GoogleAuthButtonProps) => {
  const router = useRouter();
  const searchParams = useSearchParams();
  const { login, refreshSession } = useAuth();
  const { mutate } = useGoogleAuth();
  const { mutateAsync: createVendorAsync } = useCreateVendor();

  const currentModule = getAuthModuleFromUrl(searchParams ?? new URLSearchParams());

  const onLogin = useGoogleLogin({
    onSuccess: (tokenResponse) => {
      if (onSuccess) {
        onSuccess(tokenResponse.access_token);
      } else {
        mutate(
          { token: tokenResponse.access_token },
          {
            onSuccess: async (data) => {
              login(data);
              if (currentModule === Module.VENDOR) {
                await createVendorAsync(undefined, {
                  onSuccess: async () => {
                    await refreshSession();
                    router.replace(appRoutes.vendor.profileOverview.root);
                  },
                  onError: () => {
                    router.replace(appRoutes.vendor.profileOverview.root);
                  },
                });
              } else {
                router.replace(appRoutes.leadgen.profileOverview.root);
              }
            },
          }
        );
      }
    },
  });

  return (
    <Button
      variant="none"
      onClick={() => onLogin()}
      className="border-inputBorder 3xl:h-[42px] flex h-10.5 w-full cursor-pointer items-center justify-center gap-4 rounded-md border py-2 text-sm shadow-md xl:h-8"
    >
      <Image
        className="3xl:h-6 3xl:w-6 h-6 w-6 xl:h-4 xl:w-4"
        src="https://www.svgrepo.com/show/475656/google-color.svg"
        alt="google logo"
        width={24}
        height={24}
      />
      <span className="3xl:text-base text-sm text-black/50">Continue with Google</span>
    </Button>
  );
};

export { GoogleAuthButton };
