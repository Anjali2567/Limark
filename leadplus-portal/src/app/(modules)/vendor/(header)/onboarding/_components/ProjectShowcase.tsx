import Image from 'next/image';
import { useRouter, useSearchParams } from 'next/navigation';
import { useCallback } from 'react';
import { toast } from 'sonner';

import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { useAuth } from '@/context/AuthContext';
import { useGetAuthenticatedVendor } from '@/hooks/useAuthentication';
import { useDeleteVendorShowcase, useGetAllVendorShowcases } from '@/hooks/useVendorShowcase';

import { Briefcase, Pencil, Plus, Trash2 } from 'lucide-react';

const ProjectShowcase = () => {
  const router = useRouter();
  const searchParams = useSearchParams();

  const { authenticatedUserDetails } = useAuth();
  const { data: vendor } = useGetAuthenticatedVendor();

  const { mutate: deleteShowcase } = useDeleteVendorShowcase();
  const { data: projects = [] } = useGetAllVendorShowcases({
    tenantId: authenticatedUserDetails?.tenantId || '',
    vendorId: vendor?.id || '',
  });

  const handleAddClick = useCallback(() => {
    const params = new URLSearchParams(searchParams.toString());
    params.set('action', 'add');
    router.push(`?${params.toString()}`, { scroll: false });
  }, [router, searchParams]);

  const handleEditClick = useCallback(
    (projectId: string) => {
      const params = new URLSearchParams(searchParams.toString());
      params.set('id', projectId);
      router.push(`?${params.toString()}`, { scroll: false });
    },
    [router, searchParams]
  );

  const handleDeleteProject = (projectId: string) => {
    if (!authenticatedUserDetails?.tenantId || !vendor?.id) return;

    deleteShowcase(
      {
        id: projectId,
        tenantId: authenticatedUserDetails.tenantId,
        vendorId: vendor.id,
      },
      {
        onSuccess: () => {
          toast.success('Project deleted successfully');
        },
        onError: () => {
          toast.error('Failed to delete project');
        },
      }
    );
  };

  return (
    <Card className="border-border shadow-lg">
      <CardHeader className="border-border flex items-center justify-between border-b">
        <div>
          <div className="mb-2 flex items-center gap-2">
            <Briefcase className="h-5 w-5 text-sky-500" />
            <CardTitle>Project showcase</CardTitle>
          </div>
          <CardDescription>Showcase your best work to stand out</CardDescription>
        </div>
        <Button
          type="button"
          variant="outline"
          size="sm"
          onClick={handleAddClick}
          className="border-sky-500 text-sky-500 hover:border-sky-600 hover:bg-sky-50 hover:text-sky-600"
        >
          <Plus className="mr-2 h-4 w-4" />
          Add project
        </Button>
      </CardHeader>
      <CardContent className="pt-6">
        {projects.length === 0 ? (
          <div className="bg-muted/30 border-border rounded-lg border-2 border-dashed py-12 text-center">
            <Briefcase className="text-muted-foreground mx-auto mb-3 h-12 w-12" />
            <p className="text-muted-foreground mb-1 text-sm font-medium">No projects added yet</p>
            <p className="text-muted-foreground text-xs">
              Click &ldquo;Add project&rdquo; to showcase your work
            </p>
          </div>
        ) : (
          <div className="space-y-3">
            {projects.map((project) => (
              <div
                key={project.id}
                className="border-border bg-card hover:border-primary flex items-start justify-between rounded-lg border p-4 transition-colors"
              >
                <div className="min-w-0 flex-1">
                  <div className="flex items-start gap-3">
                    {project.attachments?.[0]?.fileUrl && (
                      <div className="border-border h-16 w-16 shrink-0 overflow-hidden rounded-lg border">
                        <Image
                          src={`${process.env.NEXT_PUBLIC_BASE_URL}${project.attachments[0].fileUrl}`}
                          alt={project.projectName}
                          width={64}
                          height={64}
                          className="h-full w-full object-cover"
                        />
                      </div>
                    )}
                    <div className="min-w-0 flex-1">
                      <h4 className="text-foreground mb-1 truncate font-semibold">
                        {project.projectName}
                      </h4>
                      {project.clientName && (
                        <p className="text-muted-foreground mb-2 text-sm">
                          Client: {project.clientName}
                        </p>
                      )}
                      {project.description && (
                        <p className="text-muted-foreground line-clamp-2 text-sm">
                          {project.description}
                        </p>
                      )}
                      <div className="mt-2 flex flex-wrap gap-2">
                        {project.serviceIds && project.serviceIds.length > 0 && (
                          <Badge variant="secondary" className="text-xs">
                            {project.serviceIds.length} service
                            {project.serviceIds.length > 1 ? 's' : ''}
                          </Badge>
                        )}
                        {project.duration && (
                          <Badge variant="secondary" className="text-xs">
                            {project.duration}
                          </Badge>
                        )}
                      </div>
                    </div>
                  </div>
                </div>
                <div className="ml-2 flex shrink-0 gap-1">
                  <Button
                    type="button"
                    variant="ghost"
                    size="sm"
                    onClick={() => handleEditClick(project.id)}
                    className="hover:bg-primary/10 text-primary hover:text-primary h-8 w-8 p-0"
                  >
                    <Pencil className="h-4 w-4" />
                  </Button>
                  <Button
                    type="button"
                    variant="ghost"
                    size="sm"
                    onClick={() => handleDeleteProject(project.id)}
                    className="text-destructive hover:text-destructive hover:bg-destructive/10 h-8 w-8 p-0"
                  >
                    <Trash2 className="h-4 w-4" />
                  </Button>
                </div>
              </div>
            ))}
          </div>
        )}
      </CardContent>
    </Card>
  );
};

export { ProjectShowcase };
