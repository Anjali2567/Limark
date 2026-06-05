export enum FeedbackType {
  FEATURE_REQUEST = 'FEATURE_REQUEST',
  BUG_REPORT = 'BUG_REPORT',
  IMPROVEMENT = 'IMPROVEMENT',
  GENERAL = 'GENERAL',
}

export enum FeedbackStatus {
  NEW = 'NEW',
  REVIEWED = 'REVIEWED',
  RESPONDED = 'RESPONDED',
}

export const FEEDBACK_CATEGORY_CONFIG: Record<
  FeedbackType,
  { label: string; emoji: string; className: string }
> = {
  [FeedbackType.BUG_REPORT]: {
    label: 'Bug Report',
    emoji: '',
    className: 'bg-destructive/10 text-destructive border-destructive/20',
  },
  [FeedbackType.FEATURE_REQUEST]: {
    label: 'Feature Request',
    emoji: '',
    className: 'bg-primary/10 text-primary border-primary/20',
  },
  [FeedbackType.IMPROVEMENT]: {
    label: 'Improvement',
    emoji: '',
    className: 'bg-warning/10 text-warning border-warning/20',
  },
  [FeedbackType.GENERAL]: {
    label: 'General',
    emoji: '',
    className: 'bg-muted text-muted-foreground border-border',
  },
};

export const FEEDBACK_STATUS_CONFIG: Record<FeedbackStatus, { label: string; className: string }> =
  {
    [FeedbackStatus.NEW]: {
      label: 'New',
      className: 'bg-primary/10 text-primary border-primary/20',
    },
    [FeedbackStatus.REVIEWED]: {
      label: 'Reviewed',
      className: 'bg-warning/10 text-warning border-warning/20',
    },
    [FeedbackStatus.RESPONDED]: {
      label: 'Responded',
      className: 'bg-green-500/10 text-green-700 border-green-200',
    },
  };

export const FEEDBACK_RATING_LABELS: Record<number, string> = {
  1: 'Poor',
  2: 'Fair',
  3: 'Good',
  4: 'Great',
  5: 'Excellent',
};

export const FEEDBACK_REPLY_TEMPLATES: {
  label: string;
  getMessage: () => string;
}[] = [
  {
    label: 'Acknowledged',
    getMessage: () =>
      `Thank you for your feedback. We've recorded your input and our team is looking into it. We'll keep you updated on any progress`,
  },
  {
    label: 'Issue Fixed',
    getMessage: () =>
      `Thank you for reporting this issue. We've identified the problem and a fix has been deployed. Please try again and let us know if the issue persists.`,
  },
  {
    label: 'Added to Roadmap',
    getMessage: () =>
      `Thank you for your suggestion! We love this idea and have added it to our roadmap. We'll notify you once it's available.`,
  },
  {
    label: 'Thank You',
    getMessage: () =>
      `Thank you for your kind words! We're glad you're enjoying LeadPlus. Your feedback motivates our team to keep improving.`,
  },
];
