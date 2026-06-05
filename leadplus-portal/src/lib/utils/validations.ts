import { z } from 'zod';

export const numberValidation = ({
  min = 1,
  max,
  required = true,
  fieldName = 'Value',
}: {
  min?: number;
  max?: number;
  required?: boolean;
  fieldName?: string;
} = {}) => {
  let schema = z.number({
    message: 'Invalid input. Please enter a valid number',
  });

  if (min !== undefined) {
    schema = schema.min(min, `${fieldName} must be at least ${min}`);
  }

  if (max !== undefined) {
    schema = schema.max(max, `${fieldName} must be at most ${max}`);
  }

  if (!required) {
    return schema.optional();
  }

  return schema;
};

export const stringValidation = ({
  fieldName = 'This field',
  min = 2,
}: {
  fieldName?: string;
  min?: number;
}) => {
  return z
    .string()
    .min(1, { message: `${fieldName} is required` })
    .min(min, { message: `${fieldName} must be at least ${min} characters` })
    .transform((val) => val.trim())
    .refine((val) => val.length > 0, {
      message: `${fieldName} cannot be empty or just spaces`,
    });
};

export const passwordValidation = () => {
  return z
    .string()
    .min(8, 'Password must be at least 8 characters')
    .regex(
      /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[!@#$%^&*(),.?":{}|<>]).{8,}$/,
      'Password must include uppercase, lowercase, number, and special character.'
    );
};

export const emailValidation = () => {
  return z
    .string()
    .min(1, 'Email is required')
    .pipe(z.email({ message: 'Invalid email address' }));
};

export const validateEmailList = (val: string | undefined, required: boolean = false): boolean => {
  if (!val || val.trim() === '') return !required;
  const emails = val
    .split(',')
    .map((e) => e.trim())
    .filter((e) => e.length > 0);
  if (required && emails.length === 0) return false;
  return emails.every((email) => z.string().email().safeParse(email).success);
};

const stripTime = (date: Date): Date => {
  const d = new Date(date);
  d.setHours(0, 0, 0, 0);
  return d;
};

export const dateValidation = ({
  fieldName = 'Date',
  required = true,
  minDate,
  maxDate,
  futureOnly = false,
  pastOnly = false,
}: {
  fieldName?: string;
  required?: boolean;
  minDate?: Date;
  maxDate?: Date;
  futureOnly?: boolean;
  pastOnly?: boolean;
} = {}) => {
  let schema = z.date({
    message: `${fieldName} is required`,
  });

  if (futureOnly) {
    schema = schema.refine(
      (date) => {
        const inputDate = stripTime(date);
        const today = stripTime(new Date());
        return inputDate > today;
      },
      { message: `${fieldName} must be in the future` }
    );
  }

  if (pastOnly) {
    schema = schema.refine(
      (date) => {
        const inputDate = stripTime(date);
        const today = stripTime(new Date());
        return inputDate < today;
      },
      { message: `${fieldName} must be in the past` }
    );
  }

  if (minDate) {
    schema = schema.refine(
      (date) => {
        const inputDate = stripTime(date);
        const min = stripTime(minDate);
        return inputDate >= min;
      },
      { message: `${fieldName} must be on or after ${minDate.toLocaleDateString()}` }
    );
  }

  if (maxDate) {
    schema = schema.refine(
      (date) => {
        const inputDate = stripTime(date);
        const max = stripTime(maxDate);
        return inputDate <= max;
      },
      { message: `${fieldName} must be on or before ${maxDate.toLocaleDateString()}` }
    );
  }

  if (!required) {
    return schema.optional();
  }

  return schema;
};
