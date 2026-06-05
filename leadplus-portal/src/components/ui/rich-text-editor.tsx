'use client';

import { useCallback, useEffect, useRef, useState } from 'react';

import { RichTextEditorToolbar } from './rich-text-editor-toolbar';
import {
  useEditor,
  EditorContent,
  Editor,
  NodeViewWrapper,
  ReactNodeViewRenderer,
} from '@tiptap/react';
import StarterKit from '@tiptap/starter-kit';
import Underline from '@tiptap/extension-underline';
import TextAlign from '@tiptap/extension-text-align';
import Link from '@tiptap/extension-link';
import Image from '@tiptap/extension-image';
import Color from '@tiptap/extension-color';
import { Extension } from '@tiptap/react';
import { TextStyle } from '@tiptap/extension-text-style';
import Placeholder from '@tiptap/extension-placeholder';
import { cn } from '@/lib/utils/helpers';
import { ScrollArea } from './scroll-area';

declare module '@tiptap/core' {
  interface Commands<ReturnType> {
    fontSize: {
      setFontSize: (size: string) => ReturnType;
      unsetFontSize: () => ReturnType;
    };
  }
}

const FontSize = Extension.create({
  name: 'fontSize',

  addOptions() {
    return {
      types: ['textStyle'],
    };
  },

  addGlobalAttributes() {
    return [
      {
        types: this.options.types,
        attributes: {
          fontSize: {
            default: null,
            parseHTML: (element) => element.style.fontSize.replace(/['"]+/g, ''),
            renderHTML: (attributes) => {
              if (!attributes.fontSize) {
                return {};
              }

              return {
                style: `font-size: ${attributes.fontSize}`,
              };
            },
          },
        },
      },
    ];
  },

  addCommands() {
    return {
      setFontSize:
        (fontSize) =>
        ({ chain }) => {
          return chain().setMark('textStyle', { fontSize }).run();
        },
      unsetFontSize:
        () =>
        ({ chain }) => {
          return chain().setMark('textStyle', { fontSize: null }).removeEmptyTextStyle().run();
        },
    };
  },
});

const ImageResizableView = ({
  node,
  updateAttributes,
  selected,
}: {
  node: { attrs: Record<string, unknown> };
  updateAttributes: (attrs: Record<string, unknown>) => void;
  selected: boolean;
}) => {
  const imgRef = useRef<HTMLImageElement>(null);
  const startXRef = useRef(0);
  const startWidthRef = useRef(0);
  const [isResizing, setIsResizing] = useState(false);

  const width = (node.attrs.width as number | null) ?? null;

  const onMouseDown = useCallback((e: React.MouseEvent) => {
    e.preventDefault();
    startXRef.current = e.clientX;
    startWidthRef.current = imgRef.current?.offsetWidth ?? 300;
    setIsResizing(true);
  }, []);

  useEffect(() => {
    if (!isResizing) return;
    const onMove = (e: MouseEvent) => {
      const newWidth = Math.max(50, startWidthRef.current + (e.clientX - startXRef.current));
      updateAttributes({ width: Math.round(newWidth) });
    };
    const onUp = () => setIsResizing(false);
    document.addEventListener('mousemove', onMove);
    document.addEventListener('mouseup', onUp);
    return () => {
      document.removeEventListener('mousemove', onMove);
      document.removeEventListener('mouseup', onUp);
    };
  }, [isResizing, updateAttributes]);

  return (
    <NodeViewWrapper as="span" className="relative my-1 inline-block">
      {/* eslint-disable-next-line @next/next/no-img-element */}
      <img
        ref={imgRef}
        src={node.attrs.src as string}
        alt={(node.attrs.alt as string) ?? ''}
        draggable={false}
        className={cn('block h-auto rounded', selected && 'ring-2 ring-sky-500 ring-offset-1')}
        style={width ? { width: `${width}px`, maxWidth: '100%' } : { maxWidth: '100%' }}
      />
      {(selected || isResizing) && (
        <span
          className="absolute right-0 bottom-0 h-3 w-3 cursor-se-resize rounded-sm border border-white bg-sky-500"
          onMouseDown={onMouseDown}
        />
      )}
    </NodeViewWrapper>
  );
};

const ImageWithResize = Image.extend({
  addAttributes() {
    return {
      ...this.parent?.(),
      width: {
        default: null,
        parseHTML: (el) => {
          const w = el.getAttribute('width');
          return w ? parseInt(w, 10) : null;
        },
        renderHTML: (attrs) => {
          if (!attrs.width) return {};
          return { width: attrs.width, style: `width:${attrs.width}px;max-width:100%` };
        },
      },
    };
  },
  addNodeView() {
    return ReactNodeViewRenderer(ImageResizableView);
  },
});

interface RichTextEditorProps {
  value: string;
  onChange: (value: string) => void;
  onBlur?: () => void;
  placeholder?: string;
  disabled?: boolean;
  className?: string;
  editorClassName?: string;
  onAttachClick?: () => void;
  showToolbar?: boolean;
  editorRef?: React.MutableRefObject<Editor | null>;
  onImageUpload?: (file: File) => Promise<string | null>;
  onEditorReady?: (editor: Editor) => void;
}

const normalizeForEditor = (html: string) =>
  html
    .replace(/<\/?blockquote[^>]*>/gi, '')
    .replace(/<p style="margin\s*:\s*0\s*;\s*/g, '<p style="')
    .replace(/<p style="margin\s*:\s*0\s*">/g, '<p>')
    .replace(/<(h[1-6]) style="margin\s*:\s*0\s*;\s*/g, '<$1 style="')
    .replace(/<(h[1-6]) style="margin\s*:\s*0\s*">/g, '<$1>')
    .replace(/<(p|h[1-6]) style="">/g, '<$1>')
    .replace(/<p><br\s*\/?><\/p>/g, '<p></p>')
    .replace(/<p>&nbsp;<\/p>/g, '<p></p>');

export const RichTextEditor = ({
  value,
  onChange,
  onBlur,
  placeholder = 'Compose your email...',
  disabled = false,
  className,
  editorClassName,
  onAttachClick,
  showToolbar = true,
  editorRef,
  onImageUpload,
  onEditorReady,
}: RichTextEditorProps) => {
  const lastEmittedHtmlRef = useRef<string>('');

  const editor = useEditor({
    immediatelyRender: false,
    extensions: [
      StarterKit.configure({
        heading: {
          levels: [1, 2, 3, 4, 5, 6],
        },
        bulletList: {
          HTMLAttributes: {
            style: 'list-style-type: disc; padding-left: 1.5em;',
          },
        },
        orderedList: {
          HTMLAttributes: {
            style: 'list-style-type: decimal; padding-left: 1.5em;',
          },
        },
      }),
      Underline,
      TextAlign.configure({
        types: ['heading', 'paragraph'],
      }),
      Link.configure({
        openOnClick: false,
        HTMLAttributes: {
          style: 'color: #3b82f6; text-decoration: underline;',
          target: '_blank',
          rel: 'noopener noreferrer nofollow',
        },
      }),
      ImageWithResize,
      Color,
      TextStyle,
      FontSize,
      Placeholder.configure({
        placeholder,
        emptyEditorClass: 'is-editor-empty',
      }),
    ],
    content: normalizeForEditor(value),
    editable: !disabled,
    onUpdate: ({ editor }) => {
      const html = editor
        .getHTML()
        .replace(/<p><\/p>/g, '<p>&nbsp;</p>')
        .replace(/<p style="(?!margin: 0)/g, '<p style="margin: 0; ')
        .replace(/<p>/g, '<p style="margin: 0">')
        .replace(/<(h[1-6]) style="(?!margin: 0)/g, '<$1 style="margin: 0; ')
        .replace(/<(h[1-6])>/g, '<$1 style="margin: 0">');
      lastEmittedHtmlRef.current = html;
      onChange(html);
    },
    onBlur: () => {
      onBlur?.();
    },
    editorProps: {
      attributes: {
        class: cn(
          'min-h-[280px] border-0 resize-none focus-visible:ring-0 shadow-none px-0 py-0 text-sm leading-relaxed focus:outline-none [&_p]:my-0',
          disabled && 'opacity-50 cursor-not-allowed'
        ),
      },
      transformPastedHTML(html) {
        return html
          .replace(/<\/?blockquote[^>]*>/gi, '')
          .replace(/padding-left\s*:\s*[^;"]+;?/gi, '')
          .replace(/margin-left\s*:\s*[^;"]+;?/gi, '')
          .replace(/text-indent\s*:\s*[^;"]+;?/gi, '');
      },
    },
  });

  useEffect(() => {
    if (editor) {
      if (value === lastEmittedHtmlRef.current) {
        return;
      }
      const normalized = normalizeForEditor(value);
      if (normalized !== editor.getHTML()) {
        editor.commands.setContent(normalized);
      }
    }
  }, [value, editor]);

  useEffect(() => {
    if (editor) {
      editor.setEditable(!disabled);
    }
  }, [disabled, editor]);

  useEffect(() => {
    if (editor) {
      if (editorRef) {
        editorRef.current = editor;
      }
      onEditorReady?.(editor);
    }
  }, [editor, editorRef, onEditorReady]);

  if (!editor) {
    return null;
  }

  return (
    <div className={cn('overflow-hidden', className)}>
      <div
        className={cn(
          editorClassName,
          'bg-input-background max-h-125 overflow-y-auto rounded-md px-4 py-4'
        )}
      >
        <ScrollArea>
          <EditorContent editor={editor} />
        </ScrollArea>
      </div>
      {showToolbar && (
        <RichTextEditorToolbar
          editor={editor}
          disabled={disabled}
          onAttachClick={onAttachClick}
          onImageUpload={onImageUpload}
        />
      )}
    </div>
  );
};
