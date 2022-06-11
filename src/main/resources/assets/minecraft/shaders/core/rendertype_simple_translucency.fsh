#version 150

#moj_import <fog.glsl>

uniform sampler2D Sampler0;

in float vertexDistance;
in vec4 texProj0;

uniform mat4 ProjMat;
uniform mat4 ModelViewMat;
uniform vec4 FogColor;

out vec4 fragColor;

void main() {
	vec4 skyColor = texture(Sampler0, gl_FragCoord.xy);
    fragColor = FogColor;
    fragColor.w = 0.0;
}
