'use client';

import Image from 'next/image';
import { useRouter, useSearchParams } from 'next/navigation';
import { useCallback, useEffect, useMemo, useState } from 'react';

import { appRoutes } from '@/config/routes';
import { VendorVerificationStatus } from '@/constants/vendor.constant';
import { useAuth } from '@/context/AuthContext';
import { useGetAuthenticatedVendor } from '@/hooks/useAuthentication';
import { useSubmitVendor, useUpdateVendorDetails } from '@/hooks/useVendor';
import { cn } from '@/lib/utils/helpers';
import { VendorFormValues } from '@/types/vendor.types';
import { toast } from 'sonner';
import { ChecklistTab } from './_components/ChecklistTab';
import { LocationsAndProjectsTab } from './_components/LocationsAndProjectsTab';
import { ServicesTab } from './_components/ServicesTab';
import { VendorInformationTab } from './_components/VendorInformationTab';

import { Check, Loader2 } from 'lucide-react';

const STEPS = [
  {
    id: 1,
    label: 'Vendor Information',
  },
  {
    id: 2,
    label: 'Services',
  },
  {
    id: 3,
    label: 'Locations & Projects',
  },
  {
    id: 4,
    label: 'Checklist',
  },
];

const mandatoryFields = [
  'companyName',
  'description',
  'companySize',
  'phoneNumber',
  'address.street',
  'address.city',
  'address.state',
  'address.postalCode',
  'address.country',
  'serviceIds',
  'specificationIds',
  'regionsCovered',
  'certifications',
  'questionnaire',
];

function getNestedValue<T>(obj: T, path: string): unknown {
  return path
    .split('.')
    .reduce((acc: unknown, key) => (acc as Record<string, unknown>)?.[key], obj);
}

const OnboardingPage = () => {
  const router = useRouter();
  const searchParams = useSearchParams();

  const [currentStep, setCurrentStep] = useState<number>(() => {
    const stepFromUrl = searchParams.get('step');
    const step = stepFromUrl ? parseInt(stepFromUrl, 10) : 1;
    return step >= 1 && step <= STEPS.length ? step : 1;
  });

  const { authenticatedUser } = useAuth();
  const { data: vendor, isLoading } = useGetAuthenticatedVendor();
  const { mutate, isPending } = useUpdateVendorDetails();
  const { mutate: submitVendor, isPending: isSubmitting } = useSubmitVendor();

  const progressPercentage = useMemo(() => {
    if (!vendor) return 0;
    const filledFields = mandatoryFields.filter((field) => {
      const value = getNestedValue(vendor, field);
      if (Array.isArray(value)) return value.length > 0;
      if (typeof value === 'string') return value.trim().length > 0;
      if (typeof value === 'object' && value !== null) {
        return Object.keys(value).length > 0;
      }
      return value !== undefined && value !== null;
    });
    return Math.round((filledFields.length / mandatoryFields.length) * 100);
  }, [vendor]);

  const onSubmit = useCallback(
    (payload: VendorFormValues) => {
      mutate(
        {
          ...vendor,
          ...payload,
          faxNumber: '',
          companyName: payload.companyName || vendor?.companyName || '',
          address: {
            street: payload.street || vendor?.address.street || '',
            city: payload.city || vendor?.address.city || '',
            state: payload.state || vendor?.address.state || '',
            postalCode: payload.postalCode || vendor?.address.postalCode || '',
            country: payload.country || vendor?.address.country || '',
          },
        },
        {
          onSuccess: () => {
            if (currentStep === STEPS.length) {
              submitVendor(undefined, {
                onSuccess: () => {
                  toast.success('Vendor profile submitted successfully!');
                  router.push(appRoutes.vendor.profileOverview.root);
                },
                onError: () => {
                  toast.error('Failed to submit vendor profile. Please try again.');
                },
              });
              return;
            }
            toast.success('Vendor profile updated successfully!');
            const nextStep = currentStep + 1;
            setCurrentStep(nextStep);
            router.push(`?step=${nextStep}`, { scroll: false });
          },
          onError: () => {
            toast.error('Failed to update vendor profile. Please try again.');
          },
        }
      );
    },
    [mutate, vendor, currentStep, router, submitVendor]
  );

  const handleBack = useCallback(() => {
    const prevStep = Math.max(currentStep - 1, 1);
    setCurrentStep(prevStep);
    router.push(`?step=${prevStep}`, { scroll: false });
  }, [currentStep, router]);

  useEffect(() => {
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }, [currentStep]);

  useEffect(() => {
    if (vendor) {
      if (vendor.vendorVerificationStatus === VendorVerificationStatus.REJECTED) {
        router.push(appRoutes.vendor.profileOverview.root);
      } else if (vendor.vendorVerificationStatus === VendorVerificationStatus.APPROVED) {
        router.push(appRoutes.leadgen.search.companySearch);
      }
    }
  }, [vendor, router]);

  if (isLoading) {
    return (
      <div className="flex h-full items-center justify-center">
        <Loader2 className="h-10 w-10 animate-spin text-sky-500" />
      </div>
    );
  }

  return (
    <div className="bg-linear-to-br from-sky-50 via-white to-blue-50">
      <div className="space-y-3 py-5 text-center">
        <Image
          src="/leadplus-logo.png"
          alt="LeadPlus Logo"
          width={200}
          height={40}
          className="mx-auto object-contain"
        />
        <h1 className="text-3xl font-bold">Welcome to LeadPlus</h1>
        <p className="text-muted-foreground">Let&apos;s set up your vendor profile</p>
      </div>
      <div className="sticky top-5 z-40 mx-auto w-full max-w-5xl space-y-6 px-4 pb-4">
        <div className="mb-6 space-y-2">
          <div className="text-muted-foreground flex justify-between text-sm">
            <span>
              Step {currentStep} of {STEPS.length}
            </span>
            <span>{progressPercentage}% Complete</span>
          </div>

          <div className="bg-muted h-2 w-full overflow-hidden rounded-full">
            <div
              className="bg-primary h-full transition-all duration-500"
              style={{ width: `${progressPercentage}%` }}
            />
          </div>
        </div>

        <div className="flex items-center justify-center">
          {STEPS.map((step, index) => {
            const isActive = currentStep >= step.id;
            const isCompleted = currentStep > step.id;

            return (
              <div key={step.id} className="flex items-center">
                <div className="flex items-center gap-2">
                  <div
                    className={cn(
                      'flex h-9 w-9 items-center justify-center rounded-full text-sm font-semibold',
                      isActive
                        ? 'bg-primary text-primary-foreground'
                        : 'bg-muted text-muted-foreground'
                    )}
                  >
                    {isCompleted ? <Check className="h-4 w-4" /> : step.id}
                  </div>
                  <span
                    className={cn(
                      'hidden font-medium sm:inline',
                      isActive ? 'text-foreground' : 'text-muted-foreground'
                    )}
                  >
                    {step.label}
                  </span>
                </div>
                {index !== STEPS.length - 1 && <div className="bg-border mx-6 h-0.5 w-16" />}
              </div>
            );
          })}
        </div>
        <div className={cn(currentStep === 1 ? 'block' : 'hidden')}>
          <VendorInformationTab
            onSubmit={onSubmit}
            vendor={vendor}
            user={authenticatedUser}
            isSubmitting={isPending || isSubmitting}
          />
        </div>
        <div className={cn(currentStep === 2 ? 'block' : 'hidden')}>
          <ServicesTab
            onSubmit={onSubmit}
            handleBack={handleBack}
            vendor={vendor}
            isSubmitting={isPending || isSubmitting}
          />
        </div>
        <div className={cn(currentStep === 3 ? 'block' : 'hidden')}>
          <LocationsAndProjectsTab
            onSubmit={onSubmit}
            handleBack={handleBack}
            vendor={vendor}
            isSubmitting={isPending || isSubmitting}
          />
        </div>
        <div className={cn(currentStep === 4 ? 'block' : 'hidden')}>
          <ChecklistTab
            onSubmit={onSubmit}
            handleBack={handleBack}
            vendor={vendor}
            isSubmitting={isPending || isSubmitting}
          />
        </div>
      </div>
    </div>
  );
};

export default OnboardingPage;
