'use client';

import { FieldValues, UseFormReturn } from 'react-hook-form';
import {
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
  FormDescription,
} from '@/components/ui/form';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import { DateInput } from '@/components/ui/date-input';
import { DynamicFormConfig, FormElementType, FormFieldConfig } from '@/types/form.types';
import { cn } from '@/lib/utils/helpers';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '../ui/select';
import { ChipOption, ChipSelect } from './elements/ChipSelect';
import { AutocompleteMultiSelectInput } from './elements/AutocompleteSelectInput';
import { MultiSelectAutoComplete } from './elements/MultiSelectAutocomplete';
import { Tooltip, TooltipContent, TooltipTrigger } from '../ui/tooltip';
import { Info } from 'lucide-react';

type DynamicFormProps<T extends FieldValues> = {
  form?: UseFormReturn<T>;
  formConfig: DynamicFormConfig<T>;
  layoutVariant?: 'grid' | 'side-label';
  className?: string;
};

const getColSpanClass = (size?: number) => {
  const span = size || 12;
  const colSpanMap: Record<number, string> = {
    1: 'col-span-1',
    2: 'col-span-2',
    3: 'col-span-3',
    4: 'col-span-4',
    5: 'col-span-5',
    6: 'col-span-6',
    7: 'col-span-7',
    8: 'col-span-8',
    9: 'col-span-9',
    10: 'col-span-10',
    11: 'col-span-11',
    12: 'col-span-12',
  };
  return colSpanMap[Math.min(Math.max(span, 1), 12)] || 'col-span-12';
};

export function DynamicForm<T extends FieldValues>({
  form,
  formConfig,
  layoutVariant = 'grid',
  className,
}: DynamicFormProps<T>) {
  const renderField = (fieldConfig: FormFieldConfig<T>) => {
    switch (fieldConfig.type) {
      case FormElementType.TEXT:
        return (
          <FormField
            control={form?.control}
            name={fieldConfig.name}
            render={({ field }) => (
              <FormItem>
                {layoutVariant !== 'side-label' && (
                  <div className="flex items-center justify-between">
                    <FormLabel>
                      {fieldConfig.label}
                      {fieldConfig.required && <span className="text-destructive">*</span>}
                      {fieldConfig.tooltip && (
                        <Tooltip>
                          <TooltipTrigger asChild>
                            <Info className="text-muted-foreground h-5 w-5 cursor-pointer" />
                          </TooltipTrigger>
                          <TooltipContent side="right" className="max-w-lg text-start">
                            {fieldConfig.tooltip}
                          </TooltipContent>
                        </Tooltip>
                      )}
                    </FormLabel>
                    {fieldConfig.maxLength && (
                      <span className="text-muted-foreground text-xs">
                        {String(field.value ?? '').length}/{fieldConfig.maxLength}
                      </span>
                    )}
                  </div>
                )}
                <FormControl>
                  <Input
                    type={fieldConfig.inputType ?? 'text'}
                    placeholder={fieldConfig.placeholder}
                    disabled={fieldConfig.disabled}
                    maxLength={fieldConfig.maxLength}
                    {...field}
                  />
                </FormControl>
                {fieldConfig.description && (
                  <FormDescription>{fieldConfig.description}</FormDescription>
                )}
                <FormMessage />
              </FormItem>
            )}
          />
        );

      case FormElementType.TEXTAREA:
        return (
          <FormField
            control={form?.control}
            name={fieldConfig.name}
            render={({ field }) => (
              <FormItem>
                {layoutVariant !== 'side-label' && (
                  <div className="flex items-center justify-between">
                    <FormLabel>{fieldConfig.label}</FormLabel>
                    {fieldConfig.maxLength && (
                      <span className="text-muted-foreground text-xs">
                        {String(field.value ?? '').length}/{fieldConfig.maxLength}
                      </span>
                    )}
                  </div>
                )}
                <FormControl>
                  <Textarea
                    rows={fieldConfig.rows ?? 4}
                    placeholder={fieldConfig.placeholder}
                    disabled={fieldConfig.disabled}
                    maxLength={fieldConfig.maxLength}
                    className="bg-input-background max-h-50 min-h-25 resize-none overflow-y-auto"
                    {...field}
                  />
                </FormControl>
                {fieldConfig.description && (
                  <FormDescription>{fieldConfig.description}</FormDescription>
                )}
                <FormMessage />
              </FormItem>
            )}
          />
        );

      case FormElementType.SELECT:
        return (
          <FormField
            control={form?.control}
            name={fieldConfig.name}
            render={({ field }) => (
              <FormItem>
                {layoutVariant !== 'side-label' && <FormLabel>{fieldConfig.label}</FormLabel>}
                <Select
                  onValueChange={field.onChange}
                  value={field.value}
                  disabled={fieldConfig.disabled}
                >
                  <FormControl>
                    <SelectTrigger className="bg-input-background w-full">
                      <SelectValue placeholder={fieldConfig.placeholder} />
                    </SelectTrigger>
                  </FormControl>
                  <SelectContent>
                    {fieldConfig.options?.map((option) => (
                      <SelectItem key={option.value} value={option.value}>
                        {option.label}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
                {fieldConfig.description && (
                  <FormDescription>{fieldConfig.description}</FormDescription>
                )}
                <FormMessage />
              </FormItem>
            )}
          />
        );

      case FormElementType.CHIP_SELECT:
        return (
          <FormField
            control={form?.control}
            name={fieldConfig.name}
            render={({ field }) => {
              const chipValue: ChipOption[] = Array.isArray(field.value)
                ? field.value.map(
                    (v: string) =>
                      fieldConfig.options?.find((o) => o.value === v) || { label: v, value: v }
                  )
                : [];
              return (
                <FormItem>
                  {layoutVariant !== 'side-label' && <FormLabel>{fieldConfig.label}</FormLabel>}
                  <ChipSelect
                    value={chipValue}
                    onChange={(newValues: ChipOption[]) => {
                      field.onChange(newValues.map((v) => v.value));
                    }}
                    options={fieldConfig.options || []}
                    disabled={fieldConfig.disabled}
                  />
                  {fieldConfig.description && (
                    <FormDescription>{fieldConfig.description}</FormDescription>
                  )}
                  <FormMessage />
                </FormItem>
              );
            }}
          />
        );

      case FormElementType.AUTOCOMPLETE_SELECT:
        return (
          <FormField
            control={form?.control}
            name={fieldConfig.name}
            render={({ field }) => {
              const selectedOptions: { label: string; value: string }[] = Array.isArray(field.value)
                ? field.value.map(
                    (v: string) =>
                      fieldConfig.options?.find((o) => o.value === v) || { label: v, value: v }
                  )
                : [];
              return (
                <FormItem>
                  <div className="space-y-1">
                    {layoutVariant !== 'side-label' && <FormLabel>{fieldConfig.label}</FormLabel>}
                    {layoutVariant !== 'side-label' && (
                      <p className="text-muted-foreground text-sm">
                        {fieldConfig.labelSupportingText}
                      </p>
                    )}
                  </div>
                  <AutocompleteMultiSelectInput
                    value={selectedOptions}
                    onChange={(newValues: { label: string; value: string }[]) => {
                      field.onChange(newValues.map((v) => v.value));
                    }}
                    options={fieldConfig.options || []}
                    placeholder={fieldConfig.placeholder}
                    className="w-full"
                  />
                  {fieldConfig.description && (
                    <FormDescription>{fieldConfig.description}</FormDescription>
                  )}
                  <FormMessage />
                </FormItem>
              );
            }}
          />
        );

      case FormElementType.MULTI_SELECT:
        return (
          <FormField
            control={form?.control}
            name={fieldConfig.name}
            render={({ field }) => {
              const selectedItems: string[] = Array.isArray(field.value) ? field.value : [];
              return (
                <FormItem>
                  <div className="space-y-1">
                    {layoutVariant !== 'side-label' && <FormLabel>{fieldConfig.label}</FormLabel>}
                    {layoutVariant !== 'side-label' && fieldConfig.labelSupportingText && (
                      <p className="text-muted-foreground text-sm">
                        {fieldConfig.labelSupportingText}
                      </p>
                    )}
                  </div>
                  <FormControl>
                    <MultiSelectAutoComplete
                      items={fieldConfig.options ?? []}
                      selectedItems={selectedItems}
                      onChange={(newSelected) => {
                        field.onChange(newSelected.map((item) => item.value));
                      }}
                      placeholder={fieldConfig.placeholder}
                      disabled={fieldConfig.disabled}
                      allowNew={fieldConfig.allowNew ?? false}
                      hideSearch={fieldConfig.hideSearch ?? true}
                      className="w-full"
                    />
                  </FormControl>
                  {fieldConfig.description && (
                    <FormDescription>{fieldConfig.description}</FormDescription>
                  )}
                  <FormMessage />
                </FormItem>
              );
            }}
          />
        );

      case FormElementType.DATE:
        return (
          <FormField
            control={form?.control}
            name={fieldConfig.name}
            render={({ field }) => (
              <FormItem>
                {layoutVariant !== 'side-label' && <FormLabel>{fieldConfig.label}</FormLabel>}
                <FormControl>
                  <DateInput
                    value={field.value}
                    onChange={field.onChange}
                    placeholder={fieldConfig.placeholder}
                    min={fieldConfig.min}
                    max={fieldConfig.max}
                    disabled={fieldConfig.disabled}
                    className="bg-input-background"
                  />
                </FormControl>
                {fieldConfig.description && (
                  <FormDescription>{fieldConfig.description}</FormDescription>
                )}
                <FormMessage />
              </FormItem>
            )}
          />
        );
      default:
        return null;
    }
  };

  return (
    <div className={cn('grid gap-6', className)}>
      {formConfig.map((row, rowIndex) => {
        if (layoutVariant === 'side-label') {
          return (
            <div
              key={rowIndex}
              className="grid grid-cols-1 items-start gap-x-12 gap-y-4 md:grid-cols-12"
            >
              <div className="text-sm font-medium md:col-span-4">{row[0]?.label}</div>

              <div className="md:col-span-8">
                <div className="grid grid-cols-12 gap-4">
                  {row.map((field) => (
                    <div key={field.name} className={getColSpanClass(field.size)}>
                      {renderField(field)}
                    </div>
                  ))}
                </div>
              </div>
            </div>
          );
        }

        return (
          <div key={rowIndex} className="grid grid-cols-12 gap-6">
            {row.map((field) => (
              <div key={field.name} className={getColSpanClass(field.size)}>
                {renderField(field)}
              </div>
            ))}
          </div>
        );
      })}
    </div>
  );
}
