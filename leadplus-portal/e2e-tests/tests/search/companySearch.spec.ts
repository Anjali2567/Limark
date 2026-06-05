import { expect, test } from '../../fixtures';
import { credentials } from '../../test-data/credentials.data';
import { companySearchData } from '../../test-data/companySearch.data';

test.describe('Company Search - Full Flow', () => {

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

    test('Company search and notes management', async ({
        dashboardPage,
    }) => {

        const { companies_notes_scenarios, notes } = companySearchData;

        await test.step('Add companies via filter', async () => {
            await dashboardPage.expandCompanyFilter();
            await dashboardPage.addCompanies(companies_notes_scenarios);
        });

        await test.step('Verify companies added and table matches', async () => {
            await dashboardPage.assertCompaniesAdded(companies_notes_scenarios);
            await dashboardPage.assertTableMatchesCompanies(companies_notes_scenarios);
        });

        let beforeCount: number;

        await test.step('Open notes drawer and add notes', async () => {
            beforeCount = await dashboardPage.getNotesPillCount();

            await dashboardPage.clickFirstRowNoteButton();
            await dashboardPage.addTwoNotes(notes.first, notes.second);
        });

        await test.step('Close drawer and verify notes count increased', async () => {
            await dashboardPage.closeNotesDrawer();
            await dashboardPage.assertNotesPillCount(beforeCount + 2);
        });

        await test.step('Edit first note', async () => {
            await dashboardPage.clickFirstRowNoteButton();
            await dashboardPage.editFirstNote(notes.updated);
        });

        await test.step('Delete all notes and verify empty state', async () => {
            await dashboardPage.deleteAllNotes();
            await dashboardPage.assertNoNotes();
        });
    });

    test('Add companies to a list', async ({
        dashboardPage,
        listsPage
    }) => {

        const { companies_lists_scenarios } = companySearchData;
        const expectedUser = credentials.vendor.name;

        await test.step('Add companies via filter', async () => {
            await dashboardPage.expandCompanyFilter();
            await dashboardPage.addCompanies(companies_lists_scenarios);
        });

        await test.step('Verify companies added and table matches', async () => {
            await dashboardPage.assertCompaniesAdded(companies_lists_scenarios);
            await dashboardPage.assertTableMatchesCompanies(companies_lists_scenarios);
        });

        await test.step('Select all companies using header checkbox', async () => {
            await dashboardPage.selectAllRows();
        });

        await test.step('Verify selected count matches companies', async () => {
            await dashboardPage.assertSelectedCount(companies_lists_scenarios.length);
        });

        await test.step('Click add to list', async () => {
            await dashboardPage.clickAddToList();
        });

        await test.step('Click create new list', async () => {
            const listName = `Test 3 List ${Date.now()}`;

            await dashboardPage.createNewList(listName);
            await dashboardPage.assertListIsAutoSelected(listName);
            await dashboardPage.addList();
            await dashboardPage.layout.sidebar.goToLists();
            await listsPage.isLoaded();
            await listsPage.switchToCompanies();
            await listsPage.assertListExistsInTable(listName);
            await test.step('Verify records count matches selected companies', async () => {
                await listsPage.assertRecordsCount(
                    listName,
                    companies_lists_scenarios.length
                );
            });

            await test.step('Verify last modified date is today', async () => {
                await listsPage.assertLastModifiedIsToday(listName);
            });

            await test.step('Verify created by is logged in user', async () => {
                await listsPage.assertCreatedBy(listName, expectedUser);
            });
        });
    });


    test('Find Contacts of selected companies', async ({
        dashboardPage,
    }) => {

        const { companies_findcontacts_scenario } = companySearchData;

        await test.step('Add companies via filter', async () => {
            await dashboardPage.expandCompanyFilter();
            await dashboardPage.addCompanies(companies_findcontacts_scenario);
        });

        await test.step('Verify companies added and table matches', async () => {
            await dashboardPage.assertCompaniesAdded(companies_findcontacts_scenario);
            await dashboardPage.assertTableMatchesCompanies(companies_findcontacts_scenario);
        });

        await test.step('Select all companies using header checkbox', async () => {
            await dashboardPage.selectAllRows();
        });

        let totalContacts: number;

        await test.step('Calculate total contacts from selected companies', async () => {
            totalContacts = await dashboardPage.getTotalContactsFromAllRows();
        });

        await test.step('Click find contacts button', async () => {
            await dashboardPage.clickFindContacts();
        });

        await test.step('Verify People Search results match total contacts', async () => {
            await dashboardPage.assertPeopleResultsCount(totalContacts);
        });
    });


    test('Companies table features', async ({
        dashboardPage,
    }) => {

        const { companies_detailedview_scenario } = companySearchData;


        await test.step('Add companies via filter', async () => {
            await dashboardPage.expandCompanyFilter();
            await dashboardPage.addCompanies(companies_detailedview_scenario);
        });

        await test.step('Verify companies are added', async () => {
            await dashboardPage.assertCompaniesAdded(companies_detailedview_scenario);
        });

        await test.step('Open website from first row', async () => {
            const websiteTab = await dashboardPage.openWebsiteFromFirstRow();

            await expect(websiteTab).toHaveURL(/lat\.ai|http/);
            await websiteTab.close();
        });

        await test.step('Open LinkedIn from first row', async () => {
            const linkedInTab = await dashboardPage.openLinkedInFromFirstRow();

            await expect(linkedInTab).toHaveURL(/linkedin\.com/);
            await linkedInTab.close();
        });
    });



    test('Detailed company view', async ({
        dashboardPage,
        companyDetailsPage
    }) => {

        const { companies_detailedview_scenario } = companySearchData;

        await test.step('Add companies via filter', async () => {
            await dashboardPage.expandCompanyFilter();
            await dashboardPage.addCompanies(companies_detailedview_scenario);
        });

        await test.step('Verify companies are added', async () => {
            await dashboardPage.assertCompaniesAdded(companies_detailedview_scenario);
        });

        await test.step('Click first company in table', async () => {
            await dashboardPage.clickFirstCompanyInTable();
            await companyDetailsPage.isLoaded();
        });
    });
});
