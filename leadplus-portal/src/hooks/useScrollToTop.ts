import { RefObject, useCallback } from "react";

export const useScrollToTop = (ref: RefObject<HTMLElement | null>) => {
  const scrollToTop = useCallback(() => {
    if (ref.current) {
      ref.current.scrollIntoView({ behavior: "smooth", block: "start" });
    }
  }, [ref]);

  return { scrollToTop };
};
