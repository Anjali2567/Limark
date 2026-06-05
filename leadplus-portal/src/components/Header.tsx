'use client';

import Image from 'next/image';
import Link from 'next/link';
import { usePathname, useRouter } from 'next/navigation';
import { ReactNode, useState } from 'react';

import { ThemeSwitcher } from '@/components/ThemeSwitcher';
import { UpdateProfileDialog } from '@/components/UpdateProfileDialog';
import { Sheet, SheetContent, SheetHeader, SheetTitle, SheetTrigger } from '@/components/ui/sheet';
import { appRoutes } from '@/config/routes';
import { Module, moduleLogoHref } from '@/constants/modules.constants';
import { useAuth } from '@/context/AuthContext';
import { normalizePath } from '@/lib/utils/formatter';
import { cn } from '@/lib/utils/helpers';
import { Button } from './ui/button';
import { MenuOption, UserAvatar } from './UserAvatar';

import { LogOut, Menu, PencilLine } from 'lucide-react';

type HeaderVariant = 'light' | 'dark';

type HeaderProps = {
  nonAuthState?: boolean;
  hideLogo?: boolean;
  navLinks?: { label: string; href: string }[];
  classname?: string;
  headerClassname?: string;
  otherOptions?: ReactNode;
  userType?: Module;
  customMenuOptions?: MenuOption[];
  variant?: HeaderVariant;
};

const Header = ({
  nonAuthState = false,
  hideLogo = false,
  navLinks = [],
  classname = '',
  headerClassname = '',
  otherOptions,
  userType,
  customMenuOptions = [],
}: HeaderProps) => {
  const logoHref = userType ? (moduleLogoHref[userType] ?? '/') : '/';
  const router = useRouter();
  const pathname = usePathname();
  const { loggedInUser, authenticatedUser, logout } = useAuth();
  const [isUpdateProfileOpen, setIsUpdateProfileOpen] = useState(false);

  const logoutOption = {
    label: 'Log out',
    icon: <LogOut className="h-4 w-4" />,
    onClick: logout,
    className: 'text-red-600 focus:text-red-600',
  };

  const updateProfileOption = {
    label: 'Update profile',
    icon: <PencilLine className="h-4 w-4" />,
    onClick: () => setIsUpdateProfileOpen(true),
  };

  const handleOnSignin = () => {
    const loginUrl = userType
      ? `${appRoutes.auth.login}?type=${userType.toLowerCase()}`
      : appRoutes.auth.login;
    router.push(loginUrl);
  };

  const menuOptions = nonAuthState
    ? [logoutOption]
    : [...customMenuOptions, updateProfileOption, logoutOption];

  return (
    <header
      className={cn(
        'sticky top-0 z-50 h-16 w-full border-b px-4',
        'border-white/6 bg-lp-dark',
        headerClassname
      )}
    >
      <div className={cn('mx-auto flex h-full max-w-7xl items-center justify-between', classname)}>
        {/* Left: hamburger + logo + nav links */}
        <div className="flex items-center gap-4 md:gap-8">
          <Sheet>
            <SheetTrigger asChild>
              <Button variant="ghost" size="icon" className={cn('md:hidden')}>
                <Menu className="h-6 w-6" />
                <span className="sr-only">Toggle menu</span>
              </Button>
            </SheetTrigger>
            <SheetContent side="left" className="w-75">
              <SheetHeader className="border-b pb-4">
                <SheetTitle>
                  {!hideLogo && (
                    <div className="inline-flex items-start gap-1">
                      <Image
                        src="/leadplus-logo.png"
                        alt="LeadPlus Logo"
                        width={120}
                        height={28}
                        className="object-contain"
                      />
                      <span className="bg-primary/20 text-primary -mt-1 rounded-full px-2 py-0.5 text-[10px] font-semibold tracking-wide">
                        BETA
                      </span>
                    </div>
                  )}
                </SheetTitle>
              </SheetHeader>
              <nav className="mt-6 flex flex-col gap-4">
                {navLinks.map((link) => {
                  const isActive = normalizePath(pathname) === normalizePath(link.href);
                  return (
                    <Link
                      key={link.href}
                      href={link.href}
                      className={cn(
                        'rounded-md p-2 text-lg font-medium transition-colors',
                        isActive ? 'text-lp-gold' : 'text-white/70 hover:text-white'
                      )}
                    >
                      {link.label}
                    </Link>
                  );
                })}
              </nav>
            </SheetContent>
          </Sheet>

          {!hideLogo && (
            <Link href={logoHref} className="shrink-0">
              <div className="inline-flex items-start gap-1">
                <Image
                  src="/leadplus-logo.png"
                  alt="LeadPlus Logo"
                  width={100}
                  height={30}
                  className="object-contain"
                />
                <span className="bg-primary/20 text-primary -mt-1 rounded-full px-2 py-0.5 text-[10px] font-semibold tracking-wide">
                  BETA
                </span>
              </div>
            </Link>
          )}

          <nav className="hidden gap-6 md:flex">
            {navLinks.map((link) => {
              const isActive = normalizePath(pathname).startsWith(normalizePath(link.href));
              return (
                <Link
                  key={link.href}
                  href={link.href}
                  className={cn(
                    'lp-nav-link text-sm font-medium transition-colors',
                    isActive ? 'text-lp-gold' : 'text-white/60 hover:text-white/90'
                  )}
                >
                  {link.label}
                </Link>
              );
            })}
          </nav>
        </div>

        <div className="flex items-center gap-3">
          <ThemeSwitcher variant={'dark'} />
          {otherOptions}
          {loggedInUser ? (
            <UserAvatar
              primaryText={authenticatedUser?.name || loggedInUser?.name || ''}
              secondaryText={authenticatedUser?.email || loggedInUser?.email || ''}
              menuOptions={menuOptions}
            />
          ) : (
            <>
              <Button
                onClick={handleOnSignin}
                className={cn(
                  'lp-cta-btn rounded-lg px-5 py-2 text-sm font-bold',
                  'bg-lp-gold text-lp-dark'
                )}
              >
                Get Started
              </Button>
            </>
          )}
        </div>
      </div>
      <UpdateProfileDialog
        open={isUpdateProfileOpen}
        onOpenChange={setIsUpdateProfileOpen}
        user={{
          name: authenticatedUser?.name || loggedInUser?.name || '',
          email: authenticatedUser?.email || loggedInUser?.email || '',
          phoneNumber: authenticatedUser?.phoneNumber || '',
          company: authenticatedUser?.company || '',
        }}
      />
    </header>
  );
};

export { Header };
