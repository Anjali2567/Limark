import { expect, test } from '../../fixtures';
import { credentials } from '../../test-data/credentials.data';
import { companySearchData } from '../../test-data/companySearch.data';

test.describe('Company Details View', () => {

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

    test('Detailed company view - tabs navigation', async ({
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

        await test.step('Technologies tab', async () => {
            await companyDetailsPage.openTechnologiesTab();
        });

        await test.step('Keywords tab', async () => {
            await companyDetailsPage.openKeywordsTab();
        });

        await test.step('Job Postings tab - external links validation', async () => {
            await companyDetailsPage.openJobPostingsTab();
        });

        await test.step('Overview tab', async () => {
            await companyDetailsPage.openOverviewTab();
            await companyDetailsPage.clickFirstContact();
            await contactDetailsPage.isLoaded();
        });
    });

});