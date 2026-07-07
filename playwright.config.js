// @ts-check
const { defineConfig, devices } = require('@playwright/test');

module.exports = defineConfig({
  testDir: './tests/e2e',
  timeout: 90_000,
  expect: {
    timeout: 15_000,
  },
  fullyParallel: false,
  reporter: [
    ['list'],
    ['html', { outputFolder: 'playwright-report', open: 'never' }],
  ],
  use: {
    baseURL: process.env.ORANGEHRM_BASE_URL || 'https://opensource-demo.orangehrmlive.com',
    channel: 'chrome',
    headless: true,
    screenshot: 'only-on-failure',
    trace: 'retain-on-failure',
    video: 'off',
  },
  projects: [
    {
      name: 'chrome',
      use: { ...devices['Desktop Chrome'] },
    },
  ],
});
