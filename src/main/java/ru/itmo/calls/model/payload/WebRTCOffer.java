package ru.itmo.calls.model.payload;

import java.util.Objects;

public record WebRTCOffer(String sdp) {
    public WebRTCOffer {
        sdp = Objects.requireNonNull(sdp, "SDP is required for WebRTC offer");
    }
}
