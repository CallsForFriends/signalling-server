package ru.itmo.calls.model.dto;

public record UserMeResponse(UserInfo user) {
    public record UserInfo(Integer id) {
    }
}
