'use client';

import { useRef, useState, ChangeEvent, ReactNode } from 'react';
import Image from 'next/image';

import { Button } from '@/components/ui/button';
import { Label } from '@/components/ui/label';

import { Camera, Upload, X } from 'lucide-react';
import { toast } from 'sonner';

type ImageUploadFieldProps = {
  label?: string;
  required?: boolean;
  value?: string | null;
  onChange?: (file: File | null) => void;
  maxSizeMB?: number;
  size?: number;
  accept?: string;
  className?: string;
  variant?: 'card' | 'avatar';
  fallbackIcon?: ReactNode;
};

export const ImageUploadField = ({
  label,
  required = false,
  value,
  onChange,
  maxSizeMB = 5,
  size = 256,
  accept = 'image/*',
  className,
  variant = 'card',
  fallbackIcon,
}: ImageUploadFieldProps) => {
  const fileInputRef = useRef<HTMLInputElement | null>(null);
  const [localPreview, setLocalPreview] = useState<string | null>(null);

  const displayPreview = localPreview || value || null;

  const handleFileChange = (e: ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    if (file.size > maxSizeMB * 1024 * 1024) {
      toast.error(`File must be smaller than ${maxSizeMB}MB`);
      return;
    }

    const imageUrl = URL.createObjectURL(file);
    setLocalPreview(imageUrl);
    onChange?.(file);
  };

  const handleRemove = () => {
    setLocalPreview(null);
    onChange?.(null);
    if (fileInputRef.current) fileInputRef.current.value = '';
  };

  if (variant === 'avatar') {
    return (
      <div className={`relative shrink-0 ${className ?? ''}`}>
        <div
          className="bg-muted relative flex items-center justify-center overflow-hidden rounded-full"
          style={{ width: size, height: size }}
        >
          {displayPreview ? (
            <Image
              src={displayPreview}
              alt="Preview"
              width={size}
              height={size}
              className="h-full w-full object-cover"
            />
          ) : (
            (fallbackIcon ?? null)
          )}
        </div>
        <Button
          type="button"
          variant="none"
          onClick={() => fileInputRef.current?.click()}
          className="bg-primary text-primary-foreground hover:bg-primary/90 absolute right-0 bottom-0 rounded-full p-2 shadow-lg"
        >
          <Camera className="h-4 w-4" />
        </Button>
        <input
          ref={fileInputRef}
          type="file"
          accept={accept}
          className="hidden"
          onChange={handleFileChange}
        />
      </div>
    );
  }

  return (
    <div className={`space-y-2 ${className ?? ''}`}>
      {label && (
        <Label>
          {label} {required && <span>*</span>}
        </Label>
      )}

      {displayPreview ? (
        <div
          className="relative mx-auto overflow-hidden rounded-lg border"
          style={{ width: size, height: size }}
        >
          <Image
            src={displayPreview}
            alt="Preview"
            width={size}
            height={size}
            className="h-full w-full object-cover"
          />
          <Button
            type="button"
            variant="destructive"
            size="icon"
            className="absolute top-2 right-2"
            onClick={handleRemove}
          >
            <X className="h-4 w-4" />
          </Button>
        </div>
      ) : (
        <div
          className="flex cursor-pointer flex-col items-center justify-center rounded-lg border-2 border-dashed"
          style={{ width: size, height: size }}
          onClick={() => fileInputRef.current?.click()}
        >
          <Upload className="text-muted-foreground mb-2 h-8 w-8" />
          <p className="text-muted-foreground text-sm">Click to upload</p>
        </div>
      )}
      <input
        ref={fileInputRef}
        type="file"
        accept={accept}
        className="hidden"
        onChange={handleFileChange}
      />
    </div>
  );
};
