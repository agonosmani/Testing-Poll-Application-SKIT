package com.example.polls.controllers;

import ch.qos.logback.core.net.ObjectWriter;
import com.example.polls.controller.PollController;
import com.example.polls.model.Choice;
import com.example.polls.model.Poll;
import com.example.polls.model.User;
import com.example.polls.payload.*;
import com.example.polls.security.UserPrincipal;
import com.example.polls.service.PollService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.parameters.P;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(MockitoJUnitRunner.class)
public class PollControllerTest {

    @Mock
    private PollService pollService;

    private PollController controller;

    private UserPrincipal currentUser;

    @Before
    public void init() throws Exception {
        MockitoAnnotations.openMocks(this);

        currentUser = new UserPrincipal(1L, "Agon Osmani",
                "agon", "agon@osmani.com", "pass123", Collections.emptyList());

        controller = new PollController(pollService);
    }

    @Test
    public void testGetPolls200OKResponse() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(get("/api/polls"))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetPollsListOfEmptyPolls() {

        int page = 1, size = 30, totalElements = 0, totalPages = 0;
        boolean last = true;
        PagedResponse<PollResponse> response = new PagedResponse<PollResponse>(new ArrayList<>(),
                page,
                size,
                totalElements,
                totalPages,
                last);
        Mockito.when(this.pollService.getAllPolls(Mockito.any(UserPrincipal.class),
                                            Mockito.anyInt(),
                                            Mockito.anyInt())).thenReturn(response);

        Assert.assertEquals(response, controller.getPolls(currentUser, 1, 30));
    }

    @Test
    public void testGetPollsListOfNonEmptyList() {

        int page = 1, size = 30, totalElements = 0, totalPages = 0;
        boolean last = true;

        PagedResponse<PollResponse> response = new PagedResponse<PollResponse>(
                this.getPolls(),
                page,
                size,
                totalElements,
                totalPages,
                last);
        Mockito.when(this.pollService.getAllPolls(Mockito.any(UserPrincipal.class),
                Mockito.anyInt(),
                Mockito.anyInt())).thenReturn(response);

        Assert.assertEquals(response, controller.getPolls(currentUser, 1, 30));
    }

    @Test
    public void testCreatePollSuccessful() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        PollRequest poll = this.createPollRequest();

        Mockito.when(this.pollService.createPoll(Mockito.any(PollRequest.class))).thenReturn(this.createPoll(poll));

        ObjectMapper mapper = new ObjectMapper();
        String pollToCreate = mapper.writeValueAsString(poll);

        MvcResult result = mockMvc.perform(post("/api/polls")
                                            .content(pollToCreate)
                                            .contentType(MediaType.APPLICATION_JSON))
                                    .andExpect(status().isCreated())
                                    .andReturn();

        String uri = result.getResponse().getHeader("Location");

        Assert.assertEquals(uri, String.format("http://localhost/api/polls/%d", this.createPoll(poll).getId()));
    }

    private Poll createPoll(PollRequest pollRequest) {
        Poll poll = new Poll();
        poll.setId(1L);
        poll.setQuestion(pollRequest.getQuestion());
        Long i = 1L;
        for(ChoiceRequest choiceRequest : pollRequest.getChoices()) {
            Choice choice = new Choice();
            choice.setId(i);
            choice.setText(choiceRequest.getText());
            poll.addChoice(choice);
            i++;
        }
        return poll;
    }

    private PollRequest createPollRequest() {
        PollRequest request = new PollRequest();

        request.setQuestion("What fictional place would you most like to go to?");


        List<ChoiceRequest> choices = new ArrayList<>();

        ChoiceRequest choice1 = new ChoiceRequest();
        choice1.setText("Hogwarts");
        choices.add(choice1);

        ChoiceRequest choice2 = new ChoiceRequest();
        choice2.setText("Valyria");
        choices.add(choice2);

        ChoiceRequest choice3 = new ChoiceRequest();
        choice3.setText("Narnia");
        choices.add(choice3);

        request.setChoices(choices);


        PollLength length = new PollLength();
        length.setDays(2);
        length.setHours(2);
        request.setPollLength(length);


        return request;
    }

    private ArrayList<PollResponse> getPolls() {
        List<PollResponse> polls = new ArrayList<PollResponse>();

        PollResponse poll1 = new PollResponse();

        poll1.setId(1L);
        poll1.setQuestion("Would you rather be stuck on a broken ski lift or a broken elevator?");
        poll1.setCreationDateTime(Instant.parse("2022-08-30T10:15:30.00Z"));
        poll1.setExpirationDateTime(Instant.parse("2022-08-31T10:15:30.00Z"));

        Instant now = Instant.now();
        poll1.setExpired(poll1.getExpirationDateTime().isBefore(now));

        List<ChoiceResponse> choiceResponses = new ArrayList<>();
        ChoiceResponse choiceResponse1 = new ChoiceResponse();
        choiceResponse1.setId(1);
        choiceResponse1.setText("Ski Lift");
        choiceResponse1.setVoteCount(5L);
        choiceResponses.add(choiceResponse1);
        ChoiceResponse choiceResponse2 = new ChoiceResponse();
        choiceResponse2.setId(2);
        choiceResponse2.setText("Elevator");
        choiceResponse2.setVoteCount(6L);
        choiceResponses.add(choiceResponse2);

        poll1.setChoices(choiceResponses);


        UserSummary creatorSummary = new UserSummary(currentUser.getId(), currentUser.getUsername(), currentUser.getName());
        poll1.setCreatedBy(creatorSummary);

        poll1.setSelectedChoice(1L);

        poll1.setTotalVotes(11L);

        polls.add(poll1);

        PollResponse poll2 = new PollResponse();

        poll2.setId(2L);
        poll2.setQuestion("If you could live in a book, TV show, or movie, what would it be?");
        poll2.setCreationDateTime(Instant.parse("2022-08-30T10:15:30.00Z"));
        poll2.setExpirationDateTime(Instant.parse("2022-09-01T10:15:30.00Z"));

        poll2.setExpired(poll2.getExpirationDateTime().isBefore(now));

        List<ChoiceResponse> choiceResponses2 = new ArrayList<>();
        ChoiceResponse choiceResponse3 = new ChoiceResponse();
        choiceResponse3.setId(3);
        choiceResponse3.setText("Book");
        choiceResponse3.setVoteCount(6L);
        choiceResponses2.add(choiceResponse3);
        ChoiceResponse choiceResponse4 = new ChoiceResponse();
        choiceResponse4.setId(4);
        choiceResponse4.setText("TV Show");
        choiceResponse4.setVoteCount(8L);
        choiceResponses2.add(choiceResponse4);
        ChoiceResponse choiceResponse5 = new ChoiceResponse();
        choiceResponse5.setId(5);
        choiceResponse5.setText("Movie");
        choiceResponse5.setVoteCount(3L);
        choiceResponses2.add(choiceResponse5);

        poll2.setChoices(choiceResponses2);

        poll2.setCreatedBy(creatorSummary);

        poll2.setSelectedChoice(1L);

        poll2.setTotalVotes(17L);

        polls.add(poll2);

        return (ArrayList<PollResponse>) polls;
    }

}
