package io.watchdog.pullrequest.bot;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.watchdog.pullrequest.model.slack.MethodWrapper;
import io.watchdog.pullrequest.util.BotWebSocketHandler;
import lombok.extern.slf4j.Slf4j;
import me.ramswaroop.jbot.core.slack.Controller;
import me.ramswaroop.jbot.core.slack.EventType;
import me.ramswaroop.jbot.core.slack.SlackService;
import me.ramswaroop.jbot.core.slack.models.Event;
import me.ramswaroop.jbot.core.slack.models.Message;
import org.apache.commons.lang.StringUtils;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.util.CollectionUtils;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketConnectionManager;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * @author vladclaudiubulimac on 05/03/2018.
 */

@Slf4j
public abstract class Bot {

    private final Map<String, MethodWrapper> methodNameMap = new HashMap<>();
    private final List<String> conversationMethodNames = new ArrayList<>();
    private final Map<String, List<MethodWrapper>> eventToMethodsMap = new HashMap<>();
    private final Map<String, Queue<MethodWrapper>> conversationQueueMap = new HashMap<>();

    @Autowired
    private ApplicationContext сontext;

    /**
     * Service to access Slack APIs.
     */
    @Autowired
    protected SlackService slackService;

    /**
     * Entry point where the web socket connection starts
     * and after which your bot becomes live.
     */
    @PostConstruct
    private void startWebSocketConnection() {
        slackService.startRTM(getSlackToken());
        if (slackService.getWebSocketUrl() != null) {
            WebSocketConnectionManager manager = new WebSocketConnectionManager(client(), handler(), slackService.getWebSocketUrl());
            manager.start();
        } else {
            log.error("No websocket url returned by Slack.");
        }
    }

    @PostConstruct
    public void setup(){
        getControllerMethodsStream(getScanPackage())
                .forEach(method -> {
                    Controller controller = method.getAnnotation(Controller.class);
                    String next = controller.next();

                    if (!StringUtils.isEmpty(next)) {
                        conversationMethodNames.add(next);
                    }

                    String pattern = controller.pattern();
                    MethodWrapper methodWrapper = MethodWrapper.builder().method(method).pattern(pattern).next(next).build();

                    if (!conversationMethodNames.contains(method.getName())) {
                        EventType[] eventTypes = controller.events();
                        Arrays.stream(eventTypes).forEach(eventType -> buildEventToMethodsMap(methodWrapper, eventType));
                    }
                    methodNameMap.put(method.getName(), methodWrapper);
                });
    }

    /**
     * Method used to get a stream containing all methods annotated with {@link Controller}
     * in package provided in <code>scanPackage</code> parameter
     *
     * @param scanPackage the package to scan
     * @return {@link Stream} containing methods annotated with {@link Controller}
     */
    private Stream<Method> getControllerMethodsStream(String scanPackage) {
        Reflections reflections = new Reflections(scanPackage, new MethodAnnotationsScanner());
        return reflections.getMethodsAnnotatedWith(Controller.class).stream();
    }

    /**
     * Build the mapping between slack {@link EventType} and the {@link MethodWrapper} that will handle the slack event
     *
     * @param methodWrapper the method wrapper to handle the event
     * @param eventType the event to be handled
     */
    private void buildEventToMethodsMap(MethodWrapper methodWrapper, EventType eventType) {
        List<MethodWrapper> methodWrappers = eventToMethodsMap.getOrDefault(eventType.name(), new ArrayList<>());

        methodWrappers.add(methodWrapper);
        eventToMethodsMap.put(eventType.name(), methodWrappers);
    }

    /**
     * Get the package where the Slack controllers resides
     *
     * @return java package where slack controllers resides
     */
    protected abstract String getScanPackage();

    /**
     * Class extending this must implement this as it's
     * required to make the initial RTM.start() call.
     *
     * @return
     */
    public abstract String getSlackToken();

    /**
     * An instance of the Bot is required by
     * the {@link BotWebSocketHandler} class.
     *
     * @return
     */
    public abstract Bot getSlackBot();

    /**
     * Invoked after a successful web socket connection is
     * established. You can override this method in the child classes.
     *
     * @param session
     */
    public void afterConnectionEstablished(WebSocketSession session) {
        log.debug("WebSocket connected: {}", session);
    }

    /**
     * Invoked after the web socket connection is closed.
     * You can override this method in the child classes.
     *
     * @param session
     * @param status
     */
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.debug("WebSocket closed: {}, Close Status: {}", session, status.toString());
    }

    /**
     * Handle an error from the underlying WebSocket message transport.
     *
     * @param session
     * @param exception
     */
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("Transport Error: {}", exception);
    }

    /**
     * Invoked when a new Slack event(WebSocket text message) arrives.
     *
     * @param session
     * @param textMessage
     */
    public final void handleTextMessage(WebSocketSession session, TextMessage textMessage) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            Event event = mapper.readValue(textMessage.getPayload(), Event.class);
            event.setType(getEventType(event));

            if (isConversationOn(event)) {
                invokeChainedMethod(session, event);
            } else {
                invokeMethods(session, event);
            }
        } catch (Exception e) {
            log.error("Error handling response from Slack: {}. \nException: ", textMessage.getPayload(), e);
        }
    }

    /**
     * Get the type of the event
     *
     * @param event Slack event
     * @return the {@link Event#getType()} as {@link EventType#toString()}
     */
    private String getEventType(Event event) {
        if (event.getType() == null) {
            return EventType.ACK.name();
        }
        if (EventType.IM_OPEN.name().equalsIgnoreCase(event.getType())) {
            slackService.addDmChannel(event.getChannelId());
        }
        if(EventType.MESSAGE.name().equalsIgnoreCase(event.getType()) && StringUtils.contains(event.getText(), slackService.getCurrentUser().getId())) {
            return EventType.DIRECT_MENTION.name();
        }
        if(EventType.MESSAGE.name().equalsIgnoreCase(event.getType()) && slackService.getDmChannels().contains(event.getChannelId())) {
            return EventType.DIRECT_MESSAGE.name();
        }

        return event.getType();
    }

    /**
     * Call this method to start a conversation.
     *
     * @param event
     */
    public void startConversation(Event event, String methodName) {
        String channelId = event.getChannelId();

        if (!StringUtils.isEmpty(channelId)) {
            Queue<MethodWrapper> queue = formConversationQueue(new LinkedList<>(), methodName);
            conversationQueueMap.put(channelId, queue);
        }
    }

    /**
     * Call this method to jump to the next method in a conversation.
     *
     * @param event
     */
    public void nextConversation(Event event) {
        Queue<MethodWrapper> queue = conversationQueueMap.get(event.getChannelId());
        if (queue != null) queue.poll();
    }

    /**
     * Call this method to stop the end the conversation.
     *
     * @param event
     */
    public void stopConversation(Event event) {
        conversationQueueMap.remove(event.getChannelId());
    }

    /**
     * Check whether a conversation is up in a particular slack channel.
     *
     * @param event
     * @return true if a conversation is on, false otherwise.
     */
    public boolean isConversationOn(Event event) {
        return conversationQueueMap.get(event.getChannelId()) != null;
    }

    /**
     * Method to send a reply back to Slack after receiving an {@link Event}.
     * Learn <a href="https://api.slack.com/rtm">more on sending responses to Slack.</a>
     *
     * @param session
     * @param event
     * @param reply
     */
    public final void reply(WebSocketSession session, Event event, Message reply) {
        try {
            reply.setType(EventType.MESSAGE.name().toLowerCase());
            if (reply.getChannel() == null && event.getChannelId() != null) {
                reply.setChannel(event.getChannelId());
            }
            session.sendMessage(new TextMessage(reply.toJSONString()));
            if (log.isDebugEnabled()) {
                log.debug("Reply (Message): {}", reply.toJSONString());
            }
        } catch (IOException e) {
            log.error("Error sending event: {}. Exception: {}", event.getText(), e.getMessage());
        }
    }

    /**
     * Form a Queue with all the methods responsible for a particular conversation.
     *
     * @param queue
     * @param methodName
     * @return
     */
    private Queue<MethodWrapper> formConversationQueue(Queue<MethodWrapper> queue, String methodName) {
        MethodWrapper methodWrapper = methodNameMap.get(methodName);
        queue.add(methodWrapper);
        if (StringUtils.isEmpty(methodName)) {
            return queue;
        }

        return formConversationQueue(queue, methodWrapper.getNext());
    }

    /**
     * Invoke the methods with matching {@link Controller#events()}
     * and {@link Controller#pattern()} in events received from Slack.
     *
     * @param session
     * @param event
     */
    private void invokeMethods(WebSocketSession session, Event event) {
        try {
            List<MethodWrapper> methodWrappers = new ArrayList<>(eventToMethodsMap.getOrDefault(event.getType().toUpperCase(), Collections.emptyList()));

            getMethodWithMatchingPatternAndFilterUnmatchedMethods(event, methodWrappers).ifPresent(methodWrappers::add);

            for (MethodWrapper methodWrapper : methodWrappers) {
                Method method = methodWrapper.getMethod();
                if (method.getParameterCount() == 3) {
                    method.invoke(this, session, event, methodWrapper.getMatcher());
                } else {
                    method.invoke(сontext.getBean(method.getDeclaringClass()), session, event);
                }
            }
        } catch (Exception e) {
            log.error("Error invoking controller: ", e);
        }
    }

    /**
     * Invoke the appropriate method in a conversation.
     *
     * @param session
     * @param event
     */
    private void invokeChainedMethod(WebSocketSession session, Event event) {
        Queue<MethodWrapper> queue = conversationQueueMap.get(event.getChannelId());

        if (!CollectionUtils.isEmpty(queue)) {
            MethodWrapper methodWrapper = queue.peek();

            try {
                EventType[] eventTypes = methodWrapper.getMethod().getAnnotation(Controller.class).events();
                for (EventType eventType : eventTypes) {
                    if (eventType.name().equals(event.getType().toUpperCase())) {
                        methodWrapper.getMethod().invoke(this, session, event);
                        return;
                    }
                }
            } catch (Exception e) {
                log.error("Error invoking chained method: ", e);
            }
        }
    }

    /**
     * Search for a method whose {@link Controller#pattern()} match with the {@link Event#text}
     * in events received from Slack and also filter out the methods whose {@link Controller#pattern()} do not
     * match with slack message received ({@link Event#text}) for cases where there are no matched methods.
     *
     * @param event
     * @param methodWrappers
     * @return the MethodWrapper whose method pattern match with that of the slack message received, {@code null} if no
     * such method is found.
     */
    private Optional<MethodWrapper> getMethodWithMatchingPatternAndFilterUnmatchedMethods(Event event, List<MethodWrapper> methodWrappers) {
        Iterator<MethodWrapper> methodWrapperIterator = methodWrappers.listIterator();

        while (methodWrapperIterator.hasNext()) {
            MethodWrapper methodWrapper = methodWrapperIterator.next();
            String methodPattern = methodWrapper.getPattern();
            String text = event.getText();

            if (!StringUtils.isEmpty(methodPattern) && !StringUtils.isEmpty(text)) {
                Pattern patter = Pattern.compile(methodPattern);
                Matcher matcher = patter.matcher(text);
                if (matcher.find()) {
                    methodWrapper.setMatcher(matcher);
                    return Optional.of(methodWrapper);
                } else {
                    methodWrapperIterator.remove();
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Return new WebSocketClient for slack communication initialization
     *
     * @return StandardWebSocketClient new socket client
     */
    private StandardWebSocketClient client() {
        return new StandardWebSocketClient();
    }

    /**
     * Return new Bot WebSocket Handler to handle websocket messages received from slack
     *
     * @return new bot websocket handler
     */
    private BotWebSocketHandler handler() {
        return new BotWebSocketHandler(getSlackBot());
    }

}
