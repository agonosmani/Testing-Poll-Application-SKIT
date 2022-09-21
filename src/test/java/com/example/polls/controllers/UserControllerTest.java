package com.example.polls.controllers;

import com.example.polls.controller.UserController;
import com.example.polls.model.User;
import com.example.polls.payload.PagedResponse;
import com.example.polls.payload.PollResponse;
import com.example.polls.payload.UserIdentityAvailability;
import com.example.polls.payload.UserProfile;
import com.example.polls.repository.PollRepository;
import com.example.polls.repository.UserRepository;
import com.example.polls.repository.VoteRepository;
import com.example.polls.security.UserPrincipal;
import com.example.polls.service.PollService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(MockitoJUnitRunner.class)
public class UserControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PollRepository pollRepository;

    @Mock
    private VoteRepository voteRepository;

    @Mock
    private PollService pollService;

    private UserController userController;

    private MockMvc mockMvc;

    private ObjectMapper mapper;

    private UserIdentityAvailability usernameAvailable;
    private UserIdentityAvailability usernameUnavailable;

    @Before
    public void init() throws Exception {
        MockitoAnnotations.openMocks(this);

        userController = new UserController(userRepository, pollRepository, voteRepository, pollService);

        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();

        mapper = new ObjectMapper();

        JavaTimeModule module = new JavaTimeModule();
        mapper.registerModule(module);

        usernameAvailable = new UserIdentityAvailability(true);
        usernameUnavailable = new UserIdentityAvailability(false);
    }

    @Test
    public void testCheckUsernameAvailability200OKResponse() throws Exception {
        mockMvc.perform(get("/api/user/checkUsernameAvailability")
                        .param("username", "johnsmith"))
                .andExpect(status().isOk());
    }

    @Test
    public void testCheckUsernameAvailabilityWithUnregisteredUsername() throws Exception {

        Mockito.when(this.userRepository.existsByUsername("agonosmani"))
                .thenReturn(false);

        MvcResult result = mockMvc.perform(get("/api/user/checkUsernameAvailability")
                        .param("username", "agonosmani"))
                .andExpect(status().isOk()).andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        UserIdentityAvailability availability = mapper.readValue(jsonResponse, UserIdentityAvailability.class);

        Assert.assertEquals(availability.toString(), this.usernameAvailable.toString());
        Assert.assertNotEquals(availability.toString(), this.usernameUnavailable.toString());
    }

    @Test
    public void testCheckUsernameAvailabilityWithRegisteredUsername() throws Exception {

        Mockito.when(this.userRepository.existsByUsername("agonosmani"))
                .thenReturn(true);

        MvcResult result = mockMvc.perform(get("/api/user/checkUsernameAvailability")
                        .param("username", "agonosmani"))
                .andExpect(status().isOk()).andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        UserIdentityAvailability availability = mapper.readValue(jsonResponse, UserIdentityAvailability.class);

        Assert.assertEquals(availability.toString(), this.usernameUnavailable.toString());
        Assert.assertNotEquals(availability.toString(), this.usernameAvailable.toString());
    }

    @Test
    public void testGetUserProfileUserExists() throws Exception {

        User user = new User("John", "johnsmith", "john_smith@gmail.com", "pass123");
        user.setId(1L);

        Mockito.when(this.userRepository.findByUsername("johnsmith"))
                .thenReturn(Optional.of(user));

        long pollCount = 5L;
        Mockito.when(this.pollRepository.countByCreatedBy(user.getId()))
                .thenReturn(pollCount);

        long voteCount = 4L;
        Mockito.when(this.voteRepository.countByUserId(user.getId()))
                .thenReturn(voteCount);

        MvcResult result = mockMvc.perform(get("/api/users/johnsmith"))
                .andExpect(status().isOk()).andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        UserProfile responseProfile = mapper.readValue(jsonResponse, UserProfile.class);

        UserProfile mockProfile = new UserProfile(user.getId(), user.getUsername(), user.getName(), user.getCreatedAt(), pollCount, voteCount);

        Assert.assertEquals(responseProfile.toString(), mockProfile.toString());
    }

    @Test
    public void testGetUserProfileUserNotExists() throws Exception {

        Mockito.when(this.userRepository.findByUsername("johnsmith"))
                .thenReturn(Optional.ofNullable(null));

        mockMvc.perform(get("/api/users/johnsmith"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetPollsVotedBy() throws Exception {

        PollControllerTest test = new PollControllerTest();
        test.init();
        PagedResponse<PollResponse> mockResponse = test.getMockResponse(test.getPolls());

        Mockito.when(this.pollService.getPollsVotedBy(
                    Mockito.any(),
                    Mockito.any(),
                    Mockito.anyInt(),
                    Mockito.anyInt()
                ))
                .thenReturn(mockResponse);

        MvcResult result = mockMvc.perform(get("/api/users/johnsmith/votes"))
                .andExpect(status().isOk()).andReturn();

        String jsonResponse = result.getResponse().getContentAsString();

        PagedResponse<PollResponse> pagedResponse = mapper.readValue(jsonResponse, PagedResponse.class);

        Assert.assertEquals(pagedResponse.toString(), mockResponse.toString());
    }


}
