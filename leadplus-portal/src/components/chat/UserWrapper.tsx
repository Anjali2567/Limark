import { FC, JSX } from 'react';
import { User } from 'lucide-react';

type UserWrapperProps = {
  message: string | JSX.Element;
};

const UserWrapper: FC<UserWrapperProps> = ({ message }) => {
  return (
    <div className="mb-3 flex w-full flex-col items-end">
      <div className="flex w-full flex-row-reverse gap-2">
        <div className="bg-muted flex h-8 w-8 shrink-0 items-center justify-center rounded-full">
          <User className="text-foreground h-4 w-4" />
        </div>
        <div className="bg-accent w-fit max-w-[60%] rounded-lg px-5 py-3 text-sm wrap-break-word text-white">
          {message}
        </div>
      </div>
    </div>
  );
};

export { UserWrapper };
