import { useMemo } from 'react';

import { CheckListQuestion, CheckListSection, GroupedSection } from '@/types/question.types';

type Props = {
  questionList: CheckListQuestion[];
  sectionList: CheckListSection[];
};

export const useGetSectionQuestionList = ({ questionList, sectionList }: Props) => {
  const groupedChecklist = useMemo<GroupedSection[]>(() => {
    if (!questionList?.length) return [];

    const sectionMap = questionList.reduce<Map<string, GroupedSection>>((acc, question) => {
      const sectionId = question.questionSectionId ?? '';
      if (!acc.has(sectionId)) {
        const section = sectionList.find((s) => s.id == sectionId);
        acc.set(sectionId, {
          section: {
            id: sectionId,
            name: section?.name || '',
            position: section?.position ?? null,
          },
          questionList: [question],
        });
      } else {
        acc.get(sectionId)!.questionList.push(question);
      }
      return acc;
    }, new Map<string, GroupedSection>());

    let groupedArray = Array.from(sectionMap.values());

    const usedPositions = new Set<number>();
    groupedArray.forEach((g) => {
      if (g.section.position != null) usedPositions.add(g.section.position);
    });

    let nextSectionPosition = (usedPositions.size > 0 ? Math.max(...usedPositions) : 0) + 1;
    groupedArray = groupedArray.map((g) => {
      if (g.section.position == null) {
        g.section.position = nextSectionPosition++;
      }

      const usedQuestionPositions = new Set<number>();
      g.questionList.forEach((q) => {
        if (q.position != null) usedQuestionPositions.add(q.position);
      });

      let nextQuestionPosition =
        (usedQuestionPositions.size > 0 ? Math.max(...usedQuestionPositions) : 0) + 1;

      g.questionList = g.questionList.map((q) => {
        if (q.position == null) {
          return { ...q, position: nextQuestionPosition++ };
        }
        return q;
      });

      g.questionList.sort((a, b) => (a.position ?? 0) - (b.position ?? 0));
      return g;
    });

    groupedArray.sort((a, b) => (a.section.position ?? 0) - (b.section.position ?? 0));

    return groupedArray;
  }, [sectionList, questionList]);

  return groupedChecklist;
};
