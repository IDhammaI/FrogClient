#version 150

uniform sampler2D DiffuseSampler;
uniform float Radius;
uniform vec2 BlurXY;
uniform vec2 BlurCoord;

in vec2 texCoord;
out vec4 fragColor;

void main() {
    fragColor = texture(DiffuseSampler, texCoord);
}