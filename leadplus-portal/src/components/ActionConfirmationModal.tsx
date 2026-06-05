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
import { useModal } from '@/hooks/useModal';

import { CheckCircleIcon, XCircleIcon } from 'lucide-react';

const ActionConfirmationModal = () => {
  const { isModalOpen, modalContent, closeModal } = useModal();

  if (!isModalOpen || !modalContent) return null;

  const {
    type,
    title,
    message,
    onConfirm,
    onCancel,
    cancelButtonText = 'Cancel',
    submitButtonText = 'Confirm',
    dataTestId,
    hideIcon,
    icon: Icon,
  } = modalContent;

  let icon;
  let actionClass = '';

  switch (type) {
    case 'success':
      icon = <CheckCircleIcon className="h-6 w-6 text-green-500" />;
      actionClass = 'bg-green-600 hover:bg-green-700';
      break;
    case 'error':
      icon = <XCircleIcon className="h-6 w-6 text-red-500" />;
      actionClass = 'bg-red-600 hover:bg-red-700';
      break;
    case 'confirm':
      icon = <CheckCircleIcon className="text-primary h-6 w-6" />;
      actionClass = 'bg-primary hover:bg-primary/90';
      break;
    default:
      icon = <CheckCircleIcon className="h-6 w-6 text-gray-500" />;
      actionClass = 'bg-gray-700 hover:bg-gray-800';
  }

  const handleConfirm = () => {
    onConfirm?.();
    closeModal();
  };

  const handleCancel = () => {
    onCancel?.();
    closeModal();
  };

  return (
    <AlertDialog open={isModalOpen} onOpenChange={closeModal}>
      <AlertDialogContent className="sm:max-w-lg">
        <AlertDialogHeader>
          <div className="flex items-center gap-2">
            {!hideIcon && (Icon || icon)}
            {title && <AlertDialogTitle>{title}</AlertDialogTitle>}
          </div>

          {message && (
            <AlertDialogDescription asChild>
              <div className="whitespace-pre-wrap">{message}</div>
            </AlertDialogDescription>
          )}
        </AlertDialogHeader>

        <AlertDialogFooter>
          <AlertDialogCancel onClick={handleCancel}>{cancelButtonText}</AlertDialogCancel>

          <AlertDialogAction
            onClick={handleConfirm}
            data-testid={dataTestId}
            className={actionClass}
          >
            {submitButtonText}
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  );
};

export default ActionConfirmationModal;
