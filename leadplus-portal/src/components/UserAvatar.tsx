'use client';

import { FC, ReactNode } from 'react';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import { Button } from './ui/button';

type MenuOption = {
  label: string;
  icon: ReactNode;
  onClick: () => void;
  className?: string;
};

type UserAvatarProps = {
  primaryText?: string;
  secondaryText?: string;
  menuOptions?: MenuOption[];
  avatarSrc?: string;
  avatarFallback?: string;
};

const UserAvatar: FC<UserAvatarProps> = ({
  primaryText = 'John Doe',
  secondaryText = 'john@example.com',
  menuOptions = [],
  avatarSrc = '',
  avatarFallback,
}) => {
  const getInitials = (name: string): string => {
    return name
      .split(' ')
      .map((word) => word.charAt(0).toUpperCase())
      .slice(0, 2)
      .join('');
  };

  const fallbackText = avatarFallback || getInitials(primaryText);

  return (
    <div className="flex items-center space-x-4">
      <DropdownMenu>
        <DropdownMenuTrigger asChild>
          <Button variant="ghost" className="relative h-8 w-8 rounded-full">
            <Avatar className="ring-offset-primary h-8 w-8 ring-2 ring-white ring-offset-2">
              <AvatarImage src={avatarSrc} alt={primaryText} />
              <AvatarFallback className="bg-primary text-white">{fallbackText}</AvatarFallback>
            </Avatar>
          </Button>
        </DropdownMenuTrigger>
        <DropdownMenuContent className="w-56" align="end" forceMount>
          {(primaryText || secondaryText) && (
            <>
              <DropdownMenuLabel className="font-normal">
                <div className="flex flex-col space-y-1 text-black">
                  {primaryText && <p className="max-w-full truncate text-sm leading-none font-medium">{primaryText}</p>}
                  {secondaryText && (
                    <p className="text-muted-foreground max-w-full truncate text-xs leading-none">{secondaryText}</p>
                  )}
                </div>
              </DropdownMenuLabel>
              <DropdownMenuSeparator />
            </>
          )}
          {menuOptions.map((option, index) => (
            <DropdownMenuItem
              key={index}
              className={`cursor-pointer text-black ${option.className || ''}`}
              onClick={option.onClick}
            >
              {option.icon}
              <span className="ml-2">{option.label}</span>
            </DropdownMenuItem>
          ))}
        </DropdownMenuContent>
      </DropdownMenu>
    </div>
  );
};

export { UserAvatar };
export type { UserAvatarProps, MenuOption };
