import { validateEmailList } from '@/lib/utils/validations';
import { z } from 'zod';

export enum WizardStep {
  COMPOSE = 'compose',
  CONTACTS = 'contacts',
  REVIEW = 'review',
}

export const composeSchema = z.object({
  name: z.string().min(1, 'Announcement name is required'),
  subject: z.string().min(1, 'Subject is required'),
  body: z
    .string()
    .min(1, 'Email body is required')
    .refine(
      (val) => val.replace(/<[^>]*>/g, '').trim().length > 0 || /<img\b/i.test(val),
      { message: 'Email body cannot be empty' }
    ),
  cc: z
    .string()
    .optional()
    .refine((val) => validateEmailList(val, false), {
      message: 'Emails must be separated by a comma',
    }),
  bcc: z
    .string()
    .optional()
    .refine((val) => validateEmailList(val, false), {
      message: 'Emails must be separated by a comma',
    }),
  attachmentIds: z.array(z.string()).optional(),
});

export type ComposeFormData = z.infer<typeof composeSchema>;
