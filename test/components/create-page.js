import _ from 'underscore';
import { CONFLUENCE_BASE } from './constants';
import { delay, putPageTitleAndBody } from './util';

export default async () => {
  await page.waitForSelector('#quick-create-page-button');
  await page.evaluate(() => {
    document.querySelector('#quick-create-page-button').click();
  });
  await page.waitForSelector('#content-title');
  await page.evaluate(() => {
    document.querySelector('#content-title').value = 'Test Page 1';
  });
  await page.waitForSelector('#rte-button-publish');
  await page.evaluate(() => {
    document.querySelector('#rte-button-publish').click();
  });
  await delay(3000);
  await page.waitForSelector('h1#title-text');
  const title = await page.evaluate(() => {
    return document.querySelector('h1#title-text').innerText;
  });
  expect(title).toBe('Test Page 1');

  await page.waitForSelector('meta[name="ajs-page-id"]');
  const pageId = await page.evaluate(() => {
    return document.querySelector('meta[name="ajs-page-id"]').content;
  });
  expect(pageId).not.toBeUndefined();

  if (!_.isUndefined(pageId)){
    const body = [
      '<p>',
      '<ac:structured-macro ac:name="cert-not-after" ac:schema-version="1">',
      '<ac:parameter ac:name="host">google.com</ac:parameter>',
      '</ac:structured-macro>',
      '</p>'
    ].join('\n');
    try {
      const result = await putPageTitleAndBody(pageId, 2, 'Test Page 1', body);
      expect(result.statusText).toBe('OK');
    } catch(err) {
      console.log(err);
    }

    await page.goto(`${CONFLUENCE_BASE}/pages/viewpage.action?pageId=${pageId}`);
    await delay(1000);
    await page.waitForSelector('div#main-content');
  }
};
