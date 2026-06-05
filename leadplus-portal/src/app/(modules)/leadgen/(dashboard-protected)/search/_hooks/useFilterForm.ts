import { z } from 'zod';

export const useFilterForm = () => {
  const schema = z.object({
    contactNames: z.array(z.string()).optional(),
    companyNames: z.array(z.string()).optional(),
    locations: z
      .array(
        z.object({
          value: z.string(),
          identifier: z.string().optional(),
        })
      )
      .optional(),
    companyLocations: z
      .array(
        z.object({
          value: z.string(),
          identifier: z.string().optional(),
        })
      )
      .optional(),
    keywords: z.array(z.string()).optional(),
    regions: z.array(z.string()).optional(),
    industries: z.array(z.string()).optional(),
    employeeRanges: z.array(z.string()).optional(),
    revenueRanges: z.array(z.string()).optional(),
    technologies: z.array(z.string()).optional(),
    toolsServices: z.array(z.string()).optional(),
    titles: z.array(z.string()).optional(),
    seniority: z.array(z.string()).optional(),
    departments: z.array(z.string()).optional(),
    postalCodes: z.array(z.string()).optional(),
    sicCodes: z.array(z.string()).optional(),
    naicsCodes: z.array(z.string()).optional(),
    bdNames: z.array(z.string()).optional(),
    isrNames: z.array(z.string()).optional(),
    priorities: z.array(z.string()).optional(),
    titleCategories: z.array(z.string()).optional(),
  });

  return { schema };
};
