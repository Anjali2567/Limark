'use client';

import { useForm } from 'react-hook-form';
import { toast } from 'sonner';
import { z } from 'zod';

import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from '@/components/ui/form';
import { Input } from '@/components/ui/input';
import { useUpdateVendorDetails } from '@/hooks/useVendor';
import { Vendor } from '@/types/vendor.types';
import { zodResolver } from '@hookform/resolvers/zod';
import { buildVendorPayload } from '../utils/buildVendorPayload';

import { Facebook, Instagram, Linkedin, Twitter, Youtube } from 'lucide-react';

const schema = z.object({
  linkedin: z.string().optional(),
  twitter: z.string().optional(),
  facebook: z.string().optional(),
  instagram: z.string().optional(),
  youtube: z.string().optional(),
});

type FormValues = z.infer<typeof schema>;

type Props = { vendor?: Vendor; formId: string };

const SOCIAL_FIELDS = [
  {
    name: 'linkedin' as const,
    label: 'LinkedIn',
    icon: Linkedin,
    color: 'text-blue-700',
    placeholder: 'https://linkedin.com/company/yourcompany',
  },
  {
    name: 'twitter' as const,
    label: 'X (Twitter)',
    icon: Twitter,
    color: 'text-sky-500',
    placeholder: 'https://twitter.com/yourhandle',
  },
  {
    name: 'facebook' as const,
    label: 'Facebook',
    icon: Facebook,
    color: 'text-blue-600',
    placeholder: 'https://facebook.com/yourpage',
  },
  {
    name: 'instagram' as const,
    label: 'Instagram',
    icon: Instagram,
    color: 'text-pink-500',
    placeholder: 'https://instagram.com/yourprofile',
  },
  {
    name: 'youtube' as const,
    label: 'YouTube',
    icon: Youtube,
    color: 'text-red-600',
    placeholder: 'https://youtube.com/@yourchannel',
  },
];

export const SocialMediaSection = ({ vendor, formId }: Props) => {
  const { mutate } = useUpdateVendorDetails();

  const form = useForm<FormValues>({
    resolver: zodResolver(schema),
    values: {
      linkedin: vendor?.socialMedia?.linkedin || '',
      twitter: vendor?.socialMedia?.twitter || '',
      facebook: vendor?.socialMedia?.facebook || '',
      instagram: vendor?.socialMedia?.instagram || '',
      youtube: vendor?.socialMedia?.youtube || '',
    },
  });

  const onSubmit = (data: FormValues) => {
    mutate(buildVendorPayload(vendor, { socialMedia: data }), {
      onSuccess: () => toast.success('Social media saved'),
      onError: () => toast.error('Failed to save'),
    });
  };

  return (
    <Form {...form}>
      <form id={formId} onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
        {SOCIAL_FIELDS.map(({ name, label, icon: Icon, color, placeholder }) => (
          <FormField
            key={name}
            control={form.control}
            name={name}
            render={({ field }) => (
              <FormItem>
                <FormLabel>{label}</FormLabel>
                <FormControl>
                  <div className="relative">
                    <Icon className={`absolute top-1/2 left-3 h-4 w-4 -translate-y-1/2 ${color}`} />
                    <Input placeholder={placeholder} className="pl-9" {...field} />
                  </div>
                </FormControl>
                <FormMessage />
              </FormItem>
            )}
          />
        ))}
      </form>
    </Form>
  );
};
