/**
 * This test suit will reuse already running instance of Confluence server
 */

import { CONFLUENCE_BASE } from './components/constants';
import { delay, waitForConfluenceToStart, waitForConfluenceToStop } from './components/util';
import loginForm from './components/login-form';
import skipOnboarding from './components/skip-onboarding';
import createPage from './components/create-page';
import validateMacro from './components/validate-macro';

describe('Check HTTP Certificate plugin frontend tests', () => {
  beforeAll(async () => {
    await page.goto(CONFLUENCE_BASE);
  });
  beforeEach(async () => {
    jest.setTimeout(30000);
  });

  it('a user can login using form', loginForm);
  //it('skip onboarding dialog', skipOnboarding);
  it('a user can create a new page', createPage);
  it('macro should report OK for google certificate', validateMacro);
});
