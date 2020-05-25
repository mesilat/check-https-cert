package com.mesilat.certs;

import com.atlassian.confluence.api.model.content.Content;
import com.atlassian.confluence.api.model.pagination.PageRequest;
import com.atlassian.confluence.api.model.pagination.PageResponse;
import com.atlassian.confluence.api.service.search.CQLSearchService;
import com.atlassian.confluence.content.render.xhtml.DefaultXmlEventReaderFactory;
import com.atlassian.confluence.content.render.xhtml.XmlEventReaderFactory;
import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.pages.PageManager;
import com.atlassian.confluence.user.AuthenticatedUserThreadLocal;
import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.mywork.model.NotificationBuilder;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.atlassian.mywork.service.LocalNotificationService;
import com.atlassian.sal.api.message.I18nResolver;
import static com.mesilat.certs.CheckCertResource.MS;
import java.io.StringReader;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

@ExportAsService({CheckCertService.class})
@Named
public class CheckCertServiceImpl implements CheckCertService, InitializingBean, DisposableBean {
    public static final String PLUGIN_KEY = "com.mesilat.check-https-cert";
    private static final Logger LOGGER = LoggerFactory.getLogger(PLUGIN_KEY);

    private final PageManager pageManager;
    private final CQLSearchService searchService;
    private final XmlEventReaderFactory xmlEventReaderFactory;
    private final LocalNotificationService notificationService;
    private final I18nResolver resolver;
    private final UserAccessor userAccessor;

    @Override
    public void afterPropertiesSet() throws Exception {
        
    }
    @Override
    public void destroy() throws Exception {
        
    }
    @Override
    public void recheckAll() {
        Collection<HostInfo> result = searchForMacro();
        result.forEach(hostInfo -> {
            try {
                CheckCertificate check = new CheckCertificateImpl();
                Date notAfter = check.getNotAfter(hostInfo.getHost(), hostInfo.getPort());
                if (notAfter.getTime() < System.currentTimeMillis()) {
                    sendExpiredMessage(AuthenticatedUserThreadLocal.get(), hostInfo);
                } else if (notAfter.getTime() < System.currentTimeMillis() + 2 * MS) {
                    sendExpiringMessage(AuthenticatedUserThreadLocal.get(), hostInfo);
                }
            } catch (CheckCertificateException ex) {
                LOGGER.error(String.format("Failed to check certificate at %s:%d", hostInfo.getHost(), hostInfo.getPort()), ex);
                sendFailedCheckMessage(AuthenticatedUserThreadLocal.get(), hostInfo, ex);
            }
        });
    }

    public void sendExpiredMessage(ConfluenceUser recipient, HostInfo hostInfo) {
        Locale locale = userAccessor.getConfluenceUserPreferences(recipient).getLocale();
        String title = locale == null?
            resolver.getText("com.mesilat.check-https-cert.msg.expired.title"):
            resolver.getText(locale, "com.mesilat.check-https-cert.msg.expired.title");
        String message = locale == null?
            MessageFormat.format(resolver.getText("com.mesilat.check-https-cert.msg.expired.body"), hostInfo.toString()):
            MessageFormat.format(resolver.getText(locale, "com.mesilat.check-https-cert.msg.expired.body"), hostInfo.toString());
        sendMessage(recipient, title, message, hostInfo.toString());
    }
    public void sendExpiringMessage(ConfluenceUser recipient, HostInfo hostInfo) {
        Locale locale = userAccessor.getConfluenceUserPreferences(recipient).getLocale();
        String title = locale == null?
            resolver.getText("com.mesilat.check-https-cert.msg.expiring.title"):
            resolver.getText(locale, "com.mesilat.check-https-cert.msg.expiring.title");
        String message = locale == null?
            MessageFormat.format(resolver.getText("com.mesilat.check-https-cert.msg.expiring.body"), hostInfo.toString()):
            MessageFormat.format(resolver.getText(locale, "com.mesilat.check-https-cert.msg.expiring.body"), hostInfo.toString());
        sendMessage(recipient, title, message, hostInfo.toString());
    }
    public void sendFailedCheckMessage(ConfluenceUser recipient, HostInfo hostInfo, Exception ex) {
        Locale locale = userAccessor.getConfluenceUserPreferences(recipient).getLocale();
        String title = locale == null?
            resolver.getText("com.mesilat.check-https-cert.msg.failed.title"):
            resolver.getText(locale, "com.mesilat.check-https-cert.msg.failed.title");
        String message = locale == null?
            MessageFormat.format(resolver.getText("com.mesilat.check-https-cert.msg.failed.body"), hostInfo.toString(), ex.getMessage()):
            MessageFormat.format(resolver.getText(locale, "com.mesilat.check-https-cert.msg.failed.body"), hostInfo.toString(), ex.getMessage());
        sendMessage(recipient, title, message, hostInfo.toString());
    }
    private void sendMessage(ConfluenceUser recipient, String title, String message, String host) {
        notificationService.createOrUpdate(
            recipient.getName(),
            new NotificationBuilder()
            .application(PLUGIN_KEY)
            .title(title)
            .itemTitle(host)
            .description(message)
            .groupingId("com.mesilat.check-https-cert.notification")
            .createNotification()
        );
    }
    
    public Collection<HostInfo> searchForMacro() {
        Map<String, HostInfo> hostInfos = new HashMap<>();
        MyPageRequest pr = new MyPageRequest(0, 10);
        PageResponse<Content> search;
        do {
            search =  searchService.searchContent("siteSearch ~ \"macroName:cert-not-after\" AND type in (\"page\")", pr);
            search.forEach(content -> {
                Page page = pageManager.getPage(content.getId().asLong());
                if (page == null)
                    return;
                try {
                    parse(page).forEach(hostInfo -> {
                        hostInfos.put(hostInfo.toString(), hostInfo);
                    });
                } catch (Throwable ex) {
                    LOGGER.error(String.format("Failed to parse page %d", page.getId()), ex);
                }
            });
            pr.next();
        } while (search.hasMore());
        
        return hostInfos.values();
    }

    public List<HostInfo> parse(Page page) throws XMLStreamException {
        try (StringReader sr = new StringReader(page.getBodyAsString())) {
            XMLEventReader reader = xmlEventReaderFactory.createStorageXmlEventReader(sr);
            return PageParser.parse(reader);
        }
    }

    @Inject
    public CheckCertServiceImpl(
        @ComponentImport CQLSearchService searchService,
        @ComponentImport PageManager pageManager,
        @ComponentImport TransactionTemplate transactionTemplate,
        @ComponentImport I18nResolver resolver,
        @ComponentImport LocalNotificationService notificationService,
        @ComponentImport UserAccessor userAccessor
    ){
        this.searchService = searchService;
        this.pageManager = pageManager;
        this.resolver = resolver;
        this.notificationService = notificationService;
        this.userAccessor = userAccessor;
        this.xmlEventReaderFactory = transactionTemplate.execute(() -> {
            return new DefaultXmlEventReaderFactory();
        });
    }
    
    private static class MyPageRequest implements PageRequest {
        private int start;
        private final int limit;

        @Override
        public int getStart() {
            return start;
        }
        public void setStart(int start) {
            this.start = start;
        }
        @Override
        public int getLimit() {
            return limit;
        }
        public void next() {
            this.start += this.limit;
        }

        public MyPageRequest(int start, int limit) {
            this.start = start;
            this.limit = limit;
        }
    }
}