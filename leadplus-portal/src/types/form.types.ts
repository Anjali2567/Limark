import { HTMLInputTypeAttribute } from 'react';
import { FieldValues, Path } from 'react-hook-form';

export enum FormElementType {
  TEXT = 'TEXT',
  TEXTAREA = 'TEXTAREA',
  SELECT = 'SELECT',
  CHIP_SELECT = 'CHIP_SELECT',
  AUTOCOMPLETE_SELECT = 'AUTOCOMPLETE_SELECT',
  MULTI_SELECT = 'MULTI_SELECT',
  DATE = 'DATE',
}

export type FormOption = {
  label: string;
  value: string;
  tooltip?: string;
  icon?: string;
  children?: FormOption[];
};

export type BaseField<T extends FieldValues> = {
  name: Path<T>;
  label: string;
  required?: boolean;
  labelSupportingText?: string;
  placeholder?: string;
  description?: string;
  disabled?: boolean;
  size?: number;
  options?: FormOption[];
};

export type TextField<T extends FieldValues> = BaseField<T> & {
  type: FormElementType.TEXT;
  tooltip?: string;
  inputType?: HTMLInputTypeAttribute;
  maxLength?: number;
};

export type TextAreaField<T extends FieldValues> = BaseField<T> & {
  type: FormElementType.TEXTAREA;
  rows?: number;
  maxLength?: number;
};

export type SelectField<T extends FieldValues> = BaseField<T> & {
  type: FormElementType.SELECT;
  options?: FormOption[];
};

export type ChipSelectField<T extends FieldValues> = BaseField<T> & {
  type: FormElementType.CHIP_SELECT;
  options?: FormOption[];
};

export type AutocompleteSelectField<T extends FieldValues> = BaseField<T> & {
  type: FormElementType.AUTOCOMPLETE_SELECT;
  options?: FormOption[];
};

export type MultiSelectField<T extends FieldValues> = BaseField<T> & {
  type: FormElementType.MULTI_SELECT;
  options?: FormOption[];
  allowNew?: boolean;
  hideSearch?: boolean;
};

export type DateField<T extends FieldValues> = BaseField<T> & {
  type: FormElementType.DATE;
  min?: string;
  max?: string;
};

export type FormFieldConfig<T extends FieldValues> =
  | TextField<T>
  | TextAreaField<T>
  | SelectField<T>
  | ChipSelectField<T>
  | AutocompleteSelectField<T>
  | MultiSelectField<T>
  | DateField<T>;

export type DynamicFormConfig<T extends FieldValues> = FormFieldConfig<T>[][];
