/**
 * This test suite will launch an instance of Confluence server and
 * run the tests against it. It is advisable to run atlas-clean before
 * running this test suite as skipOnboarding will fail if no onboarding
 * splash screen is shown.
 */

import { delay, waitForConfluenceToStart, waitForConfluenceToStop } from './components/util';
import loginForm from './components/login-form';
import skipOnboarding from './components/skip-onboarding';
import createPage from './components/create-page';
import validateMacro from './components/validate-macro';

describe('Check HTTP Certificate plugin frontend tests', () => {
  let server;
  beforeAll(async () => {
    server = await waitForConfluenceToStart();
    await page.goto('http://127.0.0.1:1990/confluence');
  }, 300000);
  beforeEach(async () => {
    jest.setTimeout(60000);
  });
  afterAll(async () => {
    await waitForConfluenceToStop(server);
  }, 10000);

  it('a user can login using form', loginForm);
  it('skip onboarding dialog', skipOnboarding);
  it('a user can create a new page', createPage);
  it('macro should report OK for google certificate', validateMacro);
});
