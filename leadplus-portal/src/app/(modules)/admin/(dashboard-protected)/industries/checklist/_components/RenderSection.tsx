import { DragHandleProps } from '@/components/draggable/NestedDraggableList';
import { CheckListSection, GroupedSection } from '@/types/question.types';
import { Button } from '@/components/ui/button';

import { ChevronDownIcon, ChevronRightIcon, GripVertical, PencilIcon } from 'lucide-react';

type RenderSectionProps = {
  groupedSection: GroupedSection;
  dragHandleProps: DragHandleProps;
  isExpanded: boolean;
  onToggle: () => void;
  onEditSection: (section: CheckListSection) => void;
};

const RenderSection = ({
  groupedSection,
  dragHandleProps,
  isExpanded,
  onToggle,
  onEditSection,
}: RenderSectionProps) => {
  return (
    <div className="flex items-center justify-between gap-2">
      <div className="flex items-center gap-2">
        <GripVertical {...dragHandleProps} className="h-6 w-6 cursor-grab" />
        <p className="text-sm font-medium">{groupedSection.section.name}</p>
      </div>

      <div className="flex items-center gap-4">
        {!!groupedSection.section.id && (
          <Button
            variant="none"
            className="h-fit! p-0!"
            onClick={() => onEditSection(groupedSection.section)}
          >
            <PencilIcon className="h-4 w-4" />
          </Button>
        )}

        <Button variant="none" className="h-fit! p-0!" onClick={onToggle}>
          {isExpanded ? (
            <ChevronDownIcon className="h-4 w-4" />
          ) : (
            <ChevronRightIcon className="h-4 w-4" />
          )}
        </Button>
      </div>
    </div>
  );
};

export { RenderSection };
