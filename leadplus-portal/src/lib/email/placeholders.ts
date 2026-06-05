export const EMAIL_PLACEHOLDERS = [
  { label: '[First Name]', value: '[First Name]' },
  { label: '[Company Name]', value: '[Company Name]' },
  { label: '[Industry]', value: '[Industry]' },
  { label: '[BD Name]', value: '[BD Name]' },
  { label: '[ISR Name]', value: '[ISR Name]' },
  { label: '[Sender Name]', value: '[Sender Name]' },
  { label: '[Sender Number]', value: '[Sender Number]' },
  { label: '[BD Phone Number]', value: '[BD Phone Number]' },
  { label: '[ISR Phone Number]', value: '[ISR Phone Number]' },
] as const;

export const getEmailPlaceholders = () => EMAIL_PLACEHOLDERS;
