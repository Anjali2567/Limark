'use client';

import { useState, useEffect } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { appRoutes } from '@/config/routes';
import { FEATURES } from '@/constants/features.constants';
import { useAuth } from '@/context/AuthContext';
import { SearchBar } from './_components/SearchBar';

const LeadPlusHomepage = () => {
  const router = useRouter();
  const { loggedInUser, isLoading: isAuthLoading } = useAuth();
  const [hoveredCategory, setHoveredCategory] = useState<number | null>(null);
  const [currentActivity, setCurrentActivity] = useState(0);
  const [placeholderIdx, setPlaceholderIdx] = useState(0);

  const placeholders = [
    'SAP S/4HANA implementation partner',
    'Cloud migration specialists',
    'Cybersecurity audit firm',
    'Custom software development team',
  ];

  useEffect(() => {
    if (isAuthLoading) return;
    if (!FEATURES.LANDING_PAGES_ENABLED && !loggedInUser) {
      router.replace(`${appRoutes.auth.login}?type=vendor`);
    }
  }, [isAuthLoading, loggedInUser, router]);

  /* ── typing animation ─────────────────────────────────────────────── */
  useEffect(() => {
    const current = placeholders[placeholderIdx];
    let charIdx = 0;
    let deleting = false;
    let timeout: ReturnType<typeof setTimeout>;

    const tick = () => {
      if (!deleting) {
        charIdx++;
        if (charIdx === current.length) {
          timeout = setTimeout(() => {
            deleting = true;
            tick();
          }, 2200);
          return;
        }
        timeout = setTimeout(tick, 55);
      } else {
        charIdx--;
        if (charIdx < 0) {
          setPlaceholderIdx((prev) => (prev + 1) % placeholders.length);
          return;
        }
        timeout = setTimeout(tick, 30);
      }
    };
    tick();
    return () => clearTimeout(timeout);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [placeholderIdx]);

  /* ── live activity feed ───────────────────────────────────────────── */
  const activities = [
    { company: 'A Fortune 500 retailer', action: 'matched with 4 SAP partners', time: '2m ago' },
    {
      company: 'Series B fintech startup',
      action: 'found their cloud migration team',
      time: '5m ago',
    },
    {
      company: 'Healthcare enterprise',
      action: 'shortlisted 3 cybersecurity firms',
      time: '8m ago',
    },
    {
      company: 'Global logistics company',
      action: 'connected with AI/ML specialists',
      time: '12m ago',
    },
  ];

  useEffect(() => {
    const iv = setInterval(() => setCurrentActivity((p) => (p + 1) % activities.length), 3500);
    return () => clearInterval(iv);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const categories = [
    {
      name: 'Software Development',
      count: '2,400+',
      trending: true,
      growth: '+18%',
      color: '#3b82f6',
      desc: 'Custom apps, platforms & APIs',
    },
    {
      name: 'SAP Services',
      count: '850+',
      trending: true,
      growth: '+24%',
      color: '#f59e0b',
      desc: 'Implementation, migration & support',
    },
    {
      name: 'Cloud & DevOps',
      count: '1,200+',
      trending: false,
      growth: '+12%',
      color: '#06b6d4',
      desc: 'AWS, Azure, GCP & infrastructure',
    },
    {
      name: 'Cybersecurity',
      count: '680+',
      trending: true,
      growth: '+31%',
      color: '#ef4444',
      desc: 'Audits, compliance & SOC services',
    },
    {
      name: 'Data & AI',
      count: '920+',
      trending: true,
      growth: '+42%',
      color: '#8b5cf6',
      desc: 'ML, analytics & data engineering',
    },
    {
      name: 'Mobile & Web Apps',
      count: '1,800+',
      trending: false,
      growth: '+9%',
      color: '#10b981',
      desc: 'iOS, Android & responsive web',
    },
  ];

  const vendors = [
    {
      name: 'TechForge Solutions',
      rating: 4.9,
      reviews: 127,
      specialty: 'SAP Implementation',
      verified: 'certified',
      location: 'Austin, TX',
      size: '200–500',
      rate: '$150–200/hr',
      tags: ['SAP S/4HANA', 'ABAP', 'Fiori'],
      match: 96,
      quote: 'Migrated our entire ERP in 6 months — on time and under budget.',
      quoter: 'VP Engineering, Fortune 500 Retailer',
    },
    {
      name: 'CloudNine Partners',
      rating: 4.8,
      reviews: 89,
      specialty: 'Cloud Migration',
      verified: 'verified',
      location: 'Seattle, WA',
      size: '50–200',
      rate: '$125–175/hr',
      tags: ['AWS', 'Terraform', 'Kubernetes'],
      match: 93,
      quote: 'They reduced our infrastructure costs by 40% while improving uptime.',
      quoter: 'CTO, Series C Fintech',
    },
    {
      name: 'DataMind Labs',
      rating: 4.7,
      reviews: 156,
      specialty: 'AI & Machine Learning',
      verified: 'certified',
      location: 'San Francisco, CA',
      size: '50–200',
      rate: '$175–250/hr',
      tags: ['Python', 'TensorFlow', 'MLOps'],
      match: 91,
      quote: 'Built a recommendation engine that increased conversion by 34%.',
      quoter: 'Head of Product, E-commerce Platform',
    },
  ];

  /* ── rgba helpers (CSS variable channel triplets) ─────────────────── */
  const goldA = (a: number) => `rgba(var(--lp-gold-rgb), ${a})`;
  const darkA = (a: number) => `rgba(var(--lp-dark-rgb), ${a})`;
  const gold = 'var(--lp-gold)';
  const dark = 'var(--lp-dark)';

  if (!FEATURES.LANDING_PAGES_ENABLED && !loggedInUser) return null;

  /* ── render ───────────────────────────────────────────────────────── */
  return (
    <div className="font-display bg-lp-surface min-h-screen overflow-x-hidden">
      {/* ══════════════ HERO ══════════════ */}
      <section className="bg-lp-hero relative overflow-hidden px-4 pt-20 pb-24 md:px-14">
        {/* Ambient glow */}
        <div
          className="pointer-events-none absolute inset-0"
          style={{
            background: `radial-gradient(ellipse 60% 50% at 20% 50%, ${goldA(0.07)} 0%, transparent 70%), radial-gradient(ellipse 50% 60% at 80% 30%, rgba(56,120,200,0.05) 0%, transparent 70%)`,
          }}
        />
        {/* Grid pattern */}
        <div
          className="pointer-events-none absolute inset-0 opacity-[0.03]"
          style={{
            backgroundImage:
              'linear-gradient(rgba(255,255,255,0.5) 1px,transparent 1px),linear-gradient(90deg,rgba(255,255,255,0.5) 1px,transparent 1px)',
            backgroundSize: '60px 60px',
          }}
        />

        <div className="relative z-[1] mx-auto grid max-w-7xl items-center gap-16 md:grid-cols-[1fr_380px] md:gap-20">
          {/* Left */}
          <div>
            {/* Live activity pill */}
            <div
              className="animate-lp-fade-in mb-8 inline-flex items-center gap-2.5 rounded-full border px-4 py-2"
              style={{
                background: 'rgba(255,255,255,0.04)',
                borderColor: 'rgba(255,255,255,0.06)',
              }}
            >
              <span className="animate-lp-pulse h-[7px] w-[7px] rounded-full bg-green-500" />
              <span
                key={currentActivity}
                className="animate-lp-activity text-[13px]"
                style={{ color: 'rgba(255,255,255,0.6)' }}
              >
                <span style={{ color: 'rgba(255,255,255,0.85)', fontWeight: 500 }}>
                  {activities[currentActivity].company}
                </span>{' '}
                {activities[currentActivity].action}
                <span style={{ color: 'rgba(255,255,255,0.3)', marginLeft: '8px' }}>·</span>
                <span style={{ color: 'rgba(255,255,255,0.3)', marginLeft: '8px' }}>
                  {activities[currentActivity].time}
                </span>
              </span>
            </div>

            <h1 className="animate-lp-fade-up mb-6 font-serif text-5xl leading-[1.1] font-bold tracking-[-2px] text-white md:text-6xl">
              Stop guessing. <br />
              <span style={{ color: gold }}>Start sourcing.</span>
            </h1>

            <p
              className="animate-lp-fade-up-d1 mb-10 max-w-[520px] text-lg leading-relaxed font-light tracking-[-0.2px]"
              style={{ color: 'rgba(255,255,255,0.5)' }}
            >
              The vendor discovery platform built for IT leaders who are tired of word-of-mouth
              referrals. Every provider is background-checked, rated by real customers, and matched
              to your exact requirements.
            </p>

            <SearchBar />

            {/* Secondary CTA */}
            <div className="animate-lp-fade-up-d3 mt-4">
              <span className="text-[13px]" style={{ color: 'rgba(255,255,255,0.3)' }}>
                or{' '}
              </span>
              <a
                href="#"
                className="text-[13px] font-medium"
                style={{
                  color: gold,
                  textDecoration: 'none',
                  borderBottom: `1px solid ${goldA(0.3)}`,
                }}
              >
                Upload an RFP for AI-powered matching →
              </a>
            </div>
          </div>

          {/* Right – trust proof */}
          <div className="animate-lp-fade-up-d4">
            {/* Stats card */}
            <div
              className="mb-4 rounded-2xl border p-7"
              style={{
                background: 'rgba(255,255,255,0.03)',
                borderColor: 'rgba(255,255,255,0.06)',
              }}
            >
              <div
                className="mb-5 text-[11px] font-bold tracking-[1.5px] uppercase"
                style={{ color: 'rgba(255,255,255,0.35)' }}
              >
                Platform Intelligence
              </div>
              <div className="grid grid-cols-2 gap-6">
                {[
                  { val: '4,200+', label: 'Verified Vendors', accent: '#c8a255' },
                  { val: '12,500+', label: 'Successful Matches', accent: '#22c55e' },
                  { val: '98.2%', label: 'Satisfaction Rate', accent: '#3b82f6' },
                  { val: '<24hrs', label: 'Avg Response', accent: '#8b5cf6' },
                ].map((s, i) => (
                  <div key={i}>
                    <div className="font-serif text-[28px] leading-none font-bold tracking-tight text-white">
                      {s.val}
                    </div>
                    <div className="mt-0.5 text-xs" style={{ color: 'rgba(255,255,255,0.4)' }}>
                      {s.label}
                    </div>
                  </div>
                ))}
              </div>
            </div>

            {/* Latest review */}
            <div
              className="rounded-2xl border px-6 py-5"
              style={{
                background: 'rgba(255,255,255,0.03)',
                borderColor: 'rgba(255,255,255,0.06)',
              }}
            >
              <div
                className="mb-3.5 text-[11px] font-bold tracking-[1.5px] uppercase"
                style={{ color: 'rgba(255,255,255,0.35)' }}
              >
                Latest Verified Review
              </div>
              <p
                className="font-serif text-sm leading-relaxed italic"
                style={{ color: 'rgba(255,255,255,0.7)' }}
              >
                &ldquo;Found our SAP implementation partner in 3 days instead of 3 months. The
                verification actually means something.&rdquo;
              </p>
              <div className="mt-3 flex items-center gap-2.5">
                <div
                  className="flex h-8 w-8 items-center justify-center rounded-full text-[13px] font-bold"
                  style={{ background: `linear-gradient(135deg, ${gold}, #e0c98a)`, color: dark }}
                >
                  JR
                </div>
                <div>
                  <div
                    className="text-[13px] font-medium"
                    style={{ color: 'rgba(255,255,255,0.8)' }}
                  >
                    VP Engineering
                  </div>
                  <div className="text-[11px]" style={{ color: 'rgba(255,255,255,0.35)' }}>
                    Fortune 500 Retail · Verified Customer
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>
      {/* ══════════════ TRUST LOGOS ══════════════ */}
      <section
        className="border-b bg-white px-4 py-7 md:px-14"
        style={{ borderColor: darkA(0.06) }}
      >
        <div className="mx-auto flex max-w-7xl flex-wrap items-center justify-center gap-10">
          <span className="text-xs font-medium whitespace-nowrap text-slate-400">
            Trusted by procurement teams at
          </span>
          {['Deloitte', 'Accenture', 'Google', 'Microsoft', 'Amazon', 'Salesforce'].map((name) => (
            <div
              key={name}
              className="cursor-default text-sm font-bold tracking-[-0.3px] text-slate-400 opacity-60 transition-opacity duration-300 hover:opacity-100"
            >
              {name}
            </div>
          ))}
        </div>
      </section>

      {/* ══════════════ CATEGORIES ══════════════ */}
      <section data-section="categories" className="px-4 py-20 md:px-14">
        <div className={`mx-auto max-w-7xl`}>
          <div className="mb-10 flex items-end justify-between">
            <div>
              <div
                className="mb-3 text-[11px] font-bold tracking-[2px] uppercase"
                style={{ color: gold }}
              >
                Explore
              </div>
              <h2 className="font-serif text-4xl font-bold tracking-[-1px]" style={{ color: dark }}>
                Browse by Specialty
              </h2>
            </div>
            <a
              href="#"
              className="border-b-2 pb-0.5 text-sm font-semibold"
              style={{ color: dark, borderColor: gold }}
            >
              View All Categories
            </a>
          </div>

          <div className="grid grid-cols-1 gap-3.5 sm:grid-cols-2 lg:grid-cols-3">
            {categories.map((cat, i) => (
              <div
                key={i}
                className="lp-cat-card relative cursor-pointer overflow-hidden rounded-2xl bg-white p-6"
                onMouseEnter={() => setHoveredCategory(i)}
                onMouseLeave={() => setHoveredCategory(null)}
                style={{
                  border: `1px solid ${hoveredCategory === i ? cat.color + '30' : darkA(0.06)}`,
                }}
              >
                <div
                  className="absolute top-0 right-0 left-0 h-[3px] transition-opacity duration-300"
                  style={{ background: cat.color, opacity: hoveredCategory === i ? 1 : 0 }}
                />
                <div className="flex items-start justify-between">
                  <div>
                    <div
                      className="mb-1 text-[17px] font-semibold tracking-[-0.3px]"
                      style={{ color: dark }}
                    >
                      {cat.name}
                    </div>
                    <div className="mb-3 text-[13px] text-[#8891a4]">{cat.desc}</div>
                    <div className="flex items-center gap-3">
                      <span className="text-[13px] font-semibold text-[#4a5062]">
                        {cat.count} vendors
                      </span>
                      {cat.trending && (
                        <span className="rounded-full bg-emerald-50 px-2 py-0.5 text-[11px] font-semibold text-emerald-600">
                          ↑ {cat.growth}
                        </span>
                      )}
                    </div>
                  </div>
                  <div
                    className="flex h-[42px] w-[42px] items-center justify-center rounded-[10px]"
                    style={{ background: cat.color + '15' }}
                  >
                    <div
                      className="h-[18px] w-[18px] rounded-full opacity-60"
                      style={{ background: cat.color }}
                    />
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* ══════════════ FEATURED VENDORS ══════════════ */}
      <section data-section="vendors" className="bg-white px-4 py-20 md:px-14">
        <div className={`not-even: mx-auto max-w-7xl`}>
          <div className="mb-10 flex items-end justify-between">
            <div>
              <div
                className="mb-3 text-[11px] font-bold tracking-[2px] uppercase"
                style={{ color: gold }}
              >
                Top Rated
              </div>
              <h2 className="font-serif text-4xl font-bold tracking-[-1px]" style={{ color: dark }}>
                Highest Rated Partners
              </h2>
              <p className="mt-2 text-[15px] text-[#8891a4]">
                Based on verified customer reviews and delivery outcomes
              </p>
            </div>
            <a
              href="#"
              className="border-b-2 pb-0.5 text-sm font-semibold"
              style={{ color: dark, borderColor: gold }}
            >
              Browse All Vendors
            </a>
          </div>

          <div className="grid grid-cols-1 gap-5 md:grid-cols-2 lg:grid-cols-3">
            {vendors.map((v, i) => (
              <div
                key={i}
                className="lp-vendor-card relative cursor-pointer rounded-2xl p-7"
                style={{ background: 'var(--lp-surface)', border: `1px solid ${darkA(0.06)}` }}
              >
                {/* Match badge */}
                <div
                  className="absolute top-5 right-5 rounded-lg px-2.5 py-1 text-xs font-bold"
                  style={{ background: dark, color: gold }}
                >
                  {v.match}% match
                </div>
                {/* Verification badge */}
                <div
                  className="mb-4 inline-flex items-center gap-1.5 rounded-full px-2.5 py-1 text-[11px] font-semibold"
                  style={{
                    background: v.verified === 'certified' ? '#fffbeb' : '#ecfdf5',
                    color: v.verified === 'certified' ? '#b45309' : '#059669',
                  }}
                >
                  <svg width="10" height="10" viewBox="0 0 24 24" fill="currentColor">
                    <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z" />
                  </svg>
                  LeadPlus {v.verified === 'certified' ? 'Certified' : 'Verified'}
                </div>
                {/* Avatar */}
                <div
                  className="mb-4 flex h-[52px] w-[52px] items-center justify-center rounded-xl text-lg font-bold"
                  style={{ background: `linear-gradient(135deg, ${dark}, #1e293b)`, color: gold }}
                >
                  {v.name
                    .split(' ')
                    .map((w) => w[0])
                    .join('')
                    .slice(0, 2)}
                </div>
                <div
                  className="mb-1 text-lg font-semibold tracking-[-0.3px]"
                  style={{ color: dark }}
                >
                  {v.name}
                </div>
                <div className="mb-1 text-[13px] text-[#8891a4]">
                  {v.specialty} · {v.location}
                </div>
                <div className="mb-3.5 text-xs text-slate-400">
                  {v.size} employees · {v.rate}
                </div>
                {/* Tags */}
                <div className="mb-4 flex flex-wrap gap-1.5">
                  {v.tags.map((t, j) => (
                    <span
                      key={j}
                      className="rounded-md px-2.5 py-1 text-[11px] font-medium text-[#4a5062]"
                      style={{ background: darkA(0.04) }}
                    >
                      {t}
                    </span>
                  ))}
                </div>
                {/* Stars */}
                <div className="mb-4 flex items-center gap-1.5">
                  <div className="flex gap-0.5">
                    {[...Array(5)].map((_, j) => (
                      <svg
                        key={j}
                        width="13"
                        height="13"
                        viewBox="0 0 24 24"
                        fill={j < Math.floor(v.rating) ? gold : '#e5e7eb'}
                      >
                        <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z" />
                      </svg>
                    ))}
                  </div>
                  <span className="text-sm font-semibold" style={{ color: dark }}>
                    {v.rating}
                  </span>
                  <span className="text-xs text-slate-400">({v.reviews})</span>
                </div>
                {/* Testimonial */}
                <div className="border-t pt-3.5" style={{ borderColor: darkA(0.06) }}>
                  <p className="font-serif text-[13px] leading-relaxed text-[#4a5062] italic">
                    &ldquo;{v.quote}&rdquo;
                  </p>
                  <div className="mt-2 text-[11px] text-slate-400">— {v.quoter}</div>
                </div>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* ══════════════ WHY LEADPLUS ══════════════ */}
      <section data-section="why" className="bg-lp-surface px-4 py-20 md:px-14">
        <div className={`mx-auto max-w-7xl`}>
          <div className="mb-14 text-center">
            <div
              className="mb-3 text-[11px] font-bold tracking-[2px] uppercase"
              style={{ color: gold }}
            >
              Why LeadPlus
            </div>
            <h2
              className="mx-auto max-w-[600px] font-serif text-4xl font-bold tracking-[-1px]"
              style={{ color: dark }}
            >
              Your current vendor search is broken
            </h2>
          </div>

          <div className="mx-auto grid max-w-[900px] grid-cols-[1fr_40px_1fr]">
            {/* Without */}
            <div
              className="rounded-2xl bg-white p-8"
              style={{ border: `1px solid ${darkA(0.06)}` }}
            >
              <div className="mb-5 flex items-center gap-2 text-[11px] font-bold tracking-[1.5px] text-red-500 uppercase">
                <span className="h-0.5 w-4 rounded bg-red-500" />
                Without LeadPlus
              </div>
              {[
                'Ask around for referrals — limited to your network',
                'Google search through pages of ads and SEO spam',
                'Spend weeks vetting vendors one by one',
                'No way to verify claims or past work',
                'Compare apples to oranges across proposals',
                'Hope for the best and sign a contract',
              ].map((item, i) => (
                <div key={i} className="mb-3.5 flex items-start gap-2.5">
                  <span className="mt-0.5 shrink-0 text-sm text-red-500">✗</span>
                  <span className="text-sm leading-relaxed text-[#4a5062]">{item}</span>
                </div>
              ))}
            </div>

            {/* Arrow divider */}
            <div className="flex items-center justify-center">
              <div
                className="z-[1] flex h-10 w-10 items-center justify-center rounded-full text-base font-bold"
                style={{ background: dark, color: gold }}
              >
                →
              </div>
            </div>

            {/* With */}
            <div className="rounded-2xl p-8" style={{ background: 'var(--lp-hero-bg)' }}>
              <div
                className="mb-5 flex items-center gap-2 text-[11px] font-bold tracking-[1.5px] uppercase"
                style={{ color: gold }}
              >
                <span className="h-0.5 w-4 rounded" style={{ background: gold }} />
                With LeadPlus
              </div>
              {[
                'Search 4,200+ pre-vetted vendors instantly',
                'AI matches you by capability, not marketing budget',
                'Every vendor background-checked and rated',
                'Real case studies verified by actual customers',
                'Side-by-side comparison with match scores',
                'Reach out to multiple vendors in one click',
              ].map((item, i) => (
                <div key={i} className="mb-3.5 flex items-start gap-2.5">
                  <span className="mt-0.5 shrink-0 text-sm" style={{ color: gold }}>
                    ✓
                  </span>
                  <span
                    className="text-sm leading-relaxed"
                    style={{ color: 'rgba(255,255,255,0.7)' }}
                  >
                    {item}
                  </span>
                </div>
              ))}
            </div>
          </div>
        </div>
      </section>

      {/* ══════════════ HOW IT WORKS ══════════════ */}
      <section data-section="how" className="bg-white px-4 py-20 md:px-14">
        <div className={`mx-auto max-w-7xl`}>
          <div className="mb-14 text-center">
            <div
              className="mb-3 text-[11px] font-bold tracking-[2px] uppercase"
              style={{ color: gold }}
            >
              How It Works
            </div>
            <h2 className="font-serif text-4xl font-bold tracking-[-1px]" style={{ color: dark }}>
              From search to shortlist in minutes
            </h2>
          </div>

          <div className="grid grid-cols-1 gap-8 md:grid-cols-3">
            {[
              {
                step: '01',
                title: 'Describe What You Need',
                desc: 'Search by service type, upload an RFP, or describe your project in plain language. Our AI extracts requirements and builds a structured brief.',
                detail: 'Natural language • RFP upload • Structured forms',
              },
              {
                step: '02',
                title: 'Get Intelligent Matches',
                desc: 'Our engine scores vendors across capability fit, industry experience, verified ratings, and delivery track record. Every match is explainable.',
                detail: 'Multi-factor scoring • Match % • Transparent reasoning',
              },
              {
                step: '03',
                title: 'Compare & Connect',
                desc: 'Review rich vendor profiles, compare shortlisted providers side-by-side, and reach out to multiple vendors simultaneously.',
                detail: 'Side-by-side comparison • Bulk inquiry • Direct contact',
              },
            ].map((s, i) => (
              <div key={i}>
                <div
                  className="-mb-3 font-serif text-[64px] leading-none font-extrabold"
                  style={{ color: darkA(0.03) }}
                >
                  {s.step}
                </div>
                <div
                  className="mb-3 text-xl font-semibold tracking-[-0.3px]"
                  style={{ color: dark }}
                >
                  {s.title}
                </div>
                <div className="mb-4 text-sm leading-[1.7] text-slate-500">{s.desc}</div>
                <div className="inline-block rounded-lg bg-[#f8f8f6] px-3 py-2 text-xs font-medium text-slate-400">
                  {s.detail}
                </div>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* ══════════════ TRUST TIERS ══════════════ */}
      <section data-section="trust" className="bg-lp-hero px-4 py-20 md:px-14">
        <div className={`mx-auto max-w-7xl`}>
          <div className="mb-14 text-center">
            <div
              className="mb-3 text-[11px] font-bold tracking-[2px] uppercase"
              style={{ color: gold }}
            >
              Trust System
            </div>
            <h2 className="mx-auto max-w-[550px] font-serif text-4xl font-bold tracking-[-1px] text-white">
              Verification that actually means something
            </h2>
            <p
              className="mx-auto mt-3 max-w-[500px] text-[15px]"
              style={{ color: 'rgba(255,255,255,0.45)' }}
            >
              Three tiers of trust, each with specific requirements vendors must meet
            </p>
          </div>

          <div className="grid grid-cols-1 gap-5 md:grid-cols-3">
            {[
              {
                badge: 'Basic Listed',
                color: '#94a3b8',
                bg: 'rgba(255,255,255,0.04)',
                reqs: [
                  'Completed profile',
                  'Agreed to platform terms',
                  'Basic business information',
                ],
              },
              {
                badge: 'LeadPlus Verified',
                color: '#22c55e',
                bg: 'rgba(34,197,94,0.06)',
                reqs: [
                  'Background check passed',
                  'Business registration verified',
                  'Reference validation (3+ clients)',
                  'Portfolio review completed',
                ],
              },
              {
                badge: 'LeadPlus Certified',
                color: gold,
                bg: goldA(0.08),
                reqs: [
                  'All Verified requirements',
                  'Quality assessment passed',
                  'Customer feedback score ≥ 4.5',
                  'Active case studies validated',
                  'Annual recertification',
                ],
              },
            ].map((tier, i) => (
              <div
                key={i}
                className="rounded-2xl p-7"
                style={{ background: tier.bg, border: `1px solid ${tier.color}22` }}
              >
                <div
                  className="mb-5 inline-flex items-center gap-1.5 text-[13px] font-semibold"
                  style={{ color: tier.color }}
                >
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor">
                    <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z" />
                  </svg>
                  {tier.badge}
                </div>
                {tier.reqs.map((r, j) => (
                  <div key={j} className="mb-2.5 flex items-center gap-2.5">
                    <span className="text-xs" style={{ color: tier.color }}>
                      ✓
                    </span>
                    <span
                      className="text-[13px] leading-relaxed"
                      style={{ color: 'rgba(255,255,255,0.6)' }}
                    >
                      {r}
                    </span>
                  </div>
                ))}
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* ══════════════ FINAL CTA ══════════════ */}
      <section data-section="cta" className="bg-lp-surface px-4 py-24 text-center md:px-14">
        <div className={`mx-auto max-w-[620px]`}>
          <h2
            className="mb-4 font-serif text-[42px] leading-[1.15] font-bold tracking-[-1.5px]"
            style={{ color: dark }}
          >
            Ready to find technology partners you can actually trust?
          </h2>
          <p className="mb-9 text-[17px] leading-relaxed text-slate-500">
            Join thousands of IT leaders who&apos;ve replaced guesswork with verified vendor
            intelligence.
          </p>
          <div className="flex justify-center gap-3.5">
            <Link href={appRoutes.auth.login}>
              <button
                className="lp-cta-btn rounded-[10px] px-9 py-4 text-[15px] font-semibold text-white"
                style={{ background: dark }}
              >
                Start Searching Free
              </button>
            </Link>
            <button
              className="rounded-[10px] px-9 py-4 text-[15px] font-semibold transition-all duration-200"
              style={{ background: 'transparent', border: `2px solid ${darkA(0.15)}`, color: dark }}
              onMouseEnter={(e) => (e.currentTarget.style.borderColor = dark)}
              onMouseLeave={(e) => (e.currentTarget.style.borderColor = darkA(0.15))}
            >
              Upload an RFP
            </button>
          </div>
          <div className="mt-5 text-[13px] text-slate-400">
            Free forever for buyers · No credit card required
          </div>
        </div>
      </section>

      {/* ══════════════ FOOTER ══════════════ */}
      <footer className="bg-lp-hero px-4 pt-14 pb-10 md:px-14">
        <div className="mx-auto max-w-7xl">
          <div
            className="flex flex-col justify-between gap-10 border-b pb-10 md:flex-row"
            style={{ borderColor: 'rgba(255,255,255,0.06)' }}
          >
            <div className="max-w-[300px]">
              <div className="mb-3.5 flex items-baseline gap-0.5">
                <span className="font-serif text-[22px] font-bold text-white">Lead</span>
                <span className="font-serif text-[22px] font-bold" style={{ color: gold }}>
                  Plus
                </span>
              </div>
              <p className="text-sm leading-[1.7]" style={{ color: 'rgba(255,255,255,0.35)' }}>
                The trusted marketplace for discovering pre-verified technology service providers.
                Built for IT leaders who demand better.
              </p>
            </div>
            <div className="flex gap-14 md:gap-[72px]">
              {[
                {
                  title: 'Platform',
                  links: ['Browse Vendors', 'Categories', 'How It Works', 'Pricing'],
                },
                {
                  title: 'For Vendors',
                  links: ['Get Listed', 'Vendor Pricing', 'Success Stories', 'Resources'],
                },
                { title: 'Company', links: ['About', 'Blog', 'Careers', 'Contact'] },
              ].map((col, i) => (
                <div key={i}>
                  <div
                    className="mb-4 text-[11px] font-bold tracking-[1.5px] uppercase"
                    style={{ color: 'rgba(255,255,255,0.35)' }}
                  >
                    {col.title}
                  </div>
                  {col.links.map((link, j) => (
                    <a
                      key={j}
                      href="#"
                      className="mb-3 block text-sm transition-colors duration-200"
                      style={{ color: 'rgba(255,255,255,0.45)' }}
                      onMouseEnter={(e) => (e.currentTarget.style.color = 'rgba(255,255,255,0.8)')}
                      onMouseLeave={(e) => (e.currentTarget.style.color = 'rgba(255,255,255,0.45)')}
                    >
                      {link}
                    </a>
                  ))}
                </div>
              ))}
            </div>
          </div>
          <div className="flex flex-col items-center justify-between gap-4 pt-6 md:flex-row">
            <div className="text-[13px]" style={{ color: 'rgba(255,255,255,0.25)' }}>
              © 2025 LeadPlus. All rights reserved.
            </div>
            <div className="flex gap-6">
              {['Privacy Policy', 'Terms of Service', 'Security'].map((l, i) => (
                <a
                  key={i}
                  href="#"
                  className="text-[13px]"
                  style={{ color: 'rgba(255,255,255,0.25)', textDecoration: 'none' }}
                >
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

export default LeadPlusHomepage;
