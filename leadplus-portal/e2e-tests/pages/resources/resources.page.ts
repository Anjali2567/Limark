import { BasePage } from '../Base.page';
import { AppLayout } from '../../layouts/App.layout';
import type { Page, Locator } from '@playwright/test';
import { expect } from '@playwright/test';
import path from 'path';
import { getAllAssetFiles } from '../../utils/fileHelper';

export class ResourcesPage extends BasePage {
  readonly layout: AppLayout;

  readonly uploadButton: Locator;
  readonly fileInput: Locator;
  readonly confirmUploadButton: Locator;
  readonly uploadModal: Locator;
  readonly table: Locator;
  readonly firstRow: Locator;
  readonly tableCellFirstColumn: Locator;
  readonly alertDialog: Locator;
  readonly deleteButton: Locator;
  readonly cancelButton: Locator;
  readonly renameOption: Locator;
  readonly deleteOption: Locator;
  readonly renameInput: Locator;
  readonly saveButton: Locator;

  constructor(page: Page) {
    super(page, { pagePath: '/resources' });

    this.layout = new AppLayout(page);

    this.uploadButton = page.getByRole('button', {
      name: 'Upload Documents',
    });

    this.fileInput = page.locator('input[type="file"]');

    this.confirmUploadButton = page.getByRole('button', {
      name: 'Upload Files',
    });

    this.uploadModal = page.locator(
      'div[role="dialog"][data-state="open"]'
    );

    this.table = page.locator('table');
    this.firstRow = this.table.locator('tbody tr').first();
    this.tableCellFirstColumn = this.table.locator('td:nth-child(1)');
    this.alertDialog = page.getByRole('alertdialog');

    this.deleteButton = this.alertDialog.getByRole('button', {
      name: 'Delete',
    });

    this.cancelButton = this.alertDialog.getByRole('button', {
      name: 'Cancel',
    });

    this.renameOption = page.getByRole('menuitem', {
      name: 'Rename',
    });

    this.deleteOption = page.getByRole('menuitem', {
      name: 'Delete',
    });

    this.renameInput = page.locator('input#name');

    this.saveButton = page.getByRole('button', {
      name: 'Save Changes',
    });
  }

  async isLoaded() {
    await expect(this.page).toHaveURL(/resources/);
    await expect(this.uploadButton).toBeVisible();
  }

  async openUploadModal() {
    await this.uploadButton.click();
    await expect(this.uploadModal).toBeVisible();
  }

  async uploadAllFilesAndSubmit() {
    const files = getAllAssetFiles();

    await this.fileInput.setInputFiles(files);
    await this.confirmUploadButton.click();
  }

  async expectUploadCompleted() {
    await this.page.waitForLoadState('networkidle');

    const isUploadButtonVisible = await this.uploadButton
      .isVisible()
      .catch(() => false);

    if (!isUploadButtonVisible) {
      await expect(this.table).toBeVisible({ timeout: 30000 });
    } else {
      await expect(this.uploadButton).toBeVisible();
    }
  }

  async expectFilesInTable() {
    const files = getAllAssetFiles();

    for (const filePath of files) {
      const fileName = path.basename(filePath);
      const baseName = path.parse(fileName).name;

      await expect(this.table).toContainText(baseName, {
        timeout: 60000,
      });
    }
  }

  async validateUploadedFilesTable() {
    const files = getAllAssetFiles();
    const today = new Date().toISOString().split('T')[0];

    for (const filePath of files) {
      const fileName = path.basename(filePath);
      const baseName = path.parse(fileName).name;
      const expectedFormat = path
        .extname(fileName)
        .replace('.', '')
        .toUpperCase();

      const row = this.table.locator('tbody tr').filter({
        has: this.tableCellFirstColumn.filter({
          hasText: baseName,
        }),
      }).first();

      await expect(row).toBeVisible({ timeout: 60000 });

      const format = await row.locator('td:nth-child(3)').textContent();
      const modifiedDateRaw = await row.locator('td:nth-child(4)').textContent();

      expect(format?.trim().toUpperCase()).toBe(expectedFormat);

      const uiDate = modifiedDateRaw?.trim().split(' ')[0];
      expect(uiDate).toBe(today);
    }
  }

  async openActionsForFirstRow() {
    await expect(this.firstRow).toBeVisible();

    const actionsButton = this.firstRow.locator('td:nth-child(5) button');
    await actionsButton.click();
  }

  async clickRename() {
    await expect(this.renameOption).toBeVisible();
    await this.renameOption.click();
  }

  async expectRenameInputAutoFilled() {
    const expectedName = await this.firstRow
      .locator('td:nth-child(1)')
      .textContent();

    await expect(this.renameInput).toBeVisible();

    const actualValue = await this.renameInput.inputValue();

    expect(actualValue?.trim()).toBe(expectedName?.trim());
  }

  async renameFile(newName: string) {
    await expect(this.renameInput).toBeVisible();
    await this.renameInput.click();
    await this.renameInput.press('Control+A');
    await this.renameInput.press('Backspace');
    await this.renameInput.type(newName);
    await expect(this.renameInput).toHaveValue(newName);
    await expect(this.saveButton).toBeEnabled();
    await this.saveButton.click();
    await this.page.waitForLoadState('networkidle');
  }

  async logFirstRow() {
    await expect(this.firstRow).toBeVisible();
  }

  async openActionsForRow(rowIndex: number = 1) {
    const row = this.table.locator('tbody tr').nth(rowIndex);

    await expect(row).toBeVisible();

    const actionsButton = row.locator('td:nth-child(5) button');
    await actionsButton.click();
  }

  async clickDelete() {
    await expect(this.deleteOption).toBeVisible();
    await this.deleteOption.click();
  }

  async confirmDelete() {
    await expect(this.alertDialog).toBeVisible();
    await expect(this.deleteButton).toBeVisible();
    await this.deleteButton.click();
    await this.alertDialog.waitFor({ state: 'hidden' });
  }

  async cancelDelete() {
    await expect(this.alertDialog).toBeVisible();
    await this.cancelButton.click();
    await expect(this.alertDialog).toBeHidden();
  }
}