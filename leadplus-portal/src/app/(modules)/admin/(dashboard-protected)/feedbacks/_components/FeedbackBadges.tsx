import { Cell } from '@/components/tables/Cell';
import {
  FEEDBACK_CATEGORY_CONFIG,
  FEEDBACK_STATUS_CONFIG,
  FeedbackStatus,
  FeedbackType,
} from '@/constants/user-feedback.constant';

const CategoryBadge = ({ type }: { type: FeedbackType }) => {
  const config = FEEDBACK_CATEGORY_CONFIG[type];
  if (!config) return <Cell value={type} />;
  return (
    <span
      className={`inline-flex items-center gap-1.5 rounded-full border px-2.5 py-1 text-xs font-medium ${config.className}`}
    >
      {config.emoji} {config.label}
    </span>
  );
};

const StatusBadge = ({ status }: { status: FeedbackStatus }) => {
  const config = FEEDBACK_STATUS_CONFIG[status];
  if (!config) return <Cell value={status} />;
  return (
    <span
      className={`inline-flex items-center gap-1.5 rounded-full border px-2.5 py-1 text-xs font-medium ${config.className}`}
    >
      {status === FeedbackStatus.NEW && <span className="bg-primary h-1.5 w-1.5 rounded-full" />}
      {config.label}
    </span>
  );
};

export { CategoryBadge, StatusBadge };
