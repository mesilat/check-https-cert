package com.mesilat.certs;

import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.confluence.macro.Macro;
import com.atlassian.confluence.macro.Macro.BodyType;
import com.atlassian.confluence.macro.Macro.OutputType;
import com.atlassian.confluence.macro.MacroExecutionException;
import com.atlassian.confluence.renderer.template.TemplateRenderer;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import javax.inject.Inject;

@Scanned
public class CheckCertMacro implements Macro {
    public static final String PLUGIN_KEY = "com.mesilat.check-https-cert";

    @ComponentImport
    private final TemplateRenderer renderer;

    @Override
    public String execute(Map<String,String> parameters, String body, ConversionContext context) throws MacroExecutionException {
        Map map = ImmutableMap.builder()
            .put("host", parameters.get("host"))
            .put("port", parameters.containsKey("port")? parameters.get("port"): "443")
            .build();
        return renderFromSoy("resources", "Mesilat.CheckCert.Templates.notAfter.soy", map);
    }
    @Override
    public BodyType getBodyType() {
        return BodyType.NONE;
    }
    @Override
    public OutputType getOutputType() {
        return OutputType.INLINE;
    }

    private String renderFromSoy(String key, String soyTemplate, Map soyContext) {
        StringBuilder output = new StringBuilder();
        renderer.renderTo(output, String.format("%s:%s", PLUGIN_KEY, key), soyTemplate, soyContext);
        return output.toString();
    }

    @Inject
    public CheckCertMacro(TemplateRenderer renderer){
        this.renderer = renderer;
    }
}