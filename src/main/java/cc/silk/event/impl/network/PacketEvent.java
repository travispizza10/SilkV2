package cc.silk.event.impl.network;

import cc.silk.event.types.CancellableEvent;
import cc.silk.event.types.TransferOrder;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.packet.Packet;


@Getter
@Setter
public class PacketEvent extends CancellableEvent {
    private final TransferOrder order;
    private Packet packet;

    public PacketEvent(Packet packet, TransferOrder order) {
        this.packet = packet;
        this.order = order;
    }

}