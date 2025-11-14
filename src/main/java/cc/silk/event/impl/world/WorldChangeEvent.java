package cc.silk.event.impl.world;

import cc.silk.event.types.Event;
import lombok.Getter;
import net.minecraft.client.world.ClientWorld;

@Getter
public class WorldChangeEvent implements Event {
    ClientWorld world;

    public WorldChangeEvent(ClientWorld world) {
        this.world = world;
    }
}
