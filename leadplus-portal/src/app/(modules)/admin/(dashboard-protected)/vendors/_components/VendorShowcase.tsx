'use client';

import Image from 'next/image';
import { useSearchParams } from 'next/navigation';
import { useState, useMemo } from 'react';

import { SectionCard } from '@/components/SectionCard';
import { Badge } from '@/components/ui/badge';
import {
  Sheet,
  SheetContent,
  SheetDescription,
  SheetHeader,
  SheetTitle,
} from '@/components/ui/sheet';
import { useGetAllVendorShowcasesByVendorId } from '@/hooks/useVendorShowcase';
import { getCachedImageUrl } from '@/lib/utils/helpers';
import { VendorShowcase as VendorShowcaseType } from '@/types/vendor-showcase.types';

import { Briefcase, Calendar, FileText, User } from 'lucide-react';
import { useGetAllServices } from '@/hooks/useService';

const VendorShowcase = () => {
  const searchParams = useSearchParams();
  const vendorId = searchParams.get('id') || '';

  const { data: servicesList = [] } = useGetAllServices({});
  const { data: projects = [] } = useGetAllVendorShowcasesByVendorId({ vendorId });
  const [selectedProject, setSelectedProject] = useState<VendorShowcaseType | null>(null);

  const serviceMap = useMemo(() => {
    return servicesList.reduce(
      (acc, service) => {
        acc[service.id] = service.name;
        return acc;
      },
      {} as Record<string, string>
    );
  }, [servicesList]);

  const getServiceNames = (serviceIds: string[]): string[] => {
    return serviceIds.map((id) => serviceMap[id] || id).filter(Boolean);
  };

  return (
    <>
      <SectionCard title="Project Showcase">
        <div className="col-span-2 py-4">
          {projects.length === 0 ? (
            <div className="bg-muted/30 border-border rounded-lg border-2 border-dashed py-12 text-center">
              <Briefcase className="text-muted-foreground mx-auto mb-3 h-12 w-12" />
              <p className="text-muted-foreground mb-1 text-sm font-medium">
                No projects available
              </p>
              <p className="text-muted-foreground text-xs">
                This vendor has not added any showcase projects yet
              </p>
            </div>
          ) : (
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
              {projects.map((project) => (
                <div
                  key={project.id}
                  onClick={() => setSelectedProject(project)}
                  className="border-border bg-card hover:border-primary group cursor-pointer overflow-hidden rounded-lg border transition-all hover:shadow-lg"
                >
                  {project.attachments?.[0]?.fileUrl && (
                    <div className="relative h-36 w-full overflow-hidden bg-gray-100">
                      <Image
                        src={
                          getCachedImageUrl(project.attachments[0].fileUrl, project.updatedAt) || ''
                        }
                        alt={project.projectName}
                        fill
                        className="object-cover transition-transform duration-300 group-hover:scale-105"
                      />
                    </div>
                  )}
                  <div className="p-4">
                    <h4 className="text-foreground mb-2 line-clamp-1 font-semibold">
                      {project.projectName}
                    </h4>
                    {project.clientName && (
                      <p className="text-muted-foreground mb-2 flex items-center gap-2 text-sm">
                        <User className="h-4 w-4" />
                        {project.clientName}
                      </p>
                    )}
                    {project.description && (
                      <p className="text-muted-foreground mb-3 line-clamp-2 text-sm">
                        {project.description}
                      </p>
                    )}
                    <div className="flex flex-wrap gap-2">
                      {project.duration && (
                        <Badge variant="secondary" className="text-xs">
                          {project.duration}
                        </Badge>
                      )}
                      {project.serviceIds && project.serviceIds.length > 0 && (
                        <Badge variant="secondary" className="text-xs">
                          {project.serviceIds.length} service
                          {project.serviceIds.length > 1 ? 's' : ''}
                        </Badge>
                      )}
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </SectionCard>

      <Sheet open={!!selectedProject} onOpenChange={() => setSelectedProject(null)}>
        <SheetContent className="flex h-full w-full flex-col gap-0 overflow-y-auto sm:max-w-2xl">
          {selectedProject && (
            <>
              <SheetHeader className="bg-card border-border shrink-0 border-b p-6">
                <SheetTitle className="text-lg">{selectedProject.projectName}</SheetTitle>
                <SheetDescription className="sr-only">Project showcase details</SheetDescription>
              </SheetHeader>

              <div className="flex-1 space-y-6 p-6">
                {selectedProject.attachments?.[0]?.fileUrl && (
                  <div className="relative h-64 w-full overflow-hidden rounded-lg border">
                    <Image
                      src={
                        getCachedImageUrl(
                          selectedProject.attachments[0].fileUrl,
                          selectedProject.updatedAt
                        ) || ''
                      }
                      alt={selectedProject.projectName}
                      fill
                      className="object-cover"
                    />
                  </div>
                )}

                {selectedProject.clientName && (
                  <div className="space-y-2">
                    <div className="flex items-center gap-2 text-sm font-medium">
                      <User className="h-4 w-4 text-sky-500" />
                      <span>Client Name</span>
                    </div>
                    <p className="text-muted-foreground text-sm">{selectedProject.clientName}</p>
                  </div>
                )}

                <div className="space-y-2">
                  <div className="flex items-center gap-2 text-sm font-medium">
                    <FileText className="h-4 w-4 text-sky-500" />
                    <span>Project Description</span>
                  </div>
                  <p className="text-muted-foreground text-sm leading-relaxed whitespace-pre-line">
                    {selectedProject.description}
                  </p>
                </div>

                {selectedProject.duration && (
                  <div className="space-y-2">
                    <div className="flex items-center gap-2 text-sm font-medium">
                      <Calendar className="h-4 w-4 text-sky-500" />
                      <span>Duration/Year</span>
                    </div>
                    <p className="text-muted-foreground text-sm">{selectedProject.duration}</p>
                  </div>
                )}

                {selectedProject.serviceIds && selectedProject.serviceIds.length > 0 && (
                  <div className="space-y-2">
                    <div className="flex items-center gap-2 text-sm font-medium">
                      <Briefcase className="h-4 w-4 text-sky-500" />
                      <span>Services Provided</span>
                    </div>
                    <div className="flex flex-wrap gap-2">
                      {getServiceNames(selectedProject.serviceIds).map((serviceName, idx) => (
                        <Badge key={idx} variant="secondary">
                          {serviceName}
                        </Badge>
                      ))}
                    </div>
                  </div>
                )}

                {selectedProject.resultsAndOutcomes && (
                  <div className="space-y-2">
                    <div className="flex items-center gap-2 text-sm font-medium">
                      <FileText className="h-4 w-4 text-sky-500" />
                      <span>Results & Outcomes</span>
                    </div>
                    <p className="text-muted-foreground text-sm leading-relaxed whitespace-pre-line">
                      {selectedProject.resultsAndOutcomes}
                    </p>
                  </div>
                )}
              </div>
            </>
          )}
        </SheetContent>
      </Sheet>
    </>
  );
};

export { VendorShowcase };
