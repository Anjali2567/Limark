import { useCallback, useMemo } from 'react';

import { useAuth } from '@/context/AuthContext';
import { Button } from './ui/button';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from './ui/dropdown-menu';

import { Check, ChevronDown } from 'lucide-react';

interface WorkspaceSwitcherProps {
  collapsed?: boolean;
}

const WorkspaceAvatar = ({ initials, size = 'md' }: { initials: string; size?: 'sm' | 'md' }) => (
  <div
    className={`flex items-center justify-center rounded bg-white/10 ${
      size === 'sm' ? 'h-6 w-6' : 'h-8 w-8'
    }`}
  >
    <span className="text-xs font-semibold text-white">{initials}</span>
  </div>
);

const WorkspaceSwitcher = ({ collapsed = false }: WorkspaceSwitcherProps) => {
  const { workspaces = [], authenticatedUserDetails, changeWorkspace } = useAuth();

  const currentWorkspace = useMemo(
    () => workspaces.find((ws) => ws.workspaceId === authenticatedUserDetails?.workspaceId),
    [workspaces, authenticatedUserDetails?.workspaceId]
  );

  const getInitials = useCallback((name?: string) => {
    if (!name) return 'WS';

    return name
      .trim()
      .split(/\s+/)
      .map((n) => n[0])
      .join('')
      .slice(0, 2)
      .toUpperCase();
  }, []);

  const initials = getInitials(currentWorkspace?.workspaceName);

  if (workspaces.length === 0) return null;

  return (
    <div className="px-3 pt-4 pb-2">
      <DropdownMenu>
        <DropdownMenuTrigger asChild>
          {collapsed ? (
            <Button variant="ghost" className="h-10 w-full justify-center p-0 hover:bg-white/10">
              <WorkspaceAvatar initials={initials} />
            </Button>
          ) : (
            <Button variant="ghost" className="w-full justify-between text-white hover:bg-white/10">
              <div className="flex items-center gap-2 overflow-hidden">
                <WorkspaceAvatar initials={initials} size="sm" />
                <span className="truncate">
                  {currentWorkspace?.workspaceName ?? 'Select Workspace'}
                </span>
              </div>
              <ChevronDown className="h-4 w-4 shrink-0 text-slate-300" />
            </Button>
          )}
        </DropdownMenuTrigger>

        <DropdownMenuContent align="start" className="bg-popover border-border w-60">
          <DropdownMenuLabel className="text-muted-foreground">Workspaces</DropdownMenuLabel>
          <DropdownMenuSeparator />

          {workspaces.map((workspace) => {
            const isActive = workspace.workspaceId === currentWorkspace?.workspaceId;

            return (
              <DropdownMenuItem
                key={workspace.workspaceId}
                onClick={() => changeWorkspace(workspace.workspaceId)}
                className="hover:bg-muted flex cursor-pointer items-center justify-between"
              >
                <div className="flex items-center gap-2 overflow-hidden">
                  <div className="bg-primary/10 flex h-5 w-5 shrink-0 items-center justify-center rounded">
                    <span className="text-primary text-[10px] font-semibold">
                      {getInitials(workspace.workspaceName)}
                    </span>
                  </div>
                  <span className="truncate">{workspace.workspaceName}</span>
                </div>

                {isActive && <Check className="text-primary h-4 w-4 shrink-0" />}
              </DropdownMenuItem>
            );
          })}
        </DropdownMenuContent>
      </DropdownMenu>
    </div>
  );
};

export { WorkspaceSwitcher };
