import { test, expect } from '../../fixtures';
import { credentials } from '../../test-data/credentials.data';

test.describe('Logout from the portal', { tag: '@auth' }, () => {

  test('Logout and redirect to login page', async ({
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

    await dashboardPage.layout.logout(); 

    await expect(vendorSignInPage.emailInput).toBeVisible();
    await expect(vendorSignInPage.page).toHaveURL(/login/);
  });

});