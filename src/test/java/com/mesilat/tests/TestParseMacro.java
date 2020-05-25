package com.mesilat.tests;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import junit.framework.TestCase;

public class TestParseMacro extends TestCase {
    Pattern MACRO = Pattern.compile("\\{cert-not-after:host=(.+?)(\\|port=(\\d+)?)?\\}");

    public TestParseMacro(String testName) {
        super(testName);
    }

    public void test01() {
        String text = "{cert-not-after:host=www.google.com|port=443}";
        Matcher m = MACRO.matcher(text);
        if (m.matches()) {
            System.out.println(m.group(3));
        }
    }

    public void test02() {
        String text = "{cert-not-after:host=www.google.com}";
        Matcher m = MACRO.matcher(text);
        if (m.matches()) {
            System.out.println(m.group(3));
        }
    }
}
