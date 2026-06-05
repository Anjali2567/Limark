import { useMemo } from 'react';

import { SectionCard } from '@/components/SectionCard';
import { useGetQuestionnaireIndustry } from '@/hooks/useQuestionnaire';
import { AnswerRequest } from '@/types/vendor.types';
import { Question, Section } from '@/types/questionnaire.types';
import { sortByPosition } from '@/lib/utils/helpers';
import { renderQuestionAnswer } from '@/lib/utils/renderQuestionAnswer';

type VendorQuestionnaireAnswersProps = {
  industryIds?: number[];
  questionnaire?: AnswerRequest[];
};

const VendorQuestionnaireAnswers = ({
  industryIds = [],
  questionnaire = [],
}: VendorQuestionnaireAnswersProps) => {
  const { data: sections = [], isLoading } = useGetQuestionnaireIndustry({
    industryIds: industryIds.map(String),
  });

  const answerMap = useMemo(() => {
    return questionnaire.reduce(
      (acc, item) => {
        acc[item.questionId] = item.answer;
        return acc;
      },
      {} as Record<string, string>
    );
  }, [questionnaire]);

  const sortedSections = sortByPosition<Section>(sections || []);

  return (
    <SectionCard title="Questionnaire">
      <div className="col-span-2 space-y-6 py-4">
        {isLoading ? (
          <div className="flex items-center justify-center py-8">
            <div className="text-muted-foreground">Loading questionnaire...</div>
          </div>
        ) : sections.length === 0 ? (
          <div className="bg-muted/30 border-border rounded-lg border-2 border-dashed py-12 text-center">
            <p className="text-muted-foreground mb-1 text-sm font-medium">
              No questionnaire available
            </p>
            <p className="text-muted-foreground text-xs">
              This vendor&apos;s industry does not have a questionnaire configured yet
            </p>
          </div>
        ) : questionnaire.length === 0 ? (
          <div className="bg-muted/30 border-border rounded-lg border-2 border-dashed py-12 text-center">
            <p className="text-muted-foreground mb-1 text-sm font-medium">No answers provided</p>
            <p className="text-muted-foreground text-xs">
              This vendor has not completed the questionnaire yet
            </p>
          </div>
        ) : (
          sortedSections.map((section) => {
            const sortedQuestions = sortByPosition<Question>(section.questions || []);
            return (
              <div key={section.sectionId} className="space-y-4">
                {section.sectionName && (
                  <h3 className="text-foreground text-lg font-semibold">{section.sectionName}</h3>
                )}
                <div className="space-y-4">
                  {sortedQuestions.map((question, index) => {
                    const answer = answerMap[question.id];
                    return (
                      <div
                        key={question.id}
                        className="border-border bg-card rounded-lg border p-4"
                      >
                        <div className="flex gap-3">
                          <span className="bg-primary/10 text-primary flex h-6 w-6 shrink-0 items-center justify-center rounded-full text-sm font-semibold">
                            {index + 1}
                          </span>
                          <div className="flex-1 space-y-3">
                            <h4 className="text-foreground text-base font-medium">
                              {question.label}
                            </h4>
                            <div className="pl-1">{renderQuestionAnswer(question, answer)}</div>
                          </div>
                        </div>
                      </div>
                    );
                  })}
                </div>
              </div>
            );
          })
        )}
      </div>
    </SectionCard>
  );
};

export { VendorQuestionnaireAnswers };
