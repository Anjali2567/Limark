import { Dispatch, FC, SetStateAction, useCallback } from 'react';

import { DragHandleProps } from '@/components/draggable/NestedDraggableList';
import { Button } from '@/components/ui/button';
import { useModal } from '@/hooks/useModal';
import { useDeleteCheckListQuestion } from '@/hooks/useQuestion';
import { CheckListQuestion } from '@/types/question.types';
import { CheckListField } from './CheckListField';

import { GripVertical, PencilIcon, TrashIcon } from 'lucide-react';

interface RenderQuestionItemProps {
  item: CheckListQuestion;
  index: number;
  dragHandleProps: DragHandleProps;
  onEditQuestion: (item: CheckListQuestion, index: number) => void;
  setQuestionList: Dispatch<SetStateAction<CheckListQuestion[]>>;
}

const RenderQuestionItem: FC<RenderQuestionItemProps> = ({
  item,
  index,
  dragHandleProps,
  onEditQuestion,
  setQuestionList,
}) => {
  const { renderModal } = useModal();

  const { mutate: deleteQuestion } = useDeleteCheckListQuestion();

  const handleRemoveQuestion = useCallback(
    (id: string) => {
      renderModal({
        type: 'error',
        title: 'Delete Question',
        message: 'Are you sure you want to delete this question?',
        cancelButtonText: 'Cancel',
        submitButtonText: 'Delete',
        onConfirm: () => {
          deleteQuestion(id);
          setQuestionList((prevQuestions) => prevQuestions.filter((q) => q.id !== id));
        },
      });
    },
    [deleteQuestion, renderModal, setQuestionList]
  );

  return (
    <div
      key={`${item.id}-${index}`}
      className="border-sectionBorder mx-2 mb-4 rounded-lg border bg-white px-4 py-6"
    >
      <div className="flex items-start">
        <GripVertical {...dragHandleProps} className="mr-2 h-5 w-5 shrink-0 cursor-grab" />

        <p className="grow overflow-hidden text-sm font-medium wrap-break-word">{item.label}</p>

        <div className="ml-auto flex gap-2">
          <Button
            variant="none"
            className="h-fit! p-0!"
            onClick={() => onEditQuestion(item, index)}
          >
            <PencilIcon className="text-romanSilver h-4 w-4" />
          </Button>
          <Button
            variant="none"
            className="h-fit! p-0!"
            onClick={() => handleRemoveQuestion(item.id)}
          >
            <TrashIcon className="text-romanSilver h-4 w-4" />
          </Button>
        </div>
      </div>

      <div className="mt-3 ml-7 w-10/12">
        <CheckListField item={item} />
      </div>
    </div>
  );
};

export { RenderQuestionItem };
