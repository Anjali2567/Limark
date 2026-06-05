export enum QuestionType {
  TEXT = 'TEXT',
  TEXTAREA = 'TEXTAREA',
  MULTISELECT = 'MULTISELECT',
  BOOLEAN = 'BOOLEAN',
  RADIO = 'RADIO',
}

export const questionTypeOptions = [
  { label: 'Text', value: QuestionType.TEXT },
  { label: 'Textarea', value: QuestionType.TEXTAREA },
  { label: 'Multiselect', value: QuestionType.MULTISELECT },
  { label: 'Boolean', value: QuestionType.BOOLEAN },
  { label: 'Radio', value: QuestionType.RADIO },
];
