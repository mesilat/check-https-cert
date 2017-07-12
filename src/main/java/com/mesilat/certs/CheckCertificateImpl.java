package com.mesilat.certs;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public class CheckCertificateImpl implements CheckCertificate {

    @Override
    public Date getNotAfter(String host, int port) throws CheckCertificateException {
        try {
            SSLContext context = SSLContext.getInstance("TLS");
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            tmf.init(keyStore);
            X509TrustManager defaultTrustManager = (X509TrustManager)tmf.getTrustManagers()[0];
            CheckCertificateImpl.SavingTrustManager tm = new CheckCertificateImpl.SavingTrustManager(defaultTrustManager);
            context.init((KeyManager[]) null, new TrustManager[]{tm}, (SecureRandom) null);
            SSLSocketFactory factory = context.getSocketFactory();
            SSLSocket socket = (SSLSocket)factory.createSocket(host, port);
            socket.setSoTimeout(10000);

            try {
                socket.startHandshake();
                socket.close();
            } catch (SSLException ignore) {
            }

            X509Certificate[] chain = tm.chain;
            Date date = null;
            if (chain == null) {
                throw new CheckCertificateException("Could not obtain server certificate chain");
            } else {
                for (int line = 0; line < chain.length; ++line) {
                    X509Certificate k = chain[line];
                    if (date == null || date.after(k.getNotAfter()))
                        date = k.getNotAfter();
                }
            }
            return date;
        } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException | IOException ex) {
            throw new CheckCertificateException(ex);
        }
    }

    private static class SavingTrustManager implements X509TrustManager {
        private final X509TrustManager tm;
        private X509Certificate[] chain;

        SavingTrustManager(X509TrustManager tm) {
            this.tm = tm;
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            throw new UnsupportedOperationException();
        }
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            throw new UnsupportedOperationException();
        }
        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            this.chain = chain;
            this.tm.checkServerTrusted(chain, authType);
        }
    }
}