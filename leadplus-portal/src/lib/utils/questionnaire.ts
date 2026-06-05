import { QuestionType } from '@/constants/questions.constants';
import { Section } from '@/types/questionnaire.types';
import { AnswerRequest } from '@/types/vendor.types';

const getQuestionType = (questionId: number, sections: Section[]): QuestionType | undefined => {
  for (const section of sections) {
    const question = section.questions.find((q) => q.id === questionId);
    if (question) return question.type;
  }
  return undefined;
};

export const arrayToRecord = (
  arr: AnswerRequest[] | undefined,
  sections: Section[] = []
): Record<string, string | boolean | string[]> => {
  if (!arr || arr.length === 0) return {};
  return arr.reduce(
    (acc, item) => {
      const questionType = getQuestionType(item.questionId, sections);

      switch (questionType) {
        case QuestionType.BOOLEAN:
          acc[item.questionId] = item.answer === 'true';
          break;
        case QuestionType.MULTISELECT:
          acc[item.questionId] = item.answer ? item.answer.split(',').map((s) => s.trim()) : [];
          break;
        case QuestionType.TEXT:
        case QuestionType.TEXTAREA:
        case QuestionType.RADIO:
        default:
          acc[item.questionId] = item.answer;
          break;
      }
      return acc;
    },
    {} as Record<string, string | boolean | string[]>
  );
};

export const recordToArray = (record: Record<string, unknown> | undefined): AnswerRequest[] => {
  if (!record) return [];
  return Object.entries(record)
    .filter(([, value]) => {
      if (value === undefined || value === null || value === '') return false;
      if (typeof value === 'boolean') return true;
      if (Array.isArray(value)) return value.length > 0;
      return true;
    })
    .map(([questionId, answer]) => {
      const id = Number(questionId);
      let stringAnswer: string;
      if (typeof answer === 'boolean') {
        stringAnswer = String(answer);
      } else if (Array.isArray(answer)) {
        stringAnswer = answer.join(',');
      } else {
        stringAnswer = String(answer);
      }
      return {
        questionId: id,
        answer: stringAnswer,
      };
    });
};
