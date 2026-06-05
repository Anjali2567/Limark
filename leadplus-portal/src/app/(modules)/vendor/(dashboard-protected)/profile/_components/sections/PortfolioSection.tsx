'use client';

import { useState } from 'react';
import { toast } from 'sonner';

import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { useAuth } from '@/context/AuthContext';
import { useGetAuthenticatedVendor } from '@/hooks/useAuthentication';
import {
  useDeleteVendorShowcase,
  useGetAllVendorShowcases,
  useUpsertVendorShowcase,
} from '@/hooks/useVendorShowcase';
import { VendorShowcase } from '@/types/vendor-showcase.types';

import { Check, Pencil, Plus, Trash2 } from 'lucide-react';

type ProjectDraft = {
  projectName: string;
  clientName: string;
  duration: string;
  description: string;
};

const emptyDraft = (): ProjectDraft => ({
  projectName: '',
  clientName: '',
  duration: '',
  description: '',
});

export const PortfolioSection = () => {
  const { authenticatedUserDetails } = useAuth();
  const { data: vendor } = useGetAuthenticatedVendor();

  const tenantId = authenticatedUserDetails?.tenantId || '';
  const vendorId = vendor?.id || '';

  const { data: showcases = [] } = useGetAllVendorShowcases({ tenantId, vendorId });
  const { mutate: upsert } = useUpsertVendorShowcase();
  const { mutate: deleteShowcase } = useDeleteVendorShowcase();

  const [showAddForm, setShowAddForm] = useState(false);
  const [addDraft, setAddDraft] = useState<ProjectDraft>(emptyDraft());

  const [editingId, setEditingId] = useState<string | null>(null);
  const [editDraft, setEditDraft] = useState<ProjectDraft>(emptyDraft());

  const handleAdd = () => {
    if (!addDraft.projectName.trim()) {
      toast.error('Project title is required');
      return;
    }
    upsert(
      {
        vendorId,
        tenantId,
        file: '',
        payload: {
          projectName: addDraft.projectName,
          clientName: addDraft.clientName,
          duration: addDraft.duration,
          description: addDraft.description,
          serviceIds: [],
          resultsAndOutcomes: '',
        },
      },
      {
        onSuccess: () => {
          toast.success('Project added');
          setAddDraft(emptyDraft());
          setShowAddForm(false);
        },
        onError: () => toast.error('Failed to add project'),
      }
    );
  };

  const handleEdit = (showcase: VendorShowcase) => {
    setEditingId(showcase.id);
    setEditDraft({
      projectName: showcase.projectName,
      clientName: showcase.clientName,
      duration: showcase.duration,
      description: showcase.description,
    });
    setShowAddForm(false);
  };

  const handleSaveEdit = () => {
    if (!editDraft.projectName.trim()) {
      toast.error('Project title is required');
      return;
    }
    upsert(
      {
        id: editingId!,
        vendorId,
        tenantId,
        file: '',
        payload: {
          projectName: editDraft.projectName,
          clientName: editDraft.clientName,
          duration: editDraft.duration,
          description: editDraft.description,
          serviceIds: [],
          resultsAndOutcomes: '',
        },
      },
      {
        onSuccess: () => {
          toast.success('Project updated');
          setEditingId(null);
          setEditDraft(emptyDraft());
        },
        onError: () => toast.error('Failed to update project'),
      }
    );
  };

  const handleDelete = (id: string) => {
    deleteShowcase(
      { id, tenantId, vendorId },
      {
        onSuccess: () => toast.success('Project removed'),
        onError: () => toast.error('Failed to remove project'),
      }
    );
  };

  return (
    <div className="space-y-3">
      <div className="flex items-center justify-between">
        <Label className="text-sm font-medium">Featured Projects</Label>
        <Button
          type="button"
          variant="outline"
          size="sm"
          onClick={() => {
            setShowAddForm(true);
            setEditingId(null);
          }}
        >
          <Plus className="mr-1 h-4 w-4" />
          Add Project
        </Button>
      </div>

      {showAddForm && (
        <ProjectForm
          draft={addDraft}
          onChange={setAddDraft}
          onCancel={() => {
            setShowAddForm(false);
            setAddDraft(emptyDraft());
          }}
          onSubmit={handleAdd}
          submitLabel="Add"
        />
      )}

      <div className="space-y-3">
        {showcases.map((showcase) =>
          editingId === showcase.id ? (
            <div
              key={showcase.id}
              className="border-primary/40 bg-secondary/50 animate-in slide-in-from-top-2 space-y-3 rounded-lg border p-4 duration-200"
            >
              <p className="text-primary text-xs font-semibold tracking-wide uppercase">
                Editing project
              </p>
              <ProjectForm
                draft={editDraft}
                onChange={setEditDraft}
                onCancel={() => {
                  setEditingId(null);
                  setEditDraft(emptyDraft());
                }}
                onSubmit={handleSaveEdit}
                submitLabel="Save"
                submitIcon={<Check className="mr-1 h-4 w-4" />}
              />
            </div>
          ) : (
            <div
              key={showcase.id}
              className="border-border bg-card hover:bg-secondary/40 group rounded-lg border p-4 transition-colors"
            >
              <div className="flex items-start justify-between gap-3">
                <div className="min-w-0 flex-1">
                  <div className="mb-1 flex flex-wrap items-center gap-2">
                    <p className="text-foreground text-sm font-semibold">{showcase.projectName}</p>
                    {showcase.duration && (
                      <Badge variant="secondary" className="text-xs">
                        {showcase.duration}
                      </Badge>
                    )}
                  </div>
                  {showcase.clientName && (
                    <p className="text-muted-foreground mb-1 text-xs">
                      Client: <span className="text-foreground">{showcase.clientName}</span>
                    </p>
                  )}
                  {showcase.description && (
                    <p className="text-muted-foreground text-xs">{showcase.description}</p>
                  )}
                </div>
                <div className="flex shrink-0 items-center gap-1">
                  <button
                    type="button"
                    onClick={() => handleEdit(showcase)}
                    className="text-muted-foreground hover:text-primary hover:bg-primary/10 rounded p-1.5 opacity-0 transition-colors group-hover:opacity-100"
                    title="Edit project"
                  >
                    <Pencil className="h-3.5 w-3.5" />
                  </button>
                  <button
                    type="button"
                    onClick={() => handleDelete(showcase.id)}
                    className="text-muted-foreground hover:text-destructive hover:bg-destructive/10 rounded p-1.5 opacity-0 transition-colors group-hover:opacity-100"
                    title="Remove project"
                  >
                    <Trash2 className="h-3.5 w-3.5" />
                  </button>
                </div>
              </div>
            </div>
          )
        )}

        {showcases.length === 0 && !showAddForm && (
          <div className="border-border rounded-lg border-2 border-dashed py-10 text-center">
            <p className="text-muted-foreground text-sm">No projects added yet.</p>
          </div>
        )}
      </div>
    </div>
  );
};

type ProjectFormProps = {
  draft: ProjectDraft;
  onChange: (d: ProjectDraft) => void;
  onCancel: () => void;
  onSubmit: () => void;
  submitLabel: string;
  submitIcon?: React.ReactNode;
};

const ProjectForm = ({
  draft,
  onChange,
  onCancel,
  onSubmit,
  submitLabel,
  submitIcon,
}: ProjectFormProps) => (
  <div className="bg-secondary/50 border-border animate-in slide-in-from-top-2 space-y-3 rounded-lg border p-4 duration-200">
    <div className="grid grid-cols-1 gap-3 md:grid-cols-2">
      <div className="space-y-1.5">
        <Label className="text-xs">
          Project Title <span className="text-destructive">*</span>
        </Label>
        <Input
          value={draft.projectName}
          onChange={(e) => onChange({ ...draft, projectName: e.target.value })}
          placeholder="e.g. Cloud Migration Project"
        />
      </div>
      <div className="space-y-1.5">
        <Label className="text-xs">Client Name</Label>
        <Input
          value={draft.clientName}
          onChange={(e) => onChange({ ...draft, clientName: e.target.value })}
          placeholder="Acme Corp"
        />
      </div>
      <div className="space-y-1.5 md:col-span-2">
        <Label className="text-xs">Year Completed</Label>
        <Input
          value={draft.duration}
          onChange={(e) => onChange({ ...draft, duration: e.target.value })}
          placeholder="2024"
          className="md:w-1/2"
        />
      </div>
    </div>
    <div className="space-y-1.5">
      <Label className="text-xs">Description</Label>
      <Textarea
        value={draft.description}
        onChange={(e) => onChange({ ...draft, description: e.target.value })}
        placeholder="What did you build or achieve?"
        rows={3}
        className="resize-none"
      />
    </div>
    <div className="flex justify-end gap-2">
      <Button type="button" variant="ghost" size="sm" onClick={onCancel}>
        Cancel
      </Button>
      <Button
        type="button"
        size="sm"
        className="bg-primary hover:bg-primary/90 text-primary-foreground"
        onClick={onSubmit}
      >
        {submitIcon}
        {submitLabel}
      </Button>
    </div>
  </div>
);
