#version 330 core

layout (location = 0) in vec2 a_position;

out vec2 fragCoord;
out vec2 screenPos;

uniform vec2 u_resolution;

void main() {
    vec2 screenCoord = (a_position + 1.0) * 0.5;
    screenCoord.y = 1.0 - screenCoord.y;
    fragCoord = screenCoord * u_resolution;
    screenPos = a_position;
    gl_Position = vec4(a_position, 0.0, 1.0);
}

