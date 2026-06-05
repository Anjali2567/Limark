import { Card, CardContent } from './ui/card';

import { Info } from 'lucide-react';

const InfoCard = ({ title, description }: { title: string; description: string }) => (
  <Card className="border-sky-200 bg-sky-50">
    <CardContent className="p-4!">
      <div className="flex items-start gap-3">
        <Info className="mt-0.5 h-5 w-5 text-sky-500" />
        <div>
          <p className="text-sm font-medium">{title}</p>
          <p className="text-muted-foreground text-sm">{description}</p>
        </div>
      </div>
    </CardContent>
  </Card>
);

export { InfoCard };
