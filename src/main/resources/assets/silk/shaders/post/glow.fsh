#version 330 core

in vec2 fragCoord;
in vec2 screenPos;
out vec4 fragColor;

uniform vec2 u_resolution;
uniform vec2 u_position;
uniform vec2 u_size;
uniform float u_radius;
uniform vec4 u_color;
uniform float u_glowIntensity;
uniform float u_glowThickness;
uniform float u_bloomRadius;

float roundedBoxSDF(vec2 centerPos, vec2 size, float radius) {
    return length(max(abs(centerPos) - size + radius, 0.0)) - radius;
}

float gaussian(float x, float sigma) {
    return exp(-(x * x) / (2.0 * sigma * sigma));
}

float calculateGlow(float distance, float thickness, float intensity) {
    float glow = smoothstep(thickness, 0.0, distance);
    float bloom = exp(-abs(distance) * 0.1) * intensity;
    return max(glow, bloom);
}

void main() {
    vec2 uv = fragCoord;
    vec2 halfSize = u_size * 0.5;
    vec2 center = u_position + halfSize;
    vec2 pos = uv - center;
    
    float distance = roundedBoxSDF(pos, halfSize, u_radius);
    
    if (distance < 0.0) {
        fragColor = vec4(0.0);
        return;
    }
    
    float glow = 0.0;
    
    glow += calculateGlow(distance, u_glowThickness, u_glowIntensity) * 0.6;

    for (float i = 1.0; i <= 5.0; i++) {
        float layerDist = distance - (i * u_bloomRadius * 0.2);
        float layerIntensity = u_glowIntensity * (1.0 - i * 0.15);
        glow += gaussian(layerDist, u_bloomRadius * (1.0 + i * 0.5)) * layerIntensity * 0.3;
    }
    
    float radialBloom = exp(-distance * 0.05) * u_glowIntensity * 0.4;
    glow += radialBloom;
    
    vec2 cornerDist = abs(pos) - (halfSize - u_radius);
    if (cornerDist.x > 0.0 && cornerDist.y > 0.0) {
        float cornerGlow = exp(-length(cornerDist) * 0.1) * u_glowIntensity * 0.3;
        glow += cornerGlow;
    }
    
    if (distance < u_glowThickness * 0.3) {
        glow = max(glow, u_glowIntensity * 1.2);
    }
    
    glow = clamp(glow, 0.0, 1.5);
    
    vec3 finalColor = u_color.rgb * (1.0 + glow * 0.5);
    float finalAlpha = glow * u_color.a;
    
    float luminance = dot(finalColor, vec3(0.299, 0.587, 0.114));
    finalColor = mix(vec3(luminance), finalColor, 1.0 + glow * 0.3);
    
    fragColor = vec4(finalColor, finalAlpha);
}

