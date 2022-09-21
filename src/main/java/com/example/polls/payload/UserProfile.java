package com.example.polls.payload;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public class UserProfile {
    @JsonProperty("id")
    private Long id;
    @JsonProperty("username")
    private String username;
    @JsonProperty("name")
    private String name;
    @JsonProperty("joinedAt")
    private Instant joinedAt;
    @JsonProperty("pollCount")
    private Long pollCount;
    @JsonProperty("voteCount")
    private Long voteCount;

    public UserProfile(
            @JsonProperty("id") Long id,
            @JsonProperty("username") String username,
            @JsonProperty("name") String name,
            @JsonProperty("joinedAt") Instant joinedAt,
            @JsonProperty("pollCount") Long pollCount,
            @JsonProperty("voteCount") Long voteCount) {
        this.id = id;
        this.username = username;
        this.name = name;
        this.joinedAt = joinedAt;
        this.pollCount = pollCount;
        this.voteCount = voteCount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(Instant joinedAt) {
        this.joinedAt = joinedAt;
    }

    public Long getPollCount() {
        return pollCount;
    }

    public void setPollCount(Long pollCount) {
        this.pollCount = pollCount;
    }

    public Long getVoteCount() {
        return voteCount;
    }

    public void setVoteCount(Long voteCount) {
        this.voteCount = voteCount;
    }

    @Override
    public String toString() {
        return "UserProfile{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", name='" + name + '\'' +
                ", joinedAt=" + joinedAt +
                ", pollCount=" + pollCount +
                ", voteCount=" + voteCount +
                '}';
    }
}
