package com.example.polls.payload;
import javax.validation.constraints.NotNull;

public class VoteRequest {
    @NotNull
    private Long choiceId;

    public VoteRequest(Long choiceId) {
        this.choiceId = choiceId;
    }

    public VoteRequest() {
    }

    public Long getChoiceId() {
        return choiceId;
    }

    public void setChoiceId(Long choiceId) {
        this.choiceId = choiceId;
    }
}

