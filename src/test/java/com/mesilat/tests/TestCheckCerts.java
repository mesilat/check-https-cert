package com.mesilat.tests;

import com.mesilat.certs.CheckCertificate;
import com.mesilat.certs.CheckCertificateException;
import com.mesilat.certs.CheckCertificateImpl;
import java.util.Date;
import junit.framework.TestCase;

public class TestCheckCerts extends TestCase {
    
    public TestCheckCerts(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void test01() throws CheckCertificateException {
        CheckCertificate check = new CheckCertificateImpl();
        Date date = check.getNotAfter("www.google.com", 443);
        System.out.println(date);
    }
}