package com.sanda.truckdoc.client.api;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * @author Alexei Osipov
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RegistrationInfoPojo {
    private String name;
    private String loginKey;
    private String secretKey;

    public RegistrationInfoPojo() {
    }

    public RegistrationInfoPojo(String name, String loginKey, String secretKey) {
        this.name = name;
        this.loginKey = loginKey;
        this.secretKey = secretKey;
    }

    public String getName() {
        return name;
    }

    public String getLoginKey() {
        return loginKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLoginKey(String loginKey) {
        this.loginKey = loginKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }
}
