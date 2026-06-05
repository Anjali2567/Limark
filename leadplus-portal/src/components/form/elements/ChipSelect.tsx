import { getTechnologyIcon } from '@/lib/utils/icon';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

type ChipOption = {
  label: string;
  value: string | number;
  icon?: string;
};

type ChipSelectProps = {
  value: ChipOption[];
  onChange: (values: ChipOption[]) => void;
  options: ChipOption[];
  disabled?: boolean;
};

const ChipSelect = ({ value = [], onChange, options, disabled }: ChipSelectProps) => {
  const handleToggle = (option: ChipOption) => {
    const isSelected = value.some((v) => v.value === option.value);
    if (isSelected) {
      onChange(value.filter((v) => v.value !== option.value));
    } else {
      onChange([...value, option]);
    }
  };

  return (
    <div className="flex flex-wrap gap-2">
      {options.map((option) => {
        const isSelected = value.some((v) => v.value === option.value);
        return (
          <button
            key={option.value}
            type="button"
            onClick={() => handleToggle(option)}
            disabled={disabled}
            className={`rounded-full px-4 py-2 text-sm font-medium transition-colors ${
              isSelected
                ? 'bg-primary text-primary-foreground'
                : 'bg-background text-foreground border-border hover:bg-accent border'
            }`}
          >
            {option.icon && (
              <FontAwesomeIcon icon={getTechnologyIcon(option.icon)} className="mr-2 h-4 w-4" />
            )}
            {option.label}
          </button>
        );
      })}
    </div>
  );
};

export { ChipSelect, type ChipOption };
