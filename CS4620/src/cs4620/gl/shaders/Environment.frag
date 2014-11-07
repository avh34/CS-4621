#version 120

// You May Use The Following Functions As RenderMaterial Input
// vec4 getDiffuseColor(vec2 uv)
// vec4 getNormalColor(vec2 uv)
// vec4 getSpecularColor(vec2 uv)
// veck getEnvironmetLight(veck dir)

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

	// The end result is just simply the color of the environment the 
	// viewing ray hits
	gl_FragColor = getEnvironmentColor(V) * exposure;
	
}
