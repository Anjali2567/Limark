'use client';

import { useCallback, useEffect, useRef, useState } from 'react';

import { CheckListQuestion, CheckListSection, GroupedSection } from '@/types/question.types';
import SectionForm from './_components/SectionForm';
import { QuestionType } from '@/constants/questions.constants';
import { CheckListForm } from './_components/CheckListForm';
import { bulkItemPaginationParams } from '@/types/paginated-params';
import {
  useBulkUpdateCheckListQuestionsInSection,
  useBulkUpdateCheckListSections,
  useGetAllCheckListQuestions,
  useGetAllChecklistSections,
  useUpsertCheckListQuestion,
} from '@/hooks/useQuestion';
import { NestedDraggableList } from '@/components/draggable/NestedDraggableList';
import { RenderSection } from './_components/RenderSection';
import { RenderQuestionItem } from './_components/RenderQuestionItem';
import { useGetSectionQuestionList } from './_components/useGetSectionQuestionList';

export type CheckListFormValues = {
  id?: string;
  questionSectionId: string;
  industryIds: string[];
  type: QuestionType;
  label: string;
  position: number | null;
  options: string[];
};

const Checklist = () => {
  const questionsRef = useRef<CheckListQuestion[]>([]);
  const sectionsRef = useRef<CheckListSection[]>([]);

  const [questionList, setQuestionList] = useState<CheckListQuestion[]>([]);
  const [sectionList, setSectionList] = useState<CheckListSection[]>([]);

  const [selectedQuestionPosition, setSelectedQuestionPosition] = useState<number | null>(null);
  const [selectedQuestion, setSelectedQuestion] = useState<CheckListQuestion | null>(null);
  const [selectedSection, setSelectedSection] = useState<CheckListSection | null>(null);

  const {
    data: questions = [],
    isLoading,
    isFetching,
  } = useGetAllCheckListQuestions(bulkItemPaginationParams);
  const { data: sections = [] } = useGetAllChecklistSections();
  const { mutate: updateSectionPositions } = useBulkUpdateCheckListSections();

  const { mutate: bulkQuestionUpdate } = useBulkUpdateCheckListQuestionsInSection();
  const { mutateAsync: upsertQuestion } = useUpsertCheckListQuestion();

  const groupedChecklist = useGetSectionQuestionList({ questionList, sectionList });

  useEffect(() => {
    const questionsChanged = JSON.stringify(questionsRef.current) !== JSON.stringify(questions);
    const sectionsChanged = JSON.stringify(sectionsRef.current) !== JSON.stringify(sections);

    if (questionsChanged) {
      questionsRef.current = questions;
      setQuestionList(questions);
    }

    if (sectionsChanged) {
      sectionsRef.current = sections;
      setSectionList(sections);
    }
  }, [questions, sections]);

  const handleSubmit = async (values: CheckListFormValues) => {
    const isEdit = selectedQuestion != null && selectedQuestionPosition != null;
    const isSectionChanged = selectedQuestion?.questionSectionId != values.questionSectionId;

    const options = [QuestionType.MULTISELECT, QuestionType.RADIO].includes(values.type)
      ? values.options
      : [];
    const payload = { id: values.id ?? '', ...values };
    const data = await upsertQuestion({
      questionId: values.id,
      payload: { ...payload, position: values.position ?? 0, options },
    });

    if (isEdit && !isSectionChanged) {
      const updatedList = [...questions];
      updatedList[selectedQuestionPosition] = data;
      setQuestionList(updatedList);
    } else {
      let lastIndex = -1;
      for (let i = questionList.length - 1; i >= 0; i--) {
        if (questionList[i].questionSectionId == values.questionSectionId) {
          lastIndex = i;
          break;
        }
      }
      if (lastIndex >= 0) {
        const updatedList = [...questionList];
        updatedList.splice(lastIndex + 1, 0, data);
        setQuestionList(updatedList);
      } else {
        setQuestionList([...questionList, data]);
      }
    }
  };

  const resetForm = () => {
    setSelectedQuestion(null);
    setSelectedQuestionPosition(null);
  };

  const handleEditQuestion = useCallback(
    (item: CheckListQuestion, index: number) => {
      setSelectedQuestion(item);
      setSelectedQuestionPosition(index);
      setQuestionList(questions.filter((q) => q.id !== item.id));
    },
    [questions]
  );

  const movePositionInSection = (sectionId: string, newList: CheckListQuestion[]) => {
    if (!questionList?.length || !newList?.length) return;

    const reorderedQuestions = newList.map((q, index) => ({
      ...q,
      position: index + 1,
    }));

    const updatedList = questionList.map((q) => {
      return reorderedQuestions.find((rq) => rq.id === q.id) ?? q;
    });

    bulkQuestionUpdate({ sectionId, questions: reorderedQuestions });
    setQuestionList(updatedList);
  };

  const moveSections = (newList: GroupedSection[]) => {
    const updatedSections = newList.map((groupedSection, index) => ({
      ...groupedSection,
      section: {
        ...groupedSection.section,
        position: index + 1,
      },
    }));

    setSectionList(updatedSections.map((g) => g.section));
    updateSectionPositions(updatedSections.map((g) => g.section));
  };

  return (
    <main className="animate-in fade-in zoom-in-95 bg-background flex min-h-screen flex-col space-y-6 p-8 duration-500">
      <h1 className="text-foreground text-3xl font-bold">Checklist</h1>
      <SectionForm selectedSection={selectedSection} setSelectedSection={setSelectedSection} />
      <CheckListForm
        selectedQuestion={selectedQuestion}
        sectionList={sectionList}
        resetForm={resetForm}
        handleSubmit={handleSubmit}
      />
      {questionList.length > 0 ? (
        <div>
          <NestedDraggableList<GroupedSection, CheckListQuestion>
            items={groupedChecklist}
            getGroupId={(q) => q.section.id}
            getItemId={(q) => q.id}
            getItemsInGroup={(group) => group.questionList}
            renderGroup={(group, dragProps, expanded, toggle) => (
              <RenderSection
                groupedSection={group}
                dragHandleProps={dragProps}
                isExpanded={expanded}
                onToggle={toggle}
                onEditSection={(item) => setSelectedSection(item)}
              />
            )}
            renderItem={(item, index, dragProps) => (
              <RenderQuestionItem
                item={item}
                index={index}
                dragHandleProps={dragProps}
                onEditQuestion={handleEditQuestion}
                setQuestionList={setQuestionList}
              />
            )}
            onChange={moveSections}
            onItemDragEnd={(sectionId, newList) => movePositionInSection(sectionId, newList)}
          />
        </div>
      ) : isLoading || isFetching ? (
        <div className="flex items-center justify-center">
          <p className="text-muted-foreground text-center text-sm">Loading questions...</p>
        </div>
      ) : (
        <div>
          <p className="text-muted-foreground text-center text-sm">No questions added yet.</p>
        </div>
      )}
    </main>
  );
};

export default Checklist;
