package ru.itmo.calls.model;

public enum SignalType {
    AUTH,
    AUTH_SUCCESS,
    AUTH_FAILED,
    
    CALL_INIT,
    INCOMING_CALL,
    CALL_ACCEPT,
    CALL_REJECT,
    CALL_END,

    WEBRTC_OFFER,
    WEBRTC_ANSWER,
    WEBRTC_CANDIDATE,

    PING,
    PONG,
    ERROR
}

