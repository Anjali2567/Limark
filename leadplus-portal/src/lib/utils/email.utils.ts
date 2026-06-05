export const plainTextEmailToHtml = (text: string): string => {
  return text
    .split('\n\n')
    .map((para) => `<p>${para.replace(/\n/g, '<br>')}</p>`)
    .join('');
};

export const stripThreadedReplies = (text: string): string => {
  if (!text) return '';

  const lines = text.split(/\r?\n/);
  const cleanedLines: string[] = [];

  for (let i = 0; i < lines.length; i++) {
    const line = lines[i].trim();

    if (line.match(/^On\s.*wrote:$/i) || line.match(/^At\s.*wrote:$/i)) {
      break;
    }

    if (line.startsWith('>')) {
      continue;
    }

    cleanedLines.push(lines[i]);
  }

  return cleanedLines.join('\n').trim();
};
