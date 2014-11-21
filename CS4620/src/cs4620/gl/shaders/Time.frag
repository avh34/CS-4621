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

uniform float time;

#define WAVE_SPEED 5.
#define ROTA_SPEED 0.*10.*cos(time)
#define N_BRANCHES 10.
#define K_DIST time

void main( void ) {

	vec2 uv = 2.*vec2(worldPos.x / 10000, (worldPos.y - 1) / 10000);
	float dist = length(uv) + 1.;
	vec2 altuv = uv;
	
	altuv.x *= -sin(dist*time);
	
	float r = cos(atan(altuv.y, altuv.x)*N_BRANCHES+ROTA_SPEED + dist*K_DIST + 1.);
	float g = cos(atan(altuv.y, altuv.x)*N_BRANCHES+ROTA_SPEED + dist*K_DIST + 0.);
	float b = cos(atan(altuv.y, altuv.x)*N_BRANCHES+ROTA_SPEED + dist*K_DIST - 1.);
	
	vec3 color = vec3(r, g, b);
		
	gl_FragColor = vec4( color, 1.0 );

}