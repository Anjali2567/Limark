import { CustomerQuotationResponse } from '@/types/quotation.types';
import { RFQProjectDetailsPanel } from './RFQProjectDetailsPanel';
import { ScrollArea } from '@/components/ui/scroll-area';
import { RFQQuotationForm } from './RFQQuotationForm';

type RFQQuotationDetailsProps = {
  quotationData: CustomerQuotationResponse;
};

const RFQQuotationDetails = ({ quotationData }: RFQQuotationDetailsProps) => {
  return (
    <div className="flex flex-1 overflow-hidden">
      <div className="sticky top-18.25 h-full w-1/4 shrink-0 overflow-hidden p-8">
        <ScrollArea className="h-full">
          <RFQProjectDetailsPanel rfq={quotationData} />
        </ScrollArea>
      </div>
      <div className="flex-1 overflow-hidden">
        <ScrollArea className="mb-3 h-full p-8 pb-0 pl-0">
          <RFQQuotationForm rfq={quotationData} />
        </ScrollArea>
      </div>
    </div>
  );
};

export { RFQQuotationDetails };
