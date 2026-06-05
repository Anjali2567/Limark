import { FC, ReactNode } from 'react';

import { cn } from '@/lib/utils/helpers';
import { convertTime } from '@/lib/utils/timeConversion';

import { Bot } from 'lucide-react';

type AssistantWrapperProps = {
  children: ReactNode;
  time?: Date;
};

const AssistantWrapper: FC<AssistantWrapperProps> = ({ children, time = new Date() }) => {
  return (
    <div className="my-1 mb-3 flex items-start text-xs text-slate-400">
      <div className="bg-primary/10 flex h-8 w-8 shrink-0 items-center justify-center rounded-full">
        <Bot className="text-primary h-4 w-4" />
      </div>
      <div className={cn('mx-1 w-[70%] rounded-lg rounded-tl-none text-sm')}>
        <div className="border-sectionBorder rounded-lg border bg-white px-4 py-3">
          {children}
          {time && <p className="mt-1 block text-xs opacity-70">{convertTime(new Date(time))}</p>}
        </div>
      </div>
    </div>
  );
};

export { AssistantWrapper };
