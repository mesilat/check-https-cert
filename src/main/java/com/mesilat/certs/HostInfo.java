package com.mesilat.certs;

public class HostInfo {
    private String host;
    private Integer port;

    public String getHost() {
        return host;
    }
    public void setHost(String host) {
        this.host = host;
    }
    public Integer getPort() {
        return port == null? 443: port;
    }
    public void setPort(Integer port) {
        this.port = port;
    }
    @Override
    public String toString() {
        return String.format("%s:%d", getHost(), getPort());
    }

    public HostInfo() {
    }
    public HostInfo(String host) {
        this(host, 443);
    }
    public HostInfo(String host, Integer port) {
        this.host = host;
        this.port = port;
    }
}
