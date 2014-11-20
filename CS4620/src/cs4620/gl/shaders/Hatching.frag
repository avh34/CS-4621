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

varying vec2 fUV;
varying vec3 fN; 
varying vec4 worldPos; 

vec4 getHatchingColor (float v, vec2 uv) {
	int numTexture = 5;
	vec4 color = vec4(0.0);
	
	// Decide which textures to use
	float t = floor(v * (numTexture-1));
	float beta = (v * (numTexture-1)) - t;
	float alpha = 1 - beta;
	
	// Blend textures t and t+1	
	if (t == 0) {
		color = alpha * texture2D(texDiffuse, fUV) + beta * texture2D(texDiffuse1, fUV);
	}
	else if (t == 1) {
		color = alpha * texture2D(texDiffuse1, fUV) + beta * texture2D(texDiffuse2, fUV);
	}
	else if (t == 2) {
		color = alpha * texture2D(texDiffuse2, fUV) + beta * texture2D(texDiffuse3, fUV);
	}
	else if (t == 3) {
		color = alpha * texture2D(texDiffuse3, fUV) + beta * texture2D(texDiffuse4, fUV);
	}
	return color;
}

void main() {
	vec3 N = normalize(fN);
	vec3 V = normalize(worldCam - worldPos.xyz);
	vec4 finalColor = vec4(0.0, 0.0, 0.0, 0.0);
	
	// Shade using hatching
	for (int i = 0; i < numLights; i++) {
		float r = length(lightPosition[i] - worldPos.xyz);
		vec3 L = normalize(lightPosition[i] - worldPos.xyz); 
		float dotNL = max(dot(N,L), 0);
	  	
  		finalColor += getHatchingColor(dotNL, fUV);	
	}
	
	// Color edges and creases black
	if (dot(N, V) < .3) {
		finalColor = vec4(0.0);
	}

	gl_FragColor = finalColor; 
}


