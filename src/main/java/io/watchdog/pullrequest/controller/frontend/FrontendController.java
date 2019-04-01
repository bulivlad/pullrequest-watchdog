package io.watchdog.pullrequest.controller.frontend;

import io.watchdog.pullrequest.dto.slack.SlackTeamDTO;
import io.watchdog.pullrequest.model.slack.SlackTeam;
import io.watchdog.pullrequest.repository.ChannelRepository;
import io.watchdog.pullrequest.repository.TeamRepository;
import io.watchdog.pullrequest.service.slack.SlackTeamService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.util.List;

/**
 * @author vladclaudiubulimac on 2019-03-25.
 */

@Slf4j
@Controller
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FrontendController {

    TeamRepository teamRepository;
    ChannelRepository channelRepository;
    SlackTeamService slackTeamService;

    @Autowired
    public FrontendController(TeamRepository teamRepository, ChannelRepository channelRepository, SlackTeamService slackTeamService) {
        this.teamRepository = teamRepository;
        this.channelRepository = channelRepository;
        this.slackTeamService = slackTeamService;
    }

    @GetMapping("/home")
    public String home(Model model) {
        List<SlackTeam> all = teamRepository.findAll();
        all.forEach(e -> channelRepository.findById(e.getChannel()).ifPresent(c -> e.setChannel(c.getName())));
        model.addAttribute("teams", all);
        return "index.html";
    }

    @PostMapping("/add")
    public String add(@Valid SlackTeamDTO slackTeam) {
        slackTeamService.saveTeam(slackTeamService.convertSlackTeamDtoToSlackTeam(slackTeam));
        return "redirect:home";
    }
}
