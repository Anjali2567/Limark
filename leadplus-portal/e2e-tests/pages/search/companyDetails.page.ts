import { expect, type Page, Locator } from '@playwright/test';
import { BasePage } from '../Base.page';

export class CompanyDetailsPage extends BasePage {

  readonly overviewTab: Locator;
  readonly technologiesTab: Locator;
  readonly keywordsTab: Locator;
  readonly jobPostingsTab: Locator;
  readonly contactsTableRows: Locator;
  readonly contactNameButtons: Locator;

  constructor(page: Page) {
    super(page, { pagePath: '/leadgen/search/company-info' });

    this.overviewTab = page.getByRole('button', { name: /overview/i });
    this.technologiesTab = page.getByRole('button', { name: /technologies/i });
    this.keywordsTab = page.getByRole('button', { name: /keywords/i });
    this.jobPostingsTab = page.getByRole('button', { name: /job postings/i });
    this.contactsTableRows = page.locator('table tbody tr');
    this.contactNameButtons = this.contactsTableRows.locator('td:nth-child(1) button');
  }

  async isLoaded() {
    await expect(this.page).toHaveURL(/company-info/);
  }

  async openOverviewTab() {
    await Promise.all([
      this.page.waitForURL(/tab=overview/),
      this.overviewTab.click(),
    ]);
  }

  async clickFirstContact() {
    const firstName = this.contactNameButtons.first();

    await expect(firstName).toBeVisible();
    await Promise.all([
      this.page.waitForURL(/contact-info/),
      firstName.click(),
    ]);
  }

  async openTechnologiesTab() {
    await Promise.all([
      this.page.waitForURL(/tab=technologies/),
      this.technologiesTab.click(),
    ]);
  }

   async openKeywordsTab() {
    await Promise.all([
      this.page.waitForURL(/tab=keywords/),
      this.keywordsTab.click(),
    ]);
  }

    async openJobPostingsTab() {
    await Promise.all([
      this.page.waitForURL(/tab=job-postings/),
      this.jobPostingsTab.click(),
    ]);
}
}