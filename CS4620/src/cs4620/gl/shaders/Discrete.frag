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
	
	if (dot(N, V) < .3) {
		finalColor = vec4(1.0, 1.0, 1.0, 1.0);
	}
	
	else if (dot(N, V) < .4) {
		finalColor = vec4(0.0, 0.0, 0.0, 1.0);
	}
	
	else if (dot(N, V) < .5) {
		finalColor = vec4(1.0, 1.0, 1.0, 1.0);
	}
	
	else if (dot(N, V) < .6) {
		finalColor = vec4(0.0, 0.0, 0.0, 1.0);
	}
	
	else if (dot(N, V) < .7) {
		finalColor = vec4(1.0, 1.0, 1.0, 1.0);
	}
	
	else if (dot(N, V) < .8) {
		finalColor = vec4(0.0, 0.0, 0.0, 1.0);
	}
	
	else if (dot(N, V) < .85) {
		finalColor = vec4(1.0, 1.0, 1.0, 1.0);
	}
	
	else if (dot(N, V) < .9) {
		finalColor = vec4(0.0, 0.0, 0.0, 1.0);
	}
	
	else if (dot(N, V) < .95) {
		finalColor = vec4(1.0, 1.0, 1.0, 1.0);
	}

	gl_FragColor = (finalColor * exposure);
}
