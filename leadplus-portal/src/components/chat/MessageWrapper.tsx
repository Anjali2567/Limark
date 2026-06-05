import { FC, JSX } from 'react';
import { convertDateTime } from '@/lib/utils/timeConversion';

type MessageWrapperProps = {
  message: string | JSX.Element;
  time?: Date;
};

const MessageWrapper: FC<MessageWrapperProps> = ({ message, time = new Date() }) => {
  return (
    <div className="flex w-full flex-col items-end">
      <div className="flex text-xs text-slate-400">
        {time && <p>{convertDateTime(new Date(time))}</p>}
      </div>
      <div className="bg-accent w-fit max-w-[60%] rounded-lg rounded-tr-none px-5 py-3 text-sm wrap-break-word text-white">
        {message}
      </div>
    </div>
  );
};

export { MessageWrapper };
