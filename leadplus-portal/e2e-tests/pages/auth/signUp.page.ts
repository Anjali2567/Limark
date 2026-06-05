import { expect, type Locator, type Page } from '@playwright/test';
import { BasePage } from '../Base.page';
import { AppLayout } from '../../layouts/App.layout';
export class SignUpPage extends BasePage {
  readonly layout: AppLayout;

  readonly fullNameInput: Locator;
  readonly emailInput: Locator;
  readonly passwordInput: Locator;
  readonly companyInput: Locator;
  readonly phoneInput: Locator;
  readonly createAccountButton: Locator;
  readonly nameError: Locator;
  readonly emailError: Locator;
  readonly passwordError: Locator;
  readonly companyError: Locator;
  readonly invalidEmailError: Locator;
  readonly passwordCriteriaError: Locator;

  constructor(page: Page) {
    super(page, { pagePath: '/signup/?type=vendor'});

    this.layout = new AppLayout(page);

    this.fullNameInput = page.getByPlaceholder('Enter your full name');
    this.emailInput = page.getByPlaceholder('Enter your email');
    this.passwordInput = page.getByPlaceholder('Create a password');
    this.companyInput = page.getByPlaceholder('Enter your company name');
    this.phoneInput = page.getByPlaceholder('+1 (555) 000-0000');
    this.createAccountButton = page.getByRole('button', { name: 'Create Account' });
    this.nameError = page.getByText('Name is required');
    this.emailError = page.getByText('Email is required');
    this.passwordError = page.getByText('Password must be at least 8 characters');
    this.companyError = page.getByText('Company is required');
    this.invalidEmailError = page.getByText('Invalid email address');
    this.passwordCriteriaError = page.getByText(
      'Password must include uppercase, lowercase, number, and special character.'
    );
}

  async isLoaded() {
    await expect(this.fullNameInput).toBeVisible();
    await expect(this.emailInput).toBeVisible();
    await expect(this.passwordInput).toBeVisible();
    await expect(this.companyInput).toBeVisible();
    await expect(this.phoneInput).toBeVisible();
    await expect(this.createAccountButton).toBeVisible();
  }

  async submitForm() {
    await this.createAccountButton.click();
  }

  async expectEmptyFieldErrors() {
    await this.submitForm();

    await expect(this.nameError).toBeVisible();
    await expect(this.emailError).toBeVisible();
    await expect(this.passwordError).toBeVisible();
    await expect(this.companyError).toBeVisible();
  }

  async expectInvalidEmail(data: {
    fullName: string;
    email: string;
    password: string;
    company: string;
    phone: string;
  }) 
  {
    await this.fullNameInput.fill(data.fullName);
    await this.emailInput.fill(data.email);
    await this.passwordInput.fill(data.password);
    await this.companyInput.fill(data.company);
    await this.phoneInput.fill(data.phone);
    await this.submitForm();
    await expect(this.invalidEmailError).toBeVisible();
  }

  async expectWeakPassword(data: {
    fullName: string;
    email: string;
    password: string;
    company: string;
    phone: string;
  }) 
  {
    await this.fullNameInput.fill(data.fullName);
    await this.emailInput.fill(data.email);
    await this.passwordInput.fill(data.password);
    await this.companyInput.fill(data.company);
    await this.phoneInput.fill(data.phone);
    await this.submitForm();
    await expect(this.passwordCriteriaError).toBeVisible();
  }

  async signUp(data: {
    fullName: string;
    email: string;
    password: string;
    company: string;
    phone: string;
  }) 
  {
    await this.fullNameInput.fill(data.fullName);
    await this.emailInput.fill(data.email);
    await this.passwordInput.fill(data.password);
    await this.companyInput.fill(data.company);
    await this.phoneInput.fill(data.phone);
    await this.submitForm();
  }
}