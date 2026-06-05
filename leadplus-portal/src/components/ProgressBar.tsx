import {
  Tooltip,
  TooltipContent,
  TooltipTrigger,
} from "@/components/ui/tooltip";

type ProgressBarProps = {
  progress: number;
  label?: string;
};

const ProgressBar = ({ progress, label }: ProgressBarProps) => {
  const clampedProgress = parseFloat(
    Math.min(Math.max(progress, 0), 100).toFixed(1)
  );
  return (
    <Tooltip>
      <TooltipTrigger asChild>
        <div
          className="flex-1 h-2 bg-secondary rounded-full overflow-hidden cursor-pointer"
          role="progressbar"
          aria-valuenow={clampedProgress}
          aria-valuemin={0}
          aria-valuemax={100}
          aria-label={label || "Progress"}
        >
          <div
            className="h-full bg-gray-400 rounded-full transition-all duration-300"
            style={{ width: `${clampedProgress}%` }}
          />
        </div>
      </TooltipTrigger>
      <TooltipContent>
        <p>{clampedProgress}%</p>
      </TooltipContent>
    </Tooltip>
  );
};

export { ProgressBar };
