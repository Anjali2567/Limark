'use client';

import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { LeadCompanyJobs } from '@/types/leads.types';
import { Briefcase, Clock, ExternalLink, Loader2, MapPin } from 'lucide-react';

type Props = {
  jobs: LeadCompanyJobs[];
  isLoading: boolean;
  isFetchingNextPage: boolean;
  hasNextPage: boolean;
  fetchNextPage: () => void;
};

export const CompanyJobPostingsTab = ({
  jobs,
  isLoading,
  isFetchingNextPage,
  hasNextPage,
  fetchNextPage,
}: Props) => {
  if (isLoading) {
    return (
      <div className="flex items-center justify-center py-8">
        <Loader2 className="text-muted-foreground h-5 w-5 animate-spin" />
      </div>
    );
  }

  if (jobs.length === 0) {
    return <p className="text-muted-foreground text-center text-xs">No job postings found.</p>;
  }

  return (
    <div className="space-y-0">
      {jobs.map((job, index) => (
        <div key={index} className="border-border border-b py-3 last:border-0 last:pb-0">
          <div className="flex items-start justify-between gap-4">
            <div className="flex min-w-0 flex-1 items-start gap-3">
              <div className="bg-primary/10 mt-0.5 flex h-8 w-8 shrink-0 items-center justify-center rounded-full">
                <Briefcase className="text-primary h-4 w-4" />
              </div>
              <div className="min-w-0 flex-1">
                {job.jobUrl ? (
                  <a
                    href={job.jobUrl}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="text-foreground hover:text-primary mb-1 block text-sm font-medium transition-colors"
                  >
                    {job.title}
                  </a>
                ) : (
                  <span className="text-foreground mb-1 block text-sm font-medium">
                    {job.title}
                  </span>
                )}
                <div className="flex flex-wrap items-center gap-2">
                  {job.department && (
                    <Badge
                      variant="secondary"
                      className="text-xs"
                      style={{ fontFamily: "'Inter', sans-serif" }}
                    >
                      {job.department}
                    </Badge>
                  )}
                  {job.location && (
                    <span
                      style={{ fontFamily: "'Inter', sans-serif", fontSize: '12px' }}
                      className="text-muted-foreground flex items-center gap-1"
                    >
                      <MapPin className="h-3 w-3" />
                      {job.location}
                    </span>
                  )}
                </div>
                <div className="mt-1.5 flex items-center gap-3">
                  {job.type && (
                    <span
                      style={{ fontFamily: "'Inter', sans-serif", fontSize: '12px' }}
                      className="text-muted-foreground"
                    >
                      {job.type}
                    </span>
                  )}
                  {job.postedDate && (
                    <>
                      <span
                        style={{ fontFamily: "'Inter', sans-serif", fontSize: '12px' }}
                        className="text-muted-foreground"
                      >
                        ·
                      </span>
                      <span
                        style={{ fontFamily: "'Inter', sans-serif", fontSize: '12px' }}
                        className="text-muted-foreground flex items-center gap-1"
                      >
                        <Clock className="h-3 w-3" />
                        Posted {job.postedDate}
                      </span>
                    </>
                  )}
                </div>
              </div>
            </div>
            {(job.jobUrl || job.applyUrl) && (
              <Button
                variant="outline"
                size="sm"
                className="border-border shrink-0 text-xs"
                style={{ fontFamily: "'Inter', sans-serif" }}
                asChild
              >
                <a href={job.jobUrl || job.applyUrl} target="_blank" rel="noopener noreferrer">
                  <ExternalLink className="mr-1 h-3 w-3" />
                  View
                </a>
              </Button>
            )}
          </div>
        </div>
      ))}

      {hasNextPage && (
        <div className="mt-2 flex justify-center">
          <Button
            variant="ghost"
            size="sm"
            className="text-xs"
            disabled={isFetchingNextPage}
            onClick={() => fetchNextPage()}
          >
            {isFetchingNextPage && <Loader2 className="mr-1.5 h-3 w-3 animate-spin" />}
            {isFetchingNextPage ? 'Loading...' : 'Load more'}
          </Button>
        </div>
      )}
    </div>
  );
};
