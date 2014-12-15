#version 120

// RenderCamera Input
uniform mat4 mViewProjection;

// RenderObject Input
uniform mat4 mWorld;
uniform mat3 mWorldIT;

// RenderMesh Input
attribute vec4 vPosition; // Sem (POSITION 0)
attribute vec3 vNormal; // Sem (NORMAL 0)
attribute vec2 vUV; // Sem (TEXCOORD 0)

varying vec3 fN; 
varying vec4 worldPos; 

void main() {
  	worldPos = mWorld * vPosition;
	fN = normalize((mWorldIT * vNormal).xyz);
	gl_Position = mViewProjection * worldPos;
}