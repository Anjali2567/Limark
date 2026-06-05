import { test, expect } from '../../fixtures';
import { credentials } from '../../test-data/credentials.data';

test.describe('Authentication flow', { tag: '@auth' }, () => {

  // VALID LOGIN
  test('Valid login - user lands on dashboard', async ({
    vendorSignInPage,
    dashboardPage,
  }) => {

    await vendorSignInPage.goto();
    await vendorSignInPage.isLoaded();

    await vendorSignInPage.login(
      credentials.vendor.username,
      credentials.vendor.password
    );

    await dashboardPage.isLoaded();

    await expect(dashboardPage.page).toHaveURL(/\/leadgen\/search\/companies/);
  });

  // INVALID LOGIN
  test('Invalid credentials show error message', async ({
    vendorSignInPage,
  }) => {

    await vendorSignInPage.goto();
    await vendorSignInPage.isLoaded();

    await vendorSignInPage.login(
      credentials.invalidVendor.username,
      credentials.invalidVendor.password
    );

    await vendorSignInPage.expectInvalidCredentials();
  });

  // EMPTY FIELDS
  test('Empty fields show validation messages', async ({
    vendorSignInPage,
  }) => {

    await vendorSignInPage.goto();
    await vendorSignInPage.isLoaded();

    await vendorSignInPage.expectEmptyFieldErrors();
  });

});