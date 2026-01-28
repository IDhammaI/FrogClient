#version 150

uniform sampler2D DiffuseSampler;
uniform float Radius;
uniform float BlurType;
uniform vec2 BlurXY;
uniform vec2 BlurCoord;
uniform vec2 InSize;

in vec2 texCoord;
out vec4 fragColor;

float insideRect(vec2 p, vec2 pos, vec2 size) {
    vec2 p1 = pos;
    vec2 p2 = pos + size;
    return step(p1.x, p.x) * step(p1.y, p.y) * step(p.x, p2.x) * step(p.y, p2.y);
}

void main() {
    vec4 base = texture(DiffuseSampler, texCoord);
    float r = clamp(Radius, 0.0, 64.0);
    if (r <= 0.0) {
        fragColor = base;
        return;
    }

    vec2 spaceSize = InSize;
    if (BlurXY.x + BlurCoord.x <= InSize.x * 0.5 + 1.0 && BlurXY.y + BlurCoord.y <= InSize.y * 0.5 + 1.0) {
        spaceSize = InSize * 0.5;
    }
    vec2 p = texCoord * spaceSize;

    if (insideRect(p, BlurXY, BlurCoord) < 0.5) {
        fragColor = base;
        return;
    }

    int mode = int(BlurType + 0.5);
    vec2 texel = 1.0 / InSize;
    vec2 o = texel * r;

    if (mode == 1) {
        vec4 c00 = texture(DiffuseSampler, texCoord + vec2(-1.0, -1.0) * o);
        vec4 c01 = texture(DiffuseSampler, texCoord + vec2( 0.0, -1.0) * o);
        vec4 c02 = texture(DiffuseSampler, texCoord + vec2( 1.0, -1.0) * o);
        vec4 c10 = texture(DiffuseSampler, texCoord + vec2(-1.0,  0.0) * o);
        vec4 c11 = texture(DiffuseSampler, texCoord);
        vec4 c12 = texture(DiffuseSampler, texCoord + vec2( 1.0,  0.0) * o);
        vec4 c20 = texture(DiffuseSampler, texCoord + vec2(-1.0,  1.0) * o);
        vec4 c21 = texture(DiffuseSampler, texCoord + vec2( 0.0,  1.0) * o);
        vec4 c22 = texture(DiffuseSampler, texCoord + vec2( 1.0,  1.0) * o);

        vec4 sum = vec4(0.0);
        sum += (c00 + c02 + c20 + c22);
        sum += (c01 + c10 + c12 + c21) * 2.0;
        sum += c11 * 4.0;
        fragColor = sum / 16.0;
    } else if (mode == 2) {
        float w0 = 0.252;
        float w1 = 0.153;
        float w2 = 0.034;

        vec4 sum = texture(DiffuseSampler, texCoord) * w0;
        sum += texture(DiffuseSampler, texCoord + vec2( 1.0,  0.0) * o) * w1;
        sum += texture(DiffuseSampler, texCoord + vec2(-1.0,  0.0) * o) * w1;
        sum += texture(DiffuseSampler, texCoord + vec2( 0.0,  1.0) * o) * w1;
        sum += texture(DiffuseSampler, texCoord + vec2( 0.0, -1.0) * o) * w1;
        sum += texture(DiffuseSampler, texCoord + vec2( 2.0,  0.0) * o) * w2;
        sum += texture(DiffuseSampler, texCoord + vec2(-2.0,  0.0) * o) * w2;
        sum += texture(DiffuseSampler, texCoord + vec2( 0.0,  2.0) * o) * w2;
        sum += texture(DiffuseSampler, texCoord + vec2( 0.0, -2.0) * o) * w2;
        fragColor = sum;
    } else if (mode == 3) {
        vec2 d = o * 1.5;
        vec4 sum = texture(DiffuseSampler, texCoord) * 0.4;
        sum += texture(DiffuseSampler, texCoord + vec2( d.x,  d.y)) * 0.15;
        sum += texture(DiffuseSampler, texCoord + vec2(-d.x,  d.y)) * 0.15;
        sum += texture(DiffuseSampler, texCoord + vec2( d.x, -d.y)) * 0.15;
        sum += texture(DiffuseSampler, texCoord + vec2(-d.x, -d.y)) * 0.15;
        fragColor = sum;
    } else if (mode == 4) {
        float TAU = 6.28318530718;
        vec2 radiusStep = r / InSize;
        vec4 color = texture(DiffuseSampler, texCoord);
        float stepAngle = TAU / 16.0;
        for (float d = 0.0; d < TAU; d += stepAngle) {
            vec2 dir = vec2(cos(d), sin(d));
            for (float i = 0.2; i <= 1.0; i += 0.2) {
                color += texture(DiffuseSampler, texCoord + dir * radiusStep * i);
            }
        }
        fragColor = color / 80.0;
    } else {
        vec4 sum = vec4(0.0);
        sum += texture(DiffuseSampler, texCoord + vec2(-1.0, -1.0) * o);
        sum += texture(DiffuseSampler, texCoord + vec2( 0.0, -1.0) * o);
        sum += texture(DiffuseSampler, texCoord + vec2( 1.0, -1.0) * o);
        sum += texture(DiffuseSampler, texCoord + vec2(-1.0,  0.0) * o);
        sum += texture(DiffuseSampler, texCoord);
        sum += texture(DiffuseSampler, texCoord + vec2( 1.0,  0.0) * o);
        sum += texture(DiffuseSampler, texCoord + vec2(-1.0,  1.0) * o);
        sum += texture(DiffuseSampler, texCoord + vec2( 0.0,  1.0) * o);
        sum += texture(DiffuseSampler, texCoord + vec2( 1.0,  1.0) * o);
        fragColor = sum / 9.0;
    }
}