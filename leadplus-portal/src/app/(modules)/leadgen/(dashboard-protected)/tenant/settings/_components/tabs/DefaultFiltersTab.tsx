'use client';

import { useState } from 'react';

import { Building2, Filter, Save, Users } from 'lucide-react';

import { FilterOption } from '@/app/(modules)/leadgen/(dashboard-protected)/search/_components/FilterOption';
import { useTenantLeadFilterForm } from '@/app/(modules)/leadgen/(dashboard-protected)/tenant/settings/_hooks/useTenantLeadFilterForm';
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from '@/components/ui/alert-dialog';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Tabs, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { LeadType } from '@/constants/leadSearch.constants';
import { useAuth } from '@/context/AuthContext';

export const DefaultFiltersTab = () => {
  const { authenticatedUserDetails } = useAuth();
  const tenantId = authenticatedUserDetails?.tenantId ?? '';

  const [activeTab, setActiveTab] = useState<LeadType>(LeadType.LEAD_CONTACT);
  const [pendingTab, setPendingTab] = useState<LeadType | null>(null);

  const { activeSections, control, handleSubmit, handleSearchChange, onSubmit, isSaving, isDirty } =
    useTenantLeadFilterForm(activeTab);

  const handleTabChange = (value: string) => {
    const next = value as LeadType;
    if (isDirty) {
      setPendingTab(next);
    } else {
      setActiveTab(next);
    }
  };

  const confirmTabChange = () => {
    if (pendingTab) {
      setActiveTab(pendingTab);
      setPendingTab(null);
    }
  };

  if (!tenantId) return null;

  return (
    <>
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Filter className="h-5 w-5 text-sky-500" />
            Default Search Filters
          </CardTitle>
          <CardDescription>
            Configure default filters applied automatically when users perform people or company
            searches across all workspaces.
          </CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit(onSubmit)}>
            <Tabs value={activeTab} onValueChange={handleTabChange}>
              <TabsList className="grid w-full grid-cols-2">
                <TabsTrigger value={LeadType.LEAD_CONTACT} className="flex items-center gap-2">
                  <Users className="h-4 w-4" />
                  People Search
                </TabsTrigger>
                <TabsTrigger value={LeadType.LEAD_COMPANY} className="flex items-center gap-2">
                  <Building2 className="h-4 w-4" />
                  Company Search
                </TabsTrigger>
              </TabsList>
            </Tabs>
            <div className="mt-4 grid grid-cols-1 gap-4">
              {activeSections.map((section) => (
                <FilterOption
                  key={`${activeTab}-${section.id}`}
                  section={section}
                  control={control}
                  onSearchChange={(value) => handleSearchChange(section.id, value)}
                  flat
                  showAddButton
                />
              ))}
            </div>
            <div className="border-border mt-6 flex justify-end border-t pt-4">
              <Button type="submit" disabled={isSaving} className="px-8">
                <Save className="mr-2 h-4 w-4" />
                {isSaving ? 'Saving...' : 'Save Filters'}
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>

      <AlertDialog open={pendingTab !== null} onOpenChange={(open) => !open && setPendingTab(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Unsaved Changes</AlertDialogTitle>
            <AlertDialogDescription>
              You have unsaved changes. If you switch tabs now, your changes will be lost.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Stay</AlertDialogCancel>
            <AlertDialogAction onClick={confirmTabChange}>Discard &amp; Switch</AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </>
  );
};
