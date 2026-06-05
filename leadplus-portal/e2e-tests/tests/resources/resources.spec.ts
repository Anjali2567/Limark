import { test } from '../../fixtures';
import { credentials } from '../../test-data/credentials.data';
import { resourceTestData } from '../../test-data/resources.data';

test.describe('Resources Upload Flow', () => {

  test.beforeEach(async ({
    vendorSignInPage,
    dashboardPage,
    resourcesPage,
  }) => {

    await vendorSignInPage.goto();

    await vendorSignInPage.login(
      credentials.vendor.username,
      credentials.vendor.password
    );

    await dashboardPage.isLoaded();
    await dashboardPage.layout.sidebar.goToResources();

    await resourcesPage.isLoaded();
  });

  test('Upload and Validate Files', async ({ resourcesPage }) => {

    await test.step('Upload files', async () => {
      await resourcesPage.openUploadModal();
      await resourcesPage.uploadAllFilesAndSubmit();
      await resourcesPage.expectUploadCompleted();

    });


    await test.step('Validate uploaded files in table', async () => {
      await resourcesPage.validateUploadedFilesTable();
    });

  });

  test('Rename First File in Table', async ({ resourcesPage }) => {
    const newName = resourceTestData.renameFileName;

    await resourcesPage.openActionsForFirstRow();
    await resourcesPage.clickRename();
    await resourcesPage.expectRenameInputAutoFilled();

    await resourcesPage.renameFile(newName);

    await resourcesPage.page.waitForLoadState('networkidle');
    await resourcesPage.logFirstRow();
  });


  test('Delete second file in table', async ({ resourcesPage }) => {

  await test.step('Open actions for second row', async () => {
    await resourcesPage.openActionsForRow(1); 
  });

  await test.step('Click delete option', async () => {
    await resourcesPage.clickDelete();
  });

  await test.step('Cancel delete', async () => {
    await resourcesPage.cancelDelete();

    await resourcesPage.openActionsForRow(1);
    await resourcesPage.clickDelete();
  });

  await test.step('Confirm delete', async () => {
    await resourcesPage.confirmDelete();
  });
});

});