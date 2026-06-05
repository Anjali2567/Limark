import { ReactNode } from 'react';

import {
  Dialog,
  DialogContent,
  DialogTitle,
  DialogDescription,
  DialogFooter,
  DialogHeader,
} from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { ScrollArea } from '@/components/ui/scroll-area';

type ModalProps = {
  isOpen: boolean;
  toggleOpen: () => void;
  content?: ReactNode;
  body?: ReactNode;
  title?: string;
  description?: string;
  footer?: ReactNode;
  footerClassName?: string;
  contentClassName?: string;
  headerClassName?: string;
  bodyClassName?: string;
  showCloseButton?: boolean;
  disableOutsideClose?: boolean;
};

const Modal = ({
  isOpen,
  toggleOpen,
  content,
  body,
  title,
  description,
  footer,
  headerClassName = '',
  contentClassName = '',
  footerClassName = '',
  bodyClassName = '',
  showCloseButton = true,
  disableOutsideClose = false,
}: ModalProps) => {
  return (
    <Dialog open={isOpen} onOpenChange={toggleOpen}>
      <DialogContent
        className={contentClassName}
        showCloseButton={showCloseButton}
        onInteractOutside={disableOutsideClose ? (e) => e.preventDefault() : undefined}
      >
        <DialogHeader className={headerClassName}>
          {title && <DialogTitle className="text-xl">{title}</DialogTitle>}
          {description && <DialogDescription>{description}</DialogDescription>}
          {content && <>{content}</>}
        </DialogHeader>
        {body && (
          <ScrollArea className={`min-h-0 flex-1 overflow-auto ${bodyClassName}`}>
            {body}
          </ScrollArea>
        )}
        {footer ? (
          <DialogFooter className={footerClassName}>{footer}</DialogFooter>
        ) : (
          <DialogFooter className={footerClassName}>
            <Button variant="outline" onClick={toggleOpen}>
              Cancel
            </Button>
          </DialogFooter>
        )}
      </DialogContent>
    </Dialog>
  );
};

export { Modal };
