import { ReactNode } from "react";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";

type FormCardProps = {
  icon: ReactNode;
  title: string;
  description?: string;
  children: ReactNode;
};

const FormCard = ({ icon, title, description, children }: FormCardProps) => {
  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          {icon}
          {title}
        </CardTitle>
        {description && <CardDescription>{description}</CardDescription>}
      </CardHeader>

      <CardContent className="space-y-4">{children}</CardContent>
    </Card>
  );
};

export { FormCard };
