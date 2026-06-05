import { type Page, type Locator } from '@playwright/test';
export class Sidebar {
  readonly resourcesLink: Locator;
  readonly searchLink: Locator;
  readonly campaignsLink: Locator;
  readonly workspacesLink: Locator;
  readonly listsLink: Locator;

  constructor(private page: Page) {
    this.resourcesLink = this.page.getByText('Resources');
    this.searchLink = this.page.getByRole('button', { name: /^Search$/ });
    this.campaignsLink = this.page.getByText('All Campaigns');
    this.workspacesLink = this.page.getByRole('link', { name: 'Workspace' });
    this.listsLink = this.page.getByRole('link', { name: /lists/i });
  }

  async goToResources() {
    await this.resourcesLink.click();
  }

  async goToSearch() {
    await this.searchLink.click();
  }

  async goToCampaigns() {
    await this.campaignsLink.click();
  }

  async goToWorkspaces() {
    await this.workspacesLink.click();
  }

  async expandSearch() {
    await this.searchLink.click();
  }

  async goToLists() {
    await this.expandSearch();
    await this.listsLink.click();
  }
}