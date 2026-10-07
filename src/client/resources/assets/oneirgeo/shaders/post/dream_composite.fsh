#version 330
#extension GL_ARB_separate_shader_objects : require

// Everything that makes a frame look remembered rather than seen. Each strength is 0 when off.
//   Strength1: bloom, vignette, overexposure, chromatic aberration
//   Strength2: grain / VHS, pixelation, JPEG blocks, lens distortion
//   Tint:      rgb multiplier, saturation offset
//   Params:    seconds, warp, flicker allowed (0 in safe mode), unused
//   Camcorder: handheld tape look (480 lines, chroma bleed, tracking, scanlines), unused x3

uniform sampler2D InSampler;
uniform sampler2D BloomSampler;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
    vec2 BloomSize;
};

layout(std140) uniform DreamConfig {
    vec4 Strength1;
    vec4 Strength2;
    vec4 Tint;
    vec4 Params;
    vec4 Camcorder;
};

layout(location = 0) in vec2 texCoord;

layout(location = 0) out vec4 fragColor;

float hash(vec2 p) {
    return fract(sin(dot(p, vec2(12.9898, 78.233))) * 43758.5453);
}

vec3 toYiq(vec3 c) {
    return vec3(dot(c, vec3(0.299, 0.587, 0.114)), dot(c, vec3(0.596, -0.274, -0.322)), dot(c, vec3(0.211, -0.523, 0.312)));
}

vec3 fromYiq(vec3 y) {
    return vec3(y.x + 0.956 * y.y + 0.621 * y.z, y.x - 0.272 * y.y - 0.647 * y.z, y.x - 1.106 * y.y + 1.703 * y.z);
}

// The tracking band: a stripe rolling slowly down the frame.
float trackingBand(float y, float seconds) {
    return 1.0 - smoothstep(0.0, 0.035, abs(y - fract(seconds * 0.07)));
}

void main() {
    float bloom = Strength1.x;
    float vignette = Strength1.y;
    float exposure = Strength1.z;
    float chromatic = Strength1.w;
    float grain = Strength2.x;
    float pixelate = Strength2.y;
    float jpeg = Strength2.z;
    float lens = Strength2.w;
    float seconds = Params.x;
    float warp = Params.y;
    float flicker = Params.z;
    float cam = Camcorder.x;

    vec2 uv = texCoord;

    // lens: barrel distortion, plus a slow breathing warp when lucidity is low
    vec2 centred = uv - 0.5;
    float r2 = dot(centred, centred);
    uv = 0.5 + centred * (1.0 + lens * 0.32 * r2);
    uv += warp * 0.006 * vec2(sin(uv.y * 17.0 + seconds * 1.3), cos(uv.x * 13.0 + seconds * 1.1));

    // tape: 480 lines, each drifting sideways, torn apart inside the tracking band
    if (cam > 0.001) {
        float lines = 480.0;
        float line = floor(uv.y * lines);
        uv.x += (hash(vec2(line, floor(seconds * 30.0))) - 0.5) * 0.0014 * cam * flicker;
        uv.x += sin(uv.y * 9.0 + seconds * 2.0) * 0.0009 * cam;
        uv.x += trackingBand(uv.y, seconds) * (hash(vec2(line, floor(seconds * 24.0))) - 0.5) * 0.03 * cam * flicker;
        uv.y = (line + 0.5) / lines;
    }

    // pixelation
    if (pixelate > 0.01) {
        float pixel = mix(1.0, 7.0, pixelate);
        vec2 grid = OutSize / pixel;
        uv = (floor(uv * grid) + 0.5) / grid;
    }

    // JPEG: 8px blocks that lose detail, and in heavy cases slip sideways for a moment
    vec2 blockGrid = OutSize / 8.0;
    vec2 block = floor(uv * blockGrid);
    if (jpeg > 0.3 && flicker > 0.5) {
        float slip = hash(block.yy + floor(seconds * 2.0));
        if (slip > 1.0 - (jpeg - 0.3) * 0.12) {
            uv.x += (hash(block + 3.1) - 0.5) * 0.04;
        }
    }

    vec3 color;
    if (chromatic > 0.001) {
        vec2 offset = (uv - 0.5) * chromatic * 0.014;
        if (flicker > 0.5) {
            offset *= 1.0 + 0.25 * sin(seconds * 3.7);
        }
        color.r = texture(InSampler, uv + offset).r;
        color.g = texture(InSampler, uv).g;
        color.b = texture(InSampler, uv - offset).b;
    } else {
        color = texture(InSampler, uv).rgb;
    }

    // tape: soft picture whose colour smears to the right of what it belongs to
    if (cam > 0.001) {
        vec2 px = vec2(2.5 / OutSize.x, 0.0);
        vec3 here = toYiq(color);
        vec3 left2 = toYiq(texture(InSampler, uv - px * 2.0).rgb);
        vec3 left1 = toYiq(texture(InSampler, uv - px).rgb);
        vec3 right1 = toYiq(texture(InSampler, uv + px).rgb);
        here.yz = mix(here.yz, (left2.yz + left1.yz + right1.yz + here.yz) * 0.25, cam);
        here.x = mix(here.x, (left1.x + right1.x + here.x * 2.0) * 0.25, 0.35 * cam);
        color = fromYiq(here);
    }

    if (jpeg > 0.001) {
        vec3 blockColor = texture(InSampler, (block + 0.5) / blockGrid).rgb;
        color = mix(color, blockColor, jpeg * 0.55);
        float levels = mix(48.0, 7.0, jpeg);
        color = floor(color * levels + 0.5) / levels;
    }

    color += texture(BloomSampler, uv).rgb * bloom * 1.25;

    if (exposure > 0.001) {
        vec3 burnt = 1.0 - exp(-color * (1.0 + 2.6 * exposure));
        color = mix(color, burnt, clamp(exposure * 1.4, 0.0, 1.0));
    }

    float luma = dot(color, vec3(0.299, 0.587, 0.114));
    color = mix(vec3(luma), color, 1.0 + Tint.a);
    color *= Tint.rgb;

    if (grain > 0.001) {
        float time = flicker > 0.5 ? floor(seconds * 24.0) : 0.0;
        float noise = hash(texCoord * OutSize + time * 17.0) - 0.5;
        color += noise * grain * 0.14;
        color *= 1.0 - grain * 0.07 * (0.5 + 0.5 * sin(texCoord.y * OutSize.y * 3.14159));
        if (flicker > 0.5 && grain > 0.3) {
            float band = abs(fract(texCoord.y * 0.6 - seconds * 0.05) - 0.5);
            color *= mix(0.86, 1.0, smoothstep(0.0, 0.03, band));
        }
    }

    // tape: lifted blacks and a green-grey cast, scanlines, speckled tracking band, unsteady exposure
    if (cam > 0.001) {
        color = mix(color, color * vec3(0.95, 1.0, 0.92) * 0.9 + 0.035, cam);
        float scan = 0.5 + 0.5 * cos(texCoord.y * 240.0 * 6.28318);
        color *= 1.0 - 0.1 * cam * scan;
        float band = trackingBand(texCoord.y, seconds);
        float speck = step(0.985, hash(texCoord * OutSize + floor(seconds * 30.0)));
        color = mix(color, vec3(0.9), band * speck * cam * flicker);
        color += band * 0.05 * cam * flicker;
        color *= 1.0 + (hash(vec2(floor(seconds * 12.0), 3.7)) - 0.5) * 0.05 * cam * flicker;
        vec2 corner = (texCoord - 0.5) * vec2(1.15, 1.0);
        color *= mix(1.0, smoothstep(0.95, 0.25, length(corner)), 0.35 * cam);
    }

    if (vignette > 0.001) {
        vec2 v = (texCoord - 0.5) * vec2(1.15, 1.0);
        float shade = smoothstep(0.85, 0.18, length(v));
        color *= mix(1.0, shade, vignette);
    }

    fragColor = vec4(clamp(color, 0.0, 1.0), 1.0);
}
