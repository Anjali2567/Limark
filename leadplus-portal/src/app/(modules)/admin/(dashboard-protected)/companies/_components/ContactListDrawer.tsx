import { useMemo, useState } from "react";

import { SearchBar } from "@/components/SearchBar";
import { Badge } from "@/components/ui/badge";
import { Card } from "@/components/ui/card";
import {
  Sheet,
  SheetContent,
  SheetDescription,
  SheetHeader,
  SheetTitle,
} from "@/components/ui/sheet";
import { useGetLeadContactsByCompanyId } from "@/hooks/useLeads";
import { formatName } from "@/lib/utils/formatter";
import { Leads } from "@/types/leads.types";

import { Loader2, Mail, Phone } from "lucide-react";

type ContactListDrawerProps = {
  isCompanyContactsDrawerOpen: boolean;
  setIsCompanyContactsDrawerOpen: (open: boolean) => void;
  selectedCompany?: Leads | null;
};

const ContactListDrawer = ({
  isCompanyContactsDrawerOpen,
  setIsCompanyContactsDrawerOpen,
  selectedCompany,
}: ContactListDrawerProps) => {
  const [search, setSearchQuery] = useState<string>("");

  const handleSearchChange = (searchValue: string) => {
    setSearchQuery(searchValue);
  };
  const { data: companyContacts = [], isLoading } =
    useGetLeadContactsByCompanyId(selectedCompany?.id || "");

  const filteredCompanyContacts = useMemo(() => {
    const trimmedSearch = search.trim();
    if (!trimmedSearch) return companyContacts;

    return companyContacts.filter((contact) => {
      return contact.fullName
        .toLowerCase()
        .includes(trimmedSearch.toLowerCase());
    });
  }, [companyContacts, search]);

  return (
    <Sheet
      open={isCompanyContactsDrawerOpen}
      onOpenChange={setIsCompanyContactsDrawerOpen}
    >
      <SheetContent
        className="w-full sm:max-w-2xl flex flex-col gap-0 px-6"
        side="right"
      >
        <SheetHeader className="mb-4 shrink-0">
          <div className="flex items-center justify-between">
            <div>
              <SheetTitle className="text-2xl">
                {selectedCompany?.name}
              </SheetTitle>
              <SheetDescription className="mt-1">
                {selectedCompany?.industry} • {selectedCompany?.hqCity},{" "}
                {selectedCompany?.hqState}
              </SheetDescription>
            </div>
            <Badge variant="outline" className="text-sm">
              {selectedCompany?.contactCount} contact
              {selectedCompany?.contactCount !== 1 ? "s" : ""}
            </Badge>
          </div>
        </SheetHeader>

        <div className="mb-4 relative">
          <SearchBar
            onChange={handleSearchChange}
            placeholder="Search contacts..."
          />
        </div>

        <div className="flex-1 overflow-y-auto -mx-6 px-6 mb-4">
          <div className="space-y-3">
            {isLoading ? (
              <div className="flex justify-center items-center py-8">
                <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
              </div>
            ) : filteredCompanyContacts.length === 0 ? (
              <div className="text-center py-8 text-muted-foreground">
                No contacts found
              </div>
            ) : (
              filteredCompanyContacts.map((contact) => (
                <Card key={contact.id} className="p-4">
                  <div>
                    <div className="mb-2">
                      <p className="font-semibold text-foreground">
                        {formatName({
                          firstName: contact.firstName,
                          lastName: contact.lastName,
                        })}
                      </p>
                      <p className="text-sm text-muted-foreground">
                        {contact.title}
                      </p>
                    </div>
                    <div className="space-y-1.5">
                      {contact.email && (
                        <div className="flex items-center gap-2 text-sm text-muted-foreground">
                          <Mail className="h-3.5 w-3.5 shrink-0" />
                          <span className="truncate">{contact.email}</span>
                        </div>
                      )}
                      {contact.phoneE164 && (
                        <div className="flex items-center gap-2 text-sm text-muted-foreground">
                          <Phone className="h-3.5 w-3.5 shrink-0" />
                          <span>{contact.phoneE164}</span>
                        </div>
                      )}
                    </div>
                  </div>
                </Card>
              ))
            )}
          </div>
        </div>
      </SheetContent>
    </Sheet>
  );
};

export { ContactListDrawer };
