'use client';

import React, { useState, useCallback } from 'react';
import Image from 'next/image';
import { useRouter } from 'next/navigation';

import { useGetAllIndustries } from '@/hooks/useIndustry';
import { bulkItemPaginationParams } from '@/types/paginated-params';
import { appRoutes } from '@/config/routes';
import { getCachedImageUrl } from '@/lib/utils/helpers';

const IndustryIcons = () => {
  const [hoveredHeroIcon, setHoveredHeroIcon] = useState<string | null>(null);
  const router = useRouter();
  const { data: industries } = useGetAllIndustries(bulkItemPaginationParams);

  const onClick = useCallback(
    (industryId: string) => {
      router.push(appRoutes.customer.search.withIndustry(industryId));
    },
    [router]
  );

  return (
    <div className="animate-fade-in-up mt-12" style={{ animationDelay: '0.4s' }}>
      <div className="mb-4 text-center">
        <p className="text-muted-foreground text-sm font-medium">Explore by Industry</p>
      </div>
      <div className="relative px-3 pt-[5px]">
        <div className="scrollbar-hide overflow-x-auto">
          <div className="flex min-w-max justify-center gap-4 pb-4">
            {industries?.content?.map((industry) => {
              const isHovered = hoveredHeroIcon === industry.id;

              return (
                <button
                  key={industry.id}
                  onClick={() => onClick(industry.id)}
                  onMouseEnter={() => setHoveredHeroIcon(industry.id)}
                  onMouseLeave={() => setHoveredHeroIcon(null)}
                  className="bg-card border-border hover:border-primary group relative z-10 flex w-[120px] flex-col items-center gap-3 rounded-2xl border p-4 transition-all duration-300 hover:-translate-y-1 hover:shadow-lg"
                >
                  <div
                    className={`bg-secondary/10 relative flex h-14 w-14 items-center justify-center overflow-hidden rounded-full transition-all duration-300 ${
                      isHovered ? 'scale-110 rotate-6 shadow-md' : ''
                    }`}
                  >
                    {industry.image ? (
                      <Image
                        src={getCachedImageUrl(industry.image, industry.updatedAt) || ''}
                        alt={industry.name}
                        className="h-full w-full object-cover"
                        fill
                        sizes="56px"
                        priority
                      />
                    ) : (
                      <div className="bg-muted flex h-full w-full items-center justify-center">
                        <span className="text-muted-foreground text-xs">
                          {industry.name.charAt(0)}
                        </span>
                      </div>
                    )}
                  </div>
                  <div className="text-foreground group-hover:text-primary text-center text-xs font-medium transition-colors">
                    {industry.name}
                  </div>
                </button>
              );
            })}
          </div>
        </div>
      </div>
    </div>
  );
};

export { IndustryIcons };
