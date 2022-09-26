package com.example.polls.services;

import com.example.polls.exception.BadRequestException;
import com.example.polls.exception.ResourceNotFoundException;
import com.example.polls.model.*;
import com.example.polls.payload.*;
import com.example.polls.repository.PollRepository;
import com.example.polls.repository.UserRepository;
import com.example.polls.repository.VoteRepository;
import com.example.polls.security.UserPrincipal;
import com.example.polls.service.PollService;
import com.example.polls.util.AppConstants;
import com.example.polls.util.ModelMapper;
import org.checkerframework.checker.units.qual.C;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;

import java.time.Instant;
import java.util.*;

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
        poll1.setCreatedBy(1L);
        polls.add(poll1);

        Poll poll2 = new Poll("q2");
        poll2.setCreatedBy(1L);
        polls.add(poll2);

        Poll poll3 = new Poll("q3");
        poll3.setCreatedBy(1L);
        polls.add(poll3);

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


    // createPoll

    @Test
    public void testCreatePoll() {
        Poll mockPoll = new Poll("Quest");
        mockPoll.setChoices(Arrays.asList(new Choice("yes"), new Choice("no")));

        Mockito.when(pollRepository.save(Mockito.any()))
                .thenReturn(mockPoll);

        Poll result = pollService.createPoll(new PollRequest(
                "Quest",
                Arrays.asList(new ChoiceRequest("yes"), new ChoiceRequest("no")),
                new PollLength(1, 1)));

        Assert.assertEquals(result.toString(), mockPoll.toString());
    }

    // getPollById

    // [1, 2, 3]
    @Test(expected = ResourceNotFoundException.class)
    public void testGetPollByIdNotFound() {

        Mockito.when(this.pollRepository.findById(Mockito.anyLong()))
                        .thenReturn(Optional.ofNullable(null));

        pollService.getPollById(1L, new UserPrincipal());
    }

    // [1, 2, 4, 5]
    @Test(expected = ResourceNotFoundException.class)
    public void testGetPollByIdUserNotFound() {

        Mockito.when(this.pollRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(new Poll("Question")));

        Mockito.when(this.voteRepository.countByPollIdGroupByChoiceId(Mockito.anyLong()))
                .thenReturn(Arrays.asList(
                        new ChoiceVoteCount(1L, 5L),
                        new ChoiceVoteCount(2L, 4L)
                ));

        Mockito.when(this.userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.ofNullable(null));

        pollService.getPollById(1L, new UserPrincipal());
    }

    // [1, 2, 4, 6, 8]
    @Test
    public void testGetPollByIdCurrentUserNull() {

        Poll poll = new Poll("Question");
        poll.setCreatedBy(1L);

        Mockito.when(this.pollRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(poll));

        Mockito.when(this.voteRepository.countByPollIdGroupByChoiceId(Mockito.anyLong()))
                .thenReturn(Arrays.asList(
                        new ChoiceVoteCount(1L, 5L),
                        new ChoiceVoteCount(2L, 4L)
                ));

        Mockito.when(this.userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(new User("John")));

        pollService.getPollById(1L, null);
    }

    // [1, 2, 4, 6, 7, 8]
    @Test
    public void testGetPollByIdFunctional() {
        Poll poll = new Poll("Question");
        poll.setCreatedBy(1L);

        Mockito.when(this.pollRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(poll));

        Mockito.when(this.voteRepository.countByPollIdGroupByChoiceId(Mockito.anyLong()))
                .thenReturn(Arrays.asList(
                        new ChoiceVoteCount(1L, 5L),
                        new ChoiceVoteCount(2L, 4L)
                ));

        Mockito.when(this.userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(new User("John")));

        Choice choice = new Choice("yes", poll);
        User user = new User("John");

        Mockito.when(this.voteRepository.findByUserIdAndPollId(Mockito.anyLong(), Mockito.anyLong()))
                .thenReturn(new Vote(poll, choice, user));

        pollService.getPollById(1L, new UserPrincipal(1L,
                "john",
                "smith",
                "john@smith.com",
                "password",
                Collections.emptyList()));
    }

    // castVoteAndGetUpdatedPoll

    // [1, 2, 3]
    @Test(expected = ResourceNotFoundException.class)
    public void testCastVoteAndGetUpdatedPollNotFound() {

        Mockito.when(this.pollRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.ofNullable(null));

        pollService.castVoteAndGetUpdatedPoll(1L, new VoteRequest(),new UserPrincipal());
    }

    // [1, 2, 4, 5]
    @Test(expected = BadRequestException.class)
    public void testCastVoteAndGetUpdatedPollExpired() {

        Poll poll = new Poll("Question");
        poll.setExpirationDateTime(Instant.now().minusSeconds(10000));

        Mockito.when(this.pollRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(poll));

        pollService.castVoteAndGetUpdatedPoll(1L, new VoteRequest(),new UserPrincipal());
    }

    // [1, 2, 4, 6, 7]
    @Test(expected = ResourceNotFoundException.class)
    public void testCastVoteAndGetUpdatedChoiceNotFound() {

        Poll poll = new Poll("Question");
        poll.setExpirationDateTime(Instant.now().plusSeconds(10000));

        Mockito.when(this.pollRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(poll));

        Mockito.when(userRepository.getOne(Mockito.anyLong()))
                .thenReturn(new User("John"));

        pollService.castVoteAndGetUpdatedPoll(1L, new VoteRequest(1L), new UserPrincipal());
    }

    // [1, 2, 4, 6, 8, 9]
    @Test(expected = BadRequestException.class)
    public void testCastVoteAndGetUpdatedAlreadyVoted() {

        Poll poll = new Poll("Question");
        poll.setExpirationDateTime(Instant.now().plusSeconds(10000));
        poll.setChoices(Arrays.asList(new Choice("yes", poll), new Choice("no", poll)));

        Mockito.when(this.pollRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(poll));

        Mockito.when(userRepository.getOne(Mockito.anyLong()))
                .thenReturn(new User("John"));

        Mockito.when(voteRepository.save(Mockito.any()))
                .thenThrow(DataIntegrityViolationException.class);

        pollService.castVoteAndGetUpdatedPoll(1L, new VoteRequest(1L), new UserPrincipal());
    }

    // [1, 2, 4, 6, 8, 10, 11]
    @Test(expected = ResourceNotFoundException.class)
    public void testCastVoteAndGetUpdatedUserNotFound() {

        Poll poll = new Poll("Question");
        poll.setExpirationDateTime(Instant.now().plusSeconds(10000));

        Choice choice = new Choice("yes", poll);
        poll.setChoices(Arrays.asList(choice, new Choice("no", poll)));

        Mockito.when(this.pollRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(poll));

        User user = new User("John");
        Mockito.when(userRepository.getOne(Mockito.anyLong()))
                .thenReturn(user);

        Mockito.when(voteRepository.save(Mockito.any()))
                .thenReturn(new Vote(poll, choice, user));

        Mockito.when(voteRepository.countByPollIdGroupByChoiceId(Mockito.anyLong()))
                .thenReturn(Arrays.asList(
                        new ChoiceVoteCount(1L, 5L),
                        new ChoiceVoteCount(2L, 4L)
                ));

        Mockito.when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.ofNullable(null));

        pollService.castVoteAndGetUpdatedPoll(1L, new VoteRequest(1L), new UserPrincipal());
    }

    // [1, 2, 4, 6, 8, 10, 12]
    @Test
    public void testCastVoteAndGetUpdatedFunctional() {

        Poll poll = new Poll("Question");
        poll.setExpirationDateTime(Instant.now().plusSeconds(10000));
        poll.setCreatedBy(1L);

        Choice choice = new Choice("yes", poll);
        poll.setChoices(Arrays.asList(choice, new Choice("no", poll)));

        Mockito.when(this.pollRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(poll));

        User user = new User("John");
        Mockito.when(userRepository.getOne(Mockito.anyLong()))
                .thenReturn(user);

        Mockito.when(voteRepository.save(Mockito.any()))
                .thenReturn(new Vote(poll, choice, user));

        Mockito.when(voteRepository.countByPollIdGroupByChoiceId(Mockito.anyLong()))
                .thenReturn(Arrays.asList(
                        new ChoiceVoteCount(1L, 5L),
                        new ChoiceVoteCount(2L, 4L)
                ));

        Mockito.when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(new User("Creator")));

        pollService.castVoteAndGetUpdatedPoll(1L, new VoteRequest(1L), new UserPrincipal());
    }


}
