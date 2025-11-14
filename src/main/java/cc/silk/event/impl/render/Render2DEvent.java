package cc.silk.event.impl.render;

import cc.silk.event.types.Event;
import net.minecraft.client.gui.DrawContext;

public class Render2DEvent implements Event {
    private DrawContext context;
    private int width;
    private int height;

    public Render2DEvent(DrawContext context, int width, int height) {
        this.context = context;
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public DrawContext getContext() {
        return context;
    }

    public void setContext(DrawContext context) {
        this.context = context;
    }
}
