package io.watchdog.pullrequest.service;

import io.watchdog.pullrequest.config.RepositoryConfig;
import io.watchdog.pullrequest.dto.*;
import io.watchdog.pullrequest.model.Role;
import io.watchdog.pullrequest.model.State;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * @author vladclaudiubulimac on 2019-03-20.
 */

@RunWith(MockitoJUnitRunner.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BitBucketApiRestServiceTest {

    @Mock
    RestTemplate restTemplate;
    @Mock
    RepositoryConfig repositoryConfig;

    @InjectMocks
    BitBucketApiRestService bitBucketApiRestService;

    @Test
    public void fetchBitbucketUserDetailsByEmailEmptyUser() {
        String email = "email@example.com";
        String usersUrl = "http://dummy.com/users/{email}";

        when(repositoryConfig.getUsersUrl()).thenReturn(usersUrl);
        when(restTemplate.getForObject(eq(usersUrl), eq(BitbucketUserDTOWrapper.class), eq(email))).thenReturn(null);

        BitbucketUserDTO bitbucketUserDTO = bitBucketApiRestService.fetchBitbucketUserDetailsByEmail(email);

        assertThat(bitbucketUserDTO, notNullValue());
        assertThat(bitbucketUserDTO.getDisplayName(), nullValue());
        assertThat(bitbucketUserDTO.getFirstName(), nullValue());
        assertThat(bitbucketUserDTO.getLastName(), nullValue());
        assertThat(bitbucketUserDTO.getUsername(), nullValue());
    }

    @Test
    public void fetchBitbucketUserDetailsByEmail() {
        String email = "email@example.com";
        String usersUrl = "http://dummy.com/users/{email}";
        BitbucketUserDTO bitbucketUserDTO = new BitbucketUserDTO();
        bitbucketUserDTO.setDisplayName("BB bot");
        bitbucketUserDTO.setFirstName("BB");
        bitbucketUserDTO.setLastName("bot");
        bitbucketUserDTO.setUsername("bb-bot");
        BitbucketUserDTOWrapper bitbucketUserDTOWrapper = new BitbucketUserDTOWrapper();
        bitbucketUserDTOWrapper.setUser(bitbucketUserDTO);

        when(repositoryConfig.getUsersUrl()).thenReturn(usersUrl);
        when(restTemplate.getForObject(eq(usersUrl), eq(BitbucketUserDTOWrapper.class), eq(email))).thenReturn(bitbucketUserDTOWrapper);

        BitbucketUserDTO result = bitBucketApiRestService.fetchBitbucketUserDetailsByEmail(email);

        assertThat(bitbucketUserDTO, notNullValue());
        assertEquals(bitbucketUserDTO, result);
    }

    @Test(expected = RestClientException.class)
    public void fetchBitbucketUserDetailsByEmailRestException() {
        String email = "email@example.com";
        String usersUrl = "http://dummy.com/users/{email}";

        when(repositoryConfig.getUsersUrl()).thenReturn(usersUrl);
        when(restTemplate.getForObject(eq(usersUrl), eq(BitbucketUserDTOWrapper.class), eq(email))).thenThrow(RestClientException.class);

        BitbucketUserDTO bitbucketUserDTO = bitBucketApiRestService.fetchBitbucketUserDetailsByEmail(email);

        assertThat(bitbucketUserDTO, notNullValue());
        assertThat(bitbucketUserDTO.getDisplayName(), nullValue());
        assertThat(bitbucketUserDTO.getFirstName(), nullValue());
        assertThat(bitbucketUserDTO.getLastName(), nullValue());
        assertThat(bitbucketUserDTO.getUsername(), nullValue());
    }

    @Test
    public void fetchPaginatedPullRequest() {
        String slug = "dummy-slug";
        String queryString = "?pagelen=50&q=(reviewers.account_id=\"bb-accId1\" OR reviewers.account_id=\"bb-accId2\" OR reviewers.account_id=\"bb-accId3\" ) AND state=\"OPEN\"";
        String username = "dummy-username";
        String repoConfigEndpoint = "https://api.bitbucket.org/2.0/repositories/" + username + "/" + slug + "/pullrequests";

        when(repositoryConfig.getUsername()).thenReturn(username);
        when(repositoryConfig.getEndpoint()).thenReturn(repoConfigEndpoint);
        when(restTemplate.getForObject(eq(repoConfigEndpoint + queryString), eq(PaginatedPullRequestDTO.class), eq(username), eq(slug))).thenReturn(buildPaginatedPullRequestDTOSinglePR());

        PaginatedPullRequestDTO paginatedPullRequestDTO = bitBucketApiRestService.fetchPaginatedPullRequest(slug, queryString);

        assertEquals(1, paginatedPullRequestDTO.getPullRequests().size());
        assertEquals(1, paginatedPullRequestDTO.getSize().longValue());
        assertEquals(1, paginatedPullRequestDTO.getPageLength().longValue());
        assertNull(paginatedPullRequestDTO.getNext());

    }

    @Test(expected = ResourceAccessException.class)
    public void fetchPaginatedPullRequestResourceAccessException() {
        String slug = "dummy-slug";
        String queryString = "?pagelen=50&q=(reviewers.account_id=\"bb-accId1\" OR reviewers.account_id=\"bb-accId2\" OR reviewers.account_id=\"bb-accId3\" ) AND state=\"OPEN\"";
        String username = "dummy-username";
        String repoConfigEndpoint = "https://api.bitbucket.org/2.0/repositories/" + username + "/" + slug + "/pullrequests";

        when(repositoryConfig.getUsername()).thenReturn(username);
        when(repositoryConfig.getEndpoint()).thenReturn(repoConfigEndpoint);
        when(restTemplate.getForObject(eq(repoConfigEndpoint + queryString), eq(PaginatedPullRequestDTO.class), eq(username), eq(slug))).thenThrow(ResourceAccessException.class);

        bitBucketApiRestService.fetchPaginatedPullRequest(slug, queryString);
    }

    @Test
    public void fetchMemberDetailedPullRequest() {
        String username = "dummy-username";
        String slug = "dummy-slug";
        String repoConfigEndpoint = "https://api.bitbucket.org/2.0/repositories/" + username + "/" + slug + "/pullrequests";
        Long prId = 1L;

        ParticipantDTO participantDTO1 = buildParticipantDTO(Role.REVIEWER, "bb-user1", "bb-accId1");
        ParticipantDTO participantDTO2 = buildParticipantDTO(Role.PARTICIPANT, "bb-user2", "bb-accId2");

        PullRequestDTO pullRequestDTO = new PullRequestDTO();
        pullRequestDTO.setId(1L);
        pullRequestDTO.setState(State.OPEN);
        pullRequestDTO.setParticipants(Arrays.asList(participantDTO1, participantDTO2));
        pullRequestDTO.setSourceBranch("origin/source-branch");

        when(repositoryConfig.getUsername()).thenReturn(username);
        when(repositoryConfig.getEndpoint()).thenReturn(repoConfigEndpoint);
        when(restTemplate.getForObject(eq(repoConfigEndpoint + "/{pullRequestId}"), eq(PullRequestDTO.class), eq(username), eq(slug), eq(prId))).thenReturn(pullRequestDTO);

        PullRequestDTO result = bitBucketApiRestService.fetchMemberDetailedPullRequest(prId, slug);

        assertEquals(pullRequestDTO, result);
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
}