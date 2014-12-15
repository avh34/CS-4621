// vertex to fragment shader io
varying vec3 fN;
varying vec4 worldPos;

uniform vec3 worldCam;
uniform vec3 ambientLightIntensity;


// globals

// entry point
void main()
{
	float Intensity = 1;
	float eFallOff = 2;
	
	vec3 N = normalize(fN);
	vec3 V = normalize(worldCam - worldPos.xyz);
	
	vec4 finalColor = vec4(0.8, 0.8, 1.0, 1.0);
	
    float o = dot(normalize(fN), normalize(V));
    o = abs(o);
    o = ambientLightIntensity + Intensity*(1.0 - pow(o, eFallOff));

    gl_FragColor = vec4(o*finalColor.x, o*finalColor.y, o*finalColor.z, o);
}