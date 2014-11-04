#version 120

// Lighting Information
const int MAX_LIGHTS = 16;
uniform int numLights;
uniform vec3 lightIntensity[MAX_LIGHTS];
uniform vec3 lightPosition[MAX_LIGHTS];

// Camera Information
uniform vec3 worldCam;
uniform float exposure;

varying vec2 fUV;
varying vec3 fN; // normal at the vertex
varying vec4 worldPos; // vertex position in world coordinates

void main()
{
	// Here we reverse the direction of V so that we have the ray going from
	// the camera to the vertex
	vec3 V = normalize(worldPos.xyz - worldCam);
	vec3 N = normalize(fN);

	// Here we implement the reflection formula we dervied in the warmup exercise
	vec3 reflectedRay = V - 2*dot(V, N)*N;
	
	// The end result is just simply the color of the environment where 
	// the reflected ray hits
	gl_FragColor = getEnvironmentColor(reflectedRay) * exposure;
}
