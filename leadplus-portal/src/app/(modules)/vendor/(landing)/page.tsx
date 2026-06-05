'use client';

import React, { useState, useEffect } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';

import { appRoutes } from '@/config/routes';
import { Module } from '@/constants/modules.constants';
import { VendorVerificationStatus } from '@/constants/vendor.constant';
import { FEATURES } from '@/constants/features.constants';
import { useAuth } from '@/context/AuthContext';
import { useGetAuthenticatedVendor } from '@/hooks/useAuthentication';
import { useCreateVendor } from '@/hooks/useVendor';

const VendorRootPage = () => {
  const router = useRouter();
  const { authenticatedUser, refreshSession, loggedInUser, isLoading: isAuthLoading } = useAuth();
  const { mutate } = useCreateVendor();
  const { data: vendor, isLoading } = useGetAuthenticatedVendor();

  const [currentActivity, setCurrentActivity] = useState(0);
  const activities = [
    { company: 'TechForge Solutions', action: 'received 3 new RFP requests today', time: '1m ago' },
    { company: 'CloudNine Partners', action: 'closed a $240K enterprise deal', time: '4m ago' },
    { company: 'DataMind Labs', action: 'matched with a Fortune 500 buyer', time: '9m ago' },
    {
      company: 'BluePeak Technologies',
      action: 'submitted 5 proposals this week',
      time: '14m ago',
    },
  ];

  useEffect(() => {
    const iv = setInterval(() => setCurrentActivity((p) => (p + 1) % activities.length), 3500);
    return () => clearInterval(iv);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const onBecomeVendorClick = () => {
    mutate(undefined, {
      onSuccess: async () => {
        await refreshSession();
        router.replace(appRoutes.vendor.onboarding.root);
      },
    });
  };

  useEffect(() => {
    if (isAuthLoading) return;
    if (!FEATURES.LANDING_PAGES_ENABLED && !loggedInUser) {
      router.replace(`${appRoutes.auth.login}?type=${Module.VENDOR.toLowerCase()}`);
    }
  }, [isAuthLoading, loggedInUser, router]);

  useEffect(() => {
    if (vendor && !isLoading) {
      if (vendor.vendorVerificationStatus === VendorVerificationStatus.INCOMPLETE) {
        router.replace(appRoutes.vendor.onboarding.root);
      } else {
        router.push(appRoutes.vendor.profileOverview.root);
      }
    }
  }, [router, vendor, isLoading]);

  if (!FEATURES.LANDING_PAGES_ENABLED && !loggedInUser) return null;

  if (isLoading || vendor) {
    return (
      <div className="bg-lp-hero flex h-screen items-center justify-center">
        <div className="border-lp-gold h-12 w-12 animate-spin rounded-full border-4 border-t-transparent" />
      </div>
    );
  }

  const signupHref = `${appRoutes.auth.signup}?type=${Module.VENDOR.toLowerCase()}`;
  const loginHref = `${appRoutes.auth.login}?type=${Module.VENDOR.toLowerCase()}`;

  return (
    <div className="bg-lp-surface font-family-display min-h-screen overflow-x-hidden">
      {/* HERO */}
      <section className="bg-lp-hero relative overflow-hidden px-14 pt-20 pb-24">
        {/* Ambient glow */}
        <div className="pointer-events-none absolute inset-0 bg-[radial-gradient(ellipse_60%_50%_at_80%_50%,rgba(var(--lp-gold-rgb),0.07)_0%,transparent_70%),radial-gradient(ellipse_50%_60%_at_20%_30%,rgba(56,120,200,0.05)_0%,transparent_70%)]" />
        <div className="pointer-events-none absolute inset-0 bg-[linear-gradient(rgba(255,255,255,0.5)_1px,transparent_1px),linear-gradient(90deg,rgba(255,255,255,0.5)_1px,transparent_1px)] bg-size-[60px_60px] opacity-3" />

        <div className="relative z-10 mx-auto grid max-w-300 grid-cols-[1fr_380px] items-center gap-20">
          {/* Left */}
          <div>
            {/* Live activity pill */}
            <div className="animate-in fade-in mb-8 inline-flex items-center gap-2.5 rounded-full border border-white/5 bg-white/5 px-4 py-2 pl-3 duration-700">
              <span className="h-1.5 w-1.5 animate-pulse rounded-full bg-green-500" />
              <span
                key={currentActivity}
                className="animate-in fade-in slide-in-from-top-3 text-[13px] text-white/60 duration-700"
              >
                <span className="font-medium text-white/85">
                  {activities[currentActivity].company}
                </span>{' '}
                {activities[currentActivity].action}
                <span className="ml-2 text-white/30">·</span>
                <span className="ml-2 text-white/30">{activities[currentActivity].time}</span>
              </span>
            </div>

            <h1 className="animate-in fade-in slide-in-from-bottom-7 font-family-serif mb-6 text-[56px] leading-[1.1] font-bold tracking-[-2px] text-white duration-700">
              Turn your expertise
              <br />
              <span className="text-lp-gold">into enterprise clients.</span>
            </h1>

            <p className="animate-in fade-in slide-in-from-bottom-7 fill-mode-both mb-10 max-w-130 text-lg leading-relaxed font-light text-white/50 delay-150 duration-700">
              Join 4,200+ verified vendors on LeadPlus. Get matched with enterprise buyers who are
              actively looking for your services — not cold outreach, real intent.
            </p>

            {/* CTA buttons */}
            <div className="animate-in fade-in slide-in-from-bottom-7 fill-mode-both flex gap-3.5 delay-300 duration-700">
              {authenticatedUser ? (
                <button
                  onClick={onBecomeVendorClick}
                  className="bg-lp-gold text-lp-dark cursor-pointer rounded-[10px] border-none px-9 py-4 text-[15px] font-bold transition-all duration-200 hover:-translate-y-0.5"
                >
                  Complete Vendor Profile →
                </button>
              ) : (
                <Link href={signupHref}>
                  <button className="bg-lp-gold text-lp-dark cursor-pointer rounded-[10px] border-none px-9 py-4 text-[15px] font-bold transition-all duration-200 hover:-translate-y-0.5">
                    Get Listed Free →
                  </button>
                </Link>
              )}
              <Link href={loginHref}>
                <button className="cursor-pointer rounded-[10px] border-2 border-white/10 bg-transparent px-9 py-4 text-[15px] font-semibold text-white/70 transition-all duration-200 hover:border-white/30">
                  Sign in to your account
                </button>
              </Link>
            </div>

            <div className="animate-in fade-in slide-in-from-bottom-7 fill-mode-both mt-4.5 text-[13px] text-white/30 delay-450 duration-700">
              Free to join · No commission on deals · Cancel anytime
            </div>
          </div>

          {/* Right – trust proof */}
          <div className="animate-in fade-in slide-in-from-bottom-7 fill-mode-both delay-400 duration-700">
            {/* Stats */}
            <div className="mb-4 rounded-2xl border border-white/5 bg-white/3 p-7">
              <div className="mb-5 text-[11px] font-bold tracking-widest text-white/35 uppercase">
                Platform for Vendors
              </div>
              <div className="grid grid-cols-2 gap-6">
                {[
                  { val: '4,200+', label: 'Active Vendors' },
                  { val: '850+', label: 'Monthly RFPs' },
                  { val: '$180K', label: 'Avg Deal Size' },
                  { val: '91%', label: 'Proposal Win Rate' },
                ].map((s, i) => (
                  <div key={i}>
                    <div className="font-family-serif text-[28px] font-bold tracking-tight text-white">
                      {s.val}
                    </div>
                    <div className="mt-0.5 text-xs text-white/40">{s.label}</div>
                  </div>
                ))}
              </div>
            </div>

            {/* Vendor review */}
            <div className="rounded-2xl border border-white/5 bg-white/3 px-6 py-5">
              <div className="mb-3.5 text-[11px] font-bold tracking-widest text-white/35 uppercase">
                From a LeadPlus Vendor
              </div>
              <p className="font-family-serif text-sm leading-relaxed text-white/70 italic">
                &ldquo;We closed three enterprise deals in our first month. The buyer intent signals
                are real — these aren&rsquo;t tire-kickers.&rdquo;
              </p>
              <div className="mt-3 flex items-center gap-2.5">
                <div className="from-lp-gold text-lp-dark flex h-8 w-8 items-center justify-center rounded-full bg-linear-to-br to-[#e0c98a] text-[13px] font-bold">
                  MK
                </div>
                <div>
                  <div className="text-[13px] font-medium text-white/80">
                    CEO, TechForge Solutions
                  </div>
                  <div className="text-[11px] text-white/35">
                    LeadPlus Certified · SAP Implementation
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* HOW IT WORKS (VENDORS) */}
      <section className="bg-white px-14 py-20">
        <div className="mx-auto max-w-300">
          <div className="mb-14 text-center">
            <div className="text-lp-gold mb-3 text-[11px] font-bold tracking-[2px] uppercase">
              How It Works
            </div>
            <h2 className="font-family-serif text-lp-dark text-4xl font-bold tracking-tight">
              From profile to pipeline in days
            </h2>
          </div>

          <div className="grid grid-cols-3 gap-8">
            {[
              {
                step: '01',
                title: 'Build Your Profile',
                desc: 'Showcase your services, case studies, certifications, and tech stack. Our team verifies your credentials and helps you stand out.',
                detail: 'AI-assisted profile · Case study upload · Verification',
              },
              {
                step: '02',
                title: 'Get Matched to RFPs',
                desc: 'Our AI matches you to incoming RFPs and buyer searches based on your actual capability fit — not keyword stuffing.',
                detail: 'Capability matching · Real-time alerts · Match score',
              },
              {
                step: '03',
                title: 'Submit & Win',
                desc: 'Respond to matched RFPs with a structured proposal. Track proposal status, communicate with buyers, and close deals faster.',
                detail: 'Proposal templates · Buyer messaging · Deal tracking',
              },
            ].map((s, i) => (
              <div key={i}>
                <div className="font-family-serif text-lp-dark/3 -mb-3 text-[64px] leading-none font-extrabold">
                  {s.step}
                </div>
                <div className="text-lp-dark mb-3 text-xl font-semibold tracking-tight">
                  {s.title}
                </div>
                <div className="mb-4 text-sm leading-relaxed text-slate-600">{s.desc}</div>
                <div className="inline-block rounded-lg bg-gray-50 px-3 py-2 text-xs font-medium text-slate-400">
                  {s.detail}
                </div>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* WHY JOIN LEADPLUS */}
      <section className="bg-lp-surface px-14 py-20">
        <div className="mx-auto max-w-300">
          <div className="mb-14 text-center">
            <div className="text-lp-gold mb-3 text-[11px] font-bold tracking-[2px] uppercase">
              Why LeadPlus
            </div>
            <h2 className="font-family-serif text-lp-dark mx-auto max-w-150 text-4xl font-bold tracking-tight">
              Not all vendor marketplaces are equal
            </h2>
          </div>

          <div className="mx-auto grid max-w-225 grid-cols-[1fr_40px_1fr]">
            {/* Without LeadPlus */}
            <div className="border-lp-dark/5 rounded-2xl border bg-white p-8">
              <div className="mb-5 flex items-center gap-2 text-[11px] font-bold tracking-wide text-red-500 uppercase">
                <span className="h-0.5 w-4 rounded bg-red-500" />
                Other Platforms
              </div>
              {[
                'Pay-to-rank listing with no quality signal',
                "Unfiltered leads waste your sales team's time",
                'No verification — anyone can claim anything',
                'Race to the bottom on pricing',
                'Generic RFPs with no fit scoring',
                'Zero insight into buyer intent or budget',
              ].map((item, i) => (
                <div key={i} className="mb-3.5 flex items-start gap-2.5">
                  <span className="mt-0.5 shrink-0 text-sm text-red-500">✗</span>
                  <span className="text-sm leading-normal text-[#4a5062]">{item}</span>
                </div>
              ))}
            </div>

            {/* Arrow */}
            <div className="flex items-center justify-center">
              <div className="bg-lp-dark text-lp-gold z-10 flex h-10 w-10 items-center justify-center rounded-full text-base font-bold">
                →
              </div>
            </div>

            {/* With LeadPlus */}
            <div className="bg-lp-hero rounded-2xl p-8">
              <div className="text-lp-gold mb-5 flex items-center gap-2 text-[11px] font-bold tracking-wide uppercase">
                <span className="bg-lp-gold h-0.5 w-4 rounded" />
                With LeadPlus
              </div>
              {[
                'Capability-based matching — no pay-to-win',
                'Pre-qualified buyers with real intent and budget',
                'Verified profile builds buyer trust instantly',
                'Compete on quality and fit, not lowest price',
                'Structured RFPs matched to your specific expertise',
                'Real-time buyer signals and proposal analytics',
              ].map((item, i) => (
                <div key={i} className="mb-3.5 flex items-start gap-2.5">
                  <span className="text-lp-gold mt-0.5 shrink-0 text-sm">✓</span>
                  <span className="text-sm leading-normal text-white/70">{item}</span>
                </div>
              ))}
            </div>
          </div>
        </div>
      </section>

      {/* FEATURES */}
      <section className="bg-white px-14 py-20">
        <div className="mx-auto max-w-300">
          <div className="mb-14 text-center">
            <div className="text-lp-gold mb-3 text-[11px] font-bold tracking-[2px] uppercase">
              Everything Vendors Need
            </div>
            <h2 className="font-family-serif text-lp-dark text-4xl font-bold tracking-tight">
              Built for serious service providers
            </h2>
          </div>

          <div className="grid grid-cols-3 gap-5">
            {[
              {
                icon: '🎯',
                title: 'AI-Powered Matching',
                desc: 'Our engine scores your fit against every incoming RFP across 40+ criteria — no keyword guessing.',
              },
              {
                icon: '🛡️',
                title: 'Verification Badge',
                desc: "LeadPlus Certified badge signals to buyers that you've passed our rigorous quality assessment.",
              },
              {
                icon: '📊',
                title: 'Profile Analytics',
                desc: 'See how many buyers viewed your profile, how you rank in searches, and which services attract the most interest.',
              },
              {
                icon: '📄',
                title: 'Structured RFPs',
                desc: 'Every RFP includes scope, budget range, timeline, and decision criteria — so you only respond to real opportunities.',
              },
              {
                icon: '💬',
                title: 'Direct Buyer Messaging',
                desc: 'Communicate directly with verified buyers through the platform before committing to a full proposal.',
              },
              {
                icon: '🤝',
                title: 'Agreement Management',
                desc: 'Built-in agreement templates, e-signatures, and milestone tracking — close deals without leaving the platform.',
              },
            ].map((f, i) => (
              <div
                key={i}
                className="border-lp-dark/5 bg-lp-surface cursor-default rounded-[14px] border p-7 transition-all duration-300 ease-out hover:-translate-y-1 hover:shadow-[0_16px_40px_-12px_rgba(10,15,30,0.12)]"
              >
                <div className="mb-4 text-[32px]">{f.icon}</div>
                <div className="text-lp-dark mb-2 text-[17px] font-semibold tracking-tight">
                  {f.title}
                </div>
                <p className="text-sm leading-relaxed text-slate-600">{f.desc}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* VERIFICATION TIERS */}
      <section className="bg-lp-hero px-14 py-20">
        <div className="mx-auto max-w-300">
          <div className="mb-14 text-center">
            <div className="text-lp-gold mb-3 text-[11px] font-bold tracking-[2px] uppercase">
              Trust System
            </div>
            <h2 className="font-family-serif mx-auto max-w-137.5 text-4xl font-bold tracking-tight text-white">
              Get verified. Get seen.
            </h2>
            <p className="mx-auto mt-3 max-w-125 text-[15px] text-white/45">
              Higher verification tiers unlock more RFP matches and buyer visibility
            </p>
          </div>

          <div className="grid grid-cols-3 gap-5">
            {[
              {
                badge: 'Basic Listed',
                color: 'slate',
                reqs: [
                  'Completed company profile',
                  'Agreed to platform terms',
                  'Basic service listing',
                ],
                visibility: 'Standard search ranking',
              },
              {
                badge: 'LeadPlus Verified',
                color: 'green',
                reqs: [
                  'Background check passed',
                  'Business registration verified',
                  'Reference validation (3+ clients)',
                  'Portfolio review completed',
                ],
                visibility: '2× more profile views',
              },
              {
                badge: 'LeadPlus Certified',
                color: 'gold',
                reqs: [
                  'All Verified requirements',
                  'Quality assessment passed',
                  'Customer feedback score ≥ 4.5',
                  'Active case studies validated',
                  'Annual recertification',
                ],
                visibility: 'Priority matching · Gold badge · Featured placement',
              },
            ].map((tier, i) => (
              <div
                key={i}
                className={`flex flex-col rounded-2xl border p-7 ${
                  tier.color === 'gold'
                    ? 'border-lp-gold/20 bg-lp-gold/8'
                    : tier.color === 'green'
                      ? 'border-green-500/20 bg-green-500/6'
                      : 'border-slate-400/20 bg-white/5'
                }`}
              >
                <div
                  className={`mb-5 inline-flex items-center gap-1.5 text-[13px] font-semibold ${
                    tier.color === 'gold'
                      ? 'text-lp-gold'
                      : tier.color === 'green'
                        ? 'text-green-500'
                        : 'text-slate-400'
                  }`}
                >
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor">
                    <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z" />
                  </svg>
                  {tier.badge}
                </div>
                {tier.reqs.map((r, j) => (
                  <div key={j} className="mb-2.5 flex items-center gap-2.5">
                    <span
                      className={`text-xs ${
                        tier.color === 'gold'
                          ? 'text-lp-gold'
                          : tier.color === 'green'
                            ? 'text-green-500'
                            : 'text-slate-400'
                      }`}
                    >
                      ✓
                    </span>
                    <span className="text-[13px] leading-normal text-white/60">{r}</span>
                  </div>
                ))}
                <div
                  className={`mt-auto border-t pt-4 ${
                    tier.color === 'gold'
                      ? 'border-lp-gold/20'
                      : tier.color === 'green'
                        ? 'border-green-500/20'
                        : 'border-slate-400/20'
                  }`}
                >
                  <div
                    className={`text-[11px] font-semibold ${
                      tier.color === 'gold'
                        ? 'text-lp-gold'
                        : tier.color === 'green'
                          ? 'text-green-500'
                          : 'text-slate-400'
                    }`}
                  >
                    {tier.visibility}
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* FINAL CTA */}
      <section className="bg-lp-surface px-14 py-24 text-center">
        <div className="mx-auto max-w-155">
          <h2 className="font-family-serif text-lp-dark mb-4.5 text-[42px] leading-[1.15] font-bold tracking-[-1.5px]">
            Ready to grow your business with quality leads?
          </h2>
          <p className="mb-9 text-[17px] leading-relaxed text-slate-600">
            Join 500+ verified vendors already winning enterprise deals on LeadPlus.
          </p>
          <div className="flex justify-center gap-3.5">
            {authenticatedUser ? (
              <button
                onClick={onBecomeVendorClick}
                className="bg-lp-dark cursor-pointer rounded-[10px] border-none px-9 py-4 text-[15px] font-semibold text-white transition-all duration-200 hover:-translate-y-0.5"
              >
                Complete Vendor Profile →
              </button>
            ) : (
              <Link href={signupHref}>
                <button className="bg-lp-dark cursor-pointer rounded-[10px] border-none px-9 py-4 text-[15px] font-semibold text-white transition-all duration-200 hover:-translate-y-0.5">
                  Get Listed Free →
                </button>
              </Link>
            )}
            <Link href={loginHref}>
              <button className="border-lp-dark/15 text-lp-dark hover:border-lp-dark cursor-pointer rounded-[10px] border-2 bg-transparent px-9 py-4 text-[15px] font-semibold transition-all duration-200">
                Sign In
              </button>
            </Link>
          </div>
          <div className="mt-5 text-[13px] text-slate-400">
            Free to join · No commission · Cancel anytime
          </div>
        </div>
      </section>

      {/* FOOTER */}
      <footer className="bg-lp-hero px-14 pt-14 pb-10">
        <div className="mx-auto max-w-300">
          <div className="flex justify-between border-b border-white/5 pb-10">
            <div className="max-w-75">
              <div className="mb-3.5 flex items-baseline gap-0.5">
                <span className="font-family-serif text-[22px] font-bold text-white">Lead</span>
                <span className="font-family-serif text-lp-gold text-[22px] font-bold">Plus</span>
              </div>
              <p className="text-sm leading-relaxed text-white/35">
                The trusted marketplace connecting verified technology vendors with enterprise
                buyers worldwide.
              </p>
            </div>
            <div className="flex gap-18">
              {[
                {
                  title: 'For Vendors',
                  links: [
                    'Get Listed',
                    'Vendor Pricing',
                    'Verification Process',
                    'Success Stories',
                  ],
                },
                {
                  title: 'Platform',
                  links: ['Browse Vendors', 'How It Works', 'Pricing', 'Security'],
                },
                { title: 'Company', links: ['About', 'Blog', 'Careers', 'Contact'] },
              ].map((col, i) => (
                <div key={i}>
                  <div className="mb-4.5 text-[11px] font-bold tracking-widest text-white/35 uppercase">
                    {col.title}
                  </div>
                  {col.links.map((link, j) => (
                    <a
                      key={j}
                      href="#"
                      className="mb-3 block text-sm text-white/45 no-underline transition-colors duration-200 hover:text-white/80"
                    >
                      {link}
                    </a>
                  ))}
                </div>
              ))}
            </div>
          </div>
          <div className="flex items-center justify-between pt-6">
            <div className="text-[13px] text-white/25">© 2025 LeadPlus. All rights reserved.</div>
            <div className="flex gap-6">
              {['Privacy Policy', 'Terms of Service', 'Security'].map((l, i) => (
                <a key={i} href="#" className="text-[13px] text-white/25 no-underline">
                  {l}
                </a>
              ))}
            </div>
          </div>
        </div>
      </footer>
    </div>
  );
};

export default VendorRootPage;
