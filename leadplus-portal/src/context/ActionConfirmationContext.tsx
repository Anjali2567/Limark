import useToggle from '@/hooks/useToggle';
import { createContext, ReactNode, useState } from 'react';

type ActionFlowType = 'success' | 'error' | 'confirm';

type ActionConfirmationModalValues = {
  type: ActionFlowType;
  title: string;
  message: string | ReactNode;
  cancelButtonText?: string;
  submitButtonText?: string;
  onConfirm: () => void;
  onCancel?: () => void;
  dataTestId?: string;
  hideIcon?: boolean;
  icon?: ReactNode;
};

type ModalContextType = {
  isModalOpen: boolean;
  modalContent: ActionConfirmationModalValues | null;
  renderModal: (data: ActionConfirmationModalValues) => void;
  closeModal: () => void;
};

export const ActionModalContext = createContext<ModalContextType | null>(null);

export const ModalProvider = ({ children }: { children: React.ReactNode }) => {
  const { value: isModalOpen, setValue: setIsModalOpen } = useToggle(false);
  const [modalContent, setModalContent] = useState<ActionConfirmationModalValues | null>(null);

  const renderModal = (data: ActionConfirmationModalValues) => {
    setIsModalOpen(true);
    setModalContent(data);
  };

  const closeModal = () => {
    setIsModalOpen(false);
    setModalContent(null);
  };

  return (
    <ActionModalContext.Provider value={{ isModalOpen, modalContent, renderModal, closeModal }}>
      {children}
    </ActionModalContext.Provider>
  );
};
