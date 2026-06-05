import { AxiosError } from "axios";
import { useForm } from "react-hook-form";
import { toast } from "sonner";
import { z } from "zod";

import { zodResolver } from "@hookform/resolvers/zod";

import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { useAuth } from "@/context/AuthContext";
import { useConnectAwsSes } from "@/hooks/useMailbox";
import { FormInput } from "../ui/form-input";

const sesConnectSchema = z.object({
  email: z.email(),
});

type SesConnectFormValues = z.infer<typeof sesConnectSchema>;

interface SesConnectModalProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

export const SesConnectModal = ({
  open,
  onOpenChange,
}: SesConnectModalProps) => {
  const { authenticatedUserDetails } = useAuth();
  const workspaceId = authenticatedUserDetails?.workspaceId || "";
  const tenantId = authenticatedUserDetails?.tenantId || "";

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<SesConnectFormValues>({
    resolver: zodResolver(sesConnectSchema),
    defaultValues: {
      email: "",
    },
    mode: "onSubmit",
  });

  const { mutate: connectAwsSes, isPending } = useConnectAwsSes({
    workspaceId,
    tenantId,
  });

  const onSubmit = ({ email }: SesConnectFormValues) => {
    connectAwsSes(email, {
      onSuccess: () => {
        reset();
        onOpenChange(false);
        toast.success("Amazon SES email verified successfully");
      },
      onError: (error: AxiosError) => {
        toast.error(error?.message || "Failed to verify Amazon SES email");
      },
    });
  };

  const isLoading = isPending || isSubmitting;

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[500px]">
        <DialogHeader>
          <DialogTitle>Verify your Amazon SES email</DialogTitle>
          <DialogDescription>
            Enter the email address you want to use with Amazon SES and verify
            it.
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4 py-4">
          <FormInput
            id="ses-email"
            type="email"
            placeholder="your.email@company.com"
            disabled={isLoading}
            className="bg-input-background"
            label="Email Address"
            error={!!errors.email}
            errorMessage={errors.email?.message}
            required
            {...register("email")}
          />
          <DialogFooter>
            <Button
              type="button"
              variant="outline"
              onClick={() => onOpenChange(false)}
              disabled={isLoading}
            >
              Cancel
            </Button>
            <Button
              type="submit"
              className="bg-sky-500 hover:bg-sky-600 text-white"
              disabled={isLoading}
            >
              {isLoading ? "Sending..." : "Send Verification Email"}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
};
