"use client";

import { ChangeEvent, useState, useEffect, useRef } from "react";

import { cn } from "@/lib/utils/helpers";
import { Input } from "./ui/input";

import { Search, XIcon } from "lucide-react";
import { Button } from "./ui/button";

type SearchBarProps = {
  value?: string;
  className?: string;
  inputClassName?: string;
  onChange?: (search: string) => void;
  placeholder?: string;
};

const SearchBar = ({
  value,
  className,
  inputClassName,
  onChange,
  placeholder = "Search companies or contacts...",
}: SearchBarProps) => {
  const [search, setSearch] = useState<string>(value || "");

  const onChangeRef = useRef(onChange);

  useEffect(() => {
    onChangeRef.current = onChange;
  }, [onChange]);

  useEffect(() => {
    if (search === value) return;

    const delayDebounceFn = setTimeout(() => {
      onChangeRef.current?.(search);
    }, 500);

    return () => clearTimeout(delayDebounceFn);
  }, [search, value]);

  const handleSearchChange = (e: ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value;
    setSearch(value);
  };

  const handleClearSearch = () => {
    setSearch("");
  };

  return (
    <div className={cn("relative", className)}>
      <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
      <Input
        placeholder={placeholder}
        className={cn("pl-9 bg-card", inputClassName)}
        value={search}
        onChange={handleSearchChange}
      />
      {search && (
        <Button
          variant="none"
          size="icon"
          onClick={handleClearSearch}
          aria-label="Clear Search"
          className="absolute right-0 top-1/2 -translate-y-1/2"
        >
          <XIcon />
        </Button>
      )}
    </div>
  );
};

export { SearchBar };
