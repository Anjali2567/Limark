'use client';

import { useState } from 'react';
import { ChevronDown, ChevronUp } from 'lucide-react';
import { Service, ServiceCategory } from '@/types/services.types';

type ServiceAccordionItemProps = {
  category: ServiceCategory;
  services: Service[];
  selectedServiceIds: number[];
  onChange: (ids: number[]) => void;
  defaultOpen?: boolean;
};

const ServiceAccordionItem = ({
  category,
  services,
  selectedServiceIds,
  onChange,
  defaultOpen = false,
}: ServiceAccordionItemProps) => {
  const [isOpen, setIsOpen] = useState(defaultOpen);

  const selectedCount = services.filter((s) => selectedServiceIds.includes(s.id)).length;

  const handleToggle = (serviceId: number) => {
    if (selectedServiceIds.includes(serviceId)) {
      onChange(selectedServiceIds.filter((id) => id !== serviceId));
    } else {
      onChange([...selectedServiceIds, serviceId]);
    }
  };

  return (
    <div className="border-border rounded-lg border bg-slate-50">
      <button
        type="button"
        className="flex w-full items-center justify-between px-4 py-3 text-left"
        onClick={() => setIsOpen((prev) => !prev)}
      >
        <div className="flex items-center gap-3">
          <span className="text-foreground font-medium">{category.name}</span>
          {selectedCount > 0 && (
            <span className="bg-primary/10 text-primary rounded-full px-2 py-0.5 text-xs font-semibold">
              {selectedCount} Selected
            </span>
          )}
        </div>
        {isOpen ? (
          <ChevronUp className="text-muted-foreground h-4 w-4 shrink-0" />
        ) : (
          <ChevronDown className="text-muted-foreground h-4 w-4 shrink-0" />
        )}
      </button>

      {isOpen && (
        <div className="border-border border-t px-4 py-4">
          <p className="text-foreground mb-3 text-sm font-medium">Select Services *</p>
          <div className="flex flex-wrap gap-2">
            {services.map((service) => {
              const isSelected = selectedServiceIds.includes(service.id);
              return (
                <button
                  key={service.id}
                  type="button"
                  onClick={() => handleToggle(service.id)}
                  className={`rounded-full px-4 py-2 text-sm font-medium transition-colors ${
                    isSelected
                      ? 'bg-primary text-primary-foreground'
                      : 'bg-background text-foreground border-border hover:bg-accent border'
                  }`}
                >
                  {service.name}
                </button>
              );
            })}
          </div>
        </div>
      )}
    </div>
  );
};

export { ServiceAccordionItem };
