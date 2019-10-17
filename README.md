# Check HTTPS Certificate Addon for Confluence

If you are a system administrator with a number of cites under your management, use 'Cert Validity' macro
to print expiration date and time of your SSL certificates. Just specify a host address (and optionally a port number)
and the macro will automatically print the server certificate expiration date.

Please check the product homepage: https://www.mesilat.com/pages/confluence/check-https-cert/

Download from Atlassian Marketplace: https://marketplace.atlassian.com/plugins/com.mesilat.check-https-cert

To build the plugin you need [Atlassian SDK](https://developer.atlassian.com/server/framework/atlassian-sdk/downloads/).

Test suite uses [jest-puppeteer](https://jestjs.io/docs/en/puppeteer). To run test:

```
cd ${PROJECT_HOME}/test
yarn install
jest
```
