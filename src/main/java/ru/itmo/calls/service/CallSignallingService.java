package ru.itmo.calls.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.itmo.calls.exception.UserOfflineException;
import ru.itmo.calls.model.SignalMessage;
import ru.itmo.calls.model.SignalType;

@Service
public class CallSignallingService {
    private static final Logger log = LoggerFactory.getLogger(CallSignallingService.class);
    
    private final OnlineUsersService onlineUsersService;
    private final MessageSenderService messageSenderService;
    
    public CallSignallingService(
        OnlineUsersService onlineUsersService,
        MessageSenderService messageSenderService
    ) {
        this.onlineUsersService = onlineUsersService;
        this.messageSenderService = messageSenderService;
    }

    public void handleCallInit(SignalMessage message) {
        Integer fromUserId = message.from();
        Integer toUserId = message.to();
        
        if (onlineUsersService.isUserOffline(toUserId)) {
            throw new UserOfflineException(toUserId);
        }

        SignalMessage incomingCall = new SignalMessage(
            SignalType.INCOMING_CALL,
            fromUserId,
            toUserId,
            null
        );
        
        messageSenderService.sendMessage(toUserId, incomingCall);
        log.info("Call initiated: from {} to {}", fromUserId, toUserId);
    }

    public void handleCallAccept(SignalMessage message) {
        Integer fromUserId = message.from();
        Integer toUserId = message.to();
        
        if (onlineUsersService.isUserOffline(toUserId)) {
            throw new UserOfflineException(toUserId);
        }
        
        messageSenderService.sendMessage(toUserId, message);
        log.info("Call accepted: from {} to {}", fromUserId, toUserId);
    }

    public void handleCallReject(SignalMessage message) {
        Integer fromUserId = message.from();
        Integer toUserId = message.to();
        
        if (onlineUsersService.isUserOffline(toUserId)) {
            throw new UserOfflineException(toUserId);
        }
        
        messageSenderService.sendMessage(toUserId, message);
        log.info("Call rejected: from {} to {}", fromUserId, toUserId);
    }

    public void handleCallEnd(SignalMessage message) {
        Integer fromUserId = message.from();
        Integer toUserId = message.to();
        
        if (onlineUsersService.isUserOffline(toUserId)) {
            log.debug("Call end: recipient {} already offline", toUserId);
            return;
        }
        
        messageSenderService.sendMessage(toUserId, message);
        log.info("Call ended: from {} to {}", fromUserId, toUserId);
    }
}
