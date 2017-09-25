package com.mesilat.certs;

import com.atlassian.confluence.renderer.template.TemplateRenderer;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.v2.RenderMode;
import com.atlassian.renderer.v2.macro.BaseMacro;
import com.atlassian.renderer.v2.macro.MacroException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;

@Scanned
public class CheckCertMacroLegacy extends BaseMacro {
    public static final String PLUGIN_KEY = "com.mesilat.check-https-cert";

    private final TemplateRenderer renderer;

    @Override
    public String execute(@SuppressWarnings("rawtypes") Map parameters, String body, RenderContext renderContext) throws MacroException {
        Map<String,Object> map = new HashMap<>();
        map.put("host", parameters.get("host"));
        map.put("port", parameters.containsKey("port")? parameters.get("port"): "443");
        if (parameters.containsKey("sync")){
            try {
                CheckCertificate check = new CheckCertificateImpl();
                Date notAfter = check.getNotAfter(parameters.get("host").toString(), parameters.containsKey("port")? Integer.parseInt(parameters.get("port").toString()): 443);
                long days = (notAfter.getTime()-System.currentTimeMillis()) / CheckCertResource.MS;
                map.put("notAfterDate", notAfter.toString());
                if (days > 7){
                    map.put("className", "com-mesilat-cert-ok");
                } else if (days > 3) {
                    map.put("className", "com-mesilat-cert-warn");
                } else {
                    map.put("className", "com-mesilat-cert-error");
                }
                return renderFromSoy("resources", "Mesilat.CheckCert.Templates.notAfterSync.soy", map);
            } catch (CheckCertificateException ex) {
                throw new MacroException(ex);
            }
        } else {
            return renderFromSoy("resources", "Mesilat.CheckCert.Templates.notAfter.soy", map);
        }
    }

    @Override
    public RenderMode getBodyRenderMode() {
        return RenderMode.ALL;
    }

    @Override
    public boolean hasBody() {
        return false;
    }

    private String renderFromSoy(String key, String soyTemplate, Map soyContext) {
        StringBuilder output = new StringBuilder();
        renderer.renderTo(output, String.format("%s:%s", PLUGIN_KEY, key), soyTemplate, soyContext);
        return output.toString();
    }

    @Inject
    public CheckCertMacroLegacy(final @ComponentImport TemplateRenderer renderer){
        this.renderer = renderer;
    }
}