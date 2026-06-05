"use client";

import { AxiosError } from "axios";
import { useEffect, useMemo } from "react";
import { useForm } from "react-hook-form";
import { toast } from "sonner";

import { Button } from "@/components/ui/button";
import { Textarea } from "@/components/ui/textarea";
import { PromptSpecificationType } from "@/constants/prompt-specification.constants";
import {
  useGetPromptSpecification,
  useUpdatePromptSpecification,
} from "@/hooks/usePromptSpecification";

type PromptTabFormProps = {
  label: string;
  configType: PromptSpecificationType;
  setIsBusy: (value: boolean) => void;
};

type FormValues = {
  promptTemplate: string;
};

const PromptTabForm = ({
  label,
  configType,
  setIsBusy,
}: PromptTabFormProps) => {
  const { mutate: updatePromptSpecification, isPending } =
    useUpdatePromptSpecification();
  const { data: promptSpecification, isLoading } = useGetPromptSpecification({
    type: configType,
  });

  const defaultValues = useMemo<FormValues>(
    () => ({
      promptTemplate: promptSpecification?.promptTemplate ?? "",
    }),
    [promptSpecification?.promptTemplate]
  );

  const {
    register,
    handleSubmit,
    reset,
    formState: { isDirty, isSubmitting },
  } = useForm<FormValues>({ defaultValues });

  useEffect(() => {
    if (promptSpecification?.promptTemplate) {
      reset({ promptTemplate: promptSpecification.promptTemplate });
    }
  }, [promptSpecification?.promptTemplate, reset]);

  const onSubmit = (values: FormValues) => {
    updatePromptSpecification(
      {
        type: configType,
        promptTemplate: values.promptTemplate,
      },
      {
        onSuccess: () => {
          toast.success("Prompt template updated successfully.");
        },
        onError: (error: AxiosError) => {
          toast.error(error.message ?? "Failed to update prompt template.");
        },
      }
    );
  };

  useEffect(() => {
    setIsBusy(isLoading || isPending);
  }, [isLoading, isPending, setIsBusy]);

  if (!promptSpecification) {
    return <p className="text-sm text-muted-foreground">Loading prompt…</p>;
  }

  return (
    <div className="relative">
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
        <div className="flex items-center justify-between">
          <h3 className="font-medium text-primary">
            {`Configure ${label} Prompt`}
          </h3>

          <Button type="submit" disabled={!isDirty || isSubmitting}>
            {isSubmitting ? "Saving..." : "Save"}
          </Button>
        </div>

        <Textarea
          {...register("promptTemplate", { required: true })}
          className="min-h-125"
          placeholder="Enter prompt template..."
        />
      </form>
    </div>
  );
};

export { PromptTabForm };
