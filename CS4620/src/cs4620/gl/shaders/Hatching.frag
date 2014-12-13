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
		color = alpha * texture2D(texDiffuse, uv) + beta * texture2D(texDiffuse1, uv);
	}
	else if (t == 1) {
		color = alpha * texture2D(texDiffuse1, uv) + beta * texture2D(texDiffuse2, uv);
	}
	else if (t == 2) {
		color = alpha * texture2D(texDiffuse2, uv) + beta * texture2D(texDiffuse3, uv);
	}
	else if (t == 3) {
		color = alpha * texture2D(texDiffuse3, uv) + beta * texture2D(texDiffuse4, uv);
	}
	else if (t == 4) {
		color = alpha * texture2D(texDiffuse4, uv);
	}
	return color;
}

void main() {
	vec3 N = normalize(fN);
	vec3 V = normalize(worldCam - worldPos.xyz);
	vec4 finalColor = vec4(0.0, 0.0, 0.0, 0.0);
	vec4 phongColor = vec4(0.0, 0.0, 0.0, 0.0);
	
	// Phong shading with gray as diffuse color
	vec4 gray = vec4(0.8, 0.8, 0.8, 1);
	float distConst = 1.0;
	for (int i = 0; i < numLights; i++) {
	  float r = length(lightPosition[i] - worldPos.xyz) / distConst;
	  vec3 L = normalize(lightPosition[i] - worldPos.xyz); 
	  vec3 H = normalize(L + V);

	  // calculate diffuse term
	  vec4 Idiff = gray * max(dot(N, L), 0.0);
	  Idiff = clamp(Idiff, 0.0, 1.0);

	  // calculate specular term
	  //vec4 Ispec = getSpecularColor(fUV) * pow(max(dot(N, H), 0.0), shininess);
	  //Ispec = clamp(Ispec, 0.0, 1.0);
	  vec4 Ispec = vec4(0.0);
	  
	  // calculate ambient term
	  vec4 Iamb = gray;
	  Iamb = clamp(Iamb, 0.0, 1.0);

	  phongColor += vec4(lightIntensity[i], 0.0) * (Idiff + Ispec) / (r*r) + vec4(ambientLightIntensity, 0.0) * Iamb;
	}
	
	//Y = 0.2126 R + 0.7152 G + 0.0722 B
	//Calculate luminance
	float luminance = exposure * (0.2126 * phongColor.x + 0.7152 * phongColor.y + 0.0722 * phongColor.z);
	luminance = min(max(luminance, 0.0), 1.0);
		
	// Shade using hatching  	
  	finalColor = getHatchingColor(luminance, fUV);

	gl_FragColor = finalColor;
	 
}


