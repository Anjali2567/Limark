import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';

import { Calendar, FileText, Mail, Target, TrendingUp, Users } from 'lucide-react';

const quickActions = [
  { icon: Target, label: 'Create Campaign', action: 'create-campaign' },
  { icon: Users, label: 'Analyze Leads', action: 'analyze-leads' },
  { icon: Mail, label: 'Generate Email', action: 'generate-email' },
  { icon: TrendingUp, label: 'Campaign Performance', action: 'campaign-performance' },
  { icon: FileText, label: 'Generate Report', action: 'generate-report' },
  { icon: Calendar, label: 'Schedule Follow-up', action: 'schedule-followup' },
];

const QuickActions = () => {
  return (
    <Card>
      <CardHeader>
        <CardTitle className="text-lg">Quick Actions</CardTitle>
        <CardDescription>Common tasks you can perform</CardDescription>
      </CardHeader>
      <CardContent className="grid grid-cols-2 gap-2">
        {quickActions.map((action, index) => (
          <Button
            key={index}
            variant="outline"
            className="hover:text-foreground h-auto flex-col items-center justify-center gap-2 py-3 hover:border-sky-300 hover:bg-sky-50"
          >
            <action.icon className="h-5 w-5 text-sky-500" />
            <span
              className="text-foreground w-full truncate text-center text-xs"
              title={action.label}
            >
              {action.label}
            </span>
          </Button>
        ))}
      </CardContent>
    </Card>
  );
};

export { QuickActions };
