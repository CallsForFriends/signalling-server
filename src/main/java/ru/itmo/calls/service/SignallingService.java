package ru.itmo.calls.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.itmo.calls.exception.InvalidMessageException;
import ru.itmo.calls.model.SignalMessage;
import ru.itmo.calls.model.SignalType;

@Service
public class SignallingService {
    private static final Logger log = LoggerFactory.getLogger(SignallingService.class);

    private final PayloadValidator payloadValidator;
    private final CallSignallingService callSignallingService;
    private final WebRTCSignallingService webRTCSignallingService;
    private final MessageSenderService messageSenderService;

    public SignallingService(
            PayloadValidator payloadValidator,
            CallSignallingService callSignallingService,
            WebRTCSignallingService webRTCSignallingService,
            MessageSenderService messageSenderService
    ) {
        this.payloadValidator = payloadValidator;
        this.callSignallingService = callSignallingService;
        this.webRTCSignallingService = webRTCSignallingService;
        this.messageSenderService = messageSenderService;
    }

    public void routeMessage(SignalMessage message, Integer fromUserId) {
        if (message.type() == null) {
            throw new InvalidMessageException("Message type is required");
        }

        if (message.to() == null) {
            throw new InvalidMessageException("Recipient (to) is required");
        }

        if (fromUserId.equals(message.to())) {
            throw new InvalidMessageException("Cannot call yourself");
        }

        payloadValidator.validatePayload(message.type(), message.payload());

        SignalMessage enrichedMessage = new SignalMessage(
                message.type(),
                fromUserId,
                message.to(),
                message.payload()
        );

        log.debug(
                "Routing message: type={}, from={}, to={}",
                enrichedMessage.type(), fromUserId, enrichedMessage.to()
        );

        switch (enrichedMessage.type()) {
            case CALL_INIT -> callSignallingService.handleCallInit(enrichedMessage);
            case CALL_ACCEPT -> callSignallingService.handleCallAccept(enrichedMessage);
            case CALL_REJECT -> callSignallingService.handleCallReject(enrichedMessage);
            case CALL_END -> callSignallingService.handleCallEnd(enrichedMessage);
            case WEBRTC_OFFER, WEBRTC_ANSWER, WEBRTC_CANDIDATE ->
                    webRTCSignallingService.handleWebRTCSignal(enrichedMessage);
            case PING -> handlePing(fromUserId);
            default -> throw new InvalidMessageException("Unsupported message type: " + enrichedMessage.type());
        }
    }

    private void handlePing(Integer fromUserId) {
        SignalMessage pong = new SignalMessage(
                SignalType.PONG,
                null,
                fromUserId,
                null
        );

        try {
            messageSenderService.sendMessage(fromUserId, pong);
            log.debug("Pong sent to user {}", fromUserId);
        } catch (Exception e) {
            log.error("Failed to send pong to user {}", fromUserId, e);
        }
    }

    public void sendError(Integer userId, String errorMessage) {
        messageSenderService.sendError(userId, errorMessage);
    }
}

