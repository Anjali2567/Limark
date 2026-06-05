import { type Page, expect } from '@playwright/test';

export abstract class BasePage {
  readonly page: Page;
  readonly pagePath: string;

  constructor(page: Page, { pagePath }: { pagePath: string }) {
    this.page = page;
    this.pagePath = pagePath;
  }

  async goto(url?: string, options?: Parameters<Page['goto']>[1]) {
    await this.page.goto(url ?? this.pagePath, options);
  }

  async isUrl(url?: string) {
    const expected = url ?? this.pagePath;
    await expect(this.page).toHaveURL(new RegExp(expected));
  }

  async isLoaded() {
    await this.isUrl();
  }

  async reloadPage() {
    await this.page.reload();
  }
}