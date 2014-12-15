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

// Shading Information
uniform float shininess;

varying vec3 fN; 
varying vec4 worldPos; 

void main() {
	vec3 N = normalize(fN);
	vec3 V = normalize(worldCam - worldPos.xyz);
	vec4 finalColor = vec4(0.0);
	
	// Set the silhouette to black
	gl_FragColor = finalColor;
}


