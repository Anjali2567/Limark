import { expect, type Locator, type Page } from '@playwright/test';
import { BasePage } from '../Base.page';
import { AppLayout } from '../../layouts/App.layout';
export class SignInPage extends BasePage {
  readonly layout: AppLayout;

  readonly emailInput: Locator;
  readonly passwordInput: Locator;
  readonly signInButton: Locator;
  readonly errorMessage: Locator;
  readonly emailError: Locator;
  readonly passwordError: Locator;

  constructor(page: Page) {
    super(page, { pagePath: '/' });

    this.layout = new AppLayout(page);

    this.emailInput = page.getByPlaceholder('Enter your email');
    this.passwordInput = page.getByPlaceholder('Enter your password');
    this.signInButton = page.getByRole('button', { name: 'Login' });
    this.errorMessage = page
      .getByText('Invalid credentials')
      .first();
    this.emailError = page.getByText('Email is required');
    this.passwordError = page.getByText('Password must be at least 8 characters');
  }

  async isLoaded() {
    await expect(this.emailInput).toBeVisible();
    await expect(this.passwordInput).toBeVisible();
    await expect(this.signInButton).toBeVisible();
  }

  async login(email: string, password: string) {
    await this.emailInput.fill(email);
    await this.passwordInput.fill(password);
    await this.signInButton.click();
  }

  async submitInvalidLogin(email: string, password: string) {
    await this.login(email, password);
  }

  async expectInvalidCredentials() {
    await expect(this.errorMessage).toBeVisible();
  }

  async expectEmptyFieldErrors() {
    await this.signInButton.click();
    await expect(this.emailError).toBeVisible();
    await expect(this.passwordError).toBeVisible();
  }
}