'use client';

import { useEffect, useRef, useState } from 'react';
import { toast } from 'sonner';

import { useGetAllServices } from '@/hooks/useService';
import { useGetAllSpecifications } from '@/hooks/useSpecification';
import { useUpdateVendorDetails } from '@/hooks/useVendor';
import { Service } from '@/types/services.types';
import { Vendor } from '@/types/vendor.types';
import { buildVendorPayload } from '../utils/buildVendorPayload';

import { Briefcase, Check, ChevronDown, Search, X } from 'lucide-react';

type ServiceMultiselectProps = {
  allServices: Service[];
  selectedIds: number[];
  onChange: (ids: number[]) => void;
};

const ServiceMultiselect = ({ allServices, selectedIds, onChange }: ServiceMultiselectProps) => {
  const [open, setOpen] = useState(false);
  const [search, setSearch] = useState('');
  const containerRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const handleClickOutside = (e: MouseEvent) => {
      if (containerRef.current && !containerRef.current.contains(e.target as Node)) {
        setOpen(false);
        setSearch('');
      }
    };
    if (open) document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, [open]);

  const filtered = allServices.filter((s) => s.name.toLowerCase().includes(search.toLowerCase()));

  const toggle = (serviceId: number) => {
    onChange(
      selectedIds.includes(serviceId)
        ? selectedIds.filter((id) => id !== serviceId)
        : [...selectedIds, serviceId]
    );
  };

  return (
    <div className="relative" ref={containerRef}>
      <button
        type="button"
        onClick={() => setOpen((v) => !v)}
        className="border-input bg-background hover:bg-secondary/40 focus:ring-ring flex w-full items-center justify-between rounded-md border px-3 py-2 text-left text-sm transition-colors focus:ring-2 focus:outline-none"
      >
        <span className="text-muted-foreground">
          {selectedIds.length === 0
            ? 'Select services…'
            : `${selectedIds.length} service${selectedIds.length === 1 ? '' : 's'} selected`}
        </span>
        <ChevronDown
          className={`text-muted-foreground h-4 w-4 transition-transform duration-200 ${open ? 'rotate-180' : ''}`}
        />
      </button>

      {open && (
        <div className="border-border bg-card animate-in fade-in slide-in-from-top-2 absolute z-50 mt-1 w-full overflow-hidden rounded-lg border shadow-lg duration-150">
          <div className="border-border border-b p-2">
            <div className="relative">
              <Search className="text-muted-foreground absolute top-1/2 left-2.5 h-3.5 w-3.5 -translate-y-1/2" />
              <input
                autoFocus
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                placeholder="Search services…"
                className="border-border bg-secondary/50 placeholder:text-muted-foreground focus:ring-ring w-full rounded-md border py-1.5 pr-3 pl-8 text-sm outline-none focus:ring-1"
              />
            </div>
          </div>

          <div className="max-h-56 overflow-y-auto py-1">
            {filtered.length === 0 ? (
              <p className="text-muted-foreground py-4 text-center text-sm">No results found.</p>
            ) : (
              filtered.map((service) => {
                const isChecked = selectedIds.includes(service.id);
                return (
                  <button
                    key={service.id}
                    type="button"
                    onClick={() => toggle(service.id)}
                    className={`hover:bg-secondary/60 flex w-full items-center gap-3 px-3 py-2 text-left text-sm transition-colors ${
                      isChecked ? 'text-foreground' : 'text-muted-foreground'
                    }`}
                  >
                    <span
                      className={`flex h-4 w-4 shrink-0 items-center justify-center rounded border transition-colors ${
                        isChecked ? 'border-primary bg-primary' : 'border-border bg-background'
                      }`}
                    >
                      {isChecked && <Check className="h-3 w-3 text-white" />}
                    </span>
                    {service.name}
                  </button>
                );
              })
            )}
          </div>

          {selectedIds.length > 0 && (
            <div className="border-border bg-secondary/30 flex items-center justify-between border-t px-3 py-2">
              <span className="text-muted-foreground text-xs">{selectedIds.length} selected</span>
              <button
                type="button"
                onClick={() => onChange([])}
                className="text-destructive text-xs hover:underline"
              >
                Clear all
              </button>
            </div>
          )}
        </div>
      )}
    </div>
  );
};

type ServiceSpecCardProps = {
  service: Service;
  specificationIds: number[];
  onSpecToggle: (specId: number) => void;
  onRemove: (serviceSpecIds: number[]) => void;
};

const ServiceSpecCard = ({
  service,
  specificationIds,
  onSpecToggle,
  onRemove,
}: ServiceSpecCardProps) => {
  const { data: specs = [], isLoading } = useGetAllSpecifications({ serviceId: service.id });
  const serviceSpecIds = specs.map((s) => s.id);

  return (
    <div className="border-border bg-card overflow-hidden rounded-lg border">
      <div className="border-border flex items-center justify-between border-b bg-black/8 px-4 py-3">
        <div className="flex items-center gap-2">
          <Briefcase className="text-primary h-4 w-4 shrink-0" />
          <span className="text-foreground text-sm font-semibold">{service.name}</span>
          {specs.length > 0 && (
            <span className="bg-secondary text-muted-foreground rounded-full px-1.5 py-0.5 text-xs">
              {specs.filter((s) => specificationIds.includes(s.id)).length}/{specs.length} selected
            </span>
          )}
        </div>
        <button
          type="button"
          onClick={() => onRemove(serviceSpecIds)}
          className="text-muted-foreground hover:text-destructive transition-colors"
          title="Remove service"
        >
          <X className="h-4 w-4" />
        </button>
      </div>

      <div className="px-4 py-4">
        {isLoading && <p className="text-muted-foreground text-xs">Loading specifications…</p>}

        {!isLoading && specs.length === 0 && (
          <p className="text-muted-foreground text-xs">
            No specifications available for this service.
          </p>
        )}

        {!isLoading && specs.length > 0 && (
          <div className="flex flex-wrap gap-1.5">
            {specs.map((spec) => {
              const isSelected = specificationIds.includes(spec.id);
              return (
                <button
                  key={spec.id}
                  type="button"
                  onClick={() => onSpecToggle(spec.id)}
                  className={`rounded-full px-3 py-1 text-xs font-medium transition-colors ${
                    isSelected
                      ? 'border-primary/20 bg-primary/10 text-primary border'
                      : 'border-border bg-background text-foreground hover:bg-accent border'
                  }`}
                >
                  {spec.name}
                </button>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
};

type Props = { vendor?: Vendor; formId: string };

export const ServicesSection = ({ vendor, formId }: Props) => {
  const { mutate } = useUpdateVendorDetails();
  const { data: allServices = [] } = useGetAllServices({});

  const [serviceIds, setServiceIds] = useState<number[]>(vendor?.serviceIds || []);
  const [specificationIds, setSpecificationIds] = useState<number[]>(
    vendor?.specificationIds || []
  );

  const handleServiceChange = (newIds: number[]) => {
    setServiceIds(newIds);
  };

  const handleRemove = (serviceId: number, serviceSpecIds: number[]) => {
    setServiceIds((prev) => prev.filter((id) => id !== serviceId));
    setSpecificationIds((prev) => prev.filter((id) => !serviceSpecIds.includes(id)));
  };

  const handleSpecToggle = (specId: number) => {
    setSpecificationIds((prev) =>
      prev.includes(specId) ? prev.filter((id) => id !== specId) : [...prev, specId]
    );
  };

  const addedServices = allServices.filter((s) => serviceIds.includes(s.id));

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    mutate(buildVendorPayload(vendor, { serviceIds, specificationIds }), {
      onSuccess: () => toast.success('Services saved'),
      onError: () => toast.error('Failed to save'),
    });
  };

  return (
    <form id={formId} onSubmit={handleSubmit} className="space-y-5">
      <ServiceMultiselect
        allServices={allServices}
        selectedIds={serviceIds}
        onChange={handleServiceChange}
      />

      {addedServices.length === 0 ? (
        <p className="text-muted-foreground text-sm">
          No services selected yet. Use the dropdown above to add services.
        </p>
      ) : (
        <div className="space-y-3">
          {addedServices.map((service) => (
            <ServiceSpecCard
              key={service.id}
              service={service}
              specificationIds={specificationIds}
              onSpecToggle={handleSpecToggle}
              onRemove={(serviceSpecIds) => handleRemove(service.id, serviceSpecIds)}
            />
          ))}
        </div>
      )}
    </form>
  );
};
