#version 330
#extension GL_ARB_separate_shader_objects : require

// Vertical half of the bloom blur.

uniform sampler2D InSampler;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

layout(location = 0) in vec2 texCoord;

layout(location = 0) out vec4 fragColor;

const float WEIGHTS[5] = float[](0.227027, 0.1945946, 0.1216216, 0.054054, 0.016216);

void main() {
    vec2 stepSize = vec2(0.0, 2.0 / InSize.y);
    vec3 sum = texture(InSampler, texCoord).rgb * WEIGHTS[0];
    for (int i = 1; i < 5; i++) {
        sum += texture(InSampler, texCoord + stepSize * float(i)).rgb * WEIGHTS[i];
        sum += texture(InSampler, texCoord - stepSize * float(i)).rgb * WEIGHTS[i];
    }
    fragColor = vec4(sum, 1.0);
}
