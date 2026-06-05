import { FC, ReactNode, JSX } from 'react';
import Image from 'next/image';

import { cn } from '@/lib/utils/helpers';
import { convertDateTime } from '@/lib/utils/timeConversion';

type BotMessageWrapperProps = {
  children: ReactNode;
  time?: Date;
  icon?: JSX.Element;
  name?: string;
};

const BotResponseWrapper: FC<BotMessageWrapperProps> = ({
  children,
  time = new Date(),
  icon,
  name,
}) => {
  return (
    <div>
      <div className="my-1 flex items-center gap-2 text-xs text-slate-400">
        {icon ?? (
          <Image src={'/support-agent-avatar.svg'} alt="Support Agent" width={20} height={20} />
        )}
        {name}
        {time && <p>{convertDateTime(new Date(time))}</p>}
      </div>
      <div className={cn('mx-6 w-[70%] rounded-lg rounded-tl-none text-sm')}>{children}</div>
    </div>
  );
};

export { BotResponseWrapper };
