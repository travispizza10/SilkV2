package cc.silk.event.types;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CancellableEvent implements Event {

    private boolean cancelled;

    public void cancel() {
        setCancelled(true);
    }
}