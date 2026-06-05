'use client';

import Image from 'next/image';
import Link from 'next/link';
import { usePathname, useRouter } from 'next/navigation';
import { useCallback, useRef, useState } from 'react';

import { Button } from '@/components/ui/button';
import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from '@/components/ui/tooltip';
import { appRoutes } from '@/config/routes';
import useToggle from '@/hooks/useToggle';
import { cn } from '@/lib/utils/helpers';
import { NavigationItem } from '@/types/navigation.types';
import { FeedbackDialog } from './FeedbackDialog';
import { WorkspaceSwitcher } from './WorkspaceSwitcher';
import { Popover, PopoverContent, PopoverTrigger } from './ui/popover';

import { ChevronDown, ChevronLeft, ChevronRight, ChevronUp, MessageSquarePlus } from 'lucide-react';

const isRouteActive = (pathname: string, href?: string) =>
  !!href && (pathname === href || pathname.startsWith(href + '/'));

const BetaBadge = ({ className }: { className?: string }) => (
  <span
    className={cn(
      'bg-primary/20 text-primary rounded-full px-2 py-0.5 text-[10px] font-semibold tracking-wide',
      className
    )}
  >
    BETA
  </span>
);

const SidebarLogo = ({ collapsed, onClick }: { collapsed: boolean; onClick?: () => void }) => (
  <div
    className="flex h-16 shrink-0 cursor-pointer items-center justify-center border-b border-white/10"
    onClick={onClick}
  >
    {collapsed ? (
      <div className="flex flex-col items-center gap-0.5">
        <Image
          src="/leadplus-mini-logo.png"
          alt="LeadPlus"
          width={32}
          height={32}
          className="object-contain"
        />
        <BetaBadge className="px-1.5" />
      </div>
    ) : (
      <div className="inline-flex items-start gap-1">
        <Image
          src="/leadplus-logo.png"
          alt="LeadPlus"
          width={140}
          height={32}
          className="object-contain"
        />
        <BetaBadge className="-mt-1" />
      </div>
    )}
  </div>
);

const SidebarToggle = ({ collapsed, onToggle }: { collapsed: boolean; onToggle: () => void }) => (
  <Button
    variant="none"
    size="icon"
    onClick={onToggle}
    className="absolute top-20 -right-3 z-25 h-6 w-6 rounded-full border bg-white shadow-sm"
  >
    {collapsed ? <ChevronRight className="h-3 w-3" /> : <ChevronLeft className="h-3 w-3" />}
  </Button>
);

const SidebarChildren = ({
  childRoutes,
  pathname,
}: {
  childRoutes: NavigationItem[];
  pathname: string;
}) => (
  <div className="animate-in slide-in-from-top-2 mt-1 ml-3 space-y-1 duration-200">
    {childRoutes.map((child) => {
      const active = isRouteActive(pathname, child.href);
      return (
        <Link
          key={child.href}
          href={child.href}
          className={cn(
            'flex items-center rounded-lg px-3 py-2 text-sm transition-colors',
            active
              ? 'bg-white/10 text-white'
              : 'text-slate-400 hover:bg-white/5 hover:text-slate-200'
          )}
        >
          {child.icon && <child.icon size={16} />}
          <span className="ml-3">{child.label}</span>
        </Link>
      );
    })}
  </div>
);

type CollapsedPopoverProps = {
  page: NavigationItem;
  pathname: string;
  onClose: () => void;
};

const CollapsedPopover = ({ page, pathname, onClose }: CollapsedPopoverProps) => {
  const router = useRouter();

  const handleNavigate = (href: string) => {
    router.push(href);
    onClose();
  };

  return (
    <PopoverContent
      side="right"
      align="start"
      className="bg-lp-dark ml-2 w-52 border-white/10 p-0 shadow-xl"
      onOpenAutoFocus={(e) => e.preventDefault()}
    >
      <div className="border-b border-white/10 px-4 py-2">
        <p className="text-xs font-semibold text-white/50 uppercase">{page.label}</p>
      </div>

      <div className="space-y-1 px-2 py-1">
        {page.children?.map((child) => {
          const active = isRouteActive(pathname, child.href);

          return (
            <button
              key={child.href}
              type="button"
              onClick={() => handleNavigate(child.href)}
              className={cn(
                'flex w-full cursor-pointer items-center rounded-md px-3 py-2.5 text-left text-sm',
                active
                  ? 'bg-primary text-white'
                  : 'text-slate-300 hover:bg-white/10 hover:text-white'
              )}
            >
              {child.icon && <child.icon size={16} />}
              <span className="ml-3 font-medium">{child.label}</span>
            </button>
          );
        })}
      </div>
    </PopoverContent>
  );
};

type AppSidebarProps = {
  pages: NavigationItem[];
  hideWorkspaceSwitcher?: boolean;
};

const AppSidebar = ({ pages = [], hideWorkspaceSwitcher = false }: AppSidebarProps) => {
  const pathname = usePathname();
  const router = useRouter();
  const sidebarRef = useRef<HTMLDivElement>(null);

  const { value: collapsed, toggle } = useToggle(false);
  const { value: feedbackOpen, toggle: toggleFeedback } = useToggle(false);

  const [expanded, setExpanded] = useState<Record<string, boolean>>({});
  const [popoverId, setPopoverId] = useState<string | null>(null);

  const toggleExpand = useCallback(
    (id: string) => setExpanded((prev) => ({ ...prev, [id]: !prev[id] })),
    []
  );

  const handleLogoClick = useCallback(() => {
    router.push(appRoutes.leadgen.search.companySearch);
  }, [router]);

  return (
    <div ref={sidebarRef} className="relative">
      <aside
        className={cn(
          'bg-lp-dark flex h-screen flex-col transition-all duration-300',
          collapsed ? 'w-16' : 'w-64'
        )}
      >
        <SidebarLogo collapsed={collapsed} onClick={handleLogoClick} />
        <SidebarToggle collapsed={collapsed} onToggle={toggle} />
        {!hideWorkspaceSwitcher && (
          <>
            <WorkspaceSwitcher collapsed={collapsed} />
            <div className="mx-3 border-b border-white/10"></div>
          </>
        )}
        <nav
          className={cn(
            'sidebar-scrollbar min-h-0 flex-1 space-y-2 overflow-y-auto px-2 py-6',
            collapsed && 'hide-scrollbar'
          )}
        >
          <TooltipProvider>
            {pages.map((page) => {
              const hasChildren = !!page.children?.length;
              const singleChild = page.children?.length === 1;
              const href = singleChild ? page.children![0].href : page.href;

              const active =
                isRouteActive(pathname, page.href) ||
                page.children?.some((c) => isRouteActive(pathname, c.href));

              const button = (
                <div
                  className={cn(
                    'flex items-center space-x-3 rounded-lg py-3 transition-colors',
                    collapsed ? 'justify-center' : 'px-3',
                    active
                      ? 'bg-primary text-white'
                      : 'text-slate-300 hover:bg-white/10 hover:text-white'
                  )}
                >
                  {page.icon && <page.icon size={20} />}
                  {!collapsed && (
                    <div className="flex items-center justify-between space-x-3 truncate whitespace-nowrap">
                      <span>{page.label}</span>
                      {hasChildren &&
                        !singleChild &&
                        (expanded[page.id] ? <ChevronUp size={16} /> : <ChevronDown size={16} />)}
                      {page.tag && (
                        <span className="rounded-3xl border border-white px-2 text-[10px] text-white">
                          {page.tag}
                        </span>
                      )}
                    </div>
                  )}
                </div>
              );

              if (hasChildren && !singleChild) {
                return (
                  <div key={page.id} className="relative">
                    {!collapsed && (
                      <button className="w-full text-left" onClick={() => toggleExpand(page.id)}>
                        {button}
                      </button>
                    )}

                    {!collapsed && expanded[page.id] && (
                      <SidebarChildren childRoutes={page.children!} pathname={pathname} />
                    )}

                    {collapsed && (
                      <Popover
                        open={popoverId === page.id}
                        onOpenChange={(isOpen) => setPopoverId(isOpen ? page.id : null)}
                      >
                        <Tooltip>
                          <TooltipTrigger asChild>
                            <PopoverTrigger asChild>
                              <button
                                type="button"
                                className={cn(
                                  'flex w-full items-center justify-center rounded-lg py-3 transition-colors',
                                  active
                                    ? 'bg-primary text-white'
                                    : 'text-slate-300 hover:bg-white/10 hover:text-white'
                                )}
                              >
                                {page.icon && <page.icon size={20} />}
                              </button>
                            </PopoverTrigger>
                          </TooltipTrigger>
                          <TooltipContent side="right">{page.label}</TooltipContent>
                        </Tooltip>
                        <CollapsedPopover
                          page={page}
                          pathname={pathname}
                          onClose={() => setPopoverId(null)}
                        />
                      </Popover>
                    )}
                  </div>
                );
              }

              const link = (
                <Link href={href} aria-label={page.label}>
                  {button}
                </Link>
              );

              return collapsed ? (
                <div key={href}>
                  <Tooltip>
                    <TooltipTrigger asChild>{link}</TooltipTrigger>
                    <TooltipContent side="right">{`${page.label} ${page.tag ? `(${page.tag})` : ''}`}</TooltipContent>
                  </Tooltip>
                </div>
              ) : (
                <div key={href}>{link}</div>
              );
            })}
          </TooltipProvider>
        </nav>
        <div className="shrink-0 space-y-3 border-t border-white/10 px-2 py-4">
          <TooltipProvider>
            <Tooltip>
              <TooltipTrigger asChild>
                <Button
                  variant="none"
                  type="button"
                  onClick={toggleFeedback}
                  className={cn(
                    'flex w-full items-center rounded-lg text-slate-400 transition-colors hover:bg-white/10 hover:text-white',
                    collapsed ? 'justify-center' : 'px-3'
                  )}
                >
                  <MessageSquarePlus size={20} />
                  {!collapsed && <span className="text-sm">Give Feedback</span>}
                </Button>
              </TooltipTrigger>
              {collapsed && <TooltipContent side="right">Give Feedback</TooltipContent>}
            </Tooltip>
          </TooltipProvider>
          <div className="shrink-0 text-center text-xs text-slate-400">
            {collapsed ? 'v1' : 'LeadPlus v1.0.0'}
          </div>
        </div>
      </aside>
      <FeedbackDialog open={feedbackOpen} toggleOpen={toggleFeedback} />
    </div>
  );
};

export { AppSidebar };
