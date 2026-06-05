import { ReactNode } from 'react';
import {
  DndContext,
  closestCenter,
  DragEndEvent,
  useSensor,
  useSensors,
  PointerSensor,
  type DraggableAttributes,
  type DraggableSyntheticListeners,
} from '@dnd-kit/core';
import {
  SortableContext,
  useSortable,
  arrayMove,
  verticalListSortingStrategy,
} from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';

export type DragHandleProps = DraggableAttributes & DraggableSyntheticListeners;

interface DraggableListProps<T, D = DragHandleProps> {
  items: T[];
  onChange: (newItems: T[]) => void;
  getId: (item: T) => string;
  renderItem: (item: T, index: number, dragHandleProps: D) => ReactNode;
}

export function DraggableList<T, D = DragHandleProps>({
  items,
  onChange,
  getId,
  renderItem,
}: DraggableListProps<T, D>) {
  const sensors = useSensors(useSensor(PointerSensor));

  function handleDragEnd(event: DragEndEvent) {
    const { active, over } = event;
    if (over && active.id !== over.id) {
      const oldIndex = items.findIndex((i) => getId(i) === active.id);
      const newIndex = items.findIndex((i) => getId(i) === over.id);
      onChange(arrayMove(items, oldIndex, newIndex));
    }
  }

  return (
    <DndContext sensors={sensors} collisionDetection={closestCenter} onDragEnd={handleDragEnd}>
      <SortableContext items={items.map(getId)} strategy={verticalListSortingStrategy}>
        {items.map((item, index) => (
          <SortableItem<D> key={getId(item)} id={getId(item)}>
            {(dragHandleProps) => renderItem(item, index, dragHandleProps)}
          </SortableItem>
        ))}
      </SortableContext>
    </DndContext>
  );
}

interface SortableItemProps<D> {
  id: string;
  children: (dragHandleProps: D) => ReactNode;
}

function SortableItem<D>({ id, children }: SortableItemProps<D>) {
  const { attributes, listeners, setNodeRef, transform, transition, isDragging } = useSortable({
    id,
  });
  const dragHandleProps = { ...attributes, ...listeners } as D;

  const style: React.CSSProperties = {
    transform: CSS.Transform.toString(transform),
    transition,
    opacity: isDragging ? 0.5 : 1,
  };

  return (
    <div ref={setNodeRef} style={style}>
      {children(dragHandleProps)}
    </div>
  );
}
