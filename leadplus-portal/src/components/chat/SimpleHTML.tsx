'use client';

import { FC, useMemo } from 'react';
import DOMPurify from 'dompurify';
import { stripThreadedReplies } from '@/lib/utils/email.utils';

type SimpleHTMLProps = {
  text: string;
  className?: string;
  stripReplies?: boolean;
};

const SimpleHTML: FC<SimpleHTMLProps> = ({ text, className = '', stripReplies = false }) => {
  const processedText = useMemo(() => {
    let content = text;
    if (stripReplies) {
      content = stripThreadedReplies(content);
    }
    return content;
  }, [text, stripReplies]);

  const sanitizedHtml = useMemo(() => {
    return DOMPurify.sanitize(processedText);
  }, [processedText]);

  return (
    <div
      className={`[&_p]:my-0 ${className}`}
      dangerouslySetInnerHTML={{ __html: sanitizedHtml }}
    />
  );
};

export { SimpleHTML };
