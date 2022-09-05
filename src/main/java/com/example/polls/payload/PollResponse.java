package com.example.polls.payload;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;

public class PollResponse {
    @JsonProperty("id")
    private Long id;
    @JsonProperty("question")
    private String question;
    @JsonProperty("choices")
    private List<ChoiceResponse> choices;
    @JsonProperty("createdBy")
    private UserSummary createdBy;
    @JsonProperty("creationDateTime")
    private Instant creationDateTime;
    @JsonProperty("expirationDateTime")
    private Instant expirationDateTime;
    @JsonProperty("isExpired")
    private Boolean isExpired;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("selectedChoice")
    private Long selectedChoice;
    @JsonProperty("totalVotes")
    private Long totalVotes;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public List<ChoiceResponse> getChoices() {
        return choices;
    }

    public void setChoices(List<ChoiceResponse> choices) {
        this.choices = choices;
    }

    public UserSummary getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UserSummary createdBy) {
        this.createdBy = createdBy;
    }


    public Instant getCreationDateTime() {
        return creationDateTime;
    }

    public void setCreationDateTime(Instant creationDateTime) {
        this.creationDateTime = creationDateTime;
    }

    public Instant getExpirationDateTime() {
        return expirationDateTime;
    }

    public void setExpirationDateTime(Instant expirationDateTime) {
        this.expirationDateTime = expirationDateTime;
    }

    public Boolean getExpired() {
        return isExpired;
    }

    public void setExpired(Boolean expired) {
        isExpired = expired;
    }

    public Long getSelectedChoice() {
        return selectedChoice;
    }

    public void setSelectedChoice(Long selectedChoice) {
        this.selectedChoice = selectedChoice;
    }

    public Long getTotalVotes() {
        return totalVotes;
    }

    public void setTotalVotes(Long totalVotes) {
        this.totalVotes = totalVotes;
    }

    @Override
    public String toString() {
        return "PollResponse{" +
                "id=" + id +
                ", question='" + question + '\'' +
                ", choices=" + choices +
                ", createdBy=" + createdBy +
                ", creationDateTime=" + creationDateTime +
                ", expirationDateTime=" + expirationDateTime +
                ", isExpired=" + isExpired +
                ", selectedChoice=" + selectedChoice +
                ", totalVotes=" + totalVotes +
                '}';
    }
}
