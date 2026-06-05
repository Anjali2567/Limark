import { expect, type Page, Locator } from '@playwright/test';
import { BasePage } from '../Base.page';
import { AppLayout } from '../../layouts/App.layout';
export class CompanySearchPage extends BasePage {
  readonly layout: AppLayout;

  readonly searchSidebarBtn: Locator;
  readonly companySearchMenu: Locator;
  readonly companyFilterBtn: Locator;
  readonly companyInput: Locator;
  readonly pills: Locator;
  readonly tableRows: Locator;
  readonly rowCheckboxes: Locator;
  readonly nameColumnCells: Locator;
  readonly findContactsBtn: Locator;
  readonly noteTextArea: Locator;
  readonly addNoteBtn: Locator;
  readonly notesContainer: Locator;
  readonly noteCards: Locator;
  readonly editNoteBtn: Locator;
  readonly noteTextarea: Locator;
  readonly saveNoteBtn: Locator;
  readonly headerCheckbox: Locator;
  readonly addToListBtn: Locator;
  readonly selectedCountBadge: Locator;
  readonly createNewListBtn: Locator;
  readonly newListNameInput: Locator;
  readonly createListSubmitBtn: Locator;
  readonly listItems: Locator;
  readonly addButton: Locator;
  readonly peopleTableRows: Locator;
  readonly contactsColumn: Locator;
  readonly nameColumnCell: Locator;
  readonly companyNameSpan: Locator;

  readonly websiteLinkInFirstRow: Locator;
  readonly linkedinLinkInFirstRow: Locator;

  constructor(page: Page) {
    super(page, { pagePath: '/leadgen/search/companies' });

    this.layout = new AppLayout(page);

    this.searchSidebarBtn = page.getByRole('button', { name: /search/i });
    this.companySearchMenu = page.getByText('Company Search');
    this.companyFilterBtn = page.locator(
      '[aria-controls="filter-content-companyNames"]'
    );
    this.companyInput = page.getByPlaceholder('Add company...');
    this.pills = page.locator('[data-slot="badge"] span');
    this.tableRows = page.locator('table tbody tr');
    this.rowCheckboxes = page.locator('table tbody tr [role="checkbox"]');
    this.nameColumnCells = page.locator('table tbody tr td:nth-child(1)');
    this.findContactsBtn = page.getByRole('button', { name: /find contacts/i });
    this.noteTextArea = page.getByPlaceholder('Write a note...');
    this.addNoteBtn = page.getByRole('button', { name: /add note/i });
    this.notesContainer = page.locator('div.group');
    this.editNoteBtn = page.locator('button:has(svg.lucide-pencil)');
    this.noteTextarea = page.locator('//*[@id="radix-_r_1ip_"]/div[2]/div/div/div/textarea');// textarea
    this.saveNoteBtn = page.getByRole('button', { name: /save/i });
    this.noteCards = page.locator('div.group');
    this.headerCheckbox = page.locator('table thead [role="checkbox"]');
    this.listItems = page.locator('div.max-h-64 button');
    this.addToListBtn = page.getByRole('button', {
      name: /add .* selected to list/i
    });
    this.selectedCountBadge = this.addToListBtn.locator('span');
    this.createNewListBtn = page.getByRole('button', { name: /create new list/i });
    this.newListNameInput = page.locator('#new-list-name');
    this.createListSubmitBtn = page.getByRole('button', { name: /^create list$/i });
    this.addButton = page.locator('button', { hasText: 'Add to list' });
    this.peopleTableRows = page.locator('table tbody tr');
    this.contactsColumn = page.locator('table tbody tr td:nth-child(4)');
    this.nameColumnCell = page.locator('table tbody tr td:nth-child(2)');
    this.companyNameSpan = page.locator('table tbody tr td:nth-child(2) span[title]');
    this.websiteLinkInFirstRow = page.locator('table tbody tr:first-child td:nth-child(3) a svg.lucide-globe').locator('..');
    this.linkedinLinkInFirstRow = page.locator('table tbody tr:first-child td:nth-child(3) a[href*="linkedin.com"]');
  }

  async isLoaded() {
    await this.isUrl();
  }

  async openCompanySearch() {
    await this.searchSidebarBtn.click();
    await this.companySearchMenu.click();
  }

  async expandCompanyFilter() {
    await this.companyFilterBtn.click();
  }

  async addCompanies(companies: string[]) {
    for (const company of companies) {
      await this.companyInput.click();
      await this.companyInput.fill(company);
      await this.page.keyboard.press('Enter');
    }
  }

  async assertCompaniesAdded(companies: string[]) {
    for (const company of companies) {
      await expect(this.pills.filter({ hasText: company })).toBeVisible();
    }
  }

  async assertTableMatchesCompanies(companies: string[]) {
    const rows = this.nameColumnCells;
    const count = await rows.count();

    for (let i = 0; i < count; i++) {
      const text = (await rows.nth(i).innerText()).toLowerCase();
      const matches = companies.some(c =>
        text.includes(c.toLowerCase())
      );
      expect(matches).toBeTruthy();
    }
  }

  async selectFirstTwoRows() {
    const checkboxes = this.rowCheckboxes;

    await expect.poll(async () => checkboxes.count()).toBeGreaterThan(1);
    await checkboxes.nth(0).click();
    await checkboxes.nth(1).click();
  }

  async clickFindContacts() {
    await this.findContactsBtn.click();
  }

  async clickFirstRowNoteButton() {
    const firstRow = this.tableRows.first();

    await expect(firstRow).toBeVisible();
    await firstRow
      .locator('button[data-slot="tooltip-trigger"]')
      .click();
  }

  async addTwoNotes(note1: string, note2: string) {
    await this.noteTextArea.click();
    await this.noteTextArea.fill(note1);
    await this.addNoteBtn.click();
    await this.noteTextArea.click();
    await this.noteTextArea.fill(note2);
    await this.page.keyboard.press('Control+Enter');
  }

  async assertTwoNotesDisplayed(note1: string, note2: string) {
    const notes = this.page.locator('div.group p.whitespace-pre-wrap');

    await expect(notes).toHaveCount(2);
    await expect(notes.nth(0)).toContainText(note1);
    await expect(notes.nth(1)).toContainText(note2);
  }

  async editFirstNote(newText: string) {
    const firstNote = this.noteCards.first();

    await firstNote.locator('button:has(svg.lucide-pencil)').click();

    const textarea = this.page.locator('textarea').first();

    await expect(textarea).toBeVisible();
    await textarea.click();
    await this.page.keyboard.press('Control+A');
    await this.page.keyboard.press('Backspace');
    await textarea.type(newText);
    await this.saveNoteBtn.click();
    await expect(firstNote.locator('p')).toHaveText(newText, {
      timeout: 10000
    });
  }

  async deleteNoteByIndex(index: number) {
    const note = this.noteCards.nth(index);

    await note.hover();

    const deleteBtn = note.locator('button:has(svg.lucide-trash-2)');
    await expect(deleteBtn).toBeVisible();
    await deleteBtn.click();

    const confirmDeleteBtn = this.page.getByRole('button', { name: 'Delete' });

    await expect(confirmDeleteBtn).toBeVisible();
    await confirmDeleteBtn.click();
  }

  async deleteAllNotes() {
    const count = await this.noteCards.count();

    for (let i = count - 1; i >= 0; i--) {
      await this.deleteNoteByIndex(i);
    }
  }

  async assertNoNotes() {
    await expect(this.noteCards).toHaveCount(0);
  }

  async closeNotesDrawer() {
    const closeBtn = this.page.getByRole('button', { name: 'Close' });

    await expect(closeBtn).toBeVisible();
    await closeBtn.click();
  }

  async getNotesPillCount(): Promise<number> {
    const firstRow = this.tableRows.first();

    const pill = firstRow.locator(
      'span.bg-primary.text-primary-foreground'
    );

    if (!(await pill.isVisible().catch(() => false))) {
      return 0;
    }

    const text = await pill.innerText();
    return Number(text);
  }

  async assertNotesPillCount(expected: number) {
    const firstRow = this.tableRows.first();

    const pill = firstRow.locator(
      'span.bg-primary.text-primary-foreground'
    );

    await expect(pill).toBeVisible();
    await expect(pill).toHaveText(String(expected));
  }

  async selectAllRows() {
    await expect(this.headerCheckbox).toBeVisible();
    await this.headerCheckbox.click();
  }

  async assertSelectedCount(expected: number) {
    await expect(this.selectedCountBadge).toHaveText(String(expected));
  }

  async clickAddToList() {
    await expect(this.addToListBtn).toBeVisible();
    await this.addToListBtn.click();
  }

  async createNewList(listName: string) {
    await expect(this.createNewListBtn).toBeVisible();
    await this.createNewListBtn.click();
    await expect(this.newListNameInput).toBeVisible();
    await this.newListNameInput.fill(listName);
    await expect(this.createListSubmitBtn).toBeEnabled();
    await this.createListSubmitBtn.click();
  }

  async assertListIsAutoSelected(listName: string) {
    const listRow = this.page.locator('button', {
      has: this.page.locator('span', { hasText: listName })
    });

    await expect(listRow).toBeVisible();

    const checkbox = listRow.locator('input[type="checkbox"]');

    await expect(checkbox).toBeChecked();
  }

  async addList() {
    await expect(this.addButton).toBeVisible();
    await this.addButton.click();
  }

  async getTotalContactsFromAllRows(): Promise<number> {
    const count = await this.contactsColumn.count();

    let total = 0;

    for (let i = 0; i < count; i++) {
      const cell = this.contactsColumn.nth(i);

      const value = await cell.locator('span[title]').innerText();

      total += Number(value || 0);
    }
    return total;
  }

  async assertPeopleResultsCount(expected: number) {
    await expect(this.peopleTableRows).toHaveCount(expected);
  }

  async clickFirstCompanyInTable() {
    const firstCompany = this.companyNameSpan.first();

    await expect(firstCompany).toBeVisible();
    await firstCompany.click();
  }

  async openWebsiteFromFirstRow() {
    const [newPage] = await Promise.all([
      this.page.context().waitForEvent('page'),
      this.websiteLinkInFirstRow.click(),
    ]);

    await newPage.waitForLoadState();
    return newPage;
  }

  async openLinkedInFromFirstRow() {
    const [newPage] = await Promise.all([
      this.page.context().waitForEvent('page'),
      this.linkedinLinkInFirstRow.click(),
    ]);

    await newPage.waitForLoadState();
    return newPage;
  }
}



