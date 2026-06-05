import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  // Force static export mode
  output: "export",

  // Ensure all routes end with a trailing slash for S3 compatibility
  trailingSlash: true,

  // Disable Next.js image optimizer (requires server runtime)
  images: {
    unoptimized: true,
  },

  // Optional: keep React Compiler off for stability
  // reactCompiler: false, // safer for static export

  // Disable dev indicators like the Next.js logo
  devIndicators: false,
};

export default nextConfig;
