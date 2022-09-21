package com.example.polls.payload;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserIdentityAvailability {
    @JsonProperty("available")
    private Boolean available;

    public UserIdentityAvailability(@JsonProperty("available") Boolean available) {
        this.available = available;
    }

    @Override
    public String toString() {
        return "UserIdentityAvailability{" +
                "available=" + available +
                '}';
    }

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }
}
