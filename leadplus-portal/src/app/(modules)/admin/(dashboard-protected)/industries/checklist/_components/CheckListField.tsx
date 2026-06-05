import { QuestionType } from '@/constants/questions.constants';
import { CheckListQuestion } from '@/types/question.types';

const CheckListField = ({ item }: { item: CheckListQuestion }) => {
  switch (item.type) {
    case QuestionType.TEXT:
      return <div className="border-inputBorder h-10 rounded-lg border bg-white" />;
    case QuestionType.TEXTAREA:
      return <div className="border-inputBorder h-16 rounded-lg border bg-white" />;
    case QuestionType.BOOLEAN:
      return (
        <div className="flex gap-3">
          <span className="border-sectionBorder text-placeHolder rounded-full border bg-white px-4 py-2 text-xs">
            Yes
          </span>
          <span className="border-sectionBorder text-placeHolder rounded-full border bg-white px-4 py-2 text-xs">
            No
          </span>
        </div>
      );
    case QuestionType.MULTISELECT:
    case QuestionType.RADIO:
      return (
        <div className="flex flex-wrap gap-3">
          {item.options?.map((option, index) => (
            <span
              key={`${option}-${index}`}
              className="border-sectionBorder text-placeHolder rounded-full border bg-white px-4 py-2 text-xs"
            >
              {option}
            </span>
          ))}
        </div>
      );
    default:
      return <div className="border-inputBorder h-10 rounded-lg border bg-white" />;
  }
};

export { CheckListField };
