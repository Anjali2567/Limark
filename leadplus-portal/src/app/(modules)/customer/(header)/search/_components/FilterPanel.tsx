'use client';

import { useState } from 'react';
import { useForm, useWatch } from 'react-hook-form';

import { Form, FormControl, FormField, FormItem } from '@/components/ui/form';
import { useDebounce } from '@/hooks/useDebounce';
import { FilterSection } from './FilterSection';
import { useGetAllSpecifications } from '@/hooks/useSpecification';
import { useGetAllServices } from '@/hooks/useService';
import { CheckboxFilterGroup } from '@/components/form/elements/CheckboxFilterGroup';
import { FilterValues } from '../page';

type FilterPanelProps = {
  onSubmit: (data: FilterValues) => void;
  initialServiceIds?: number[];
  initialSpecificationIds?: number[];
};

const sortWithSelectedFirst = (items: { id: number; name: string }[], selectedIds: number[]) =>
  items
    .slice()
    .sort((a, b) => {
      const aSelected = selectedIds.includes(a.id) ? -1 : 1;
      const bSelected = selectedIds.includes(b.id) ? -1 : 1;
      return aSelected - bSelected;
    })
    .map((s) => ({ label: s.name, value: s.id }));

const FilterPanel = ({
  onSubmit,
  initialServiceIds = [],
  initialSpecificationIds = [],
}: FilterPanelProps) => {
  const { data: specifications = [] } = useGetAllSpecifications({});
  const { data: services = [] } = useGetAllServices({});

  const [openSections, setOpenSections] = useState<Record<string, boolean>>({
    specifications: true,
    services: true,
  });

  const toggleSection = (section: string) => {
    setOpenSections((prev) => ({ ...prev, [section]: !prev[section] }));
  };

  const form = useForm<FilterValues>({
    defaultValues: {
      specifications: initialSpecificationIds,
      services: initialServiceIds,
    },
  });

  const {
    control,
    handleSubmit,
    setValue,
    formState: { isDirty },
    reset,
  } = form;

  const watchedValues = useWatch({ control });

  const onChange = (data: FilterValues) => {
    reset(data);
    onSubmit(data);
  };

  useDebounce(watchedValues, 500, () => {
    if (isDirty) {
      handleSubmit(onChange)();
    }
  });

  return (
    <div className="bg-card border-border flex h-full flex-col divide-y rounded-lg border shadow-xs">
      <Form {...form}>
        <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col space-y-4 py-4">
          <FilterSection
            title="Service Type"
            isOpen={openSections.services}
            onToggle={() => toggleSection('services')}
            count={watchedValues.services?.length}
          >
            <FormField
              control={control}
              name="services"
              render={({ field }) => (
                <FormItem>
                  <FormControl>
                    <CheckboxFilterGroup
                      groupId="services"
                      options={sortWithSelectedFirst(services, initialServiceIds)}
                      selectedValues={field.value || []}
                      onChange={(values) =>
                        setValue('services', values.map(Number), { shouldDirty: true })
                      }
                      maxHeight={200}
                    />
                  </FormControl>
                </FormItem>
              )}
            />
          </FilterSection>

          <FilterSection
            title="Specifications"
            isOpen={openSections.specifications}
            onToggle={() => toggleSection('specifications')}
            count={watchedValues.specifications?.length}
          >
            <FormField
              control={control}
              name="specifications"
              render={({ field }) => (
                <FormItem>
                  <FormControl>
                    <CheckboxFilterGroup
                      groupId="specifications"
                      options={sortWithSelectedFirst(specifications, initialSpecificationIds)}
                      selectedValues={field.value || []}
                      onChange={(values) =>
                        setValue('specifications', values.map(Number), { shouldDirty: true })
                      }
                      maxHeight={200}
                    />
                  </FormControl>
                </FormItem>
              )}
            />
          </FilterSection>
        </form>
      </Form>
    </div>
  );
};

export { FilterPanel };
