package com.mesilat.certs;

import java.util.Date;

public interface CheckCertificate {
    Date getNotAfter(String host, int port) throws CheckCertificateException;
}
