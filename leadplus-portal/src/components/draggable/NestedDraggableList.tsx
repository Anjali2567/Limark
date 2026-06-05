import { CSSProperties, ReactNode, useEffect, useRef, useState } from 'react';
import {
  DndContext,
  closestCenter,
  DragEndEvent,
  useSensor,
  useSensors,
  PointerSensor,
} from '@dnd-kit/core';
import {
  SortableContext,
  useSortable,
  arrayMove,
  verticalListSortingStrategy,
} from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';
import { AnimatedCollapse } from '../animations/AnimatedCollapse';

export type DragHandleProps = ReturnType<typeof useSortable>['attributes'] &
  ReturnType<typeof useSortable>['listeners'];

interface DraggableListProps<GroupType, ItemType, DragHandlePropsType = DragHandleProps> {
  items: GroupType[];
  getGroupId: (group: GroupType) => string;
  getItemId: (item: ItemType) => string;
  getItemsInGroup: (group: GroupType) => ItemType[];
  onChange?: (newGroups: GroupType[]) => void;
  onItemDragEnd?: (groupId: string, newList: ItemType[]) => void;
  renderGroup: (
    group: GroupType,
    dragHandleProps: DragHandlePropsType,
    isExpanded: boolean,
    toggleGroup: () => void
  ) => ReactNode;
  renderItem: (item: ItemType, index: number, dragHandleProps: DragHandlePropsType) => ReactNode;
}

const NestedDraggableList = <GroupType, ItemType, DragHandlePropsType = DragHandleProps>({
  items,
  getGroupId,
  getItemId,
  getItemsInGroup,
  onChange,
  onItemDragEnd,
  renderGroup,
  renderItem,
}: DraggableListProps<GroupType, ItemType, DragHandlePropsType>) => {
  const sensors = useSensors(useSensor(PointerSensor));

  const prevItems = useRef<typeof items>([]);
  const [expandedGroups, setExpandedGroups] = useState<Set<string>>(new Set(items.map(getGroupId)));

  useEffect(() => {
    setExpandedGroups((prev) => {
      const newSet = new Set(prev);
      const prevGroupMap = new Map(prevItems.current.map((g) => [getGroupId(g), g]));
      items.forEach((item) => {
        const groupId = getGroupId(item);
        const prevGroup = prevGroupMap.get(groupId);
        const newQuestions = getItemsInGroup(item);
        if (!prevGroup) {
          newSet.add(groupId);
        } else {
          const prevQuestions = getItemsInGroup(prevGroup);
          const questionsChanged = prevQuestions.length !== newQuestions.length;
          if (questionsChanged && !newSet.has(groupId)) {
            newSet.add(groupId);
          }
        }
      });
      return newSet;
    });
    prevItems.current = items;
  }, [items, getGroupId, getItemId, getItemsInGroup]);

  const toggleGroup = (id: string) => {
    setExpandedGroups((prev) => {
      const newSet = new Set(prev);
      if (newSet.has(id)) newSet.delete(id);
      else newSet.add(id);
      return newSet;
    });
  };

  const handleGroupDragEnd = (event: DragEndEvent) => {
    const { active, over } = event;
    if (!over || active.id === over.id) return;

    const oldIndex = items.findIndex((group) => getGroupId(group) === active.id);
    const newIndex = items.findIndex((group) => getGroupId(group) === over.id);

    onChange?.(arrayMove(items, oldIndex, newIndex));
  };

  return (
    <DndContext sensors={sensors} collisionDetection={closestCenter} onDragEnd={handleGroupDragEnd}>
      <SortableContext items={items.map(getGroupId)} strategy={verticalListSortingStrategy}>
        {items.map((group) => {
          const groupId = getGroupId(group);
          return (
            <SortableGroup<GroupType, ItemType, DragHandlePropsType>
              key={groupId}
              id={groupId}
              group={group}
              getItemId={getItemId}
              getItemsInGroup={getItemsInGroup}
              renderGroup={renderGroup}
              renderItem={renderItem}
              onItemDragEnd={onItemDragEnd}
              isExpanded={expandedGroups.has(groupId)}
              onToggle={() => toggleGroup(groupId)}
            />
          );
        })}
      </SortableContext>
    </DndContext>
  );
};

type SortableGroupProps<GroupType, ItemType, DragHandlePropsType> = {
  id: string;
  group: GroupType;
  getItemId: (item: ItemType) => string;
  getItemsInGroup: (group: GroupType) => ItemType[];
  renderGroup: (
    group: GroupType,
    dragHandleProps: DragHandlePropsType,
    isExpanded: boolean,
    toggleGroup: () => void
  ) => ReactNode;
  renderItem: (item: ItemType, index: number, dragHandleProps: DragHandlePropsType) => ReactNode;
  onItemDragEnd?: (groupId: string, newList: ItemType[]) => void;
  isExpanded: boolean;
  onToggle: () => void;
};

const SortableGroup = <GroupType, ItemType, DragHandlePropsType>({
  id,
  group,
  getItemId,
  getItemsInGroup,
  renderGroup,
  renderItem,
  onItemDragEnd,
  isExpanded,
  onToggle,
}: SortableGroupProps<GroupType, ItemType, DragHandlePropsType>) => {
  const { attributes, listeners, setNodeRef, transform, transition, isDragging } = useSortable({
    id,
  });

  const dragHandleProps = { ...attributes, ...listeners } as DragHandlePropsType;

  const style: CSSProperties = {
    transform: CSS.Transform.toString(transform),
    transition,
    opacity: isDragging ? 0.5 : 1,
  };

  const sensors = useSensors(useSensor(PointerSensor));
  const items = getItemsInGroup(group);

  const handleItemDragEnd = (event: DragEndEvent) => {
    const { active, over } = event;
    if (!over || active.id === over.id) return;

    const oldIndex = items.findIndex((item) => getItemId(item) === active.id);
    const newIndex = items.findIndex((item) => getItemId(item) === over.id);

    const newList = arrayMove(items, oldIndex, newIndex);
    onItemDragEnd?.(id, newList);
  };

  return (
    <div
      ref={setNodeRef}
      style={style}
      className="border-sectionBorder bg-lavenderBlue my-6 flex flex-col gap-4 rounded-lg border px-3 py-4"
    >
      {renderGroup(group, dragHandleProps, isExpanded, onToggle)}
      <AnimatedCollapse isExpanded={isExpanded}>
        <DndContext
          sensors={sensors}
          collisionDetection={closestCenter}
          onDragEnd={handleItemDragEnd}
        >
          <SortableContext items={items.map(getItemId)} strategy={verticalListSortingStrategy}>
            {items.map((item, index) => (
              <SortableItem<DragHandlePropsType> key={getItemId(item)} id={getItemId(item)}>
                {(itemDragHandleProps) => renderItem(item, index, itemDragHandleProps)}
              </SortableItem>
            ))}
          </SortableContext>
        </DndContext>
      </AnimatedCollapse>
    </div>
  );
};

type SortableItemProps<D> = {
  id: string;
  children: (dragHandleProps: D) => ReactNode;
};

const SortableItem = <D,>({ id, children }: SortableItemProps<D>) => {
  const { attributes, listeners, setNodeRef, transform, transition, isDragging } = useSortable({
    id,
  });

  const style: CSSProperties = {
    transform: CSS.Transform.toString(transform),
    transition,
    opacity: isDragging ? 0.5 : 1,
  };

  const dragHandleProps = { ...attributes, ...listeners } as D;

  return (
    <div ref={setNodeRef} style={style}>
      {children(dragHandleProps)}
    </div>
  );
};

export { NestedDraggableList };
