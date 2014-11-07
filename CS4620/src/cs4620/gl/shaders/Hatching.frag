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


void main() {
	
	vec3 N = normalize(fN);
	vec3 V = normalize(worldCam - worldPos.xyz);
	vec4 finalColor = vec4(0.0, 0.0, 0.0, 0.0);
	
	int numTexture = 3;
	
	// Shade using hatching
	for (int i = 0; i < numLights; i++) {
		float r = length(lightPosition[i] - worldPos.xyz);
		vec3 L = normalize(lightPosition[i] - worldPos.xyz); 
		float dotNL = max(dot(N,L), 0);
	  
  		//Use texture t and t+1
		float t = floor(dotNL * (numTexture-1));
		float beta = (dotNL * (numTexture-1)) - t;
		float alpha = 1 - beta;
		
		if (t == 0) {
			finalColor += alpha * texture2D(texNormal, fUV) + beta * texture2D(texSpecular, fUV);
		}
		else {
			finalColor += alpha * texture2D(texSpecular, fUV) + beta * texture2D(texDiffuse, fUV);
		}
	}
	
	// Color edges black
	if (dot(N, V) < .3) {
		finalColor = vec4(0.0);
	}

	gl_FragColor = finalColor; 
}