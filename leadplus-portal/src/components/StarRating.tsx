import { useState } from 'react';
import { Star } from 'lucide-react';

import { FEEDBACK_RATING_LABELS } from '@/constants/user-feedback.constant';
import { cn } from '@/lib/utils/helpers';

type StarRatingProps = {
  rating: number;
  size?: 'sm' | 'md';
  showLabel?: boolean;
  onRate?: (rating: number) => void;
};

const StarRating = ({ rating, size = 'sm', showLabel = false, onRate }: StarRatingProps) => {
  const [hovered, setHovered] = useState(0);
  const interactive = !!onRate;

  const starClass = cn('transition-colors', size === 'sm' ? 'h-3.5 w-3.5' : 'h-6 w-6');

  const stars = [1, 2, 3, 4, 5].map((star) => {
    const filled = star <= (hovered || rating);
    const colorClass = filled
      ? 'fill-yellow-400 text-yellow-400'
      : 'stroke-muted-foreground/30 text-muted';

    return interactive ? (
      <button
        key={star}
        type="button"
        onMouseEnter={() => setHovered(star)}
        onMouseLeave={() => setHovered(0)}
        onClick={() => onRate(star)}
        className="p-0.5 transition-transform hover:scale-110"
        aria-label={`Rate ${star} star${star !== 1 ? 's' : ''}`}
      >
        <Star className={cn(starClass, colorClass)} />
      </button>
    ) : (
      <Star key={star} className={cn(starClass, colorClass)} />
    );
  });

  return (
    <div className={cn('flex items-center', interactive ? 'gap-1' : 'gap-0.5')}>
      {stars}
      {showLabel && (hovered || rating) > 0 && (
        <span className="text-muted-foreground ml-2 text-sm">
          {FEEDBACK_RATING_LABELS[hovered || rating]}
        </span>
      )}
    </div>
  );
};

export { StarRating };
