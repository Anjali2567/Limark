import { QuestionType } from '@/constants/questions.constants';
import { PaginationParams } from './paginated-params';

export type CheckListSection = {
  id: string;
  name: string;
  position: number | null;
  createdBy?: string;
  createdAt?: string;
  updatedBy?: string;
  updatedAt?: string;
};

export type CheckListSectionRequest = {
  name: string;
  position?: number | null;
};

export type CheckListSectionUpdateRequest = {
  id: string;
  name: string;
  position?: number | null;
};

export type CheckListQuestion = {
  id: string;
  questionSectionId: string;
  position: number | null;
  industryIds: string[];
  type: QuestionType;
  label: string;
  options?: string[];
};

export type CheckListQuestionRequest = Omit<CheckListQuestion, 'id'>;

export type UpsertCheckListSectionRequest = {
  sectionId?: string;
  payload: { name: string };
};

export type UpsertRFQCheckListQuestionRequest = {
  questionId?: string;
  payload: CheckListQuestionRequest;
};

export type CheckListParams = {
  questionIds?: string[];
  capabilityIds?: string[];
} & PaginationParams;

export type GroupedSection = {
  section: CheckListSection;
  questionList: CheckListQuestion[];
};
