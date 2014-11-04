#version 120

// Lighting Information
const int MAX_LIGHTS = 16;
uniform int numLights;
uniform vec3 lightIntensity[MAX_LIGHTS];
uniform vec3 lightPosition[MAX_LIGHTS];
uniform vec3 ambientLightIntensity;

// Camera Information
uniform vec3 worldCam;
uniform float exposure;

// Shading Information (0 : smooth, 1 : rough)
uniform float roughness;

varying vec2 fUV;
varying vec3 fN;       // Normal at the vertex
varying vec4 worldPos; // Vertex position in world coordinates

void main()
{
   // Interpolating normals will change the length of the normal, so renormalize the normal.
	vec3 N = normalize(fN);	
	vec3 V = normalize(worldCam - worldPos.xyz);

	// Vector to hold our final result
	vec4 finalColor = vec4(0.0, 0.0, 0.0, 0.0);

    // Loop through each light to calculate the correct shading
	for (int i = 0; i < numLights; i++) {

		// Distance to the light source
		float R = length(lightPosition[i] - worldPos.xyz);

		// Light direction and half vector
		vec3 L = normalize(lightPosition[i] - worldPos.xyz); 
		vec3 H = normalize(L + V);

		// Fresnel term (note that F_0 is given as 0.04)
		float F = 0.04 + (1 - 0.04) * pow((1 - dot(V, H)), 5);

		// Microfacet distribution calculation
		float NH2    = dot(N, H)*dot(N, H);
		float M2     = roughness * roughness;
		float mFacet = exp((NH2 - 1)/(M2*NH2))/(M2*NH2*NH2);

		// Geometric attenutation term
		float G  = min(1, min(2*dot(N, H)*dot(N, V)/dot(V, H), 2*dot(N, H)*dot(N, L)/dot(V, H)));
	  
		// Cook-Torrance shading computation
		vec4 color  = getSpecularColor(fUV)*(F/3.14159)*(mFacet*G)/(dot(N, V)*dot(N, L)) + getDiffuseColor(fUV);
		     color *= max(dot(N, L), 0)*vec4(lightIntensity[i], 0.0)/(R*R);
		     color += getDiffuseColor(fUV)*vec4(ambientLightIntensity, 0.0);

		// Sum the calculated shadings from all light sources
		finalColor += color;
	}

	gl_FragColor = finalColor * exposure; 

}