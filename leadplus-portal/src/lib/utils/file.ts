export const formatFileSize = (size: number): string => {
  const units = ["B", "KB", "MB"];
  let index = 0;

  while (size >= 1024 && index < units.length - 1) {
    size /= 1024;
    index++;
  }

  const formattedSize =
    index === 0
      ? size.toString()
      : size.toLocaleString(undefined, {
          minimumFractionDigits: 2,
          maximumFractionDigits: 2,
        });

  return `${formattedSize} ${units[index]}`;
};
