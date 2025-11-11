package ru.itmo.calls.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.itmo.calls.exception.InvalidMessageException;
import ru.itmo.calls.model.SignalType;
import ru.itmo.calls.model.payload.*;

@Service
public class PayloadValidator {
    private static final Logger log = LoggerFactory.getLogger(PayloadValidator.class);
    
    private final ObjectMapper objectMapper;
    
    public PayloadValidator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void validatePayload(SignalType type, JsonNode payload) {
        switch (type) {
            case WEBRTC_OFFER -> validateWebRTCOffer(payload);
            case WEBRTC_ANSWER -> validateWebRTCAnswer(payload);
            case WEBRTC_CANDIDATE -> validateWebRTCCandidate(payload);
            case CALL_REJECT -> validateCallReject(payload);
            case CALL_INIT, CALL_ACCEPT, CALL_END, INCOMING_CALL, PING, PONG -> {
                if (payload != null && !payload.isNull()) {
                    log.debug("Message type {} has optional payload", type);
                }
            }
            case ERROR -> {
                if (payload == null || !payload.has("message")) {
                    throw new InvalidMessageException("Error message must have 'message' field");
                }
            }
            default -> log.warn("Unknown message type for validation: {}", type);
        }
    }
    
    private void validateWebRTCOffer(JsonNode payload) {
        if (payload == null) {
            throw new InvalidMessageException("WebRTC offer must have payload");
        }
        
        try {
            objectMapper.treeToValue(payload, WebRTCOffer.class);
        } catch (Exception e) {
            throw new InvalidMessageException("Invalid WebRTC offer payload: " + e.getMessage());
        }
    }
    
    private void validateWebRTCAnswer(JsonNode payload) {
        if (payload == null) {
            throw new InvalidMessageException("WebRTC answer must have payload");
        }
        
        try {
            objectMapper.treeToValue(payload, WebRTCAnswer.class);
        } catch (Exception e) {
            throw new InvalidMessageException("Invalid WebRTC answer payload: " + e.getMessage());
        }
    }
    
    private void validateWebRTCCandidate(JsonNode payload) {
        if (payload == null) {
            throw new InvalidMessageException("WebRTC candidate must have payload");
        }
        
        try {
            objectMapper.treeToValue(payload, WebRTCCandidate.class);
        } catch (Exception e) {
            throw new InvalidMessageException("Invalid WebRTC candidate payload: " + e.getMessage());
        }
    }
    
    private void validateCallReject(JsonNode payload) {
        if (payload != null && !payload.isNull()) {
            try {
                objectMapper.treeToValue(payload, CallReject.class);
            } catch (Exception e) {
                log.warn("Invalid call reject payload, using default reason: {}", e.getMessage());
            }
        }
    }
}
