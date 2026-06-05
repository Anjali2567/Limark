import { FC, ReactNode } from 'react';

type SimpleMarkdownProps = {
  text: string;
  className?: string;
};

const SimpleMarkdown: FC<SimpleMarkdownProps> = ({ text, className = '' }) => {
  const normalizedText = text.replace(/\\r\\n/g, '\n').replace(/\\n/g, '\n');
  const lines = normalizedText.split(/\r?\n/);
  const elements: ReactNode[] = [];
  let listBuffer: string[] = [];
  let isOrderedList = false;
  let tableBuffer: string[] = [];

  const flushList = () => {
    if (!listBuffer.length) return;
    const ListTag = isOrderedList ? 'ol' : 'ul';
    elements.push(
      <ListTag
        className={`mb-2 ml-6 ${isOrderedList ? 'list-decimal' : 'list-disc'} marker:text-blue-800`}
        key={`list-${elements.length}`}
      >
        {listBuffer.map((item, i) => (
          <li key={i} className="mb-1">
            {renderInline(item)}
          </li>
        ))}
      </ListTag>
    );
    listBuffer = [];
  };

  const renderInline = (chunk: string): ReactNode => {
    const tokens = chunk
      .split(
        /(\[[^\]]+\]\(https?:\/\/[^\s)]+\)|`[^`]+`|\*\*[^*]+\*\*|__[^_]+__|\*[^*]+\*|_[^_]+_)/g
      )
      .filter(Boolean);

    return (
      <>
        {tokens.map((tok, i) => {
          const linkMatch = tok.match(/^\[([^\]]+)\]\((https?:\/\/[^\s)]+)\)$/);
          if (linkMatch) {
            return (
              <a
                className="text-blue-600 underline"
                href={linkMatch[2]}
                key={i}
                rel="noopener noreferrer"
                target="_blank"
              >
                {linkMatch[1]}
              </a>
            );
          }

          if (tok.startsWith('`') && tok.endsWith('`')) {
            return (
              <code className="rounded bg-slate-100 px-1" key={i}>
                {tok.slice(1, -1)}
              </code>
            );
          }

          if (
            (tok.startsWith('**') && tok.endsWith('**')) ||
            (tok.startsWith('__') && tok.endsWith('__'))
          ) {
            return (
              <span className="font-semibold" key={i}>
                {tok.slice(2, -2)}
              </span>
            );
          }

          if (
            (tok.startsWith('*') && tok.endsWith('*')) ||
            (tok.startsWith('_') && tok.endsWith('_'))
          ) {
            return <em key={i}>{tok.slice(1, -1)}</em>;
          }

          return <span key={i}>{tok}</span>;
        })}
      </>
    );
  };

  const isTableSeparator = (line: string) =>
    /^\|?\s*:?-{3,}:?\s*(\|\s*:?-{3,}:?\s*)+\|?$/.test(line);

  const parseTableRow = (line: string) =>
    line
      .trim()
      .replace(/^\|/, '')
      .replace(/\|$/, '')
      .split('|')
      .map((cell) => cell.trim());

  const flushTable = () => {
    if (tableBuffer.length < 2 || !isTableSeparator(tableBuffer[1])) {
      tableBuffer.forEach((row, i) => {
        if (row.trim()) {
          elements.push(
            <p className="mb-2 last:mb-0" key={`table-fallback-${elements.length}-${i}`}>
              {renderInline(row.trim())}
            </p>
          );
        }
      });
      tableBuffer = [];
      return;
    }

    const headers = parseTableRow(tableBuffer[0]);
    const rows = tableBuffer.slice(2).map(parseTableRow);

    elements.push(
      <div className="mb-3 overflow-x-auto" key={`table-${elements.length}`}>
        <table className="w-full border-collapse text-sm">
          <thead>
            <tr>
              {headers.map((header, i) => (
                <th
                  className="border-border bg-muted/50 border px-3 py-2 text-left font-semibold"
                  key={i}
                >
                  {renderInline(header)}
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {rows.map((row, rowIndex) => (
              <tr key={rowIndex}>
                {headers.map((_, cellIndex) => (
                  <td className="border-border border px-3 py-2" key={cellIndex}>
                    {renderInline(row[cellIndex] ?? '')}
                  </td>
                ))}
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    );
    tableBuffer = [];
  };

  lines.forEach((line, idx) => {
    const trimmed = line.trim();
    const isBullet = /^[-*+] /.test(trimmed);
    const isOrdered = /^\d+\. /.test(trimmed);
    const isTableLine = trimmed.includes('|') && parseTableRow(trimmed).length > 1;

    if (isTableLine) {
      flushList();
      tableBuffer.push(trimmed);
      return;
    }

    flushTable();

    const headingMatch = /^#{1,6} /.exec(trimmed);
    if (headingMatch) {
      flushList();
      const level = headingMatch[0].trim().length;
      const headingText = trimmed.replace(/^#{1,6} /, '');
      elements.push(
        <h4 className="my-2 text-base font-semibold" key={`h${level}-${idx}`}>
          {renderInline(headingText)}
        </h4>
      );
      return;
    }

    if (isBullet || isOrdered) {
      const listType = isOrdered;
      if (listBuffer.length && listType !== isOrderedList) {
        flushList();
      }
      isOrderedList = listType;
      listBuffer.push(trimmed.replace(/^([-*+] |\d+\. )/, ''));
    } else {
      flushList();
      if (trimmed) {
        elements.push(
          <p className="mb-2 last:mb-0" key={`p-${idx}`}>
            {renderInline(trimmed)}
          </p>
        );
      }
    }
  });

  flushList();
  flushTable();
  return <div className={className}>{elements}</div>;
};

export { SimpleMarkdown };
