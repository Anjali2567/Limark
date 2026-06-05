import {
  ImageIcon,
  FileText,
  FileSpreadsheet,
  Presentation,
  FileArchive,
  File,
} from "lucide-react";

type FileRenderProps = {
  fileName: string;
  fileUrl?: string;
  fileExtension?: string;
};

const getFileIcon = (extension?: string, className: string = "w-4 h-4") => {
  const fmt = extension?.toLowerCase();

  if (fmt && ["jpg", "jpeg", "png", "gif", "svg", "webp"].includes(fmt)) {
    return <ImageIcon className={`${className} text-purple-600`} />;
  }
  if (fmt && ["pdf"].includes(fmt)) {
    return <FileText className={`${className} text-red-500`} />;
  }
  if (fmt && ["doc", "docx"].includes(fmt)) {
    return <FileText className={`${className} text-blue-600`} />;
  }
  if (fmt && ["xls", "xlsx", "csv"].includes(fmt)) {
    return <FileSpreadsheet className={`${className} text-green-600`} />;
  }
  if (fmt && ["ppt", "pptx"].includes(fmt)) {
    return <Presentation className={`${className} text-orange-500`} />;
  }
  if (fmt && ["zip", "rar", "7z", "tar"].includes(fmt)) {
    return <FileArchive className={`${className} text-yellow-600`} />;
  }

  return <File className={className} />;
};

const FileRender = ({ fileName, fileUrl, fileExtension }: FileRenderProps) => {
  const baseUrl = process.env.NEXT_PUBLIC_BASE_URL;
  const parts = fileName?.split(".") || [];

  const name = fileExtension
    ? fileName
    : parts.length > 1
    ? parts.slice(0, -1).join(".")
    : parts.join(".");

  const extension =
    fileExtension?.toLowerCase() ||
    (parts.length > 1 ? parts[parts.length - 1]?.toLowerCase() : undefined);

  const icon = getFileIcon(extension);

  return (
    <div className="min-w-0 flex-1 overflow-hidden" title={fileName}>
      {fileUrl ? (
        <a
          href={`${baseUrl}${fileUrl}`}
          target="_blank"
          rel="noopener noreferrer"
          className="flex items-center gap-2 cursor-pointer w-full group"
        >
          <div className="p-2 bg-secondary rounded border border-border">
            {icon}
          </div>
          <div className="flex items-center min-w-0 max-w-full overflow-hidden font-semibold">
            <span className="truncate shrink min-w-0">{name}</span>
          </div>
        </a>
      ) : (
        <span className="flex items-center gap-2 cursor-pointer w-full">
          <div className="p-2 bg-secondary rounded border border-border">
            {icon}
          </div>

          <div className="flex items-center min-w-0 max-w-full overflow-hidden font-semibold">
            <span className="truncate shrink min-w-0">{name}</span>
          </div>
        </span>
      )}
    </div>
  );
};

export { FileRender };
