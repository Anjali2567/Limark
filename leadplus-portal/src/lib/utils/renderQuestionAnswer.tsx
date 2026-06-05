import { ReactNode } from 'react';
import { Badge } from '@/components/ui/badge';
import { CheckCircle2, XCircle } from 'lucide-react';
import { QuestionType } from '@/constants/questions.constants';
import { Question } from '@/types/questionnaire.types';

export const renderQuestionAnswer = (question: Question, answer: string | undefined): ReactNode => {
  if (answer === undefined || answer === null || answer === '') {
    return <span className="text-muted-foreground text-sm italic">No answer provided</span>;
  }

  switch (question.type) {
    case QuestionType.BOOLEAN:
      const isTrue = answer === 'true';
      return (
        <div className="flex items-center gap-2">
          {isTrue ? (
            <>
              <CheckCircle2 className="h-4 w-4 text-green-600" />
              <span className="text-sm font-medium text-green-600">Yes</span>
            </>
          ) : (
            <>
              <XCircle className="h-4 w-4 text-gray-500" />
              <span className="text-sm font-medium text-gray-500">No</span>
            </>
          )}
        </div>
      );

    case QuestionType.MULTISELECT:
      const options = answer ? answer.split(',').map((s) => s.trim()) : [];
      if (options.length === 0) {
        return <span className="text-muted-foreground text-sm italic">No answer provided</span>;
      }
      return (
        <div className="flex flex-wrap gap-2">
          {options.map((option, idx) => (
            <Badge key={`${option}-${idx}`} variant="secondary" className="text-xs">
              {option}
            </Badge>
          ))}
        </div>
      );

    case QuestionType.TEXTAREA:
      return (
        <p className="text-muted-foreground text-sm leading-relaxed whitespace-pre-wrap">
          {answer}
        </p>
      );

    case QuestionType.TEXT:
    case QuestionType.RADIO:
    default:
      return <p className="text-muted-foreground text-sm">{answer}</p>;
  }
};
