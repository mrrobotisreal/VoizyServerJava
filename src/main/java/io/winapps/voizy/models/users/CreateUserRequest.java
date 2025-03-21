package io.winapps.voizy.models.users;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CreateUserRequest {
    @JsonProperty("email")
    private String email;

    @JsonProperty("password")
    private String password;

    @JsonProperty("preferredName")
    private String preferredName;

    @JsonProperty("username")
    private String username;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() { return password; }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPreferredName() { return preferredName; }

    public void setPreferredName(String preferredName) {
        this.preferredName = preferredName;
    }

    public String getUsername() { return username; }

    public void setUsername(String username) {
        this.username = username;
    }
}