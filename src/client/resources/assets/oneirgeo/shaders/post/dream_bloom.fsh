#version 330
#extension GL_ARB_separate_shader_objects : require

// Bright pass and the horizontal half of a separable blur.

uniform sampler2D InSampler;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

layout(location = 0) in vec2 texCoord;

layout(location = 0) out vec4 fragColor;

const float WEIGHTS[5] = float[](0.227027, 0.1945946, 0.1216216, 0.054054, 0.016216);

vec3 bright(vec2 uv) {
    vec3 color = texture(InSampler, uv).rgb;
    float luma = dot(color, vec3(0.299, 0.587, 0.114));
    return color * smoothstep(0.52, 0.95, luma);
}

void main() {
    vec2 stepSize = vec2(2.0 / InSize.x, 0.0);
    vec3 sum = bright(texCoord) * WEIGHTS[0];
    for (int i = 1; i < 5; i++) {
        sum += bright(texCoord + stepSize * float(i)) * WEIGHTS[i];
        sum += bright(texCoord - stepSize * float(i)) * WEIGHTS[i];
    }
    fragColor = vec4(sum, 1.0);
}
