import { useRouter, useSearchParams } from 'next/navigation';

import { StatusBadge } from '@/components/StatusBadge';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import {
  Sheet,
  SheetContent,
  SheetDescription,
  SheetHeader,
  SheetTitle,
} from '@/components/ui/sheet';
import { formatString } from '@/lib/utils/formatter';
import { Agreement } from '@/types/agreements.types';

import { Calendar, Edit } from 'lucide-react';

type DocumentDrawerProps = {
  isOpen: boolean;
  setIsOpen: (isOpen: boolean) => void;
  document?: Agreement;
};

const DocumentDrawer = ({ isOpen, setIsOpen, document }: DocumentDrawerProps) => {
  const router = useRouter();
  const searchParams = useSearchParams();

  const handleEdit = () => {
    const params = new URLSearchParams(searchParams.toString());
    params.set('id', document?.id || '');
    router.push(`?${params.toString()}`);
  };

  return (
    <Sheet open={isOpen} onOpenChange={setIsOpen}>
      <SheetContent className="flex w-full flex-col gap-0 p-0 sm:max-w-175">
        <SheetHeader className="border-border shrink-0 border-b bg-white px-6 py-4 shadow-sm">
          <SheetTitle className="text-xl">{formatString(document?.agreementType)}</SheetTitle>
          <SheetDescription>
            <span className="flex items-center gap-4">
              <Badge variant="outline" className="font-mono">
                v{document?.version}
              </Badge>
              <span className="flex items-center gap-2 text-sm">
                <Calendar className="h-4 w-4" />
                Effective:{' '}
                {document &&
                  new Date(document.createdAt).toLocaleDateString('en-US', {
                    year: 'numeric',
                    month: 'long',
                    day: 'numeric',
                  })}
              </span>
              <StatusBadge status={document?.latest ? 'ACTIVE' : ''} />
            </span>
          </SheetDescription>
        </SheetHeader>

        <div className="flex-1 space-y-4 overflow-y-auto px-6 py-6">
          <div className="bg-secondary/50 border-border rounded-lg border p-6">
            <div
              className="text-foreground leading-relaxed"
              dangerouslySetInnerHTML={{ __html: document?.content || '' }}
            />
          </div>
        </div>

        {document?.latest && (
          <div className="border-border flex shrink-0 items-center justify-end gap-3 border-t bg-white px-6 py-4">
            <Button variant="outline" onClick={() => setIsOpen(false)}>
              Close
            </Button>
            <Button className="bg-primary hover:bg-primary/90" onClick={handleEdit}>
              <Edit className="mr-2 h-4 w-4" />
              Edit
            </Button>
          </div>
        )}
      </SheetContent>
    </Sheet>
  );
};

export { DocumentDrawer };
