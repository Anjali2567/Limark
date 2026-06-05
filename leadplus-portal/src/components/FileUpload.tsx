import { ChangeEvent, DragEvent, useState } from 'react';
import { toast } from 'sonner';

import { Label } from '@/components/ui/label';

import { CloudUpload, Loader2 } from 'lucide-react';

const MAX_FILE_SIZE = 10 * 1024 * 1024;
const ACCEPTED_EXTENSIONS = '.pdf,.ppt,.pptx,.jpg,.jpeg,.png,.gif,.webp';

type FileUploadProps = {
  multiple?: boolean;
  onFilesSelected: (files: FileList | null) => void;
  isLoading?: boolean;
};

const FileUpload = ({ multiple = true, onFilesSelected, isLoading = false }: FileUploadProps) => {
  const [isDragging, setIsDragging] = useState<boolean>(false);

  const validateFiles = (files: FileList): FileList | null => {
    const validFiles: File[] = [];

    for (let i = 0; i < files.length; i++) {
      const file = files[i];

      if (file.size > MAX_FILE_SIZE) {
        toast.error(`"${file.name}" exceeds 10MB limit`);
        continue;
      }

      if (!ACCEPTED_EXTENSIONS.split(',').some((ext) => file.name.toLowerCase().endsWith(ext))) {
        toast.error(`"${file.name}" is not an accepted file type`);
        continue;
      }
      validFiles.push(file);
    }

    if (validFiles.length === 0) {
      return null;
    }

    const dataTransfer = new DataTransfer();
    validFiles.forEach((file) => dataTransfer.items.add(file));

    return dataTransfer.files;
  };

  const handleDragOver = (e: DragEvent) => {
    if (isLoading) return;
    e.preventDefault();
    e.stopPropagation();
    setIsDragging(true);
  };

  const handleDragLeave = (e: DragEvent) => {
    if (isLoading) return;
    e.preventDefault();
    e.stopPropagation();
    setIsDragging(false);
  };

  const handleDrop = (e: DragEvent) => {
    if (isLoading) return;
    e.preventDefault();
    e.stopPropagation();
    setIsDragging(false);

    if (e.dataTransfer.files && e.dataTransfer.files.length > 0) {
      const validatedFiles = validateFiles(e.dataTransfer.files);
      onFilesSelected(validatedFiles);
    }
  };

  const handleFileChange = (e: ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files.length > 0) {
      const validatedFiles = validateFiles(e.target.files);
      onFilesSelected(validatedFiles);
    }
  };

  return (
    <div className="flex w-full items-center justify-center">
      <Label
        onDragOver={handleDragOver}
        onDragLeave={handleDragLeave}
        onDrop={handleDrop}
        className={`flex h-48 w-full flex-col items-center justify-center rounded-lg border-2 border-dashed transition-colors ${
          isLoading ? 'cursor-not-allowed opacity-70' : 'cursor-pointer'
        } ${
          isDragging
            ? 'border-sky-500 bg-sky-500/10'
            : 'border-input bg-secondary/50 hover:bg-secondary'
        }`}
      >
        <div className="flex flex-col items-center justify-center pt-5 pb-6">
          {isLoading ? (
            <>
              <Loader2 className="mb-3 h-12 w-12 animate-spin text-sky-500" />
              <p className="text-muted-foreground mb-2 text-sm font-medium">Uploading...</p>
              <p className="text-muted-foreground mb-4 text-xs">
                Please wait while the file is being processed
              </p>
            </>
          ) : (
            <>
              <CloudUpload
                className={`mb-3 h-12 w-12 transition-colors ${
                  isDragging ? 'text-sky-500' : 'text-warning'
                }`}
              />
              <p className="text-muted-foreground mb-2 text-sm font-medium">Drag and drop</p>
              <p className="text-muted-foreground mb-4 text-xs">
                or choose {multiple ? 'files' : 'a file'} from your computer
              </p>
              <div className="bg-secondary text-foreground hover:bg-secondary/80 rounded px-4 py-2 text-sm font-medium transition-colors">
                {multiple ? 'Choose Files' : 'Choose File'}
              </div>
            </>
          )}
        </div>
        {!isLoading && (
          <input
            type="file"
            multiple={multiple}
            accept={ACCEPTED_EXTENSIONS}
            className="hidden"
            onChange={handleFileChange}
          />
        )}
      </Label>
    </div>
  );
};

export { FileUpload };
