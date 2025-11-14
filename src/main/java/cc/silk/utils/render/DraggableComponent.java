package cc.silk.utils.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

public class DraggableComponent {
    private float x;
    private float y;
    private float width;
    private float height;

    private boolean dragging = false;
    private float dragOffsetX = 0;
    private float dragOffsetY = 0;

    public DraggableComponent(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void update() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.mouse == null || mc.getWindow() == null)
            return;

        if (mc.currentScreen == null) {
            dragging = false;
            return;
        }

        double mouseX = mc.mouse.getX() * mc.getWindow().getScaledWidth() / (double) mc.getWindow().getWidth();
        double mouseY = mc.mouse.getY() * mc.getWindow().getScaledHeight() / (double) mc.getWindow().getHeight();

        boolean leftClick = GLFW.glfwGetMouseButton(mc.getWindow().getHandle(),
                GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;

        if (leftClick) {
            if (!dragging) {
                if (isMouseOver(mouseX, mouseY)) {
                    dragging = true;
                    dragOffsetX = (float) (mouseX - x);
                    dragOffsetY = (float) (mouseY - y);
                }
            }

            if (dragging) {
                x = (float) mouseX - dragOffsetX;
                y = (float) mouseY - dragOffsetY;

                x = MathHelper.clamp(x, 0, mc.getWindow().getScaledWidth() - width);
                y = MathHelper.clamp(y, 0, mc.getWindow().getScaledHeight() - height);
            }
        } else {
            dragging = false;
        }
    }

    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public boolean isDragging() {
        return dragging;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public void setHeight(float height) {
        this.height = height;
    }
}
