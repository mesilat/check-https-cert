export default async () => {
  await page.waitForSelector('span.conf-macro.com-mesilat-check-cert-not-after');
  const host = await page.evaluate(() => {
    return document.querySelector('span.conf-macro.com-mesilat-check-cert-not-after').getAttribute('cert-data-host');
  });
  expect(host).toBe('google.com');

  const port = await page.evaluate(() => {
    return document.querySelector('span.conf-macro.com-mesilat-check-cert-not-after').getAttribute('cert-data-port');
  });
  expect(port).toBe('443');

  const className = await page.evaluate(() => {
    return document.querySelector('span.conf-macro.com-mesilat-check-cert-not-after').className;
  });
  expect(className).toMatch('com-mesilat-cert-ok');
};
