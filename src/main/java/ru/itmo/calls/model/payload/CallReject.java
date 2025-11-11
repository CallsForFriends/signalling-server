package ru.itmo.calls.model.payload;

public record CallReject(CallRejectReason reason) {
    public CallReject {
        reason = reason == null ? CallRejectReason.DECLINED : reason;
    }
}
