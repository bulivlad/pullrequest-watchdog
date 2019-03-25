package io.watchdog.pullrequest.controller.frontend;

import io.watchdog.pullrequest.model.slack.SlackTeam;
import io.watchdog.pullrequest.repository.ChannelRepository;
import io.watchdog.pullrequest.repository.TeamRepository;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * @author vladclaudiubulimac on 2019-03-25.
 */

@Controller
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FrontendController {

    TeamRepository teamRepository;
    ChannelRepository channelRepository;

    @Autowired
    public FrontendController(TeamRepository teamRepository, ChannelRepository channelRepository) {
        this.teamRepository = teamRepository;
        this.channelRepository = channelRepository;
    }

    @GetMapping("/home")
    public String home(Model model) {
        List<SlackTeam> all = teamRepository.findAll();
        all.forEach(e -> channelRepository.findById(e.getChannel()).ifPresent(c -> e.setChannel(c.getName())));
        model.addAttribute("teams", all);
        return "index.html";
    }

}
