import { test } from '../../fixtures';
import { credentials } from '../../test-data/credentials.data';

test.describe('Lists Flow', () => {

    test.beforeEach(async ({
        vendorSignInPage,
        dashboardPage,
        listsPage,
    }) => {

        await vendorSignInPage.goto();
        await vendorSignInPage.login(
            credentials.vendor.username,
            credentials.vendor.password
        );
        await dashboardPage.isLoaded();
        await dashboardPage.layout.sidebar.goToLists();
        await listsPage.isLoaded();
    });

    test('Navigation to Companies', async ({ listsPage }) => {

        await test.step('Verify companies page loads correctly', async () => {
            await listsPage.switchToCompanies();
        });
    });

});