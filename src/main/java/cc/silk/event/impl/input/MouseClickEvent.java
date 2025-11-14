package cc.silk.event.impl.input;

import cc.silk.event.types.Event;

public record MouseClickEvent(int button, int action, int mods) implements Event {

}