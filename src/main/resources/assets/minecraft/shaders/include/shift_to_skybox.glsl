vec4 linearBlend(vec4 inColor, float vertexDistance, float fogStart,
		float fogEnd, vec4 fogColor) {
	if (vertexDistance <= fogStart) {
		return inColor;
	}

	float fogValue =
			vertexDistance < fogEnd ?
					smoothstep(fogStart, fogEnd, vertexDistance) : 1.0;
	return vec4(mix(inColor.rgb, fogColor.rgb, fogValue * fogColor.a),
			inColor.a);
}

vec4 shift_to_skybox_from_fog(vec4 fogIn, vec4 worldIn, float vertexDistance,
		float FogStart, float FogEnd, vec4 gl_FragCoord,
		sampler2D SkyboxSampler) {
	return linearBlend(worldIn, vertexDistance, FogStart, FogEnd,
			vec4(
					texture(SkyboxSampler,
							vec2(
									gl_FragCoord.x
											/ textureSize(SkyboxSampler, 0).x,
									gl_FragCoord.y
											/ textureSize(SkyboxSampler, 0).y)).xyz,
					1.0));
}
