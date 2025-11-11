package ru.itmo.calls.model.payload;

import java.util.Objects;

public record WebRTCCandidate(String candidate, String sdpMid, Integer sdpMLineIndex) {
    public WebRTCCandidate {
        candidate = Objects.requireNonNull(candidate, "Candidate is required for ICE candidate");
    }
}
