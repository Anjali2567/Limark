export default function Loading() {
  return (
    <div className="border-t-primary z-10 flex h-100 w-full max-w-md items-center justify-center rounded-lg border-t-4 bg-white/95 shadow-2xl backdrop-blur-sm transition-all duration-300 ease-in-out">
      <div className="text-center">
        <div className="mx-auto h-12 w-12 animate-spin rounded-full border-4 border-blue-500 border-t-transparent"></div>
      </div>
    </div>
  );
}
