package io.watchdog.pullrequest.service;

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

    public Map<String, List<ReviewerDTO>> getUnapprovedPRsWithReviwers(List<String> reviewers) {
        Map<String, List<ReviewerDTO>> unapprovedPRs = new HashMap<>();
        Stream<PullRequestDTO> teamUnapprovedPullRequests = getTeamUnapprovedPullRequests(reviewers);
        teamUnapprovedPullRequests.forEach(e -> {
            List<ReviewerDTO> usersToApprove = getUsersToApprove(reviewers, e);
            if (!usersToApprove.isEmpty()) {
                unapprovedPRs.put(e.getSourceBranch(), usersToApprove);
            }
        });
        return unapprovedPRs;
    }

    public List<PullRequestDTO> getUnapprovedPRs(List<String> reviewers) {
        Stream<PullRequestDTO> teamUnapprovedPullRequests = getTeamUnapprovedPullRequests(reviewers);
        return teamUnapprovedPullRequests.collect(Collectors.toList());
    }

    private Stream<PullRequestDTO> getTeamUnapprovedPullRequests(List<String> member) {
        Stream<PullRequestDTO> pullRequestDTOStream = getAllTeamPullRequests(member);
        return pullRequestDTOStream.map(PullRequestDTO::getId)
                .map(this::fetchMemberDetailedPullRequest)
                .map(Optional::get)
                .filter(pullRequestDTO -> State.OPEN.equals(pullRequestDTO.getState()));
    }

    private Stream<PullRequestDTO> getAllTeamPullRequests(List<String> reviewers){
        PaginatedPullRequestDTO paginatedPullRequestDTOs = fetchPaginatedTeamOpenPullRequests(reviewers);
        List<PullRequestDTO> pullRequestDTOs = new ArrayList<>(paginatedPullRequestDTOs.getPullRequests());
        PullRequestDTOIterator pullRequestDTOIterator = new PullRequestDTOIterator(paginatedPullRequestDTOs, restTemplate);
        pullRequestDTOIterator.forEachRemaining(paginatedPullRequest -> pullRequestDTOs.addAll(paginatedPullRequest.getPullRequests()));
        return pullRequestDTOs.stream();
    }

    private PaginatedPullRequestDTO fetchPaginatedTeamOpenPullRequests(List<String> reviewers) {
        log.info("Getting Open PR's for {}", reviewers);
        String queryString = buildReviewersQueryString(reviewers);
        try {
            PaginatedPullRequestDTO paginatedPullRequests = restTemplate.getForObject(
                    repositoryConfig.getEndpoint() + queryString,
                    PaginatedPullRequestDTO.class,
                    repositoryConfig.getUsername(),
                    repositoryConfig.getSlug());
            log.info("Open PR's retrieved for {}", reviewers);
            return nonNull(paginatedPullRequests) ? paginatedPullRequests : new PaginatedPullRequestDTO();
        } catch (RestClientException ex) {
            log.error("Exception on retrieving PR's for " + reviewers, ex);
            return new PaginatedPullRequestDTO();
        }
    }

    private Optional<PullRequestDTO> fetchMemberDetailedPullRequest(Long pullRequestId) {
        log.info("Getting PR with id {}", pullRequestId);
        try {
            PullRequestDTO pullRequest = restTemplate.getForObject(
                    repositoryConfig.getEndpoint() + "/{pullRequestId}",
                    PullRequestDTO.class,
                    repositoryConfig.getUsername(),
                    repositoryConfig.getSlug(),
                    pullRequestId);
            pullRequest.setLink(String.format(repositoryConfig.getPullRequestsUrl(), pullRequestId));
            return Optional.of(pullRequest);
        } catch (RestClientException ex) {
            log.error("Exception getting PR with id " + pullRequestId, ex);
            return Optional.empty();
        }
    }

    private List<ReviewerDTO> getUsersToApprove(List<String> reviewers, PullRequestDTO pullRequestDTO) {
        return pullRequestDTO.getParticipants().stream()
                .filter(participantDTO -> Role.REVIEWER.equals(participantDTO.getRole()))
                .filter(participantDTO -> reviewers.contains(participantDTO.getReviewerDTO().getUsername()))
                .filter(participantDTO -> !participantDTO.getApproved())
                .map(ParticipantDTO::getReviewerDTO)
                .collect(Collectors.toList());
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
}
