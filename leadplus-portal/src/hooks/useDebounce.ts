import { useEffect, useRef } from 'react';

/**
 * A custom hook that debounces a callback function
 * @param value The value to be debounced
 * @param delay Delay in milliseconds
 * @param callback Function to be called after the delay
 */
export const useDebounce = <T>(value: T, delay: number, callback: (value: T) => void) => {
  const callbackRef = useRef(callback);

  useEffect(() => {
    callbackRef.current = callback;
  }, [callback]);

  useEffect(() => {
    const timeoutId = setTimeout(() => {
      callbackRef.current(value);
    }, delay);

    return () => {
      clearTimeout(timeoutId);
    };
  }, [value, delay]);
};
