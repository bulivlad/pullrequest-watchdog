package io.watchdog.pullrequest.service;

import com.mchange.v2.lang.StringUtils;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import io.watchdog.pullrequest.config.RepositoryConfig;
import io.watchdog.pullrequest.dto.PaginatedPullRequestDTO;
import io.watchdog.pullrequest.dto.ParticipantDTO;
import io.watchdog.pullrequest.dto.PullRequestDTO;
import io.watchdog.pullrequest.dto.ReviewerDTO;
import io.watchdog.pullrequest.model.Role;
import io.watchdog.pullrequest.model.State;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.nonNull;

/**
 * @author vladclaudiubulimac on 2019-03-01.
 */

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PullRequestRetrieveService {

    RestTemplate restTemplate;
    RepositoryConfig repositoryConfig;

    @Autowired
    public PullRequestRetrieveService(RestTemplate restTemplate, RepositoryConfig repositoryConfig) {
        this.restTemplate = restTemplate;
        this.repositoryConfig = repositoryConfig;
    }

    public Map<String, List<ReviewerDTO>> getUnapprovedPRsWithReviewers(List<String> reviewers) {
        Map<String, List<ReviewerDTO>> unapprovedPRs = new HashMap<>();
        Stream<PullRequestDTO> teamUnapprovedPullRequests = getTeamOpenedPullRequests(reviewers, repositoryConfig.getSlug());
        teamUnapprovedPullRequests.forEach(pullRequestDTO -> {
            Set<ReviewerDTO> usersToApprove = getUsersToApprove(reviewers, pullRequestDTO);
            if (!usersToApprove.isEmpty()) {
                unapprovedPRs.put(pullRequestDTO.getSourceBranch(), new ArrayList<>(usersToApprove));
            }
        });
        return unapprovedPRs;
    }

    public List<PullRequestDTO> getUnapprovedPRs(List<String> reviewers, String repoSlug) {
        Stream<PullRequestDTO> teamUnapprovedPullRequests = getTeamOpenedPullRequests(reviewers, repoSlug);
        return teamUnapprovedPullRequests.peek(pullRequestDTO -> {
            Set<ReviewerDTO> usersToApprove = getUsersToApprove(reviewers, pullRequestDTO);
            pullRequestDTO.setReviewers(new ArrayList<>(usersToApprove));
        }).collect(Collectors.toList());
    }

    private Stream<PullRequestDTO> getTeamOpenedPullRequests(List<String> member, String repoSlug) {
        Stream<PullRequestDTO> pullRequestDTOStream = getAllTeamPullRequests(member, repoSlug);
        return pullRequestDTOStream.map(PullRequestDTO::getId)
                .map(pullRequestId -> fetchMemberDetailedPullRequest(pullRequestId, repoSlug))
                .map(Optional::get)
                .filter(pullRequestDTO -> State.OPEN.equals(pullRequestDTO.getState()));
    }

    private Stream<PullRequestDTO> getAllTeamPullRequests(List<String> reviewers, String repoSlug){
        PaginatedPullRequestDTO paginatedPullRequestDTOs = fetchPaginatedTeamOpenPullRequests(reviewers, repoSlug);
        Set<PullRequestDTO> pullRequestDTOs = new HashSet<>(paginatedPullRequestDTOs.getPullRequests());
        PullRequestDTOIterator pullRequestDTOIterator = new PullRequestDTOIterator(paginatedPullRequestDTOs, restTemplate);
        pullRequestDTOIterator.forEachRemaining(paginatedPullRequest -> pullRequestDTOs.addAll(paginatedPullRequest.getPullRequests()));
        return pullRequestDTOs.stream();
    }

    private PaginatedPullRequestDTO fetchPaginatedTeamOpenPullRequests(List<String> reviewers, String repoSlug) {
        log.info("Getting Open PR's for {}", reviewers);
        String queryString = buildReviewersQueryString(reviewers);
        try {
            PaginatedPullRequestDTO paginatedPullRequests = restTemplate.getForObject(
                    repositoryConfig.getEndpoint() + queryString,
                    PaginatedPullRequestDTO.class,
                    repositoryConfig.getUsername(),
                    getRepoSlug(repoSlug));
            log.info("Open PR's retrieved for {}", reviewers);
            return nonNull(paginatedPullRequests) ? paginatedPullRequests : new PaginatedPullRequestDTO();
        } catch (RestClientException ex) {
            log.error("Exception on retrieving PR's for " + reviewers, ex);
            return new PaginatedPullRequestDTO();
        }
    }

    private Optional<PullRequestDTO> fetchMemberDetailedPullRequest(Long pullRequestId, String repoSlug) {
        log.info("Getting PR with id {}", pullRequestId);
        try {
            PullRequestDTO pullRequest = restTemplate.getForObject(
                    repositoryConfig.getEndpoint() + "/{pullRequestId}",
                    PullRequestDTO.class,
                    repositoryConfig.getUsername(),
                    getRepoSlug(repoSlug),
                    pullRequestId);
            pullRequest.setLink(String.format(
                    repositoryConfig.getPullRequestsUrl(),
                    repositoryConfig.getUsername(),
                    getRepoSlug(repoSlug),
                    pullRequestId));
            return Optional.of(pullRequest);
        } catch (RestClientException ex) {
            log.error("Exception getting PR with id " + pullRequestId, ex);
            return Optional.empty();
        }
    }

    private Set<ReviewerDTO> getUsersToApprove(List<String> reviewers, PullRequestDTO pullRequestDTO) {
        return pullRequestDTO.getParticipants().stream()
                .filter(participantDTO -> Role.REVIEWER.equals(participantDTO.getRole()))
                .filter(participantDTO -> reviewers.contains(participantDTO.getReviewerDTO().getUsername()))
                .filter(participantDTO -> !participantDTO.getApproved())
                .map(ParticipantDTO::getReviewerDTO)
                .collect(Collectors.toSet());
    }

    private String buildReviewersQueryString(List<String> reviewers) {
        StringBuilder stringBuilder = new StringBuilder("?pagelen=50&q=(");
        reviewers.forEach(reviewer -> appendReviewers(stringBuilder, reviewer));
        return stringBuilder.substring(0, stringBuilder.lastIndexOf("OR")).concat(") AND state=\"OPEN\"");
    }

    private void appendReviewers(StringBuilder stringBuilder, String reviewer) {
        stringBuilder.append("reviewers.username=\"");
        stringBuilder.append(reviewer);
        stringBuilder.append("\" OR ");
    }

    private String getRepoSlug(String repoSlug) {
        return StringUtils.nonEmptyString(repoSlug) ? repoSlug : repositoryConfig.getSlug();
    }
}
