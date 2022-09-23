package com.example.polls.services;

import com.example.polls.exception.BadRequestException;
import com.example.polls.model.*;
import com.example.polls.payload.PagedResponse;
import com.example.polls.payload.PollResponse;
import com.example.polls.repository.PollRepository;
import com.example.polls.repository.UserRepository;
import com.example.polls.repository.VoteRepository;
import com.example.polls.security.UserPrincipal;
import com.example.polls.service.PollService;
import com.example.polls.util.AppConstants;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class PollServiceTest {

    @Mock
    private PollRepository pollRepository;

    @Mock
    private VoteRepository voteRepository;

    @Mock
    private UserRepository userRepository;

    private PollService pollService;

    @Before
    public void init() throws Exception {
        pollService = new PollService(pollRepository, voteRepository, userRepository);
    }

    // [1, 2, 3]
    @Test(expected = BadRequestException.class)
    public void testGetAllPollsPageLessThanZero() {
        pollService.getAllPolls(new UserPrincipal(1L,
                "john",
                "smith",
                "john@smith.com",
                "password",
                Collections.emptyList()), -1, 4);
    }

    // [1, 2, 4, 5]
    @Test(expected = BadRequestException.class)
    public void testGetAllPollsSizeTooLarge() {
        pollService.getAllPolls(new UserPrincipal(1L,
                "john",
                "smith",
                "john@smith.com",
                "password",
                Collections.emptyList()), 2,  AppConstants.MAX_PAGE_SIZE+5);
    }

    // [1, 2, 4, 6, 7]
    @Test
    public void testGetAllPollsPageExceeded() {

        List<Poll> polls = new ArrayList<>();
        polls.add(new Poll("q1"));
        polls.add(new Poll("q2"));
        polls.add(new Poll("q3"));

        Pageable pageable = PageRequest.of(2, 3, Sort.Direction.DESC, "createdAt");

        Page<Poll> polls1 = new PageImpl<>(new ArrayList<>(), pageable, polls.size());

        Mockito.when(this.pollRepository.findAll(PageRequest.of(2, 3, Sort.Direction.DESC, "createdAt")))
                        .thenReturn(polls1);

        PagedResponse<?> response = pollService.getAllPolls(new UserPrincipal(1L,
                "john",
                "smith",
                "john@smith.com",
                "password",
                Collections.emptyList()), 2,  3);

        Assert.assertEquals(new PagedResponse<>(Collections.emptyList(), polls1.getNumber(),
                polls1.getSize(), polls1.getTotalElements(), polls1.getTotalPages(), polls1.isLast()).toString(),
                response.toString()
                );
    }

    // [1, 2, 4, 6, 8, 9, 8, 10]
    @Test
    public void testGetAllPollsFunctional() {

        List<Poll> polls = new ArrayList<>();
        Poll poll1 = new Poll("q1");
        polls.add(poll1);
        polls.add(new Poll("q2"));
        polls.add(new Poll("q3"));

        Pageable pageable = PageRequest.of(0, 3, Sort.Direction.DESC, "createdAt");

        Page<Poll> polls1 = new PageImpl<>(polls.subList(0, 3), pageable, polls.size());

        Mockito.when(this.pollRepository.findAll(PageRequest.of(0, 3, Sort.Direction.DESC, "createdAt")))
                .thenReturn(polls1);

        List<ChoiceVoteCount> votes = new ArrayList<>();

        votes.add(new ChoiceVoteCount(1L, 4L));

        Mockito.when(this.voteRepository.countByPollIdInGroupByChoiceId(Mockito.anyList()))
                .thenReturn(votes);

        List<Vote> userVotes = new ArrayList<>();

        User creator = new User("john");

        userVotes.add(new Vote(poll1, new Choice("c1", poll1), creator));

        Mockito.when(this.voteRepository.findByUserIdAndPollIdIn(Mockito.anyLong(), Mockito.anyList()))
                .thenReturn(userVotes);

        List<User> creators = new ArrayList<>();

        creators.add(creator);

        Mockito.when(this.userRepository.findByIdIn(Mockito.anyList()))
                .thenReturn(creators);

        PagedResponse<?> response = pollService.getAllPolls(new UserPrincipal(1L,
                "john",
                "smith",
                "john@smith.com",
                "password",
                Collections.emptyList()), 0,  3);

        Assert.assertNotNull(response);
    }



}
