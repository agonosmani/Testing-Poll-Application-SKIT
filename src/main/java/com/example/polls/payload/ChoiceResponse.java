package com.example.polls.payload;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ChoiceResponse {
    @JsonProperty("id")
    private long id;
    @JsonProperty("text")
    private String text;
    @JsonProperty("voteCount")
    private long voteCount;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getVoteCount() {
        return voteCount;
    }

    public void setVoteCount(long voteCount) {
        this.voteCount = voteCount;
    }

    @Override
    public String toString() {
        return "ChoiceResponse{" +
                "id=" + id +
                ", text='" + text + '\'' +
                ", voteCount=" + voteCount +
                '}';
    }
}
