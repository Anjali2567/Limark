import {
  DropdownMenuContent,
  DropdownMenuItem,
} from "@/components/ui/dropdown-menu";
import { Edit2, RefreshCw, Trash2 } from "lucide-react";

type ActionsProps = {
  handleRenameResource?: () => void;
  handleReplaceResource?: () => void;
  handleDeleteResource?: () => void;
};
const Actions = ({
  handleRenameResource,
  handleReplaceResource,
  handleDeleteResource,
}: ActionsProps) => {
  return (
    <DropdownMenuContent align="end">
      <DropdownMenuItem
        onClick={(e) => {
          e.stopPropagation();
          handleRenameResource?.();
        }}
      >
        <Edit2 className="mr-2 h-4 w-4" /> Rename
      </DropdownMenuItem>
      <DropdownMenuItem
        onClick={(e) => {
          e.stopPropagation();
          handleReplaceResource?.();
        }}
      >
        <RefreshCw className="mr-2 h-4 w-4" /> Replace
      </DropdownMenuItem>
      <DropdownMenuItem
        className="text-destructive focus:text-destructive"
        onClick={(e) => {
          e.stopPropagation();
          handleDeleteResource?.();
        }}
      >
        <Trash2 className="mr-2 h-4 w-4" /> Delete
      </DropdownMenuItem>
    </DropdownMenuContent>
  );
};

export { Actions };
