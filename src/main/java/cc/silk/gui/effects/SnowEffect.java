package cc.silk.gui.effects;

import cc.silk.utils.render.nanovg.NanoVGRenderer;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SnowEffect {
    private final List<SnowParticle> particles = new ArrayList<>();
    private final Random random = new Random();
    private static final int PARTICLE_COUNT = 100;
    private static final Color SNOW_COLOR = new Color(255, 255, 255);

    public void init(int screenWidth, int screenHeight) {
        particles.clear();
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            float x = random.nextFloat() * screenWidth;
            float y = random.nextFloat() * screenHeight;
            particles.add(new SnowParticle(x, y, screenHeight, random));
        }
    }

    public void update(float deltaTime, int screenWidth, int screenHeight) {
        if (particles.isEmpty()) {
            init(screenWidth, screenHeight);
        }

        for (SnowParticle particle : particles) {
            particle.update(deltaTime);

            if (particle.getX() < -10 || particle.getX() > screenWidth + 10) {
                particle.reset(random.nextFloat() * screenWidth, particle.getY());
            }
        }
    }

    public void render(float alpha) {
        for (SnowParticle particle : particles) {
            int particleAlpha = (int) (particle.getOpacity() * 255 * alpha);
            Color color = new Color(SNOW_COLOR.getRed(), SNOW_COLOR.getGreen(), SNOW_COLOR.getBlue(), particleAlpha);
            
            NanoVGRenderer.drawCircle(particle.getX(), particle.getY(), particle.getSize(), color);
        }
    }

    public void clear() {
        particles.clear();
    }
}
