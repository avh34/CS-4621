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
	float shininess = 50;

	vec3 N = normalize(fN);
	vec3 V = normalize(worldCam - worldPos.xyz);

	vec4 finalColor = vec4(0.0, 0.0, 0.0, 0.0);
	vec4 color = vec4(0.0, 0.0, 0.0, 0.0);

	vec2 uv = 2.*vec2(worldPos.x / 5, (worldPos.y - .75) / 5);
	float dist = length(uv) + 1.;
	vec2 altuv = uv;
	
	altuv.x *= -sin(dist*time);
	
	float r = cos(atan(altuv.y, altuv.x)*N_BRANCHES+ROTA_SPEED + dist*K_DIST + 1.);
	float g = cos(atan(altuv.y, altuv.x)*N_BRANCHES+ROTA_SPEED + dist*K_DIST + 0.);
	float b = cos(atan(altuv.y, altuv.x)*N_BRANCHES+ROTA_SPEED + dist*K_DIST - 1.);
	
	color = vec4(r, g, b, 1.0);
	
	for (int i = 0; i < numLights; i++) {
	  float r = length(lightPosition[i] - worldPos.xyz);
	  vec3 L = normalize(lightPosition[i] - worldPos.xyz); 
	  vec3 H = normalize(L + V);

	  // calculate diffuse term
	  vec4 Idiff = getDiffuseColor(fUV) * max(dot(N, L), 0.0);
	  Idiff = clamp(Idiff, 0.0, 1.0);

	  // calculate specular term
	  vec4 Ispec = getSpecularColor(fUV) * pow(max(dot(N, H), 0.0), shininess);
	  Ispec = clamp(Ispec, 0.0, 1.0);

	  // calculate ambient term
	  vec4 Iamb = getDiffuseColor(fUV);
	  Iamb = clamp(Iamb, 0.0, 1.0);

	  
	  finalColor += color * (vec4(lightIntensity[i], 0.0) * (Idiff + Ispec) / (r*r) + vec4(ambientLightIntensity, 0.0) * Iamb);
	}
		
	gl_FragColor = finalColor;

}