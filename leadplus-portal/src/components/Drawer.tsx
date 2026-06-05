'use client';

import { ReactNode, useState } from 'react';
import {
  Sheet,
  SheetContent,
  SheetDescription,
  SheetHeader,
  SheetTitle,
} from '@/components/ui/sheet';
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from '@/components/ui/alert-dialog';
import { ScrollArea } from '@/components/ui/scroll-area';
import { cn } from '@/lib/utils/helpers';

type DrawerProps = {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  title: string;
  description?: string;
  children: ReactNode;
  footer?: ReactNode;
  className?: string;
  preventOutsideClick?: boolean;
  side?: 'left' | 'right' | 'top' | 'bottom';
  maxWidth?: 'sm' | 'md' | 'lg' | 'xl' | '2xl' | '3xl' | '4xl' | 'full';
  hasUnsavedChanges?: boolean;
  onDiscard?: () => void;
};

const MAX_WIDTH_CLASSES = {
  sm: 'sm:max-w-sm',
  md: 'sm:max-w-md',
  lg: 'sm:max-w-lg',
  xl: 'sm:max-w-xl',
  '2xl': 'sm:max-w-2xl',
  '3xl': 'sm:max-w-3xl',
  '4xl': 'sm:max-w-4xl',
  full: 'sm:max-w-full',
};

const Drawer = ({
  open,
  onOpenChange,
  title,
  description,
  children,
  footer,
  className,
  preventOutsideClick = false,
  side = 'right',
  maxWidth = '3xl',
  hasUnsavedChanges = false,
  onDiscard,
}: DrawerProps) => {
  const [showConfirmDialog, setShowConfirmDialog] = useState(false);

  const handleOpenChange = (shouldOpen: boolean) => {
    if (!shouldOpen && hasUnsavedChanges) {
      setShowConfirmDialog(true);
      return;
    }
    onOpenChange(shouldOpen);
  };

  const handleDiscard = () => {
    setShowConfirmDialog(false);
    onDiscard?.();
    onOpenChange(false);
  };

  const handleCancelClose = () => {
    setShowConfirmDialog(false);
  };

  return (
    <>
      <Sheet open={open} onOpenChange={handleOpenChange}>
        <SheetContent
          className={cn('flex w-full flex-col gap-0 p-0', MAX_WIDTH_CLASSES[maxWidth], className)}
          side={side}
          onInteractOutside={(e) => {
            if (preventOutsideClick || hasUnsavedChanges) {
              e.preventDefault();
            }
          }}
        >
          <SheetHeader className="border-border shrink-0 border-b">
            <SheetTitle className="text-foreground text-xl font-semibold">{title}</SheetTitle>
            {description && (
              <SheetDescription className="text-muted-foreground text-sm">
                {description}
              </SheetDescription>
            )}
          </SheetHeader>
          <ScrollArea className="min-h-0 flex-1">
            <div className="p-4">{children}</div>
          </ScrollArea>
          {footer && (
            <div className="bg-background border-border shrink-0 border-t p-4">{footer}</div>
          )}
        </SheetContent>
      </Sheet>

      <AlertDialog open={showConfirmDialog} onOpenChange={setShowConfirmDialog}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Unsaved Changes</AlertDialogTitle>
            <AlertDialogDescription>
              You have unsaved changes. What would you like to do?
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel onClick={handleCancelClose}>Cancel</AlertDialogCancel>
            <AlertDialogAction
              onClick={handleDiscard}
              className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
            >
              Discard
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </>
  );
};

export { Drawer };
