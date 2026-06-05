import { test as base, expect } from '@playwright/test';
import { SignInPage } from '../pages/auth/signIn.page';
import { CompanySearchPage } from '../pages/search/companySearch.page';
import { SignUpPage } from '../pages/auth/signUp.page';
import { ResourcesPage } from '../pages/resources/resources.page';
import { WorkspacesPage } from '../pages/workspace/workspaces.page';
import { ListsPage } from '../pages/search/lists.page';
import { CompanyDetailsPage } from '../pages/search/companyDetails.page';
import { ContactDetailsPage } from '../pages/search/contactDetails.page';

type CommonFixtures = {
  vendorSignInPage: SignInPage;
  dashboardPage: CompanySearchPage;
  signUpPage: SignUpPage;
  resourcesPage: ResourcesPage;
  workspacesPage: WorkspacesPage;
  listsPage: ListsPage;
  companyDetailsPage: CompanyDetailsPage;
  contactDetailsPage: ContactDetailsPage;
};

export const test = base.extend<CommonFixtures>({
  vendorSignInPage: async ({ page }, use) => {
    await use(new SignInPage(page));
  },

  dashboardPage: async ({ page }, use) => {
    await use(new CompanySearchPage(page));
  },

  signUpPage: async({ page }, use) => {
    await use(new SignUpPage(page));
  },

  resourcesPage: async({ page }, use) => {
    await use(new ResourcesPage(page));
  },

  workspacesPage: async({ page }, use) => {
    await use(new WorkspacesPage(page));
  },

  listsPage: async({ page }, use) => {
    await use(new ListsPage(page));
  },

  companyDetailsPage: async ({ page }, use) => {
    await use(new CompanyDetailsPage(page));
  },

  contactDetailsPage: async ({ page }, use) => {
    await use(new ContactDetailsPage(page));
  },
});

export { expect };