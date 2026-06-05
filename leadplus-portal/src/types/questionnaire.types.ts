import { QuestionType } from '@/constants/questions.constants';

export type Question = {
  id: number;
  questionSectionId: string;
  position: number;
  type: QuestionType;
  label: string;
  options: string[];
};

export type Section = {
  sectionId: string;
  sectionName: string;
  position?: number;
  questions: Question[];
};

export type Questionnaire = Section[];

export type QuestionnaireParams = {
  industryIds: string[];
};
