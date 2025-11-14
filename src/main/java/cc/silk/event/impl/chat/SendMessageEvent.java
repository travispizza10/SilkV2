package cc.silk.event.impl.chat;

import cc.silk.event.types.CancellableEvent;

public class SendMessageEvent extends CancellableEvent {
    private final String message;

    public SendMessageEvent(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
