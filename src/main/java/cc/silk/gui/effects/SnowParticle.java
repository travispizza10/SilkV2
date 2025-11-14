package cc.silk.gui.effects;

import java.util.Random;

public class SnowParticle {
    private float x;
    private float y;
    private float size;
    private float speed;
    private float sway;
    private float swayOffset;
    private float opacity;
    private final float maxY;

    public SnowParticle(float x, float y, float maxY, Random random) {
        this.x = x;
        this.y = y;
        this.maxY = maxY;
        this.size = 2f + random.nextFloat() * 3f;
        this.speed = 0.5f + random.nextFloat() * 1.5f;
        this.sway = 0.3f + random.nextFloat() * 0.7f;
        this.swayOffset = random.nextFloat() * (float) Math.PI * 2;
        this.opacity = 0.3f + random.nextFloat() * 0.5f;
    }

    public void update(float deltaTime) {
        y += speed * deltaTime * 60f;
        x += (float) Math.sin(y * 0.01f + swayOffset) * sway;

        if (y > maxY) {
            y = -10;
        }
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getSize() {
        return size;
    }

    public float getOpacity() {
        return opacity;
    }

    public void reset(float newX, float newY) {
        this.x = newX;
        this.y = newY;
    }
}
