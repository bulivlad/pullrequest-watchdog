package io.watchdog.pullrequest.controller.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.watchdog.pullrequest.model.RestResponse;
import io.watchdog.pullrequest.model.slack.SlackTeam;
import io.watchdog.pullrequest.service.TeamService;
import io.watchdog.pullrequest.service.slack.SlackTeamService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/**
 * @author vladbulimac on 15/03/2019.
 */

@SpringBootTest
@AutoConfigureMockMvc
@RunWith(SpringRunner.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class TeamControllerTest {

    @MockBean
    TeamService teamService;
    @MockBean
    SlackTeamService slackTeamService;

    @Autowired
    MockMvc mockMvc;

    ObjectMapper objectMapper;

    @Before
    public void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    @WithMockUser(username = "admin", password = "Passw0rd")
    public void getTeams() throws Exception {
        SlackTeam team = SlackTeam.builder().name("teamName").channel("channel").build();
        List<SlackTeam> mockTeam = Collections.singletonList(team);

        when(teamService.getAllTeams()).thenReturn(mockTeam);

        mockMvc.perform(get("/team/")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(mockTeam)));
    }

    @Test
    public void getTeamsNotAuthorized() throws Exception {
        mockMvc.perform(get("/team/").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin", password = "Passw0rd")
    public void getAllTeamsInChannel() throws Exception {
        String channel = "channel";
        SlackTeam team = SlackTeam.builder().name("teamName").channel(channel).build();
        SlackTeam team1 = SlackTeam.builder().name("teamName1").channel(channel).build();
        List<SlackTeam> mockTeam = Arrays.asList(team, team1);

        when(teamService.getAllTeamsInChannel(eq(channel))).thenReturn(mockTeam);

        mockMvc.perform(
                    get("/team/" + channel + "/")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(mockTeam)));
    }

    @Test
    public void getAllTeamsInChannelNotAuthorized() throws Exception {
        mockMvc.perform(
                get("/team/channel/")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin", password = "Passw0rd")
    public void getSpecificTeam() throws Exception {
        String channel = "channel";
        String teamName = "teamName";
        SlackTeam team = SlackTeam.builder().name(teamName).channel(channel).build();

        when(teamService.getSpecificTeamOrNewTeam(channel, teamName)).thenReturn(team);

        mockMvc.perform(
                get("/team/" + teamName + "/" + channel + "/")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(team)));
    }

    @Test
    public void getSpecificTeamNotAuthorized() throws Exception {
        mockMvc.perform(
                get("/team/teamName/channel/")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin", password = "Passw0rd")
    public void addTeam() throws Exception {
        SlackTeam team = SlackTeam.builder().name("teamName").channel("channel").build();
        RestResponse response = RestResponse.builder()
                .affectedEntity(team.getClass().getSimpleName())
                .entityName(team.getName())
                .scheduled(true)
                .build();

        when(slackTeamService.saveTeam(eq(team))).thenReturn(true);

        mockMvc.perform(
                post("/team/")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(team)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    @WithMockUser(username = "admin", password = "Passw0rd")
    public void addTeamFailure() throws Exception {
        SlackTeam team = SlackTeam.builder().name("teamName").channel("channel").build();
        RestResponse response = RestResponse.builder()
                .affectedEntity(team.getClass().getSimpleName())
                .entityName(team.getName())
                .scheduled(false)
                .build();

        when(slackTeamService.saveTeam(eq(team))).thenReturn(false);


        mockMvc.perform(
                post("/team/")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(team)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    public void addTeamNotAuthorized() throws Exception {
        mockMvc.perform(
                post("/team/")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin", password = "Passw0rd")
    public void updateTeam() throws Exception {
        SlackTeam team = SlackTeam.builder().name("teamName").channel("channel").build();
        RestResponse response = RestResponse.builder()
                .affectedEntity(team.getClass().getSimpleName())
                .entityName(team.getName())
                .scheduled(true)
                .build();

        when(slackTeamService.updateTeam(eq(team))).thenReturn(true);

        mockMvc.perform(
                put("/team/")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(team)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    @WithMockUser(username = "admin", password = "Passw0rd")
    public void updateTeamFailure() throws Exception {
        SlackTeam team = SlackTeam.builder().name("teamName").channel("channel").build();
        RestResponse response = RestResponse.builder()
                .affectedEntity(team.getClass().getSimpleName())
                .entityName(team.getName())
                .scheduled(false)
                .build();

        when(slackTeamService.updateTeam(eq(team))).thenReturn(false);

        mockMvc.perform(
                put("/team/")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(team)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    public void updateTeamNotAuthorized() throws Exception {
        mockMvc.perform(
                put("/team/")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin", password = "Passw0rd")
    public void unscheduleTeam() throws Exception {
        String channel = "channel";
        String teamName = "teamName";
        String slug = "dummy-slug";
        SlackTeam team = SlackTeam.builder().name(teamName).channel(channel).build();
        RestResponse response = RestResponse.builder()
                .entityName(team.getName())
                .scheduled(true)
                .build();

        when(slackTeamService.unscheduleTeam(eq(channel), eq(teamName), eq(slug))).thenReturn(true);

        mockMvc.perform(
                put("/team/" + teamName + "/" + channel + "/" + slug + "/unschedule/")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    @WithMockUser(username = "admin", password = "Passw0rd")
    public void unscheduleTeamFailure() throws Exception {
        String channel = "channel";
        String teamName = "teamName";
        String slug = "dummy-slug";
        SlackTeam team = SlackTeam.builder().name(teamName).channel(channel).build();
        RestResponse response = RestResponse.builder()
                .entityName(team.getName())
                .scheduled(false)
                .build();

        when(slackTeamService.unscheduleTeam(eq(channel), eq(teamName), eq(slug))).thenReturn(false);

        mockMvc.perform(
                put("/team/" + teamName + "/" +  channel + "/" + slug + "/unschedule/")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    public void unscheduleTeamNotAuthorized() throws Exception {
        mockMvc.perform(
                put("/team/teamName/channel/unschedule/")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin", password = "Passw0rd")
    public void removeTeam() throws Exception {
        String channel = "channel";
        String teamName = "teamName";
        String slug = "dummy-slug";
        SlackTeam team = SlackTeam.builder().name(teamName).channel(channel).build();
        RestResponse response = RestResponse.builder().entityName(team.getName()).build();

        when(teamService.deleteTeam(eq(channel), eq(teamName), eq(slug))).thenReturn(true);

        mockMvc.perform(
                delete("/team/" + teamName + "/" + channel + "/" + slug + "/")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    public void removeTeamNotAuthorized() throws Exception {
        mockMvc.perform(
                delete("/team/teamName/channel/")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}