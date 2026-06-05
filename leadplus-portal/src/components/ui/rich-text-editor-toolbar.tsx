'use client';

import { Editor, useEditorState } from '@tiptap/react';
import { Button } from '@/components/ui/button';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { Popover, PopoverContent, PopoverTrigger } from '@/components/ui/popover';
import {
  Bold,
  Italic,
  Underline as UnderlineIcon,
  AlignLeft,
  AlignCenter,
  AlignRight,
  List,
  ListOrdered,
  Link2,
  Undo2,
  Redo2,
  Type,
  Paperclip,
  Trash2,
  ImageIcon,
} from 'lucide-react';
import { Input } from '@/components/ui/input';
import { useCallback, useRef, useState } from 'react';
import { cn } from '@/lib/utils/helpers';

interface RichTextEditorToolbarProps {
  editor: Editor | null;
  disabled?: boolean;
  deleteDisabled?: boolean;
  hideSaveButton?: boolean;
  onAttachClick?: () => void;
  onImageUpload?: (file: File) => Promise<string | null>;
  onDelete?: () => void;
  className?: string;
}

export const RichTextEditorToolbar = ({
  editor,
  disabled = false,
  deleteDisabled = false,
  hideSaveButton = false,
  onAttachClick,
  onImageUpload,
  onDelete,
  className,
}: RichTextEditorToolbarProps) => {
  const fileInputRef = useRef<HTMLInputElement>(null);
  const [linkUrl, setLinkUrl] = useState('');
  const [isLinkPopoverOpen, setIsLinkPopoverOpen] = useState(false);
  const [isUploading, setIsUploading] = useState(false);

  const editorState = useEditorState({
    editor,
    selector: ({ editor }) => ({
      canUndo: editor?.can().undo(),
      canRedo: editor?.can().redo(),
      isBold: editor?.isActive('bold'),
      isItalic: editor?.isActive('italic'),
      isUnderline: editor?.isActive('underline'),
      isLink: editor?.isActive('link'),
      isBulletList: editor?.isActive('bulletList'),
      isOrderedList: editor?.isActive('orderedList'),
      isAlignLeft: editor?.isActive({ textAlign: 'left' }),
      isAlignCenter: editor?.isActive({ textAlign: 'center' }),
      isAlignRight: editor?.isActive({ textAlign: 'right' }),
      color: editor?.getAttributes('textStyle').color as string | undefined,
      fontSize: editor?.getAttributes('textStyle').fontSize as string | undefined,
    }),
  });

  const openLinkPopover = useCallback(() => {
    if (editor) {
      const previousUrl = editor.getAttributes('link').href;
      setLinkUrl(previousUrl || '');
      setIsLinkPopoverOpen(true);
    }
  }, [editor]);

  const saveLink = useCallback(() => {
    if (editor) {
      if (linkUrl === '') {
        editor.chain().focus().extendMarkRange('link').unsetLink().run();
        setIsLinkPopoverOpen(false);
        return;
      }

      let url = linkUrl;
      const pattern = /^https?:\/\//;

      if (!pattern.test(url)) {
        url = 'https://' + url;
      }

      if (editor.isActive('link')) {
        editor.chain().focus().extendMarkRange('link').setLink({ href: url }).run();
      } else {
        editor.chain().focus().setLink({ href: url }).run();
      }

      editor.commands.setTextSelection(editor.state.selection.to);
      editor.commands.unsetMark('link');
      setIsLinkPopoverOpen(false);
    }
  }, [editor, linkUrl]);

  const removeLink = useCallback(() => {
    if (editor) {
      editor.chain().focus().extendMarkRange('link').unsetLink().run();
      setLinkUrl('');
      setIsLinkPopoverOpen(false);
    }
  }, [editor]);

  const handleFileChange = useCallback(
    async (e: React.ChangeEvent<HTMLInputElement>) => {
      const file = e.target.files?.[0];
      if (file && onImageUpload && editor) {
        setIsUploading(true);
        try {
          const url = await onImageUpload(file);
          if (url) {
            editor.chain().focus().setImage({ src: url }).run();
          }
        } finally {
          setIsUploading(false);
          if (fileInputRef.current) fileInputRef.current.value = '';
        }
      }
    },
    [editor, onImageUpload]
  );

  const setFontSize = useCallback(
    (size: string) => {
      if (!editor) return;

      switch (size) {
        case 'small':
          editor.chain().focus().setFontSize('12px').run();
          break;
        case 'normal':
          editor.chain().focus().unsetFontSize().run();
          break;
        case 'large':
          editor.chain().focus().setFontSize('20px').run();
          break;
        case 'huge':
          editor.chain().focus().setFontSize('32px').run();
          break;
      }
    },
    [editor]
  );

  if (!editor) {
    return null;
  }

  return (
    <div
      className={cn(
        'border-border bg-muted/30 flex flex-wrap items-center justify-between gap-y-1 border-t px-4 py-2',
        className
      )}
    >
      <div className="flex flex-wrap items-center gap-2">
        {!hideSaveButton && (
          <Button disabled={disabled} className="h-9 bg-sky-500 px-6 text-white hover:bg-sky-600">
            Save
          </Button>
        )}
        <div className="flex items-center gap-0.5">
          <Button
            type="button"
            variant="ghost"
            size="icon"
            className="text-muted-foreground h-9 w-9"
            title="Undo"
            onClick={() => editor.chain().focus().undo().run()}
            disabled={disabled || !editorState?.canUndo}
          >
            <Undo2 className="h-4 w-4" />
          </Button>
          <Button
            type="button"
            variant="ghost"
            size="icon"
            className="text-muted-foreground h-9 w-9"
            title="Redo"
            onClick={() => editor.chain().focus().redo().run()}
            disabled={disabled || !editorState?.canRedo}
          >
            <Redo2 className="h-4 w-4" />
          </Button>
        </div>

        <div className="bg-border mx-1 h-6 w-px" />

        {/* Font Size */}
        <Select
          value={
            editorState?.fontSize === '12px'
              ? 'small'
              : editorState?.fontSize === '20px'
                ? 'large'
                : editorState?.fontSize === '32px'
                  ? 'huge'
                  : 'normal'
          }
          onValueChange={setFontSize}
          disabled={disabled}
        >
          <SelectTrigger className="h-9 w-25 border-0 shadow-none focus:ring-0">
            <SelectValue placeholder="Size" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="small">Small</SelectItem>
            <SelectItem value="normal">Normal</SelectItem>
            <SelectItem value="large">Large</SelectItem>
            <SelectItem value="huge">Huge</SelectItem>
          </SelectContent>
        </Select>

        <div className="bg-border mx-1 h-6 w-px" />

        {/* Text Formatting */}
        <Button
          type="button"
          variant="ghost"
          size="icon"
          className={cn(
            'text-muted-foreground h-9 w-9',
            editorState?.isBold && 'bg-muted text-foreground'
          )}
          title="Bold"
          onClick={() => editor.chain().focus().toggleBold().run()}
          disabled={disabled}
        >
          <Bold className="h-4 w-4" />
        </Button>
        <Button
          type="button"
          variant="ghost"
          size="icon"
          className={cn(
            'text-muted-foreground h-9 w-9',
            editorState?.isItalic && 'bg-muted text-foreground'
          )}
          title="Italic"
          onClick={() => editor.chain().focus().toggleItalic().run()}
          disabled={disabled}
        >
          <Italic className="h-4 w-4" />
        </Button>
        <Button
          type="button"
          variant="ghost"
          size="icon"
          className={cn(
            'text-muted-foreground h-9 w-9',
            editorState?.isUnderline && 'bg-muted text-foreground'
          )}
          title="Underline"
          onClick={() => editor.chain().focus().toggleUnderline().run()}
          disabled={disabled}
        >
          <UnderlineIcon className="h-4 w-4" />
        </Button>
        <Popover>
          <PopoverTrigger asChild disabled={disabled}>
            <Button
              type="button"
              variant="ghost"
              size="icon"
              className={cn(
                'text-muted-foreground h-9 w-9',
                editorState?.color && 'bg-muted text-foreground'
              )}
              title="Text color"
            >
              <Type
                className="h-4 w-4"
                style={{
                  color: editorState?.color || 'currentColor',
                }}
              />
            </Button>
          </PopoverTrigger>
          <PopoverContent className="w-auto p-3" align="start">
            <div className="flex flex-col gap-2">
              <div className="text-muted-foreground text-xs font-medium">Color</div>
              <div className="grid grid-cols-5 gap-1">
                <button
                  type="button"
                  className="hover:bg-muted rounded-md p-1 transition-colors"
                  onClick={() => editor.chain().focus().unsetColor().run()}
                  title="Default"
                >
                  <div className="h-4 w-4 rounded-sm border bg-black" />
                </button>
                <button
                  type="button"
                  className="hover:bg-muted rounded-md p-1 transition-colors"
                  onClick={() => editor.chain().focus().setColor('#ef4444').run()}
                  title="Red"
                >
                  <div className="h-4 w-4 rounded-sm border bg-red-500" />
                </button>
                <button
                  type="button"
                  className="hover:bg-muted rounded-md p-1 transition-colors"
                  onClick={() => editor.chain().focus().setColor('#f97316').run()}
                  title="Orange"
                >
                  <div className="h-4 w-4 rounded-sm border bg-orange-500" />
                </button>
                <button
                  type="button"
                  className="hover:bg-muted rounded-md p-1 transition-colors"
                  onClick={() => editor.chain().focus().setColor('#eab308').run()}
                  title="Yellow"
                >
                  <div className="h-4 w-4 rounded-sm border bg-yellow-500" />
                </button>
                <button
                  type="button"
                  className="hover:bg-muted rounded-md p-1 transition-colors"
                  onClick={() => editor.chain().focus().setColor('#22c55e').run()}
                  title="Green"
                >
                  <div className="h-4 w-4 rounded-sm border bg-green-500" />
                </button>
                <button
                  type="button"
                  className="hover:bg-muted rounded-md p-1 transition-colors"
                  onClick={() => editor.chain().focus().setColor('#06b6d4').run()}
                  title="Cyan"
                >
                  <div className="h-4 w-4 rounded-sm border bg-cyan-500" />
                </button>
                <button
                  type="button"
                  className="hover:bg-muted rounded-md p-1 transition-colors"
                  onClick={() => editor.chain().focus().setColor('#3b82f6').run()}
                  title="Blue"
                >
                  <div className="h-4 w-4 rounded-sm border bg-blue-500" />
                </button>
                <button
                  type="button"
                  className="hover:bg-muted rounded-md p-1 transition-colors"
                  onClick={() => editor.chain().focus().setColor('#8b5cf6').run()}
                  title="Purple"
                >
                  <div className="h-4 w-4 rounded-sm border bg-violet-500" />
                </button>
                <button
                  type="button"
                  className="hover:bg-muted rounded-md p-1 transition-colors"
                  onClick={() => editor.chain().focus().setColor('#d946ef').run()}
                  title="Pink"
                >
                  <div className="h-4 w-4 rounded-sm border bg-fuchsia-500" />
                </button>
                <button
                  type="button"
                  className="hover:bg-muted rounded-md p-1 transition-colors"
                  onClick={() => editor.chain().focus().setColor('#64748b').run()}
                  title="Slate"
                >
                  <div className="h-4 w-4 rounded-sm border bg-slate-500" />
                </button>
              </div>
            </div>
          </PopoverContent>
        </Popover>

        <div className="bg-border mx-1 h-6 w-px" />

        <Button
          type="button"
          variant="ghost"
          size="icon"
          className={cn(
            'text-muted-foreground h-9 w-9',
            editorState?.isAlignLeft && 'bg-muted text-foreground'
          )}
          title="Align left"
          onClick={() => editor.chain().focus().setTextAlign('left').run()}
          disabled={disabled}
        >
          <AlignLeft className="h-4 w-4" />
        </Button>
        <Button
          type="button"
          variant="ghost"
          size="icon"
          className={cn(
            'text-muted-foreground h-9 w-9',
            editorState?.isAlignCenter && 'bg-muted text-foreground'
          )}
          title="Align center"
          onClick={() => editor.chain().focus().setTextAlign('center').run()}
          disabled={disabled}
        >
          <AlignCenter className="h-4 w-4" />
        </Button>
        <Button
          type="button"
          variant="ghost"
          size="icon"
          className={cn(
            'text-muted-foreground h-9 w-9',
            editorState?.isAlignRight && 'bg-muted text-foreground'
          )}
          title="Align right"
          onClick={() => editor.chain().focus().setTextAlign('right').run()}
          disabled={disabled}
        >
          <AlignRight className="h-4 w-4" />
        </Button>

        <div className="bg-border mx-1 h-6 w-px" />

        <Button
          type="button"
          variant="ghost"
          size="icon"
          className={cn(
            'text-muted-foreground h-9 w-9',
            editorState?.isOrderedList && 'bg-muted text-foreground'
          )}
          title="Numbered list"
          onClick={() => editor.chain().focus().toggleOrderedList().run()}
          disabled={disabled}
        >
          <ListOrdered className="h-4 w-4" />
        </Button>
        <Button
          type="button"
          variant="ghost"
          size="icon"
          className={cn(
            'text-muted-foreground h-9 w-9',
            editorState?.isBulletList && 'bg-muted text-foreground'
          )}
          title="Bulleted list"
          onClick={() => editor.chain().focus().toggleBulletList().run()}
          disabled={disabled}
        >
          <List className="h-4 w-4" />
        </Button>

        <div className="bg-border mx-1 h-6 w-px" />

        <Popover open={isLinkPopoverOpen} onOpenChange={setIsLinkPopoverOpen}>
          <PopoverTrigger asChild disabled={disabled}>
            <Button
              type="button"
              variant="ghost"
              size="icon"
              className={cn(
                'text-muted-foreground h-9 w-9',
                editorState?.isLink && 'bg-muted text-foreground'
              )}
              title="Insert link"
              onClick={openLinkPopover}
            >
              <Link2 className="h-4 w-4" />
            </Button>
          </PopoverTrigger>
          <PopoverContent className="w-80 p-3" align="start">
            <div className="flex flex-col gap-2">
              <div className="text-muted-foreground text-xs font-medium">Link</div>
              <div className="flex items-center gap-2">
                <Input
                  value={linkUrl}
                  onChange={(e) => setLinkUrl(e.target.value)}
                  onKeyDown={(e) => {
                    if (e.key === 'Enter') {
                      saveLink();
                    }
                  }}
                  placeholder="https://example.com"
                  className="h-8"
                />
                <Button onClick={saveLink} size="sm" className="h-8 px-3">
                  Save
                </Button>
              </div>
              {editorState?.isLink && (
                <Button
                  type="button"
                  onClick={removeLink}
                  variant="ghost"
                  size="sm"
                  className="text-destructive hover:text-destructive h-8 w-full justify-start px-2"
                >
                  <Trash2 className="mr-2 h-3 w-3" />
                  Remove link
                </Button>
              )}
            </div>
          </PopoverContent>
        </Popover>
        {onImageUpload && (
          <Button
            type="button"
            variant="ghost"
            size="icon"
            className="text-muted-foreground h-9 w-9"
            title={isUploading ? 'Uploading…' : 'Insert image'}
            onClick={() => fileInputRef.current?.click()}
            disabled={disabled || isUploading}
          >
            {isUploading ? (
              <div className="h-4 w-4 animate-spin rounded-full border-2 border-current border-t-transparent" />
            ) : (
              <ImageIcon className="h-4 w-4" />
            )}
          </Button>
        )}
        {onAttachClick && (
          <Button
            type="button"
            variant="ghost"
            size="icon"
            className="text-muted-foreground h-9 w-9"
            title="Attach files"
            onClick={onAttachClick}
            disabled={disabled}
          >
            <Paperclip className="h-4 w-4" />
          </Button>
        )}
      </div>
      {onDelete && (
        <Button
          type="button"
          variant="ghost"
          size="icon"
          className="text-muted-foreground hover:text-destructive h-9 w-9"
          title="Delete email"
          onClick={onDelete}
          disabled={deleteDisabled || disabled}
        >
          <Trash2 className="h-4 w-4" />
        </Button>
      )}
      <input
        type="file"
        ref={fileInputRef}
        style={{ display: 'none' }}
        accept="image/*"
        onChange={handleFileChange}
      />
    </div>
  );
};
