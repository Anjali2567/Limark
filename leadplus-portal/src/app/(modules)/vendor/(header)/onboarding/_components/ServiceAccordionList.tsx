'use client';

import { useState } from 'react';
import { Search } from 'lucide-react';
import { Input } from '@/components/ui/input';
import { Service, ServiceCategory } from '@/types/services.types';
import { ServiceAccordionItem } from './ServiceAccordionItem';

type ServiceAccordionListProps = {
  categories: ServiceCategory[];
  services: Service[];
  selectedServiceIds: number[];
  onChange: (ids: number[]) => void;
};

const ServiceAccordionList = ({
  categories,
  services,
  selectedServiceIds,
  onChange,
}: ServiceAccordionListProps) => {
  const [searchQuery, setSearchQuery] = useState('');

  const lowerQuery = searchQuery.toLowerCase();

  const visibleCategories = categories.filter((category) => {
    const categoryServices = services.filter((s) => s.serviceCategoryId === category.id);
    if (categoryServices.length === 0) return false;
    if (!lowerQuery) return true;
    return (
      category.name.toLowerCase().includes(lowerQuery) ||
      categoryServices.some((s) => s.name.toLowerCase().includes(lowerQuery))
    );
  });

  return (
    <div className="space-y-4">
      <div className="relative">
        <Search className="text-muted-foreground absolute top-1/2 left-3 h-4 w-4 -translate-y-1/2" />
        <Input
          type="text"
          placeholder="Search for Services"
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          className="pl-9"
        />
      </div>

      <p className="text-foreground font-semibold">All Services</p>

      <div className="space-y-2">
        {visibleCategories.length === 0 ? (
          <p className="text-muted-foreground py-6 text-center text-sm">No services found</p>
        ) : (
          visibleCategories.map((category) => {
            const categoryServices = services.filter((s) => s.serviceCategoryId === category.id);
            const hasSelected = categoryServices.some((s) => selectedServiceIds.includes(s.id));
            return (
              <ServiceAccordionItem
                key={category.id}
                category={category}
                services={categoryServices}
                selectedServiceIds={selectedServiceIds}
                onChange={onChange}
                defaultOpen={hasSelected}
              />
            );
          })
        )}
      </div>
    </div>
  );
};

export { ServiceAccordionList };
