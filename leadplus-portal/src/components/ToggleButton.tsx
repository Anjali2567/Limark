import { FC, MouseEvent } from 'react';
import clsx from 'clsx';

interface ToggleButtonProps {
  onToggle?: (newState: boolean) => void;
  initial?: boolean;
  activeColor?: string;
  inactiveColor?: string;
  disabled?: boolean;
  toolTip?: string;
}

const ToggleButton: FC<ToggleButtonProps> = ({
  onToggle,
  initial = false,
  disabled = false,
  activeColor = 'bg-blue-500',
  inactiveColor = 'bg-gray-300',
  toolTip = '',
}) => {
  const handleClick = (e: MouseEvent<HTMLButtonElement>) => {
    e.stopPropagation();
    onToggle?.(!initial);
  };

  return (
    <div className="flex items-center">
      <button
        type="button"
        onClick={handleClick}
        disabled={disabled}
        title={toolTip}
        className={clsx(
          'relative inline-flex h-6 w-11 cursor-pointer items-center rounded-full transition-colors disabled:cursor-not-allowed disabled:opacity-50',
          initial ? activeColor : inactiveColor
        )}
      >
        <span
          className={`inline-block h-4 w-4 transform rounded-full bg-white transition-transform ${
            initial ? 'translate-x-6' : 'translate-x-1'
          }`}
        />
      </button>
    </div>
  );
};

export default ToggleButton;
