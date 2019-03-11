package com.microkubes.tools.gateway;

import java.util.ArrayList;
import java.util.List;

public class ServiceInfo {
    private String name;
    private String host;
    private int port;
    private String[] paths;

    public ServiceInfo(String name, String host, int port, String[] paths) {
        this.name = name;
        this.host = host;
        this.port = port;
        this.paths = paths;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String[] getPaths() {
        return paths;
    }

    public void setPaths(String[] paths) {
        this.paths = paths;
    }

    public static class ServiceInfoBuilder {
        private String name;
        private String host;
        private int port;
        private List<String> paths;


        public ServiceInfoBuilder setName(String name) {
            this.name = name;
            return this;
        }

        public ServiceInfoBuilder host(String host) {
            this.host = host;
            return this;
        }

        public ServiceInfoBuilder port(int port) {
            this.port = port;
            return this;
        }

        public ServiceInfoBuilder addPath(String path) {
            if (this.paths == null) {
                this.paths = new ArrayList<>();
            }
            this.paths.add(path);
            return this;
        }

        public ServiceInfo getServiceInfo() {
            return new ServiceInfo(name, host, port, paths.toArray(new String[]{}));
        }

    }

}
