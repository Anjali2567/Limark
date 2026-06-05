'use client';

import { useRouter, useSearchParams } from 'next/navigation';
import { useCallback, useEffect, useRef, useState } from 'react';
import { useForm } from 'react-hook-form';
import { toast } from 'sonner';
import { Editor } from '@tiptap/react';

import { Breadcrumbs } from '@/components/Breadcrumbs';
import { Page } from '@/components/Page';
import { Button } from '@/components/ui/button';
import { Form } from '@/components/ui/form';
import { FormSelect } from '@/components/ui/form-select';
import { RichTextEditorToolbar } from '@/components/ui/rich-text-editor-toolbar';
import { RichTextEditor } from '@/components/ui/rich-text-editor';

import { appRoutes } from '@/config/routes';
import { useGetAgreementById, useUpdateAgreement } from '@/hooks/useAgreement';
import { formatString } from '@/lib/utils/formatter';

import { Loader2, Upload } from 'lucide-react';

const options = [
  { value: 'NAME', label: 'NAME' },
  { value: 'EMAIL', label: 'EMAIL' },
  { value: 'COMPANY_NAME', label: 'COMPANY NAME' },
  { value: 'PHONE_NUMBER', label: 'PHONE NUMBER' },
  { value: 'WEBSITE', label: 'WEBSITE' },
  { value: 'DATE_TODAY', label: 'DATE' },
  { value: 'ADDRESS_STREET', label: 'STREET' },
  { value: 'ADDRESS_CITY', label: 'CITY' },
  { value: 'ADDRESS_STATE', label: 'STATE' },
  { value: 'ADDRESS_COUNTRY', label: 'COUNTRY' },
];

type FormValues = {
  content: string;
};

const DocumentEditor = () => {
  const router = useRouter();
  const searchParams = useSearchParams();
  const id = searchParams.get('id') || '';

  const editorRef = useRef<Editor | null>(null);
  const [toolbarEditor, setToolbarEditor] = useState<Editor | null>(null);
  const [selectedField, setSelectedField] = useState('');

  const { mutate: updateAgreement } = useUpdateAgreement();
  const { data, isLoading, isError } = useGetAgreementById(id);

  const form = useForm<FormValues>({
    defaultValues: {
      content: '',
    },
  });

  const { handleSubmit, watch, setValue } = form;

  useEffect(() => {
    if (data?.content) {
      setValue('content', data.content);
    }
  }, [data, setValue]);

  useEffect(() => {
    if (isError) {
      toast.error('Failed to load document details');
      router.push(appRoutes.admin.documents.root);
    }
  }, [isError, router]);

  const onSubmit = useCallback(
    (values: FormValues) => {
      if (!data?.agreementType) return;
      updateAgreement(
        {
          agreementType: data.agreementType,
          content: values.content,
        },
        {
          onSuccess: () => {
            toast.success('Document updated successfully');
            router.push(appRoutes.admin.documents.root);
          },
          onError: () => {
            toast.error('Failed to update document');
          },
        }
      );
    },
    [data, router, updateAgreement]
  );

  const handleInsertField = useCallback(() => {
    if (!selectedField || !editorRef.current) return;
    editorRef.current.chain().focus().insertContent(`{{${selectedField}}}`).run();
    setSelectedField('');
  }, [selectedField]);

  if (isLoading) {
    return (
      <div className="flex h-full w-full items-center justify-center">
        <Loader2 className="h-10 w-10 animate-spin text-sky-500" />
      </div>
    );
  }

  return (
    <Page className="flex-col p-8">
      <Breadcrumbs
        items={[
          { label: 'Documents', href: appRoutes.admin.documents.root },
          { label: formatString(data?.agreementType) || '' },
        ]}
      />
      <Form {...form}>
        <form onSubmit={handleSubmit(onSubmit)} className="flex flex-1 flex-col overflow-hidden">
          <div className="mb-4 flex items-center justify-between">
            <h1 className="text-foreground text-2xl font-bold">Edit Document</h1>
            <Button type="submit" className="bg-primary hover:bg-primary/90">
              <Upload className="mr-2 h-4 w-4" />
              Save Changes
            </Button>
          </div>
          <div className="border-border flex flex-1 overflow-hidden border-t">
            <div className="flex flex-1 flex-col overflow-hidden">
              <RichTextEditorToolbar
                editor={toolbarEditor}
                className="shrink-0 border-t-0 border-b bg-white"
                hideSaveButton
              />
              <div className="flex-1 overflow-hidden">
                <RichTextEditor
                  placeholder="Enter document information"
                  value={watch('content')}
                  onChange={(val: string) => setValue('content', val, { shouldDirty: true })}
                  showToolbar={false}
                  editorRef={editorRef}
                  onEditorReady={setToolbarEditor}
                  editorClassName="border border-t-0 border-r-0 min-h-0 h-full bg-white! rounded-none!"
                  className="h-full"
                />
              </div>
            </div>
            <div className="border-border w-80 shrink-0 space-y-6 overflow-auto border-l p-6">
              <h3 className="text-foreground mb-4 text-lg font-bold">Insert Field</h3>
              <FormSelect
                value={selectedField}
                onValueChange={setSelectedField}
                options={options}
                placeholder="Select a field to insert"
                className="bg-white"
              />
              <Button
                type="button"
                onClick={handleInsertField}
                variant="outline"
                className="w-full"
                disabled={!selectedField}
              >
                Insert Field
              </Button>
            </div>
          </div>
        </form>
      </Form>
    </Page>
  );
};

export { DocumentEditor };
