"use client";

import { AnimatePresence, motion } from "motion/react";
import { JSX, useMemo } from "react";

import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { EmailDeliveryStatus } from "@/constants/Campaign";
import useToggle from "@/hooks/useToggle";
import { formatName } from "@/lib/utils/formatter";
import { cn } from "@/lib/utils/helpers";
import { CampaignCompanyResponse } from "@/types/campaign.types";

import {
  Briefcase,
  Building,
  CheckCircle2,
  ChevronDown,
  ChevronUp,
  Mail,
  MapPin,
  MessageSquare,
  Phone,
  User,
  Users,
} from "lucide-react";

type CompaniesContactCardProps = {
  company: CampaignCompanyResponse;
};

type Item = {
  key: string;
  icon: JSX.Element;
  value: string;
};

const isItem = (item: Item | false | null | undefined | ""): item is Item =>
  Boolean(item);

const CompaniesContactCard = ({ company }: CompaniesContactCardProps) => {
  const { value: isOpen, toggle } = useToggle(false);

  const contacts = company.campaignContacts ?? [];
  const contactCount = contacts.length;
  const replyCount = contacts.filter((contact) =>
    contact.emailData?.some(
      (email) => email.emailDeliveryStatus === EmailDeliveryStatus.REPLIED
    )
  ).length;

  const metaItems = useMemo<Item[]>(() => {
    const items = [
      company.industry && {
        key: "industry",
        icon: <Building className="h-3 w-3" />,
        value: company.industry,
      },
      (company.hqState || company.hqCountry) && {
        key: "location",
        icon: <MapPin className="h-3 w-3" />,
        value: [company.hqState, company.hqCountry].filter(Boolean).join(", "),
      },
      company.employeeCount && {
        key: "employees",
        icon: <Users className="h-3 w-3" />,
        value: `${company.employeeCount} employees`,
      },
      contactCount > 0 && {
        key: "contacts",
        icon: <User className="h-3 w-3" />,
        value: `${contactCount} contact${contactCount !== 1 ? "s" : ""}`,
      },
    ];
    return items.filter(isItem);
  }, [
    company.industry,
    company.hqState,
    company.hqCountry,
    company.employeeCount,
    contactCount,
  ]);

  return (
    <Card className="border-border shadow-sm">
      <div className="p-4 space-y-2">
        <div className="flex items-start justify-between">
          <h3 className="font-bold text-foreground">{company.name}</h3>
          <Button
            variant="ghost"
            size="sm"
            className="h-6 w-6 p-0"
            onClick={toggle}
          >
            {isOpen ? (
              <ChevronUp
                className="h-4 w-4"
                aria-label="Collapse company information"
              />
            ) : (
              <ChevronDown
                className="h-4 w-4"
                aria-label="Expand company information"
              />
            )}
          </Button>
        </div>

        {company.accountSummary && (
          <p
            className={cn(
              "text-sm text-muted-foreground",
              isOpen ? "line-clamp-none" : "line-clamp-1"
            )}
          >
            {company.accountSummary}
          </p>
        )}

        <div className="flex flex-wrap gap-x-4 gap-y-2 text-xs text-muted-foreground">
          {metaItems.map((item) => (
            <span key={item.key} className="inline-flex items-center gap-1">
              {item.icon}
              {item.value}
            </span>
          ))}
          <span className="inline-flex items-center gap-1">
            <MessageSquare className="h-3 w-3 text-orange-500" />
            <span className="text-orange-600 font-medium">
              {replyCount} repl{replyCount !== 1 ? "ies" : "y"}
            </span>
          </span>
        </div>

        <AnimatePresence>
          {isOpen && contacts.length > 0 && (
            <motion.div
              initial={{ height: 0, opacity: 0 }}
              animate={{ height: "auto", opacity: 1 }}
              exit={{ height: 0, opacity: 0 }}
              transition={{ duration: 0.2 }}
              className="overflow-hidden"
            >
              <div className="mt-3 pt-3 border-t border-border space-y-3">
                {contacts.map((contact) => {
                  const hasReplied = contact.emailData?.some(
                    (email) =>
                      email.emailDeliveryStatus === EmailDeliveryStatus.REPLIED
                  );
                  return (
                    <div
                      key={contact.id}
                      className={cn(
                        "flex gap-3 text-sm p-3 rounded-md relative",
                        hasReplied
                          ? "bg-green-50 border border-green-200"
                          : "bg-secondary/50"
                      )}
                    >
                      {hasReplied && (
                        <CheckCircle2 className="absolute top-2 right-2 h-4 w-4 text-green-600" />
                      )}

                      <div className="flex-1 flex flex-col gap-1 pr-16">
                        <div className="flex items-center gap-2 font-medium text-foreground">
                          <User className="h-4 w-4 text-muted-foreground" />
                          {formatName({
                            firstName: contact.firstName,
                            lastName: contact.lastName,
                          }) || "N/A"}
                          {hasReplied && (
                            <Badge className="bg-green-100 text-green-700 border-green-200 text-xs px-1.5 py-0">
                              Replied
                            </Badge>
                          )}
                        </div>

                        {contact.title && (
                          <span className="flex items-center gap-2 text-muted-foreground pl-6">
                            <Briefcase className="h-3 w-3" />
                            {contact.title}
                          </span>
                        )}

                        {(contact.email || contact.phoneE164) && (
                          <div className="flex flex-wrap gap-3 pl-6 mt-1 text-xs text-sky-500">
                            {contact.email && (
                              <a
                                href={`mailto:${contact.email}`}
                                className="inline-flex items-center gap-1 hover:underline cursor-pointer"
                              >
                                <Mail className="h-3 w-3" />
                                {contact.email}
                              </a>
                            )}
                            {contact.phoneE164 && (
                              <a
                                href={`tel:${contact.phoneE164}`}
                                className="inline-flex items-center gap-1 hover:underline cursor-pointer"
                              >
                                <Phone className="h-3 w-3" />
                                {contact.phoneE164}
                              </a>
                            )}
                          </div>
                        )}
                      </div>
                    </div>
                  );
                })}
              </div>
            </motion.div>
          )}
        </AnimatePresence>
      </div>
    </Card>
  );
};

export { CompaniesContactCard };
