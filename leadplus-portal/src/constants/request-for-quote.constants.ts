export const BUDGETS = [
  { label: 'Under $1,000', value: 'BELOW_1K' },
  { label: '$1,000 - $5,000', value: 'BETWEEN_1K_AND_5K' },
  { label: '$5,000 - $10,000', value: 'BETWEEN_5K_AND_10K' },
  { label: '$10,000 - $25,000', value: 'BETWEEN_10K_AND_25K' },
  { label: '$25,000 - $50,000', value: 'BETWEEN_25K_AND_50K' },
  { label: '$50,000+', value: 'ABOVE_50K' },
] as const;
