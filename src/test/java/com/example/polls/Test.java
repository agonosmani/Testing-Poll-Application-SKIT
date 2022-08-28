package com.example.polls;

import com.example.polls.controller.PollController;
import com.example.polls.model.Poll;
import com.example.polls.repository.PollRepository;
import com.example.polls.service.PollService;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(MockitoJUnitRunner.class)
public class Test {

    @Mock
    private PollService pollService;

    private Poll poll;

    private PollController controller;

    @Before
    public void init() throws Exception {
        MockitoAnnotations.openMocks(this);

        controller = new PollController(pollService);
    }

    @org.junit.Test
    public void testMockMVC() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(get("/api/polls"))
                .andExpect(status().isOk());
    }

}
