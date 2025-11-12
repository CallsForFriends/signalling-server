package ru.itmo.calls.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.itmo.calls.exception.UserOfflineException;
import ru.itmo.calls.model.SignalMessage;

@Service
public class WebRTCSignallingService {
    private static final Logger log = LoggerFactory.getLogger(WebRTCSignallingService.class);

    private final OnlineUsersService onlineUsersService;
    private final MessageSenderService messageSenderService;

    public WebRTCSignallingService(
            OnlineUsersService onlineUsersService,
            MessageSenderService messageSenderService
    ) {
        this.onlineUsersService = onlineUsersService;
        this.messageSenderService = messageSenderService;
    }

    public void handleWebRTCSignal(SignalMessage message) {
        Integer fromUserId = message.from();
        Integer toUserId = message.to();

        if (onlineUsersService.isUserOffline(toUserId)) {
            throw new UserOfflineException(toUserId);
        }

        messageSenderService.sendMessage(toUserId, message);
        log.debug(
                "WebRTC signal forwarded: type={}, from={}, to={}",
                message.type(), fromUserId, toUserId
        );
    }
}
