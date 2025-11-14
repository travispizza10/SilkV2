package cc.silk.event.impl.render;

import cc.silk.event.types.Event;
import lombok.Getter;
import net.minecraft.client.util.math.MatrixStack;

@Getter
public class Render3DEvent implements Event {
    MatrixStack matrixStack;

    public Render3DEvent(MatrixStack matrixStack) {
        this.matrixStack = matrixStack;
    }
}
