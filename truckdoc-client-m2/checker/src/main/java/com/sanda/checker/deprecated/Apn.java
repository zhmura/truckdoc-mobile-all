package com.sanda.checker.deprecated;

/**
 * Created by astra on 04.08.2015.
 */
class Apn {

    public final String apn;
    public final String name;
    public final String type;
    public final String proxy;
    public final String port;
    public final String user;
    public final String password;
    public final String server;
    public final String mmsc;
    public final String mmsproxy;
    public final String mmsport;
    public final String mcc;
    public final String mnc;
    public final String numeric;

    private Apn(Builder builder) {
        apn = builder.apn;
        name = builder.name;
        type = builder.type;
        proxy = builder.proxy;
        port = builder.port;
        user = builder.user;
        password = builder.password;
        server = builder.server;
        mmsc = builder.mmsc;
        mmsproxy = builder.mmsproxy;
        mmsport = builder.mmsport;
        mcc = builder.mcc;
        mnc = builder.mnc;
        numeric = builder.numeric;
    }

    @Override
    public String toString() {
        return "Apn{" + "apn='" + apn + '\'' +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", proxy='" + proxy + '\'' +
                ", port='" + port + '\'' +
                ", user='" + user + '\'' +
                ", password='" + password + '\'' +
                ", server='" + server + '\'' +
                ", mmsc='" + mmsc + '\'' +
                ", mmsproxy='" + mmsproxy + '\'' +
                ", mmsport='" + mmsport + '\'' +
                ", mcc='" + mcc + '\'' +
                ", mnc='" + mnc + '\'' +
                ", numeric='" + numeric + '\'' +
                '}';
    }

    public static Builder create() {
        return new Builder();
    }

    public static final class Builder {

        private String apn;
        private String name;
        private String type;
        private String proxy;
        private String port;
        private String user;
        private String password;
        private String server;
        private String mmsc;
        private String mmsproxy;
        private String mmsport;
        private String mcc;
        private String mnc;
        private String numeric;

        private Builder() {
        }

        public Builder apn(String apn) {
            this.apn = apn;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder proxy(String proxy) {
            this.proxy = proxy;
            return this;
        }

        public Builder port(String port) {
            this.port = port;
            return this;
        }

        public Builder user(String user) {
            this.user = user;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder server(String server) {
            this.server = server;
            return this;
        }

        public Builder mmsc(String mmsc) {
            this.mmsc = mmsc;
            return this;
        }

        public Builder mmsproxy(String mmsproxy) {
            this.mmsproxy = mmsproxy;
            return this;
        }

        public Builder mmsport(String mmsport) {
            this.mmsport = mmsport;
            return this;
        }

        public Builder mcc(String mcc) {
            this.mcc = mcc;
            return this;
        }

        public Builder mnc(String mnc) {
            this.mnc = mnc;
            return this;
        }

        public Builder numeric(String numeric) {
            this.numeric = numeric;
            return this;
        }

        public Apn build() {
            return new Apn(this);
        }
    }
}
