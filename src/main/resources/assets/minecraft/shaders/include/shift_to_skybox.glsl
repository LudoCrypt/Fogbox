vec2 sampleCube(vec3 v, out float faceIndex) {
	vec3 vAbs = abs(v);
	float ma;
	vec2 uv;
	if(vAbs.z >= vAbs.x && vAbs.z >= vAbs.y) {
		faceIndex = v.z < 0.0 ? 2.0 : 4.0;
		ma = 0.5 / vAbs.z;
		uv = vec2(v.z < 0.0 ? -v.x : v.x, -v.y);
	} else if(vAbs.y >= vAbs.x) {
		faceIndex = v.y < 0.0 ? 5.0 : 0.0;
		ma = 0.5 / vAbs.y;
		uv = vec2(-v.x, v.y < 0.0 ? v.z : -v.z);
	} else {
		faceIndex = v.x < 0.0 ? 1.0 : 3.0;
		ma = 0.5 / vAbs.x;
		uv = vec2(v.x < 0.0 ? v.z : -v.z, -v.y);
	}
	return uv * ma + 0.5;
}

vec4 fog_linear(vec4 inColor, float vertexDistance, float fogStart,
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

vec4 blendOpacity(vec4 foreground, vec4 background) {
	return vec4(
			(background.x * (1 - foreground.w) + foreground.x * foreground.w),
			(background.y * (1 - foreground.w) + foreground.y * foreground.w),
			(background.z * (1 - foreground.w) + foreground.z * foreground.w),
			1.0);
}

vec4 betwixTwo(vec4 primary, vec4 secondary, float distance) {
	if (distance == 512.0) {
		return primary;
	} else {
		return blendOpacity(
				vec4(primary.xyz, min(((distance / 32) + 0.5), 1.0)), secondary);
	}
}

vec4 betwixSix(vec4 primary, vec4 secondary, float secondaryDistance,
		vec4 tertiary, float tertiaryDistance, vec4 quaternary,
		float quaternaryDistance, vec4 quinary, float quinaryDistance,
		vec4 senary, float senaryDistance) {
	return betwixTwo(
			betwixTwo(
					betwixTwo(
							betwixTwo(
									betwixTwo(primary, secondary,
											secondaryDistance), tertiary,
									tertiaryDistance), quaternary,
							quaternaryDistance), quinary, quinaryDistance),
			senary, senaryDistance);
}

vec2 lerp(vec4 line, vec2 delta) {
	return vec2(line.x + (delta.x * (line.z - line.x)),
			line.y + (delta.y * (line.w - line.y)));
}

vec2 ratioOnLine(vec4 ratio, vec4 line) {
	return vec2(((ratio.x * line.z) + (ratio.y * line.x)) / (ratio.x + ratio.y),
			((ratio.z * line.w) + (ratio.w * line.y)) / (ratio.z + ratio.w));
}

vec4 getUv(float faceIndex, vec4 uv, vec4 delta) {
	if (faceIndex == 0.0) {
		return vec4(
				lerp(
						vec4(ratioOnLine(vec4(0, 3, 0, 2), uv),
								ratioOnLine(vec4(1, 2, 1, 1), uv)).zyxw,
						delta.xy), 1.0, 1.0);
	} else if (faceIndex == 1.0) {
		return vec4(
				lerp(
						vec4(ratioOnLine(vec4(1, 2, 0, 2), uv),
								ratioOnLine(vec4(2, 1, 1, 1), uv)).zyxw,
						delta.xy), 1.0, 1.0);
	} else if (faceIndex == 2.0) {
		return vec4(
				lerp(
						vec4(ratioOnLine(vec4(2, 1, 0, 2), uv),
								ratioOnLine(vec4(3, 0, 1, 1), uv)).zyxw,
						delta.xy), 1.0, 1.0);
	} else if (faceIndex == 3.0) {
		return vec4(
				lerp(
						vec4(ratioOnLine(vec4(1, 2, 1, 1), uv),
								ratioOnLine(vec4(2, 1, 2, 0), uv)).zyxw,
						delta.xy), 1.0, 1.0);
	} else if (faceIndex == 4.0) {
		return vec4(
				lerp(
						vec4(ratioOnLine(vec4(2, 1, 1, 1), uv),
								ratioOnLine(vec4(3, 0, 2, 0), uv)).zyxw,
						delta.xy), 1.0, 1.0);
	} else if (faceIndex == 5.0) {
		return vec4(
				lerp(
						vec4(ratioOnLine(vec4(0, 3, 1, 1), uv),
								ratioOnLine(vec4(1, 2, 2, 0), uv)).zyxw,
						delta.xy), 1.0, 1.0);
	}
}

vec4 shift_to_skybox_from_fog(vec4 foggy, mat4 ModelMatrix, mat4 ProjMat,
		vec4 fogless, float vertexDistance, vec4 vertexColor,
		vec4 ColorModulator, float FogStart, float FogEnd, vec4 FogColor,
		vec4 glPos, vec4 UVPrimary, sampler2D SkyboxAtlasSampler,
		vec4 UVSecondary, float DistanceSecondary, vec4 UVTertiary,
		float DistanceTertiary, vec4 UVQuaternary, float DistanceQuaternary,
		vec4 UVQuinary, float DistanceQuinary, vec4 UVSenary,
		float DistanceSenary) {

	float near = 0.05;
	float far = (ProjMat[2][2] - 1.) / (ProjMat[2][2] + 1.) * near;
	vec3 rd = normalize(
			(inverse(ProjMat * ModelMatrix)
					* vec4(glPos.xy / glPos.w * (far - near), far + near,
							far - near)).xyz);
	float faceIndex = 0.0;
	vec4 texPos = vec4(sampleCube(rd, faceIndex), 1.0, 1.0);

	return fog_linear(fogless, vertexDistance, FogStart, FogEnd,
			betwixSix(
					blendOpacity(
							textureProj(SkyboxAtlasSampler,
									getUv(faceIndex, UVPrimary, texPos)),
							FogColor),
					blendOpacity(
							textureProj(SkyboxAtlasSampler,
									getUv(faceIndex, UVSecondary, texPos)),
							FogColor), DistanceSecondary,
					blendOpacity(
							textureProj(SkyboxAtlasSampler,
									getUv(faceIndex, UVTertiary, texPos)),
							FogColor), DistanceTertiary,
					blendOpacity(
							textureProj(SkyboxAtlasSampler,
									getUv(faceIndex, UVQuaternary, texPos)),
							FogColor), DistanceQuaternary,
					blendOpacity(
							textureProj(SkyboxAtlasSampler,
									getUv(faceIndex, UVQuinary, texPos)),
							FogColor), DistanceQuinary,
					blendOpacity(
							textureProj(SkyboxAtlasSampler,
									getUv(faceIndex, UVSenary, texPos)),
							FogColor), DistanceSenary));
}
