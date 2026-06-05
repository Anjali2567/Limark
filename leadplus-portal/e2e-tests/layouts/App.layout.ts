import { Sidebar } from './Sidebar.layout';
import { type Page, expect, type Locator } from '@playwright/test';

export class AppLayout {
  readonly sidebar: Sidebar;
  readonly profileIcon: Locator;
  readonly logoutOption: Locator;

  constructor(private page: Page) {
    this.sidebar = new Sidebar(page);
    this.profileIcon = this.page
      .locator('button[data-slot="dropdown-menu-trigger"]')
      .last();
    this.logoutOption = this.page.getByRole('menuitem', { name: 'Log out' });
  }

  async openProfileMenu() {
    await this.profileIcon.click();
  }

  async logout() {
    await this.openProfileMenu();
    await expect(this.logoutOption).toBeVisible();
    await this.logoutOption.click();
  }
}