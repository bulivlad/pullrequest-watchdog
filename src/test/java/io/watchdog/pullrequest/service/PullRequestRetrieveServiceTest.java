package io.watchdog.pullrequest.service;

import io.watchdog.pullrequest.config.RepositoryConfig;
import io.watchdog.pullrequest.dto.PaginatedPullRequestDTO;
import io.watchdog.pullrequest.dto.ParticipantDTO;
import io.watchdog.pullrequest.dto.PullRequestDTO;
import io.watchdog.pullrequest.dto.ReviewerDTO;
import io.watchdog.pullrequest.model.Role;
import io.watchdog.pullrequest.model.State;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * @author vladclaudiubulimac on 2019-03-20.
 */

@RunWith(MockitoJUnitRunner.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PullRequestRetrieveServiceTest {

    @Mock
    RestTemplate restTemplate;
    @Mock
    RepositoryConfig repositoryConfig;

    @InjectMocks
    PullRequestRetrieveService pullRequestRetrieveService;

    private static final String REPO_CONFIG_ENDPOINT = "https://dummy.com/repositories/{username}/{slug}/pullrequests";
    private static final String REVIEWERS_QUERY_STRING = "?pagelen=50&q=(reviewers.account_id=\"bb-accId1\" OR reviewers.account_id=\"bb-accId2\" OR reviewers.account_id=\"bb-accId3\" ) AND state=\"OPEN\"";
    private static final String SLUG = "dummy-slug";
    private static final String USERNAME = "dummy-username";

    @Before
    public void setup() {
        when(repositoryConfig.getEndpoint()).thenReturn(REPO_CONFIG_ENDPOINT);
        when(repositoryConfig.getPullRequestsUrl()).thenReturn("https://dummy.com/%s/%s/pull-requests/%s");
        when(repositoryConfig.getSlug()).thenReturn(SLUG);
        when(repositoryConfig.getUsername()).thenReturn(USERNAME);
    }

    @Test
    public void getUnapprovedPRsWithReviewersOneReviewer() {
        List<String> reviewers = Arrays.asList("bb-accId1", "bb-accId2", "bb-accId3");
        ReviewerDTO reviewerDTO = new ReviewerDTO();
        reviewerDTO.setUsername("bb-user1");
        reviewerDTO.setAccountId("bb-accId1");

        when(restTemplate.getForObject(eq(REPO_CONFIG_ENDPOINT + REVIEWERS_QUERY_STRING), eq(PaginatedPullRequestDTO.class), eq(USERNAME), eq(SLUG))).thenReturn(buildPaginatedPullRequestDTOSinglePR());
        when(restTemplate.getForObject(eq(REPO_CONFIG_ENDPOINT + "/{pullRequestId}"), eq(PullRequestDTO.class), eq(USERNAME), eq(SLUG), eq(1L))).thenReturn(buildPullRequestDTOOneReviewer());

        Map<String, List<ReviewerDTO>> result = pullRequestRetrieveService.getUnapprovedPRsWithReviewers(reviewers);

        assertThat(result, notNullValue());
        assertThat(result.size(), equalTo(1));
        assertThat(result.containsKey("origin/source-branch"), is(true));
        assertThat(result.get("origin/source-branch"), equalTo(Collections.singletonList(reviewerDTO)));
    }

    @Test
    public void getUnapprovedPRsWithReviewersMultipleReviewers() {
        List<String> reviewers = Arrays.asList("bb-accId1", "bb-accId2", "bb-accId3");
        ReviewerDTO reviewerDTO = new ReviewerDTO();
        reviewerDTO.setUsername("bb-user1");
        reviewerDTO.setAccountId("bb-accId1");
        ReviewerDTO reviewerDTO3 = new ReviewerDTO();
        reviewerDTO3.setUsername("bb-user3");
        reviewerDTO3.setAccountId("bb-accId3");
        List<ReviewerDTO> expected = Arrays.asList(reviewerDTO, reviewerDTO3);

        when(restTemplate.getForObject(eq(REPO_CONFIG_ENDPOINT + REVIEWERS_QUERY_STRING), eq(PaginatedPullRequestDTO.class), eq(USERNAME), eq(SLUG))).thenReturn(buildPaginatedPullRequestDTOMultiplePRs());
        PullRequestDTO prId1 = buildPullRequestDTOMultipleReviewers(1L, "origin/source-branch1");
        when(restTemplate.getForObject(eq(REPO_CONFIG_ENDPOINT + "/{pullRequestId}"), eq(PullRequestDTO.class), eq(USERNAME), eq(SLUG), eq(1L))).thenReturn(prId1);
        PullRequestDTO prId2 = buildPullRequestDTOMultipleReviewers(2L, "origin/source-branch2");
        when(restTemplate.getForObject(eq(REPO_CONFIG_ENDPOINT + "/{pullRequestId}"), eq(PullRequestDTO.class), eq(USERNAME), eq(SLUG), eq(2L))).thenReturn(prId2);
        PullRequestDTO prId3 = buildPullRequestDTOMultipleReviewers(3L, "origin/source-branch3");
        when(restTemplate.getForObject(eq(REPO_CONFIG_ENDPOINT + "/{pullRequestId}"), eq(PullRequestDTO.class), eq(USERNAME), eq(SLUG), eq(3L))).thenReturn(prId3);

        Map<String, List<ReviewerDTO>> result = pullRequestRetrieveService.getUnapprovedPRsWithReviewers(reviewers);

        assertThat(result, notNullValue());
        assertThat(result.size(), equalTo(3));
        assertThat(result.containsKey("origin/source-branch1"), is(true));
        assertThat(result.containsKey("origin/source-branch2"), is(true));
        assertThat(result.containsKey("origin/source-branch3"), is(true));
        assertThat(result.get("origin/source-branch1").containsAll(expected), is(true));
        assertThat(result.get("origin/source-branch2").containsAll(expected), is(true));
        assertThat(result.get("origin/source-branch3").containsAll(expected), is(true));
    }

    @Test
    public void getUnapprovedPRsSinglePR() {
        List<String> reviewers = Arrays.asList("bb-accId1", "bb-accId2", "bb-accId3");

        ParticipantDTO participantDTO1 = buildParticipantDTO(Role.REVIEWER, "bb-user1", "bb-accId1");
        ParticipantDTO participantDTO2 = buildParticipantDTO(Role.PARTICIPANT, "bb-user2", "bb-accId2");
        ReviewerDTO reviewerDTO = new ReviewerDTO();
        reviewerDTO.setUsername("bb-user1");
        reviewerDTO.setAccountId("bb-accId1");

        PullRequestDTO expected = buildPullRequestDTO(1L, "origin/source-branch", Arrays.asList(participantDTO1, participantDTO2), Collections.singletonList(reviewerDTO));

        when(restTemplate.getForObject(eq(REPO_CONFIG_ENDPOINT + REVIEWERS_QUERY_STRING), eq(PaginatedPullRequestDTO.class), eq(USERNAME), eq(SLUG))).thenReturn(buildPaginatedPullRequestDTOSinglePR());
        when(restTemplate.getForObject(eq(REPO_CONFIG_ENDPOINT + "/{pullRequestId}"), eq(PullRequestDTO.class), eq(USERNAME), eq(SLUG), eq(1L))).thenReturn(buildPullRequestDTOOneReviewer());

        List<PullRequestDTO> unapprovedPRs = pullRequestRetrieveService.getUnapprovedPRs(reviewers, "dummy-slug");

        assertThat(unapprovedPRs, notNullValue());
        assertThat(unapprovedPRs.size(), equalTo(1));
        assertThat(unapprovedPRs.get(0), equalTo(expected));
    }

    @Test
    public void getUnapprovedPRsMultiplePR() {
        List<String> reviewers = Arrays.asList("bb-accId1", "bb-accId2", "bb-accId3");

        ParticipantDTO participantDTO1 = buildParticipantDTO(Role.REVIEWER, "bb-user1", "bb-accId1");
        ParticipantDTO participantDTO2 = buildParticipantDTO(Role.PARTICIPANT, "bb-user2", "bb-accId2");
        ParticipantDTO participantDTO3 = buildParticipantDTO(Role.REVIEWER, "bb-user3", "bb-accId3");

        PullRequestDTO expectedPr1 = buildPullRequestDTO(1L, "origin/source-branch-1", Arrays.asList(participantDTO1, participantDTO2, participantDTO3), Arrays.asList(participantDTO1.getReviewerDTO(), participantDTO3.getReviewerDTO()));
        PullRequestDTO expectedPr2 = buildPullRequestDTO(2L, "origin/source-branch-2", Arrays.asList(participantDTO1, participantDTO2, participantDTO3), Arrays.asList(participantDTO1.getReviewerDTO(), participantDTO3.getReviewerDTO()));
        PullRequestDTO expectedPr3 = buildPullRequestDTO(3L, "origin/source-branch-3", Arrays.asList(participantDTO1, participantDTO2, participantDTO3), Arrays.asList(participantDTO1.getReviewerDTO(), participantDTO3.getReviewerDTO()));

        when(restTemplate.getForObject(eq(REPO_CONFIG_ENDPOINT + REVIEWERS_QUERY_STRING), eq(PaginatedPullRequestDTO.class), eq(USERNAME), eq(SLUG))).thenReturn(buildPaginatedPullRequestDTOMultiplePRs());
        PullRequestDTO prId1 = buildPullRequestDTOMultipleReviewers(1L, "origin/source-branch-1");
        when(restTemplate.getForObject(eq(REPO_CONFIG_ENDPOINT + "/{pullRequestId}"), eq(PullRequestDTO.class), eq(USERNAME), eq(SLUG), eq(1L))).thenReturn(prId1);
        PullRequestDTO prId2 = buildPullRequestDTOMultipleReviewers(2L, "origin/source-branch-2");
        when(restTemplate.getForObject(eq(REPO_CONFIG_ENDPOINT + "/{pullRequestId}"), eq(PullRequestDTO.class), eq(USERNAME), eq(SLUG), eq(2L))).thenReturn(prId2);
        PullRequestDTO prId3 = buildPullRequestDTOMultipleReviewers(3L, "origin/source-branch-3");
        when(restTemplate.getForObject(eq(REPO_CONFIG_ENDPOINT + "/{pullRequestId}"), eq(PullRequestDTO.class), eq(USERNAME), eq(SLUG), eq(3L))).thenReturn(prId3);

        List<PullRequestDTO> unapprovedPRs = pullRequestRetrieveService.getUnapprovedPRs(reviewers, "dummy-slug");

        assertThat(unapprovedPRs, notNullValue());
        assertThat(unapprovedPRs.size(), equalTo(3));
        boolean assertPr1 = unapprovedPRs.stream().allMatch(e -> e.getReviewers().containsAll(expectedPr1.getReviewers()));
        boolean assertPr2 = unapprovedPRs.stream().allMatch(e -> e.getReviewers().containsAll(expectedPr2.getReviewers()));
        boolean assertPr3 = unapprovedPRs.stream().allMatch(e -> e.getReviewers().containsAll(expectedPr3.getReviewers()));
        assertTrue(assertPr1);
        assertTrue(assertPr2);
        assertTrue(assertPr3);
    }

    private PaginatedPullRequestDTO buildPaginatedPullRequestDTOSinglePR() {
        PullRequestDTO pullRequestDTO = new PullRequestDTO();
        pullRequestDTO.setId(1L);
        PaginatedPullRequestDTO paginatedPullRequestDTO = new PaginatedPullRequestDTO();
        paginatedPullRequestDTO.setSize(1L);
        paginatedPullRequestDTO.setPage(1L);
        paginatedPullRequestDTO.setPageLength(1L);
        paginatedPullRequestDTO.setNext(null);
        paginatedPullRequestDTO.setPullRequests(Collections.singletonList(pullRequestDTO));

        return paginatedPullRequestDTO;
    }

    private PaginatedPullRequestDTO buildPaginatedPullRequestDTOMultiplePRs() {
        PullRequestDTO pullRequestDTO = new PullRequestDTO();
        pullRequestDTO.setId(1L);
        PullRequestDTO pullRequestDTO2 = new PullRequestDTO();
        pullRequestDTO2.setId(2L);
        PullRequestDTO pullRequestDTO3 = new PullRequestDTO();
        pullRequestDTO3.setId(3L);
        PaginatedPullRequestDTO paginatedPullRequestDTO = new PaginatedPullRequestDTO();
        paginatedPullRequestDTO.setSize(3L);
        paginatedPullRequestDTO.setPage(1L);
        paginatedPullRequestDTO.setPageLength(3L);
        paginatedPullRequestDTO.setNext(null);
        paginatedPullRequestDTO.setPullRequests(Arrays.asList(pullRequestDTO, pullRequestDTO2, pullRequestDTO3));

        return paginatedPullRequestDTO;
    }

    private PullRequestDTO buildPullRequestDTOOneReviewer() {
        ParticipantDTO participantDTO1 = buildParticipantDTO(Role.REVIEWER, "bb-user1", "bb-accId1");
        ParticipantDTO participantDTO2 = buildParticipantDTO(Role.PARTICIPANT, "bb-user2", "bb-accId2");

        PullRequestDTO pullRequestDTO = new PullRequestDTO();
        pullRequestDTO.setId(1L);
        pullRequestDTO.setState(State.OPEN);
        pullRequestDTO.setParticipants(Arrays.asList(participantDTO1, participantDTO2));
        pullRequestDTO.setSourceBranch("origin/source-branch");

        return  pullRequestDTO;
    }

    private PullRequestDTO buildPullRequestDTOMultipleReviewers(Long prId, String sourceBranch) {
        ParticipantDTO participantDTO1 = buildParticipantDTO(Role.REVIEWER, "bb-user1", "bb-accId1");
        ParticipantDTO participantDTO2 = buildParticipantDTO(Role.PARTICIPANT, "bb-user2", "bb-accId2");
        ParticipantDTO participantDTO3 = buildParticipantDTO(Role.REVIEWER, "bb-user3", "bb-accId3");

        PullRequestDTO pullRequestDTO = new PullRequestDTO();
        pullRequestDTO.setId(prId);
        pullRequestDTO.setState(State.OPEN);
        pullRequestDTO.setParticipants(Arrays.asList(participantDTO1, participantDTO2, participantDTO3));
        pullRequestDTO.setSourceBranch(sourceBranch);

        return  pullRequestDTO;
    }

    private ParticipantDTO buildParticipantDTO(Role reviewer, String username, String accountId) {
        ParticipantDTO participantDTO = new ParticipantDTO();
        participantDTO.setRole(reviewer);
        ReviewerDTO reviewerDTO = new ReviewerDTO();
        reviewerDTO.setUsername(username);
        reviewerDTO.setAccountId(accountId);
        participantDTO.setReviewerDTO(reviewerDTO);
        participantDTO.setApproved(false);
        return participantDTO;
    }

    private PullRequestDTO buildPullRequestDTO(Long id, String sourceBranch, List<ParticipantDTO> participantsDTO, List<ReviewerDTO> reviewerDTO) {
        PullRequestDTO expected = new PullRequestDTO();
        expected.setId(id);
        expected.setSourceBranch(sourceBranch);
        expected.setState(State.OPEN);
        expected.setReviewers(reviewerDTO);
        expected.setParticipants(participantsDTO);
        expected.setLink("https://dummy.com/dummy-username/dummy-slug/pull-requests/" + id.toString());
        return expected;
    }
}