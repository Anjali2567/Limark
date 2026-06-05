import { Button } from '@/components/ui/button';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import { Sheet, SheetContent, SheetHeader } from '@/components/ui/sheet';
import useToggle from '@/hooks/useToggle';
import { formatEmployeeRange, formatName, formatString } from '@/lib/utils/formatter';
import { cn } from '@/lib/utils/helpers';
import { LeadContactData } from '@/types/leadSearch.types';

import { ChevronsRight, EllipsisVertical, Globe, Linkedin, Twitter } from 'lucide-react';

type SearchResultActionProps = {
  data: LeadContactData;
};

const SearchResultAction = ({ data }: SearchResultActionProps) => {
  const { value: isDrawerOpen, toggle: toggleDrawer, setFalse: closeDrawer } = useToggle();
  const { value: isCompanyOpen, toggle: toggleCompany, setFalse: closeCompany } = useToggle();

  const closeDrawerAndCompany = () => {
    closeDrawer();
    closeCompany();
  };

  return (
    <>
      <DropdownMenu>
        <DropdownMenuTrigger asChild>
          <Button variant="none" className="h-fit">
            <EllipsisVertical />
          </Button>
        </DropdownMenuTrigger>

        <DropdownMenuContent align="end">
          <DropdownMenuItem onClick={toggleDrawer}>View Details</DropdownMenuItem>
        </DropdownMenuContent>
      </DropdownMenu>

      <Sheet open={isDrawerOpen} onOpenChange={closeDrawerAndCompany}>
        <SheetContent
          side="right"
          className={cn(
            'overflow-hidden transition-all duration-300',
            isCompanyOpen ? 'sm:max-w-200' : 'sm:max-w-md'
          )}
        >
          <div
            className={cn(
              'grid h-full transition-all',
              isCompanyOpen ? 'grid-cols-[1fr_1fr]' : 'grid-cols-1'
            )}
          >
            <div>
              <SheetHeader className="p-0!">
                <div className="border-border border-b p-6">
                  <h2 className="text-xl font-semibold">
                    {formatName({
                      firstName: data.firstName,
                      lastName: data.lastName,
                    })}
                  </h2>
                  {data.linkedinUrl && (
                    <a
                      href={data.linkedinUrl}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="text-primary flex items-center gap-1 text-sm hover:underline"
                    >
                      <Linkedin className="h-3.5 w-3.5" />
                      <span>LinkedIn</span>
                    </a>
                  )}
                </div>
              </SheetHeader>

              <div className="space-y-6 p-6 text-sm">
                <div className="grid grid-cols-2 gap-4">
                  <Detail label="Company" value={data.companyName} />
                  <Detail label="Department" value={formatString(data.department)} />
                  <Detail label="Seniority" value={formatString(data.seniority)} />
                </div>

                <div className="grid grid-cols-1 gap-4">
                  <a href={`mailto:${data.email}`} className="text-primary hover:underline">
                    <Detail label="Email" value={data.email} />
                  </a>
                  <Detail label="Phone" value={data.phoneE164} />
                  <Detail
                    label="Location"
                    value={[data.locationCity, data.locationState, data.locationCountry]
                      .filter(Boolean)
                      .join(', ')}
                  />
                </div>
                <div>
                  <h3 className="mb-3 text-sm font-semibold">Social Media</h3>
                  <div className="flex flex-col gap-2">
                    {data.linkedinUrl && (
                      <SocialLink
                        href={data.linkedinUrl}
                        icon={<Linkedin className="h-5 w-5" />}
                        label="LinkedIn"
                      />
                    )}
                  </div>
                </div>
                {isCompanyOpen ? (
                  <div className="absolute bottom-5 flex items-center justify-center">
                    <Button variant="outline" size="sm" className="w-full" onClick={toggleCompany}>
                      <ChevronsRight />
                    </Button>
                  </div>
                ) : (
                  <div>
                    <Button variant="outline" size="sm" className="w-full" onClick={toggleCompany}>
                      View Company Details
                    </Button>
                  </div>
                )}
              </div>
            </div>

            {isCompanyOpen && (
              <div className="animate-in slide-in-from-right overflow-y-auto border-l duration-300">
                <div className="border-border border-b p-6">
                  <h2 className="text-xl font-semibold">{data.companyName}</h2>
                  {data.companyDomain && (
                    <a
                      href={`https://${data.companyDomain}`}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="text-primary flex items-center gap-1 text-sm hover:underline"
                    >
                      <Globe className="h-4 w-4" />
                      {data.companyDomain}
                    </a>
                  )}
                </div>

                <div className="flex-1 space-y-6 overflow-y-auto p-6">
                  {data.companyDescription && (
                    <div className="mb-6">
                      <h3 className="mb-2 text-sm font-semibold">About</h3>
                      <p className="text-muted-foreground text-sm leading-relaxed">
                        {data.companyDescription}
                      </p>
                    </div>
                  )}
                  <div className="grid grid-cols-2 gap-4">
                    <Detail
                      label="Location"
                      value={[data.companyHqCity, data.companyHqState, data.companyHqCountry]
                        .filter(Boolean)
                        .join(', ')}
                    />
                    <Detail label="Industry" value={data.companySegment} />
                    <Detail
                      label="Employee Range"
                      value={formatEmployeeRange(data.companyEmployeeCount)}
                    />
                    <Detail label="Revenue" value={data.companyRevenueUsd} />
                  </div>
                  <div>
                    <h3 className="mb-3 text-sm font-semibold">Social Media</h3>
                    <div className="flex flex-col gap-2">
                      {data.companyLinkedinUrl && (
                        <SocialLink
                          href={data.companyLinkedinUrl}
                          icon={<Linkedin className="h-5 w-5" />}
                          label="LinkedIn"
                        />
                      )}
                      {data.companyTwitterUrl && (
                        <SocialLink
                          href={data.companyTwitterUrl}
                          icon={<Twitter className="h-5 w-5" />}
                          label="Twitter"
                        />
                      )}
                    </div>
                  </div>
                </div>
              </div>
            )}
          </div>
        </SheetContent>
      </Sheet>
    </>
  );
};

const Detail = ({ label, value }: { label: string; value?: string }) => {
  return (
    <div>
      <p className="text-muted-foreground mb-1 text-xs">{label}</p>
      {value ? <p className="text-sm">{value}</p> : <p className="text-xs text-slate-400">N/A</p>}
    </div>
  );
};

const SocialLink = ({
  href,
  icon,
  label,
}: {
  href: string;
  icon: React.ReactNode;
  label: string;
}) => (
  <a
    href={href}
    target="_blank"
    rel="noopener noreferrer"
    className="flex items-center gap-2 rounded-md transition-colors hover:translate-x-2 hover:scale-105"
  >
    <span className="text-primary">{icon}</span>
    <span className="text-sm">{label}</span>
  </a>
);

export { SearchResultAction };
