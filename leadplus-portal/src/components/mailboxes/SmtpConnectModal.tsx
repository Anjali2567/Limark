import { AxiosError } from "axios";
import { useState } from "react";
import { toast } from "sonner";

import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { useAuth } from "@/context/AuthContext";
import { useConnectSmtp } from "@/hooks/useMailbox";

interface SmtpConnectModalProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

export function SmtpConnectModal({
  open,
  onOpenChange,
}: SmtpConnectModalProps) {
  const { authenticatedUserDetails } = useAuth();
  const workspaceId = authenticatedUserDetails?.workspaceId || "";
  const tenantId = authenticatedUserDetails?.tenantId || "";

  const [email, setEmail] = useState("");
  const [appPassword, setAppPassword] = useState("");

  const { mutate: connectSmtp, isPending } = useConnectSmtp();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!email || !appPassword) return;
    connectSmtp(
      { email, appPassword, workspaceId, tenantId },
      {
        onSuccess: () => {
          onOpenChange(false);
          toast.success("SMTP connected successfully");
        },
        onError: (error: AxiosError) => {
          toast.error(error?.message || "SMTP connection failed");
        },
      }
    );
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[425px]">
        <DialogHeader>
          <DialogTitle>Connect SMTP Account</DialogTitle>
        </DialogHeader>
        <form onSubmit={handleSubmit} className="grid gap-4 py-4">
          <div className="grid grid-cols-4 items-center gap-4">
            <Label htmlFor="email" className="text-right">
              Email
            </Label>
            <Input
              id="email"
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="col-span-3"
              placeholder="name@example.com"
              required
            />
          </div>
          <div className="grid grid-cols-4 items-center gap-4">
            <Label htmlFor="app-password" className="text-right">
              Password
            </Label>
            <Input
              id="app-password"
              type="password"
              value={appPassword}
              onChange={(e) => setAppPassword(e.target.value)}
              className="col-span-3"
              required
            />
          </div>
          <DialogFooter>
            <Button type="submit" disabled={isPending}>
              {isPending ? "Connecting..." : "Connect"}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
