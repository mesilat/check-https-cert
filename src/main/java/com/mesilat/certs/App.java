package com.mesilat.certs;

import java.util.Date;

public class App {
    private static final long MS = 24l * 3600l * 1000l;

    public static void main(String[] args) throws CheckCertificateException {
        if (args.length == 0){
            System.out.println("Usage: java -jar check-https-cert.jar host [port]");
            System.exit(1);
        }
        String host = args[0];
        int port = args.length > 1? Integer.parseInt(args[1]): 443;
        CheckCertificate check = new CheckCertificateImpl();
        Date notAfter = check.getNotAfter(host, port);
        System.out.println(String.format("%d: %s", (notAfter.getTime()-System.currentTimeMillis()) / MS, notAfter.toString()));
    }   
}