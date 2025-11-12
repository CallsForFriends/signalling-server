package ru.itmo.calls.model.payload;

import java.util.Objects;

public record WebRTCAnswer(String sdp) {
    public WebRTCAnswer {
        sdp = Objects.requireNonNull(sdp, "SDP is required for WebRTC answer");
    }
}
