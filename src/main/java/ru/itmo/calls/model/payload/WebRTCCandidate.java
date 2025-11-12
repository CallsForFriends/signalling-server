package ru.itmo.calls.model.payload;

public record WebRTCCandidate(
        String candidate,
        String sdpMid,
        Integer sdpMLineIndex,
        String usernameFragment
) {
}
