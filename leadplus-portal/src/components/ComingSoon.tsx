import { Wand2 } from "lucide-react";

const ComingSoon = () => {
  return (
    <div className="flex items-center justify-center h-full">
      <div className="max-w-md space-y-4 text-center">
        <div className="w-20 h-20 bg-slate-100 rounded-full flex items-center justify-center mx-auto text-slate-300">
          <Wand2 className="h-10 w-10" />
        </div>
        <h1 className="text-3xl font-bold text-slate-900">Coming Soon</h1>
        <p className="text-slate-500 text-lg">
          We&apos;re working hard to bring you the best lead management
          experience. Stay tuned for updates!
        </p>
      </div>
    </div>
  );
};

export { ComingSoon };
