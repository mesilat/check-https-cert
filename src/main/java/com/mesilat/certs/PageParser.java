package com.mesilat.certs;

import com.atlassian.confluence.content.render.xhtml.XhtmlConstants;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

public class PageParser {    
    static final QName AC_MACRO = new QName(
        XhtmlConstants.CONFLUENCE_XHTML_NAMESPACE_URI,
        "structured-macro",
        XhtmlConstants.CONFLUENCE_XHTML_NAMESPACE_ALTERNATE_PREFIX);
    static final QName AC_NAME = new QName(
        XhtmlConstants.CONFLUENCE_XHTML_NAMESPACE_URI,
        "name",
        XhtmlConstants.CONFLUENCE_XHTML_NAMESPACE_ALTERNATE_PREFIX);
    static final QName AC_PARAMETER = new QName(
        XhtmlConstants.CONFLUENCE_XHTML_NAMESPACE_URI,
        "parameter",
        XhtmlConstants.CONFLUENCE_XHTML_NAMESPACE_ALTERNATE_PREFIX);
    static final QName AC_PLAIN_TEXT_BODY = new QName(
        XhtmlConstants.CONFLUENCE_XHTML_NAMESPACE_URI,
        "plain-text-body",
        XhtmlConstants.CONFLUENCE_XHTML_NAMESPACE_ALTERNATE_PREFIX);
    static final Pattern MACRO = Pattern.compile("\\{cert-not-after:host=(.+?)(\\|port=(\\d+)?)?\\}");

    protected final XMLEventReader reader;
    protected final Stack<Object> data = new Stack();
    private final List<HostInfo> result = new ArrayList<>();
    
    protected void startElement(StartElement elt) {
        if (AC_MACRO.equals(elt.getName())){
            if ("cert-not-after".equals(elt.getAttributeByName(AC_NAME).getValue())) {
                data.push(new HostInfo());
                return;
            } else if ("unmigrated-wiki-markup".equals(elt.getAttributeByName(AC_NAME).getValue())) {
                data.push(new HostInfo());
                return;
            }
        } else if (AC_PARAMETER.equals(elt.getName())) {
            if ("host".equals(elt.getAttributeByName(AC_NAME).getValue())) {
                data.push(new Host());
                return;
            } else if ("port".equals(elt.getAttributeByName(AC_NAME).getValue())) {
                data.push(new Port());
                return;
            }
        } else if (AC_PLAIN_TEXT_BODY.equals(elt.getName())) {
            data.push(new PlainTextBody());
            return;
        }
        data.push(null);
    }
    protected void endElement(EndElement elt) {
        Object obj = data.pop();
        if (obj instanceof Host) {
            assert(data.peek() instanceof HostInfo);
            HostInfo info = (HostInfo)data.peek();
            info.setHost(((Host)obj).getText());
        } else if (obj instanceof Port) {
            assert(data.peek() instanceof HostInfo);
            HostInfo info = (HostInfo)data.peek();
            info.setPort(parseInt(((Port)obj).getText()));
        } else if (obj instanceof PlainTextBody) {
            assert(data.peek() instanceof HostInfo);
            HostInfo info = (HostInfo)data.peek();
            Matcher m = MACRO.matcher(((PlainTextBody)obj).getText().trim());
            if (m.matches()) {
                info.setHost(m.group(1));
                info.setPort(parseInt(m.group(3)));
            }
        } else if (obj instanceof HostInfo) {
            result.add((HostInfo)obj);
        }
    }
    protected void characters (Characters ch){
        String text = ch.getData();
        if (text == null)
            return;

        if (data.peek() instanceof TextHolder) {
            TextHolder holder = (TextHolder)data.peek();
            holder.addText(text);
        }
    }

    public List<HostInfo> parse() throws XMLStreamException {
        while (reader.hasNext()){
            XMLEvent evt = reader.nextEvent();
            if (evt.isStartElement()){
                startElement(evt.asStartElement());
            } else if (evt.isEndElement()) {
                endElement(evt.asEndElement());
            } else if (evt.isCharacters()) {
                characters(evt.asCharacters());
            }
        }
        assert(data.isEmpty());
        return result;
    }
    private Integer parseInt(String text) {
        if (text == null)
            return null;
        try {
            return Integer.parseInt(text);
        } catch(Throwable ignore) {
            return null;
        }
    }

    protected PageParser(XMLEventReader reader) {
        this.reader = reader;
    }

    public static List<HostInfo> parse(XMLEventReader reader) throws XMLStreamException {
        PageParser parser = new PageParser(reader);
        return parser.parse();
    }
    
    public static class TextHolder {
        private final StringBuilder sb;
        public void addText(String text) {
            sb.append(text);
        }
        public String getText() {
            return sb.toString().trim();
        }
        public TextHolder() {
            sb = new StringBuilder();
        }
    }
    public static class Host extends TextHolder {
    }
    public static class Port extends TextHolder {
    }
    public static class PlainTextBody extends TextHolder {
    }
}
