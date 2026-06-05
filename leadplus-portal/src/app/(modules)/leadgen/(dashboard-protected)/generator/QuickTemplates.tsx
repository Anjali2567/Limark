import { Card, CardContent } from '@/components/ui/card';
import { ChartNoAxesCombined, Cpu, UserStar } from 'lucide-react';

type QuickTemplatesProps = {
  handleGenerate: (templatePrompt?: string) => void;
};
const QuickTemplates = ({ handleGenerate }: QuickTemplatesProps) => {
  const templates = [
    {
      icon: ChartNoAxesCombined,
      title: 'AWS Experts',
      prompt:
        'Find contacts in California companies with fewer than 1,000 employees who work with AWS.',
    },
    {
      icon: UserStar,
      title: 'Fintech Data Engineers',
      prompt:
        'Find Data Engineers with experience in Spark, Kafka, and AWS working in fintech companies.',
    },
    {
      icon: Cpu,
      title: 'VPs in Technology',
      prompt:
        'Find VPs in Tech companies with 500-1000 employees in the United States generating over $10M in revenue.',
    },
  ];

  return (
    <div className="w-full max-w-3xl pt-8">
      <div className="mb-6 flex items-center gap-4">
        <div className="bg-border h-px flex-1" />
        <span className="text-muted-foreground text-xs font-bold tracking-wider uppercase">
          Or start with a template
        </span>
        <div className="bg-border h-px flex-1" />
      </div>

      <div className="grid grid-cols-1 gap-4 md:grid-cols-3">
        {templates.map((t, i) => (
          <Card
            key={i}
            className="group bg-card border-border cursor-pointer text-center transition-all hover:border-sky-500 hover:shadow-md"
            onClick={() => handleGenerate(t.prompt)}
          >
            <CardContent className="flex flex-col items-center space-y-4 p-6">
              <div className="bg-secondary/50 text-muted-foreground rounded-full border p-3 transition-colors group-hover:border-sky-500 group-hover:text-sky-500">
                <t.icon className="h-6 w-6" />
              </div>
              <div>
                <h3 className="text-foreground font-semibold">{t.title}</h3>
                <p className="text-muted-foreground mt-2 text-xs">{t.prompt}</p>
              </div>
            </CardContent>
          </Card>
        ))}
      </div>
    </div>
  );
};

export { QuickTemplates };
