package cs4620.gl;


import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import org.lwjgl.BufferUtils;

import cs4620.common.Mesh;
import cs4620.mesh.MeshData;
import egl.ArrayBind;
import egl.GL.BufferTarget;
import egl.GL.BufferUsageHint;
import egl.GL.GLType;
import egl.GLBuffer;
import egl.IDisposable;
import egl.NativeMem;
import egl.Semantic;
import egl.math.Vector3;
import egl.math.Vector3i;
import egl.math.Vector4;

public class RenderMesh implements IDisposable {
	private static final int VERTEX_SIZE = 8 * 4;
	public static final ArrayBind[] VERTEX_DECLARATION = {
		new ArrayBind(Semantic.Position, GLType.Float, 3, 0),
		new ArrayBind(Semantic.Normal, GLType.Float, 3, 3 * 4),
		new ArrayBind(Semantic.TexCoord, GLType.Float, 2, 6 * 4)
	};
	
	public float thickness = 0.15f;
	public RenderMesh silhouette;
	
	public final GLBuffer vBuffer = new GLBuffer(BufferTarget.ArrayBuffer, BufferUsageHint.StaticDraw, false);
	public final GLBuffer iBuffer = new GLBuffer(BufferTarget.ElementArrayBuffer, BufferUsageHint.StaticDraw, false);
	
	public int indexCount;
	
	public final Mesh sceneMesh;
	
	public Vector3 minCoords;
	public Vector3 maxCoords;
	
	public ArrayList<Vector3> vertices;
	public ArrayList<Vector3i> indices;
	
	public RenderMesh(Mesh m) {
		sceneMesh = m;
	}
	@Override
	public void dispose() {
		vBuffer.dispose();
		iBuffer.dispose();
	}
	
	public void build (MeshData data) {
		buildObject(data);
		buildSilhouette(data);
	}
	
	public void buildSilhouette (MeshData data) {
		silhouette = new RenderMesh(sceneMesh);
		MeshData silData = new MeshData();

		silData.vertexCount = data.vertexCount;
		silData.indexCount = data.indexCount;
		silData.maxCoords = data.maxCoords.clone().mul(thickness);
		silData.minCoords = data.minCoords.clone().mul(thickness);
		data.positions.position(0);
		data.indices.position(0);
		data.normals.position(0);
		//data.uvs.position(0);
		
		FloatBuffer pb = BufferUtils.createFloatBuffer(silData.vertexCount * 3);
		FloatBuffer nb = BufferUtils.createFloatBuffer(silData.vertexCount * 3);
		IntBuffer ib = BufferUtils.createIntBuffer(silData.indexCount);
		FloatBuffer uvb = BufferUtils.createFloatBuffer(silData.vertexCount * 2);
		
		HashMap<Vector3,Vector3> map = new HashMap<Vector3,Vector3>();
		
		int currentIndex = 0;
		while(data.normals.hasRemaining()) {
			float normalX = data.normals.get();
			float normalY = data.normals.get();
			float normalZ = data.normals.get();
			nb.put(normalX * -1f);
			nb.put(normalY * -1f);
			nb.put(normalZ * -1f);
			
			Vector3 newDisplace = new Vector3(normalX, normalY, normalZ);
			Vector3 currentVertex = vertices.get(currentIndex);
			if(map.containsKey(currentVertex)) {
				Vector3 oldDisplace = map.get(currentVertex);
				map.put(currentVertex, newDisplace.add(oldDisplace));
			}
			else {
				map.put(currentVertex, newDisplace);
			}
			currentIndex++;
		}
		
		while(data.positions.hasRemaining()) {
			float x = data.positions.get();
			float y = data.positions.get();
			float z = data.positions.get();
			
			Vector3 displace = map.get(new Vector3(x,y,z));
			displace.normalize();
			pb.put(x + thickness * displace.x);
			pb.put(y + thickness * displace.y);
			pb.put(z + thickness * displace.z);
		}
		
		while(data.indices.hasRemaining()) {
			int vt1 = data.indices.get();
			int vt2 = data.indices.get();
			int vt3 = data.indices.get();
			
			ib.put(vt1);
			ib.put(vt3);
			ib.put(vt2);	
			
			/*
			float u0 = data.uvs.get();
			float v0 = data.uvs.get();
			float u1 = data.uvs.get();
			float v1 = data.uvs.get();
			float u2 = data.uvs.get();
			float v2 = data.uvs.get();
			uvb.put(u0);
			uvb.put(v0);
			uvb.put(u2);
			uvb.put(v2);
			uvb.put(u1);
			uvb.put(v1);
			*/
		}
		
		silData.positions = pb;
		silData.normals = nb;
		silData.indices = ib;
		silData.uvs = data.uvs;
		silhouette.buildObject(silData);
	}
	
	public void buildObject (MeshData data) {
		
		minCoords = data.minCoords;
		maxCoords = data.maxCoords;
		vertices = new ArrayList<Vector3>();
		indices  = new ArrayList<Vector3i>();
		
		// Interlace The Data
		ByteBuffer bb = NativeMem.createByteBuffer(data.vertexCount * VERTEX_SIZE);
		data.positions.position(0);
		data.positions.limit(data.vertexCount * 3);
		data.normals.position(0);
		data.normals.limit(data.vertexCount * 3);
		data.uvs.position(0);
		data.uvs.limit(data.vertexCount * 2);
		for(int i = 0;i < data.vertexCount;i++) {
			float x = data.positions.get();
			float y = data.positions.get();
			float z = data.positions.get();
			vertices.add(new Vector3(x,y,z));
			//bb.putFloat(data.positions.get());
			//bb.putFloat(data.positions.get());
			//bb.putFloat(data.positions.get());
			bb.putFloat(x);
			bb.putFloat(y);
			bb.putFloat(z);
			
			bb.putFloat(data.normals.get());
			bb.putFloat(data.normals.get());
			bb.putFloat(data.normals.get());
			bb.putFloat(data.uvs.get());
			bb.putFloat(data.uvs.get());
		}
		bb.flip();
		
		// Send Data To GPU
		vBuffer.init();
		vBuffer.setAsVertex(VERTEX_SIZE);
		vBuffer.setDataInitial(bb);
		
		iBuffer.init();
		iBuffer.setAsIndexInt();
		data.indices.position(0);
		data.indices.limit(data.indexCount);
		iBuffer.setDataInitial(data.indices);
		for (int i=0; i<data.indexCount; i=i+3){
			indices.add(new Vector3i(data.indices.get(i),data.indices.get(i+1),data.indices.get(i+2))); 
		}
		indexCount = data.indexCount;
	}
	
	/*private void buildGLBuffer () {
		vBuffer.dispose();
		iBuffer.dispose();
		ByteBuffer bb = NativeMem.createByteBuffer(vertices.size() * VERTEX_SIZE);
		
		for(int i = 0; i < vertices.size(); i++) {
			float x = vertices.get(i).x;
			float y = vertices.get(i).x;
			float z = data.positions.get();
			vertices.add(new Vector3(x,y,z));
			//bb.putFloat(data.positions.get());
			//bb.putFloat(data.positions.get());
			//bb.putFloat(data.positions.get());
			bb.putFloat();
			bb.putFloat(y);
			bb.putFloat(z);
			
			bb.putFloat(data.normals.get());
			bb.putFloat(data.normals.get());
			bb.putFloat(data.normals.get());
			bb.putFloat(data.uvs.get());
			bb.putFloat(data.uvs.get());
		}
		bb.flip();
		
		// Send Data To GPU
		vBuffer.init();
		vBuffer.setAsVertex(VERTEX_SIZE);
		vBuffer.setDataInitial(bb);
		
		iBuffer.init();
		iBuffer.setAsIndexInt();
		data.indices.position(0);
		data.indices.limit(data.indexCount);
		iBuffer.setDataInitial(data.indices);
		for (int i=0; i<data.indexCount; i=i+3){
			indices.add(new Vector3i(data.indices.get(i),data.indices.get(i+1),data.indices.get(i+2))); 
		}
		indexCount = data.indexCount;
	
	}*/

}
