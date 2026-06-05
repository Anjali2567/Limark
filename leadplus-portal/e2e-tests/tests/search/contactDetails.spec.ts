import { test } from '../../fixtures';
import { credentials } from '../../test-data/credentials.data';
import { companySearchData } from '../../test-data/companySearch.data';

test.describe('Contact Details View', () => {

    test.beforeEach(async ({
        vendorSignInPage,
        dashboardPage,
    }) => {

        await vendorSignInPage.goto();

        await vendorSignInPage.login(
            credentials.vendor.username,
            credentials.vendor.password
        );

        await dashboardPage.isLoaded();
    });

    test('Contact details page tabs and external links validation', async ({
        dashboardPage,
        companyDetailsPage,
        contactDetailsPage,
    }) => {

        const { companies_detailedview_scenario } = companySearchData;

        await test.step('Add companies via filter', async () => {
            await dashboardPage.expandCompanyFilter();
            await dashboardPage.addCompanies(companies_detailedview_scenario);
        });

        await test.step('Verify companies are added', async () => {
            await dashboardPage.assertCompaniesAdded(companies_detailedview_scenario);
        });

        await test.step('Open first company from table', async () => {
            await dashboardPage.clickFirstCompanyInTable();
        });

        await test.step('Verify navigation to company details page', async () => {
            await companyDetailsPage.isLoaded();
        });

        await test.step('Open Overview tab', async () => {
            await companyDetailsPage.openOverviewTab();
        });

        await test.step('Open first contact from Overview table', async () => {
            await companyDetailsPage.clickFirstContact();
        });

        await test.step('Verify navigation to contact details page', async () => {
            await contactDetailsPage.isLoaded();
        });

        await test.step('Validate contact LinkedIn profile', async () => {
            const tab = await contactDetailsPage.openContactLinkedIn();
            await contactDetailsPage.assertLinkedInProfilePage(tab);
            await tab.close();
        });

        await test.step('Validate company LinkedIn profile', async () => {
            const tab = await contactDetailsPage.openCompanyLinkedIn();
            await contactDetailsPage.assertLinkedInCompanyPage(tab);
            await tab.close();
        });

        await test.step('Validate company website', async () => {
            const tab = await contactDetailsPage.openCompanyWebsite();
            await contactDetailsPage.assertCompanyWebsite(tab);
            await tab.close();
        });

        await test.step('Navigate to Keywords tab from contact details', async () => {
            await contactDetailsPage.openKeywordsTab();
            await contactDetailsPage.isLoaded();
        });

        await test.step('Navigate to Technologies tab from contact details', async () => {
            await contactDetailsPage.openTechnologiesTab();
            await contactDetailsPage.isLoaded();
        });

        await test.step('Navigate to History tab from contact details', async () => {
            await contactDetailsPage.openHistoryTab();
            await contactDetailsPage.isLoaded();
        });

    });

});

