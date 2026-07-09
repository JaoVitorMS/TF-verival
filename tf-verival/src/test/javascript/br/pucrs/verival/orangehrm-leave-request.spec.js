const { expect, test } = require('@playwright/test');

const enabled = process.env.ORANGEHRM_E2E_ENABLED === 'true';
const adminUsername = process.env.ORANGEHRM_ADMIN_USERNAME || 'Admin';
const adminPassword = process.env.ORANGEHRM_ADMIN_PASSWORD || 'admin123';
const employeePassword = process.env.ORANGEHRM_EMPLOYEE_PASSWORD || 'P@ssw0rd123!';
const leaveType = process.env.ORANGEHRM_LEAVE_TYPE || 'CAN - Vacation';
const fromDate = process.env.ORANGEHRM_FROM_DATE || '2026-22-12';
const toDate = process.env.ORANGEHRM_TO_DATE || '2026-23-12';
const baseUrl = process.env.ORANGEHRM_BASE_URL || 'https://opensource-demo.orangehrmlive.com';

test.use({
  baseURL: baseUrl,
  headless: false,
  viewport: {
    width: 1400,
    height: 900,
  },
  launchOptions: {
    slowMo: 300,
  },
});

test.describe.configure({
  timeout: 180_000,
});

test.describe('OrangeHRM - jornada de solicitacao de licenca/folga', () => {
  test('funcionario solicita licenca e admin aprova a solicitacao', async ({ page }) => {
    await runLeaveRequestJourney(page, 'approve');
  });

  test('funcionario solicita licenca e admin rejeita a solicitacao', async ({ page }) => {
    await runLeaveRequestJourney(page, 'reject');
  });
});

async function runLeaveRequestJourney(page, decision) {
  const suffix = Date.now().toString().slice(-8);
  const employee = {
    firstName: `E2E${suffix}`,
    lastName: 'Leave',
    username: `e2e${suffix}`,
    get fullName() {
      return `${this.firstName} ${this.lastName}`;
    },
  };

  await login(page, adminUsername, adminPassword);
  await createEmployeeWithLogin(page, employee);
  await addLeaveEntitlement(page, employee.fullName, leaveType, '5');
  await logout(page);

  await login(page, employee.username, employeePassword);
  await applyLeave(page, leaveType, fromDate, toDate, 'Solicitacao E2E de licenca/folga');
  await logout(page);

  await login(page, adminUsername, adminPassword);
  await decideLeaveRequest(page, employee.fullName, fromDate, toDate, decision);
}

async function login(page, username, password) {
  await page.goto('/web/index.php/auth/login', { waitUntil: 'networkidle' });
  await page.getByPlaceholder('Username').fill(username);
  await page.getByPlaceholder('Password').fill(password);
  await page.getByRole('button', { name: /login/i }).click();
  await expect(page).toHaveURL(/dashboard\/index/);
  await expect(page.getByRole('link', { name: 'Leave' })).toBeVisible();
}

async function logout(page) {
  const dropdown = page.locator('.oxd-userdropdown-tab');
  if (await dropdown.isVisible({ timeout: 5_000 }).catch(() => false)) {
    await dropdown.click();
    await page.getByRole('menuitem', { name: /Logout/i }).click();
    await expect(page).toHaveURL(/auth\/login/);
    return;
  }

  await page.goto('/web/index.php/auth/logout');
  await expect(page).toHaveURL(/auth\/login/);
}

async function createEmployeeWithLogin(page, employee) {
  await page.goto('/web/index.php/pim/addEmployee', { waitUntil: 'networkidle' });
  await page.getByPlaceholder('First Name').fill(employee.firstName);
  await page.getByPlaceholder('Last Name').fill(employee.lastName);
  await page.locator('.oxd-switch-input').click();
  await fillByLabel(page, 'Username', employee.username);
  await fillByLabel(page, 'Password', employeePassword);
  await fillByLabel(page, 'Confirm Password', employeePassword);
  await page.getByRole('button', { name: /^Save$/ }).click();
  await expect(page).toHaveURL(/pim\/viewPersonalDetails\/empNumber/);
  await expect(page.getByRole('heading', { name: 'Personal Details' })).toBeVisible();
}

async function addLeaveEntitlement(page, employeeFullName, type, entitlement) {
  await page.goto('/web/index.php/leave/addLeaveEntitlement', { waitUntil: 'networkidle' });
  await selectAutocomplete(page, employeeFullName);
  await selectDropdownByLabel(page, 'Leave Type', type);
  await fillByLabel(page, 'Entitlement', entitlement);
  await page.getByRole('button', { name: /^Save$/ }).click();
  const confirm = page.getByRole('button', { name: /Confirm|Ok|Yes/i });
  if (await confirm.waitFor({ state: 'visible', timeout: 10_000 }).then(() => true).catch(() => false)) {
    await confirm.click();
  }
  await expect(page.getByText(/Successfully Saved|Success/i).first()).toBeVisible({ timeout: 20_000 });
}

async function applyLeave(page, type, startDate, endDate, comment) {
  await page.goto('/web/index.php/leave/applyLeave', { waitUntil: 'networkidle' });
  await expect(page.getByText('Apply Leave')).toBeVisible();
  await selectDropdownByLabel(page, 'Leave Type', type);
  await fillDateByLabel(page, 'From Date', startDate);
  await fillDateByLabel(page, 'To Date', endDate);
  const comments = page.locator('textarea').first();
  if (await comments.isVisible({ timeout: 5_000 }).catch(() => false)) {
    await comments.fill(comment);
  }
  await page.getByRole('button', { name: /^Apply$/ }).click();
  await expect(page.getByText(/Successfully Saved|Success/i).first()).toBeVisible({ timeout: 30_000 });
}

async function decideLeaveRequest(page, employeeFullName, startDate, endDate, decision) {
  await page.goto('/web/index.php/leave/viewLeaveList', { waitUntil: 'networkidle' });
  await fillDateByLabel(page, 'From Date', startDate);
  await fillDateByLabel(page, 'To Date', endDate);
  await selectAutocomplete(page, employeeFullName);
  await page.getByRole('button', { name: /^Search$/ }).click();

  const row = page.locator('.oxd-table-card').filter({ hasText: employeeFullName }).first();
  await expect(row).toBeVisible({ timeout: 30_000 });
  await expect(row).toContainText(/Pending Approval/);

  const actionIcon = decision === 'reject' ? 'i.bi-x' : 'i.bi-check';
  const action = row.locator(`button:has(${actionIcon})`).first();
  if (await action.isVisible({ timeout: 5_000 }).catch(() => false)) {
    await action.click();
  } else {
    const fallbackIndex = decision === 'reject' ? 0 : 1;
    await row.locator('button').nth(fallbackIndex).click();
  }

  await expect(page.getByText(/Successfully Updated|Success/i).first()).toBeVisible({ timeout: 30_000 });
  await expect(row).not.toBeVisible({ timeout: 30_000 });
  await expect(page.getByText('No Records Found').last()).toBeVisible();
}

async function fillByLabel(page, label, value) {
  const input = fieldByLabel(page, label).locator('input, textarea').first();
  await input.fill(value);
}

async function fillDateByLabel(page, label, value) {
  const input = fieldByLabel(page, label).locator('input').first();
  await input.click();
  await input.press(process.platform === 'darwin' ? 'Meta+A' : 'Control+A');
  await input.fill(value);
  await input.press('Escape').catch(() => {});
}

async function selectDropdownByLabel(page, label, optionText) {
  await fieldByLabel(page, label).locator('.oxd-select-text').click();
  await page.locator('.oxd-select-dropdown').getByText(optionText, { exact: true }).click();
}

async function selectAutocomplete(page, value) {
  const input = page.getByPlaceholder('Type for hints...').first();
  await input.click();
  await input.fill(value);
  const option = page.locator('.oxd-autocomplete-option').filter({ hasText: value }).first();
  await expect(option).toBeVisible({ timeout: 20_000 });
  await option.click();
}

function fieldByLabel(page, label) {
  return page.locator('.oxd-input-group').filter({
    has: page.locator('label', { hasText: new RegExp(`^${escapeRegExp(label)}$`) }),
  });
}

function escapeRegExp(value) {
  return value.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}
