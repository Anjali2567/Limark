import { useRouter } from 'next/navigation';
import { useMemo } from 'react';
import { toast } from 'sonner';

import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Checkbox } from '@/components/ui/checkbox';
import { appRoutes } from '@/config/routes';
import { useAuth } from '@/context/AuthContext';
import { useAuthModal } from '@/context/AuthModalContext';
import { formatLocation } from '@/lib/utils/formatter';
import { cn, getCachedImageUrl, getInitials } from '@/lib/utils/helpers';
import { getTechnologyIcon } from '@/lib/utils/icon';
import { VendorCardDetails } from '@/types/vendor.types';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { Award, Briefcase, Building, Clock, Code, MapPin, Package, Users } from 'lucide-react';

type VendorCardProps = {
  item: VendorCardDetails;
  isSelected: boolean;
  onToggleSelection: (id: string) => void;
  onRequestQuote: (id: string) => void;
};

const VendorCard = ({ item, isSelected, onToggleSelection, onRequestQuote }: VendorCardProps) => {
  const router = useRouter();
  const { loggedInUser } = useAuth();
  const { showAuthModal } = useAuthModal();

  const items = useMemo(() => {
    return [
      { icon: MapPin, label: formatLocation(item.address || {}) },
      { icon: Building, label: `Est. ${item.yearEstablished}` },
      { icon: Users, label: `${item.companySize} employees` },
      { icon: Briefcase, label: `${item.projects} projects` },
    ];
  }, [item]);

  const handleRequestQuote = () => {
    if (!loggedInUser) {
      showAuthModal();
      return;
    }
    if (!loggedInUser.verified) {
      toast.warning('Please verify your email address before submitting a quote request.');
      return;
    }
    onRequestQuote(item.id);
  };

  const isBlurred = !loggedInUser;

  return (
    <div
      className={cn(
        'bg-card rounded-lg border p-6 transition-all duration-300',
        isSelected
          ? 'border-primary shadow-lg'
          : 'border-border hover:border-primary/50 hover:shadow-md'
      )}
    >
      <div className="flex gap-6">
        <div className="shrink-0 pt-1">
          <Checkbox
            checked={isSelected}
            onCheckedChange={() => onToggleSelection(item.id)}
            className="h-5 w-5"
          />
        </div>
        <div className="shrink-0">
          <Avatar className="border-border h-20 w-20 border-2 shadow-sm">
            <AvatarImage
              src={getCachedImageUrl(item.logo, item.updatedAt)}
              alt={item.companyName}
            />
            <AvatarFallback className="bg-sky-500 text-xl font-bold text-white">
              {getInitials(item.companyName || '')}
            </AvatarFallback>
          </Avatar>
        </div>
        <div className="min-w-0 flex-1">
          <div className="mb-2 flex items-start justify-between gap-4">
            <div className="flex-1">
              <div className="mb-1 flex items-center gap-2">
                <h3 className="text-foreground text-lg font-semibold">{item.companyName}</h3>
                {item.responseTime && (
                  <div className="text-muted-foreground flex items-center gap-1 text-sm">
                    <Clock className="h-3.5 w-3.5" />
                    <span>{item.responseTime}</span>
                  </div>
                )}
              </div>
              <p className="text-muted-foreground mb-2 text-sm">{item.tagline}</p>
            </div>
            <div className="flex shrink-0 gap-2">
              {loggedInUser && (
                <Button
                  variant="outline"
                  onClick={() => router.push(appRoutes.customer.vendor.detail(item.id))}
                >
                  View Profile
                </Button>
              )}
              <Button
                onClick={handleRequestQuote}
                className="bg-primary hover:bg-primary/90 text-primary-foreground"
              >
                Request Quote
              </Button>
            </div>
          </div>
          <div className="text-muted-foreground mb-3 flex flex-wrap items-center gap-4 text-sm">
            {items.map(({ icon: Icon, label }, idx) => (
              <div
                key={idx}
                className={cn(
                  'relative flex items-center gap-1',
                  isBlurred && 'group cursor-pointer'
                )}
                onClick={isBlurred ? showAuthModal : undefined}
              >
                <Icon className="h-4 w-4" />
                <span className={cn(isBlurred && 'blur-sm select-none')}>{label}</span>
              </div>
            ))}
          </div>
          <p className="text-muted-foreground mb-4 text-sm leading-relaxed">{item.description}</p>
          <div className="mb-4">
            <div className="mb-2 flex items-center gap-2">
              <Code className="text-muted-foreground h-4 w-4" />
              <span className="text-muted-foreground text-sm font-medium">Specifications:</span>
            </div>
            <div className="flex flex-wrap gap-2">
              {item.specificationList.map((tech, idx) => (
                <div
                  key={idx}
                  className="bg-muted border-border flex items-center gap-1.5 rounded-lg border px-3 py-1.5"
                >
                  {tech.icon && (
                    <FontAwesomeIcon
                      icon={getTechnologyIcon(tech.icon.toString())}
                      className="mr-2 h-4 w-4"
                    />
                  )}
                  <span className="text-foreground text-xs font-medium">{tech.name}</span>
                </div>
              ))}
            </div>
          </div>
          <div className="mb-4">
            <div className="mb-2 flex items-center gap-2">
              <Award className="text-muted-foreground h-4 w-4" />
              <span className="text-muted-foreground text-sm font-medium">Certifications:</span>
            </div>
            <div
              className={cn('relative flex flex-wrap gap-2', isBlurred && 'group cursor-pointer')}
              onClick={isBlurred ? showAuthModal : undefined}
            >
              <div className={cn('flex flex-wrap gap-2', isBlurred && 'blur-sm select-none')}>
                {item.certifications &&
                  item.certifications.map((cert, idx) => (
                    <Badge key={idx} variant="secondary" className="border-border border text-xs">
                      {cert}
                    </Badge>
                  ))}
              </div>
            </div>
          </div>
          <div>
            <div className="mb-3 flex items-center gap-2">
              <Package className="text-muted-foreground h-4 w-4" />
              <span className="text-muted-foreground text-sm font-medium">Key Services:</span>
            </div>
            <div className="flex flex-wrap gap-2">
              {item.serviceList.map((service, idx) => (
                <Badge key={idx} variant="secondary" className="border-border border text-xs">
                  {service.name}
                </Badge>
              ))}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export { VendorCard };
