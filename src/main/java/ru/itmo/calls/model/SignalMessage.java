package ru.itmo.calls.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@JsonInclude(NON_NULL)
public record SignalMessage(
        SignalType type,
        Integer from,
        Integer to,
        JsonNode payload
) {
    public static SignalMessage error(String message, Integer toUserId) {
        ObjectNode errorPayload = JsonNodeFactory.instance.objectNode();
        errorPayload.put("message", message);

        return new SignalMessage(
                SignalType.ERROR,
                null,
                toUserId,
                errorPayload
        );
    }
}

