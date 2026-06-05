'use client';

import Image from 'next/image';

import { Drawer } from '@/components/Drawer';
import { Button } from '@/components/ui/button';
import { CustomerQuotation } from '@/types/customer-quotation.types';
import {
  Building2,
  Check,
  Download,
  ExternalLink,
  Mail,
  MapPin,
  MessageSquare,
  Paperclip,
  Phone,
} from 'lucide-react';
import { formatLocation } from '@/lib/utils/formatter';
import { getCachedImageUrl } from '@/lib/utils/helpers';

type QuotationDrawerProps = {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  quote: CustomerQuotation | null;
};

const QuotationDrawer = ({ open, onOpenChange, quote }: QuotationDrawerProps) => {
  if (!quote) return null;

  const totalCost = quote.items?.reduce((sum, item) => sum + item.price, 0) ?? 0;

  const footer = (
    <div className="flex gap-3">
      <Button
        variant="outline"
        onClick={() => onOpenChange(false)}
        className="border-border flex-1"
      >
        Close
      </Button>
      <Button className="bg-primary hover:bg-primary/90 text-primary-foreground flex-1">
        <MessageSquare className="mr-2 h-4 w-4" />
        Contact Vendor
      </Button>
    </div>
  );

  return (
    <Drawer
      open={open}
      onOpenChange={onOpenChange}
      title={`Quote Details - ${quote.vendorCompanyName ?? 'N/A'}`}
      description="Complete breakdown and vendor information"
      maxWidth="3xl"
      footer={footer}
    >
      <div className="space-y-6">
        <div className="bg-muted/30 flex items-start gap-4 rounded-lg p-4">
          {quote.vendorLogo ? (
            <Image
              src={getCachedImageUrl(quote.vendorLogo, quote.updatedAt) || ''}
              alt={quote.vendorCompanyName ?? 'Vendor'}
              width={80}
              height={80}
              unoptimized
              className="border-border h-20 w-20 shrink-0 rounded-lg border object-cover"
            />
          ) : (
            <div className="bg-muted border-border flex h-20 w-20 shrink-0 items-center justify-center rounded-lg border">
              <Building2 className="text-muted-foreground h-8 w-8" />
            </div>
          )}
          <div className="min-w-0 flex-1">
            <h3 className="text-foreground mb-2 font-semibold" style={{ fontSize: '18px' }}>
              {quote.vendorCompanyName ?? 'N/A'}
            </h3>
            <div className="space-y-1">
              <div className="text-muted-foreground flex items-center gap-2">
                <MapPin className="h-4 w-4 shrink-0" />
                <span className="text-sm">
                  {quote.vendorAddress ? formatLocation(quote.vendorAddress) : 'N/A'}
                </span>
              </div>
              <div className="text-muted-foreground flex items-center gap-2">
                <Mail className="h-4 w-4 shrink-0" />
                <span className="text-sm">{quote.vendorEmail ?? 'N/A'}</span>
              </div>
              <div className="text-muted-foreground flex items-center gap-2">
                <Phone className="h-4 w-4 shrink-0" />
                <span className="text-sm">{quote.vendorPhoneNumber ?? 'N/A'}</span>
              </div>
              {quote.vendorWebsite ? (
                <a
                  href={quote.vendorWebsite}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="text-primary flex items-center gap-2 text-sm hover:underline"
                >
                  <ExternalLink className="h-4 w-4 shrink-0" />
                  <span>{quote.vendorWebsite}</span>
                </a>
              ) : (
                <div className="text-muted-foreground flex items-center gap-2 text-sm">
                  <ExternalLink className="h-4 w-4 shrink-0" />
                  <span>N/A</span>
                </div>
              )}
            </div>
          </div>
        </div>
        <div>
          <h4 className="text-foreground mb-3 text-[16px] font-semibold">
            Detailed Price Breakdown
          </h4>
          <div className="border-border overflow-hidden rounded-lg border">
            <table className="w-full">
              <thead className="bg-muted/50">
                <tr>
                  <th className="text-foreground p-4 text-left text-[12px] font-semibold">
                    Service
                  </th>
                  <th className="text-foreground p-4 text-left text-[12px] font-semibold">
                    Description
                  </th>
                  <th className="text-foreground p-4 text-right text-[12px] font-semibold">
                    Duration
                  </th>
                  <th className="text-foreground p-4 text-right text-[12px] font-semibold">Cost</th>
                </tr>
              </thead>
              <tbody>
                {quote.items && quote.items.length > 0 ? (
                  quote.items.map((item, idx) => (
                    <tr key={idx} className={idx % 2 === 0 ? 'bg-muted/20' : ''}>
                      <td className="text-foreground p-4 text-sm font-medium">
                        {item.serviceName || 'N/A'}
                      </td>
                      <td className="text-muted-foreground p-4 text-[12px]">
                        {item.description || 'N/A'}
                      </td>
                      <td className="text-foreground p-4 text-right text-[12px]">
                        {item.duration || 'N/A'}
                      </td>
                      <td className="text-primary p-4 text-right text-sm font-semibold">
                        ${item.price.toLocaleString()}
                      </td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan={4} className="text-muted-foreground p-6 text-center text-sm">
                      No items available
                    </td>
                  </tr>
                )}
                <tr className="border-border bg-muted/50 border-t-2">
                  <td colSpan={3} className="text-foreground p-4 text-right text-sm font-semibold">
                    Total
                  </td>
                  <td className="text-primary p-4 text-right text-sm font-bold">
                    ${totalCost.toLocaleString()}
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        {quote.attachments && quote.attachments.length > 0 && (
          <div>
            <h4 className="text-foreground mb-3 text-[16px] font-semibold">Attachments</h4>
            <div className="space-y-2">
              {quote.attachments.map((attachment, idx) => (
                <button
                  key={idx}
                  className="bg-muted/30 hover:bg-muted/50 border-border flex w-full items-center gap-3 rounded-lg border p-3 transition-colors"
                >
                  <Paperclip className="text-primary h-5 w-5 shrink-0" />
                  <span
                    className="text-foreground flex-1 truncate text-left font-medium"
                    style={{ fontSize: '14px' }}
                  >
                    {attachment}
                  </span>
                  <Download className="text-muted-foreground h-4 w-4 shrink-0" />
                </button>
              ))}
            </div>
          </div>
        )}
        <div className="grid grid-cols-2 gap-6">
          <div>
            <h4 className="text-foreground mb-3 text-[16px] font-semibold">Payment Terms</h4>
            <p className="text-muted-foreground text-sm">{quote.paymentTerms || 'N/A'}</p>
          </div>
          <div>
            <h4 className="text-foreground mb-3 text-[16px] font-semibold">Deliverables</h4>
            {quote.deliverables && quote.deliverables.length > 0 ? (
              <div className="space-y-1">
                {quote.deliverables.map((item, idx) => (
                  <div key={idx} className="text-muted-foreground flex items-center gap-2">
                    <Check className="text-primary h-4 w-4 shrink-0" />
                    <span className="text-[12px]">{item}</span>
                  </div>
                ))}
              </div>
            ) : (
              <p className="text-muted-foreground text-sm">None specified</p>
            )}
          </div>
        </div>
      </div>
    </Drawer>
  );
};

export { QuotationDrawer };
