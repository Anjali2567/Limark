import { Geist, Geist_Mono } from 'next/font/google';
import { ReactNode } from 'react';
import { Metadata } from 'next';

import '@fortawesome/fontawesome-svg-core/styles.css';

import './globals.css';
import { Providers } from './providers';

const geistSans = Geist({
  variable: '--font-geist-sans',
  subsets: ['latin'],
});

const geistMono = Geist_Mono({
  variable: '--font-geist-mono',
  subsets: ['latin'],
});

export const metadata: Metadata = {
  title: 'LeadPlus',
  description: 'LeadPlus Portal',
  icons: {
    icon: '/leadplus-mini-logo.png',
  },
};

interface RootLayoutProps {
  children: ReactNode;
}

export default function RootLayout({ children }: RootLayoutProps) {
  return (
    <html lang="en" suppressHydrationWarning>
      <body className={`${geistSans.variable} ${geistMono.variable} antialiased`}>
        <Providers>{children}</Providers>
      </body>
    </html>
  );
}
