package com.mesilat.certs;

public class CheckCertificateException extends Exception {
    public CheckCertificateException(String msg){
        super(msg);
    }
    public CheckCertificateException(Throwable cause){
        super(cause);
    }
    public CheckCertificateException(String msg, Throwable cause){
        super(msg, cause);
    }
}