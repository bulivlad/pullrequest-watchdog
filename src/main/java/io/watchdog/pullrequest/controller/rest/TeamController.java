package io.watchdog.pullrequest.controller.rest;

import io.watchdog.pullrequest.model.RestResponse;
import io.watchdog.pullrequest.model.slack.SlackTeam;
import io.watchdog.pullrequest.service.TeamService;
import io.watchdog.pullrequest.service.slack.SlackTeamService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author vladclaudiubulimac on 2019-03-03.
 */

@RestController
@RequestMapping("/team")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TeamController {

    TeamService teamService;
    SlackTeamService slackTeamService;

    @Autowired
    public TeamController(TeamService teamService, SlackTeamService slackTeamService) {
        this.teamService = teamService;
        this.slackTeamService = slackTeamService;
    }

    @GetMapping(path = "/", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody List getTeams() {
        return teamService.getAllTeams();
    }

    @GetMapping(path = "/{channelName}/",
            produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody List<SlackTeam> getAllTeamsInChannel(@PathVariable String channelName) {
        return teamService.getAllTeamsInChannel(channelName);
    }

    @GetMapping(path = "/{teamName}/{channelName}/",
            produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody SlackTeam getSpecificTeam(@PathVariable String teamName, @PathVariable String channelName) {
        return teamService.getSpecificTeamOrNewTeam(channelName, teamName);
    }

    @PostMapping(path = "/", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity addTeam(@RequestBody SlackTeam slackTeam) {
        boolean saved = slackTeamService.saveTeam(slackTeam);
        RestResponse response = RestResponse.builder()
                .affectedEntity(slackTeam.getClass().getSimpleName())
                .entityName(slackTeam.getName())
                .scheduled(saved)
                .build();
        return ResponseEntity.ok(response);
    }

    @PutMapping(path = "/", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity updateTeam(@RequestBody SlackTeam slackTeam) {
        boolean updated = slackTeamService.updateTeam(slackTeam);
        RestResponse response = RestResponse.builder()
                .affectedEntity(slackTeam.getClass().getSimpleName())
                .entityName(slackTeam.getName())
                .scheduled(updated)
                .build();
        return ResponseEntity.ok(response);
    }

    @PutMapping(path = "/{teamName}/{channelName}/unschedule/", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity unscheduleTeam(@PathVariable String teamName, @PathVariable String channelName) {
        boolean unscheduled = slackTeamService.unscheduleTeam(channelName, teamName);
        RestResponse response = RestResponse.builder()
                .entityName(teamName)
                .scheduled(unscheduled)
                .build();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping(path = "/{teamName}/{channelName}/",
            produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity removeTeam(@PathVariable String teamName, @PathVariable String channelName) {
        slackTeamService.removeTeam(channelName, teamName);
        RestResponse response = RestResponse.builder().entityName(teamName).build();
        return ResponseEntity.ok(response);
    }

}
