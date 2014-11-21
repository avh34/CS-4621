#version 120

// You May Use The Following Functions As RenderMaterial Input
// vec4 getDiffuseColor(vec2 uv)
// vec4 getNormalColor(vec2 uv)
// vec4 getSpecularColor(vec2 uv)

// Lighting Information
const int MAX_LIGHTS = 16;
uniform int numLights;
uniform vec3 lightIntensity[MAX_LIGHTS];
uniform vec3 lightPosition[MAX_LIGHTS];
uniform vec3 ambientLightIntensity;

// Camera Information
uniform vec3 worldCam;
uniform float exposure;

varying vec2 fUV;
varying vec3 fN; // normal at the vertex
varying vec4 worldPos; // vertex position in world coordinates

void main() {
	// interpolating normals will change the length of the normal, so renormalize the normal.
	vec3 N = normalize(fN);
	vec3 V = normalize(worldCam - worldPos.xyz);
	vec4 finalColor = vec4(0.0, 0.0, 0.0, 0.0);
	float intensity = 0;
	
	for (int i = 0; i < numLights; i++) {
		intensity = dot(N, lightPosition[i]);
	
	if (intensity > 0.95)
		finalColor += vec4(1.0,0.5,0.5,1.0);
	else if (intensity > 0.5)
		finalColor += vec4(0.6,0.3,0.3,1.0);
	else if (intensity > 0.25)
		finalColor += vec4(0.4,0.2,0.2,1.0);
	else
		finalColor += vec4(0.2,0.1,0.1,1.0);
	
	}
	
	finalColor = finalColor/numLights;
	
	gl_FragColor = (finalColor * exposure);
}
