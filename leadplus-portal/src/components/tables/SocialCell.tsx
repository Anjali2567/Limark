import { Facebook, Globe, Linkedin, Twitter } from 'lucide-react';

type SocialCellProps = {
  linkedInUrl?: string;
  twitterUrl?: string;
  facebookUrl?: string;
  websiteUrl?: string;
};

const SocialCell = ({ linkedInUrl, twitterUrl, facebookUrl, websiteUrl }: SocialCellProps) => {
  return (
    <div className="flex w-full items-center gap-2">
      {websiteUrl && (
        <a
          href={websiteUrl}
          target="_blank"
          rel="noopener noreferrer"
          className="text-muted-foreground hover:text-primary transition-colors"
        >
          <Globe className="h-4 w-4" />
        </a>
      )}
      {linkedInUrl && (
        <a
          href={linkedInUrl}
          target="_blank"
          rel="noopener noreferrer"
          className="text-muted-foreground hover:text-primary transition-colors"
        >
          <Linkedin className="h-4 w-4" />
        </a>
      )}
      {twitterUrl && (
        <a
          href={twitterUrl}
          target="_blank"
          rel="noopener noreferrer"
          className="text-muted-foreground hover:text-primary transition-colors"
        >
          <Twitter className="h-4 w-4" />
        </a>
      )}
      {facebookUrl && (
        <a
          href={facebookUrl}
          target="_blank"
          rel="noopener noreferrer"
          className="text-muted-foreground hover:text-primary transition-colors"
        >
          <Facebook className="h-4 w-4" />
        </a>
      )}
    </div>
  );
};

export { SocialCell };
