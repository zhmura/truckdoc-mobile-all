package com.sanda.truckdoc.network.api;

import org.jetbrains.annotations.NotNull;

/**
 * @author Alexei Osipov
 */
public class UserKey {

    private String name;
    private String login;
    private String secret;

    public UserKey(@NotNull String name, @NotNull String login, @NotNull String secret) {
        this.name = name;
        this.login = login;
        this.secret = secret;
    }

    public String getLogin() {
        return login;
    }

    public String getSecret() {
        return secret;
    }

    public String getName() {
        return name;
    }
}
