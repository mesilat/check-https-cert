package com.mesilat.certs;

import com.atlassian.confluence.user.AuthenticatedUserThreadLocal;
import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.scheduler.JobRunner;
import com.atlassian.scheduler.JobRunnerRequest;
import com.atlassian.scheduler.JobRunnerResponse;
import com.atlassian.user.Group;
import static com.mesilat.certs.CheckCertServiceImpl.PLUGIN_KEY;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CheckSslCertsJob implements JobRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(PLUGIN_KEY);
    
    private final CheckCertService checkCertService;
    @ComponentImport
    private final UserAccessor userAccessor;

    @Override
    public JobRunnerResponse runJob(JobRunnerRequest request) {
        ConfluenceUser originalUser = AuthenticatedUserThreadLocal.get();
        Group confluenceAdministrators = userAccessor.getGroup("confluence-administrators");
        if (confluenceAdministrators == null) {
            LOGGER.warn("Group \"confluence-administrators\" is not available");
            return null;
        }
        try {
            userAccessor.getMembers(confluenceAdministrators).forEach(user -> {
                AuthenticatedUserThreadLocal.set(user);
                checkCertService.recheckAll();         
            });            
        } finally {
            AuthenticatedUserThreadLocal.set(originalUser);
        }
        return null;
    }

    public CheckSslCertsJob(CheckCertService checkCertService, UserAccessor userAccessor) {
        this.checkCertService = checkCertService;
        this.userAccessor = userAccessor;
    }
}