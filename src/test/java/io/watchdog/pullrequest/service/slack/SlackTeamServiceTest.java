package io.watchdog.pullrequest.service.slack;

import com.mongodb.MongoWriteException;
import io.watchdog.pullrequest.config.RepositoryConfig;
import io.watchdog.pullrequest.dto.PullRequestDTO;
import io.watchdog.pullrequest.dto.ReviewerDTO;
import io.watchdog.pullrequest.dto.slack.SlackChannelDTO;
import io.watchdog.pullrequest.dto.slack.SlackUserDTO;
import io.watchdog.pullrequest.model.BitbucketUser;
import io.watchdog.pullrequest.model.CorrelatedUser;
import io.watchdog.pullrequest.model.slack.SlackChannel;
import io.watchdog.pullrequest.model.slack.SlackTeam;
import io.watchdog.pullrequest.model.slack.SlackUser;
import io.watchdog.pullrequest.service.PullRequestRetrieveService;
import io.watchdog.pullrequest.service.TeamService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import me.ramswaroop.jbot.core.slack.models.Event;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.quartz.SchedulerException;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author vladbulimac on 16/03/2019.
 */

@RunWith(MockitoJUnitRunner.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SlackTeamServiceTest {

    @Mock
    TeamService teamService;
    @Mock
    PullRequestRetrieveService pullRequestRetrieveService;
    @Mock
    SlackApiRestService slackApiRestService;
    @Mock
    RepositoryConfig repositoryConfig;
    @Mock
    SlackChannelService slackChannelService;

    @InjectMocks
    SlackTeamService slackTeamService;

    @Test
    public void saveTeam() throws SchedulerException {
        String channelId = "channel";
        SlackChannel slackChannel = new SlackChannel("channel", "dummy-channel-name", Collections.emptyList());
        SlackTeam slackTeam = SlackTeam.builder().name("teamName").channel(channelId).build();

        when(teamService.saveTeam(eq(slackTeam))).thenReturn(slackTeam);
        when(slackChannelService.findChannelByIdOrName(eq(channelId), eq(channelId))).thenReturn(Optional.of(slackChannel));

        boolean result = slackTeamService.saveTeam(slackTeam);

        assertThat(result, is(true));
    }

    @Test
    public void saveTeamNoChannelFound() throws SchedulerException {
        String channelId = "channel";
        SlackTeam slackTeam = SlackTeam.builder().name("teamName").channel(channelId).build();
        SlackChannelDTO slackChannelDTO = new SlackChannelDTO();
        SlackChannel slackChannel = new SlackChannel("channel", "dummy-channel-name", Collections.emptyList());

        when(teamService.saveTeam(eq(slackTeam))).thenReturn(slackTeam);
        when(slackChannelService.findChannelByIdOrName(eq(channelId), eq(channelId))).thenReturn(Optional.empty());
        when(slackApiRestService.retrieveSlackChannelDetails(eq(channelId))).thenReturn(slackChannelDTO);
        when(slackChannelService.convertSlackChannelDtoToSlackChannel(eq(slackChannelDTO))).thenReturn(slackChannel);

        boolean result = slackTeamService.saveTeam(slackTeam);

        assertThat(result, is(true));
        verify(slackApiRestService).retrieveSlackChannelDetails(channelId);
        verify(slackChannelService).convertSlackChannelDtoToSlackChannel(slackChannelDTO);
    }

    @Test
    public void saveTeamSchedulerException() throws SchedulerException {
        String channelId = "channel";
        SlackTeam slackTeam = SlackTeam.builder().name("teamName").channel(channelId).build();
        SlackChannel slackChannel = new SlackChannel(channelId, "dummy-channel-name", Collections.emptyList());

        when(teamService.saveTeam(eq(slackTeam))).thenThrow(SchedulerException.class);
        when(slackChannelService.findChannelByIdOrName(eq(channelId), eq(channelId))).thenReturn(Optional.of(slackChannel));

        boolean result = slackTeamService.saveTeam(slackTeam);

        assertThat(result, is(false));
    }

    @Test
    public void saveTeamMongoWriteException() throws SchedulerException {
        String channelId = "channel";
        SlackTeam slackTeam = SlackTeam.builder().name("teamName").channel(channelId).build();
        SlackChannel slackChannel = new SlackChannel(channelId, "dummy-channel-name", Collections.emptyList());

        when(teamService.saveTeam(eq(slackTeam))).thenThrow(MongoWriteException.class);
        when(slackChannelService.findChannelByIdOrName(eq(channelId), eq(channelId))).thenReturn(Optional.of(slackChannel));

        boolean result = slackTeamService.saveTeam(slackTeam);

        assertThat(result, is(false));
    }

    @Test
    public void updateTeam() throws SchedulerException {
        String channelId = "channel";
        SlackTeam slackTeam = SlackTeam.builder().name("teamName").channel(channelId).build();
        SlackChannel slackChannel = new SlackChannel(channelId, "dummy-channel-name", Collections.emptyList());

        when(teamService.saveTeam(eq(slackTeam))).thenReturn(slackTeam);
        when(slackChannelService.findChannelByIdOrName(eq(channelId), eq(channelId))).thenReturn(Optional.of(slackChannel));

        boolean result = slackTeamService.saveTeam(slackTeam);

        assertThat(result, is(true));
    }

    @Test
    public void updateTeamNoChannelFound() throws SchedulerException {
        String channelId = "channel";
        SlackTeam slackTeam = SlackTeam.builder().name("teamName").channel(channelId).build();
        SlackChannelDTO slackChannelDTO = new SlackChannelDTO();
        SlackChannel slackChannel = new SlackChannel("channel", "dummy-channel-name", Collections.emptyList());

        when(teamService.saveTeam(eq(slackTeam))).thenReturn(slackTeam);
        when(slackChannelService.findChannelByIdOrName(eq(channelId), eq(channelId))).thenReturn(Optional.empty());
        when(slackApiRestService.retrieveSlackChannelDetails(eq(channelId))).thenReturn(slackChannelDTO);
        when(slackChannelService.convertSlackChannelDtoToSlackChannel(eq(slackChannelDTO))).thenReturn(slackChannel);

        boolean result = slackTeamService.saveTeam(slackTeam);

        assertThat(result, is(true));
        verify(slackApiRestService).retrieveSlackChannelDetails(channelId);
        verify(slackChannelService).convertSlackChannelDtoToSlackChannel(slackChannelDTO);
    }

    @Test
    public void updateTeamSchedulerException() throws SchedulerException {
        String channelId = "channel";
        SlackTeam slackTeam = SlackTeam.builder().name("teamName").channel(channelId).build();
        SlackChannel slackChannel = new SlackChannel(channelId, "dummy-channel-name", Collections.emptyList());

        when(teamService.saveTeam(eq(slackTeam))).thenThrow(SchedulerException.class);
        when(slackChannelService.findChannelByIdOrName(eq(channelId), eq(channelId))).thenReturn(Optional.of(slackChannel));

        boolean result = slackTeamService.saveTeam(slackTeam);

        assertThat(result, is(false));
    }

    @Test
    public void getSlackMessages() {
        BitbucketUser bitbucketUser = new BitbucketUser();
        bitbucketUser.setUsername("bbusername");
        SlackUser slackUser = new SlackUser();
        slackUser.setMention("bbusernamemention");
        slackUser.setUsername("bbusername");
        CorrelatedUser correlatedUser = new CorrelatedUser();
        correlatedUser.setBitbucketUser(bitbucketUser);
        correlatedUser.setSlackUser(slackUser);

        BitbucketUser bitbucketUser1 = new BitbucketUser();
        bitbucketUser1.setUsername("bbusername1");
        SlackUser slackUser1 = new SlackUser();
        slackUser1.setMention("bbusernamemention1");
        slackUser1.setUsername("bbusername1");
        CorrelatedUser correlatedUser1 = new CorrelatedUser();
        correlatedUser1.setBitbucketUser(bitbucketUser1);
        correlatedUser1.setSlackUser(slackUser1);

        List<CorrelatedUser> correlatedUsers = Arrays.asList(correlatedUser, correlatedUser1);

        ReviewerDTO reviewerDTO = new ReviewerDTO();
        reviewerDTO.setUsername("bbusername");
        ReviewerDTO reviewerDTO1 = new ReviewerDTO();
        reviewerDTO1.setUsername("bbusername1");

        PullRequestDTO pullRequestDTO = new PullRequestDTO();
        pullRequestDTO.setReviewers(Arrays.asList(reviewerDTO, reviewerDTO1));
        pullRequestDTO.setSourceBranch("sourceBranch1");
        pullRequestDTO.setLink("http://pr1");
        PullRequestDTO pullRequestDTO1 = new PullRequestDTO();
        pullRequestDTO1.setReviewers(Collections.singletonList(reviewerDTO1));
        pullRequestDTO1.setSourceBranch("sourceBranch2");
        pullRequestDTO1.setLink("http://pr2");

        List<PullRequestDTO> pullRequestDTOS = Arrays.asList(pullRequestDTO, pullRequestDTO1);

        List<String> expected = new ArrayList<>();
        expected.add("PRs waiting for reviewers today for repo *dummy-slug*:\n");
        expected.add("sourceBranch1 - http://pr1 - <@bbusernamemention> <@bbusernamemention1> ");
        expected.add("sourceBranch2 - http://pr2 - <@bbusernamemention1> ");

        when(pullRequestRetrieveService.getUnapprovedPRs(anyList(), anyString())).thenReturn(pullRequestDTOS);

        List<String> result = slackTeamService.getSlackMessages(correlatedUsers, "dummy-slug");

        assertThat(result, equalTo(expected));
    }

    @Test
    public void getSlackMessagesNoUnnaprovedPRs() {
        BitbucketUser bitbucketUser = new BitbucketUser();
        bitbucketUser.setUsername("bbusername");
        SlackUser slackUser = new SlackUser();
        slackUser.setMention("bbusernamemention");
        slackUser.setUsername("bbusername");
        CorrelatedUser correlatedUser = new CorrelatedUser();
        correlatedUser.setBitbucketUser(bitbucketUser);
        correlatedUser.setSlackUser(slackUser);

        BitbucketUser bitbucketUser1 = new BitbucketUser();
        bitbucketUser1.setUsername("bbusername1");
        SlackUser slackUser1 = new SlackUser();
        slackUser1.setMention("bbusernamemention1");
        slackUser1.setUsername("bbusername1");
        CorrelatedUser correlatedUser1 = new CorrelatedUser();
        correlatedUser1.setBitbucketUser(bitbucketUser1);
        correlatedUser1.setSlackUser(slackUser1);

        List<CorrelatedUser> correlatedUsers = Arrays.asList(correlatedUser, correlatedUser1);

        List<String> expected = new ArrayList<>();
        expected.add(":woohoo: No PRs to be reviewed today for repo *dummy-slug* !\n");

        when(pullRequestRetrieveService.getUnapprovedPRs(anyList(), anyString())).thenReturn(Collections.emptyList());

        List<String> result = slackTeamService.getSlackMessages(correlatedUsers, "dummy-slug");

        assertThat(result, equalTo(expected));
    }

    @Test
    public void buildSlackTeam() {
        Event event = new Event();
        event.setText("add team rdc2-team for repository dummy-slug with members [<@user1>, <@user2>] and scheduler 0 20 11 1/1 * ? *");
        event.setChannelId("channelId");

        SlackUserDTO slackUserDTO = new SlackUserDTO();
        SlackUserDTO.User user = slackUserDTO.new User();
        user.setRealName("name1");
        user.setDisplayName("name1");
        user.setEmail("name1@example.com");
        slackUserDTO.setUser(user);

        SlackUserDTO slackUserDTO1 = new SlackUserDTO();
        SlackUserDTO.User user1 = slackUserDTO1.new User();
        user1.setRealName("name2");
        user1.setDisplayName("name2");
        user1.setEmail("name2@example.com");
        slackUserDTO1.setUser(user1);

        SlackUser slackUser = new SlackUser();
        slackUser.setEmail("name1@example.com");
        slackUser.setMention("user1");
        slackUser.setUsername("name1");
        slackUser.setName("name1");

        SlackUser slackUser1 = new SlackUser();
        slackUser1.setEmail("name2@example.com");
        slackUser1.setMention("user2");
        slackUser1.setUsername("name2");
        slackUser1.setName("name2");

        SlackTeam expected = SlackTeam.builder()
                .channel("channelId")
                .name("rdc2-team")
                .member(new CorrelatedUser(slackUser))
                .member(new CorrelatedUser(slackUser1))
                .checkingSchedule("0 20 11 1/1 * ? *")
                .slug("dummy-slug")
                .build();

        when(slackApiRestService.retrieveSlackUserDetails("user1")).thenReturn(slackUserDTO);
        when(slackApiRestService.retrieveSlackUserDetails("user2")).thenReturn(slackUserDTO1);

        SlackTeam result = slackTeamService.buildSlackTeam(event);

        assertNotNull(result);
        assertEquals(expected, result);
    }

    @Test
    public void buildSlackTeamNoSlug() {
        Event event = new Event();
        event.setText("add team rdc2-team with members [<@user1>, <@user2>] and scheduler 0 20 11 1/1 * ? *");
        event.setChannelId("channelId");

        SlackUserDTO slackUserDTO = new SlackUserDTO();
        SlackUserDTO.User user = slackUserDTO.new User();
        user.setRealName("name1");
        user.setDisplayName("name1");
        user.setEmail("name1@example.com");
        slackUserDTO.setUser(user);

        SlackUserDTO slackUserDTO1 = new SlackUserDTO();
        SlackUserDTO.User user1 = slackUserDTO1.new User();
        user1.setRealName("name2");
        user1.setDisplayName("name2");
        user1.setEmail("name2@example.com");
        slackUserDTO1.setUser(user1);

        SlackUser slackUser = new SlackUser();
        slackUser.setEmail("name1@example.com");
        slackUser.setMention("user1");
        slackUser.setUsername("name1");
        slackUser.setName("name1");

        SlackUser slackUser1 = new SlackUser();
        slackUser1.setEmail("name2@example.com");
        slackUser1.setMention("user2");
        slackUser1.setUsername("name2");
        slackUser1.setName("name2");

        SlackTeam expected = SlackTeam.builder()
                .channel("channelId")
                .name("rdc2-team")
                .member(new CorrelatedUser(slackUser))
                .member(new CorrelatedUser(slackUser1))
                .checkingSchedule("0 20 11 1/1 * ? *")
                .slug("")
                .build();

        when(slackApiRestService.retrieveSlackUserDetails("user1")).thenReturn(slackUserDTO);
        when(slackApiRestService.retrieveSlackUserDetails("user2")).thenReturn(slackUserDTO1);

        SlackTeam result = slackTeamService.buildSlackTeam(event);

        assertNotNull(result);
        assertEquals(expected, result);
    }

    @Test
    public void buildSlackTeamNoScheduler() {
        Event event = new Event();
        event.setText("add team rdc2-team with members [<@user1>, <@user2>] and scheduler ");
        event.setChannelId("channelId");

        SlackUserDTO slackUserDTO = new SlackUserDTO();
        SlackUserDTO.User user = slackUserDTO.new User();
        user.setRealName("name1");
        user.setDisplayName("name1");
        user.setEmail("name1@example.com");
        slackUserDTO.setUser(user);

        SlackUserDTO slackUserDTO1 = new SlackUserDTO();
        SlackUserDTO.User user1 = slackUserDTO1.new User();
        user1.setRealName("name2");
        user1.setDisplayName("name2");
        user1.setEmail("name2@example.com");
        slackUserDTO1.setUser(user1);

        SlackUser slackUser = new SlackUser();
        slackUser.setEmail("name1@example.com");
        slackUser.setMention("user1");
        slackUser.setUsername("name1");
        slackUser.setName("name1");

        SlackUser slackUser1 = new SlackUser();
        slackUser1.setEmail("name2@example.com");
        slackUser1.setMention("user2");
        slackUser1.setUsername("name2");
        slackUser1.setName("name2");

        SlackTeam expected = SlackTeam.builder()
                .channel("channelId")
                .name("rdc2-team")
                .member(new CorrelatedUser(slackUser))
                .member(new CorrelatedUser(slackUser1))
                .checkingSchedule("")
                .slug("")
                .build();

        when(slackApiRestService.retrieveSlackUserDetails("user1")).thenReturn(slackUserDTO);
        when(slackApiRestService.retrieveSlackUserDetails("user2")).thenReturn(slackUserDTO1);

        SlackTeam result = slackTeamService.buildSlackTeam(event);

        assertNotNull(result);
        assertEquals(expected, result);
    }

    @Test
    public void removeTeam() throws SchedulerException {
        String channel = "channel";
        String teamName = "teamName";
        String slug = "dummy-slug";

        when(teamService.deleteTeam(eq(channel), eq(teamName), eq(slug))).thenReturn(true);

        boolean result = slackTeamService.removeTeam(channel, teamName, slug);

        assertTrue(result);
    }

    @Test
    public void removeTeamIllegalArgumentException() throws SchedulerException {
        String channel = "channel";
        String teamName = "teamName";
        String slug = "dummy-slug";

        when(teamService.deleteTeam(eq(channel), eq(teamName), eq(slug))).thenThrow(IllegalArgumentException.class);

        boolean result = slackTeamService.removeTeam(channel, teamName, slug);

        assertFalse(result);
    }

    @Test
    public void unscheduleTeam() throws SchedulerException {
        String channel = "channelId";
        String teamName = "teamName";
        String slug = "dummy-slug";
        SlackTeam slackTeam = SlackTeam.builder()
                .channel(channel)
                .name(teamName)
                .checkingSchedule("0 20 11 1/1 * ? *")
                .build();

        when(teamService.getSpecificTeamInRepo(eq(channel), eq(teamName), eq(slug))).thenReturn(Optional.of(slackTeam));
        when(teamService.updateTeam(eq(slackTeam))).thenReturn(slackTeam);

        boolean result = slackTeamService.unscheduleTeam(channel, teamName, slug);

        assertTrue(result);
    }

    @Test
    public void unscheduleTeamMongoWriteException() throws SchedulerException {
        String channel = "channelId";
        String teamName = "teamName";
        String slug = "dummy-slug";
        SlackTeam slackTeam = SlackTeam.builder()
                .channel(channel)
                .name(teamName)
                .checkingSchedule("0 20 11 1/1 * ? *")
                .build();

        when(teamService.getSpecificTeamInRepo(eq(channel), eq(teamName), eq(slug))).thenReturn(Optional.of(slackTeam));
        when(teamService.updateTeam(eq(slackTeam))).thenThrow(MongoWriteException.class);

        boolean result = slackTeamService.unscheduleTeam(channel, teamName, slug);

        assertFalse(result);
    }

    @Test
    public void unscheduleTeamSchedulerException() throws SchedulerException {
        String channel = "channelId";
        String teamName = "teamName";
        String slug = "dummy-slug";
        SlackTeam slackTeam = SlackTeam.builder()
                .channel(channel)
                .name(teamName)
                .checkingSchedule("0 20 11 1/1 * ? *")
                .build();

        when(teamService.getSpecificTeamInRepo(eq(channel), eq(teamName), eq(slug))).thenReturn(Optional.of(slackTeam));
        when(teamService.updateTeam(eq(slackTeam))).thenThrow(SchedulerException.class);

        boolean result = slackTeamService.unscheduleTeam(channel, teamName, slug);

        assertFalse(result);
    }
}