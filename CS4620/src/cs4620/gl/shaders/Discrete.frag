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
varying vec3 fN;       // Normal at the vertex
varying vec4 worldPos; // Vertex position in world coordinates

void main() {

	// Interpolating normals will change the length of the normal, so renormalize the normal.
	vec3 N = normalize(fN);
	vec3 V = normalize(worldCam - worldPos.xyz);
	vec4 finalColor = getDiffuseColor(fUV);
	float intensity = 0;
	
	// Calculate intensity
	for (int i = 0; i < numLights; i++) {
		vec3 L = normalize(lightPosition[i] - worldPos.xyz); 
		vec3 H = normalize(L + V);
	 	float r = length(lightPosition[i] - worldPos.xyz);
		intensity = max(dot(N, H)*100/(r*r), intensity);
	}
	
	// Discretize based on intensity, use the diffuse color of the object.
	if (intensity > 7.5)
		finalColor = getDiffuseColor(fUV);
	else if (intensity > 4.6)
		finalColor = getDiffuseColor(fUV)*.75;
	else if (intensity > 1.8)
		finalColor = getDiffuseColor(fUV)*.5;
	else
		finalColor = getDiffuseColor(fUV)*.25;
	
	gl_FragColor = (finalColor * exposure);
}
