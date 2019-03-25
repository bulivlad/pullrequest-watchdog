package io.watchdog.pullrequest.service.slack;

import io.watchdog.pullrequest.dto.slack.SlackChannelDTO;
import io.watchdog.pullrequest.model.slack.SlackChannel;
import io.watchdog.pullrequest.repository.ChannelRepository;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import me.ramswaroop.jbot.core.slack.models.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * @author vladclaudiubulimac on 2019-03-25.
 */

@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SlackChannelService {

    ChannelRepository channelRepository;
    SlackApiRestService slackApiRestService;

    @Autowired
    public SlackChannelService(ChannelRepository channelRepository, SlackApiRestService SlackApiRestService) {
        this.channelRepository = channelRepository;
        this.slackApiRestService = SlackApiRestService;
    }

    public Optional<SlackChannel> findChannelByIdOrName(String channelId, String channelName) {
        return channelRepository.findByIdOrName(channelId, channelName);
    }

    public SlackChannel saveChannel(SlackChannel channel) {
        return channelRepository.save(channel);
    }

    public SlackChannel saveJoinedChannel(Event event) {
        String channelId = event.getChannel().getId();
        SlackChannel entityToSave = channelRepository.findById(channelId)
                .orElse(convertSlackChannelDtoToSlackChannel(slackApiRestService.retrieveSlackChannelDetails(channelId)));

        if(channelRepository.existsById(channelId) && event.getChannel().getName() != null){
            if(!entityToSave.getName().equals(event.getChannel().getName())) {
                entityToSave.getPreviousNames().add(entityToSave.getName());
                entityToSave.setName(event.getChannel().getName());
            }
        }
        return channelRepository.save(entityToSave);
    }

    public SlackChannel convertSlackChannelDtoToSlackChannel(SlackChannelDTO slackChannelDTO) {
        SlackChannel slackChannel = new SlackChannel();
        slackChannel.setId(slackChannelDTO.getChannelId());
        slackChannel.setName(slackChannelDTO.getChannelName());
        slackChannel.setPreviousNames(slackChannelDTO.getChannelPreviousNames());
        return slackChannel;
    }
}
