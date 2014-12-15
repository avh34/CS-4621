package cs4620.gl;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Robot;
import java.awt.Toolkit;
import java.nio.IntBuffer;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Cursor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

import cs4620.common.Scene;
import cs4620.common.SceneObject;
import cs4620.common.event.SceneTransformationEvent;
import cs4620.scene.form.ScenePanel;
import cs4620.scene.form.SimpleMeshWindow;
import egl.math.Matrix4;
import egl.math.Vector3;
import egl.math.Vector3i;
import egl.math.Vector4;

public class CameraController {
	
	public boolean ishighlighted = true;
	protected final Scene scene;
	public RenderCamera camera;
	protected final RenderEnvironment rEnv;

	protected boolean prevFrameButtonDown = false;
	protected int prevMouseX, prevMouseY;
	
	protected boolean orbitMode = false;
	
	//get mouse position in relation to window. 
	protected float mouse_y =Display.getDisplayMode().getHeight()/2;		 
	protected float mouse_x = Display.getDisplayMode().getWidth()/2;
	protected boolean window = false;
	Robot mouseMover;
	
	public CameraController(Scene s, RenderEnvironment re, RenderCamera c) {
		scene = s;
		rEnv = re;
		camera = c;
	}
	
	public void isHighlighted(){
		 ishighlighted = !ishighlighted;
}
	public void changeWindow(){
		window = false;
	}	
	
	
	/**
	 * Update the camera's transformation matrix in response to user input.
	 * 
	 * Pairs of keys are available to translate the camera in three direction oriented to the camera,
	 * and to rotate around three axes oriented to the camera.  Mouse input can also be used to rotate 
	 * the camera around the horizontal and vertical axes.  All effects of these controls are achieved
	 * by altering the transformation stored in the SceneCamera that is referenced by the RenderCamera
	 * this controller is associated with.
	 * 
	 * @param et  time elapsed since previous frame
	 * @throws AWTException 
	 */

	public void update(double et) {		
		Robot mouseMover;
		
		Vector3 motion = new Vector3();
		Vector3 rotation = new Vector3();
		
		if(Keyboard.isKeyDown(Keyboard.KEY_DOWN)) { rotation.add(-1, 0, 0); }
		if(Keyboard.isKeyDown(Keyboard.KEY_UP)) { rotation.add(1, 0, 0); }
		if(Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) { rotation.add(0, -1, 0); }
		if(Keyboard.isKeyDown(Keyboard.KEY_LEFT)) { rotation.add(0, 1, 0); }
		
		if(Keyboard.isKeyDown(Keyboard.KEY_W)) { motion.add(0, 0, -1); }
		if(Keyboard.isKeyDown(Keyboard.KEY_S)) { motion.add(0, 0, 1); }
		if(Keyboard.isKeyDown(Keyboard.KEY_A)) { motion.add(-1, 0, 0); }
		if(Keyboard.isKeyDown(Keyboard.KEY_D)) { motion.add(1, 0, 0); }
		if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) { motion.add(0, -1, 0); }
		if(Keyboard.isKeyDown(Keyboard.KEY_SPACE)) { motion.add(0, 1, 0); }
		
		//check if correct window is brought to front- otherwise mouse position is being calculated from 
		//relation to options window- want center of screen in relation to screen.
		
		if(!window){
			  mouse_y =Display.getDisplayMode().getHeight()/2;		 
			  mouse_x = Display.getDisplayMode().getWidth()/2;
			  window = true;
		}
	
		if(ishighlighted){
		try {
			int m= Cursor.getMinCursorSize();
			try {
				Mouse.setNativeCursor(new Cursor(m, m, m/2,m/2, 1, IntBuffer.allocate(m*m), null));
			} catch (LWJGLException e) {
				e.printStackTrace();
		}
			mouseMover = new Robot();
			Dimension screen_size = Toolkit.getDefaultToolkit().getScreenSize();
			//Current Mouse Positions
			int thisMouseX = (int) (int) java.awt.MouseInfo.getPointerInfo().getLocation().getX() - Display.getX();
			int thisMouseY = (int) java.awt.MouseInfo.getPointerInfo().getLocation().getY() - Display.getY();
						

			//Center of display
			float centery = Display.getY()  + Display.getHeight()/ 2;
			float centerx = Display.getX() + Display.getWidth()/ 2;
			rotation.add(0, -0.05f * (thisMouseX - mouse_x), 0);
			rotation.add(0.05f * (mouse_y - thisMouseY), 0, 0);
			
			//return mouse to center of display
			 mouseMover.mouseMove((int) centerx, (int) centery);

		
	} catch (AWTException e) {
		e.printStackTrace();
	}}
		
		RenderObject parent = rEnv.findObject(scene.objects.get(camera.sceneObject.parent));
		Matrix4 pMat = parent == null ? new Matrix4() : parent.mWorldTransform;
		if(motion.lenSq() > 0.01) {
			motion.normalize();
			motion.mul(5 * (float)et);
			translate(pMat, camera.sceneObject.transformation, motion);
		}
		if(rotation.lenSq() > 0.01) {
			rotation.mul((float)(100.0 * et));
			rotate(pMat, camera.sceneObject.transformation, rotation);
		}
		scene.sendEvent(new SceneTransformationEvent(camera.sceneObject));
	}

	/**
	 * Apply a rotation to the camera.
	 * 
	 * Rotate the camera about one ore more of its local axes, by modifying <b>transformation</b>.  The 
	 * camera is rotated by rotation.x about its horizontal axis, by rotation.y about its vertical axis, 
	 * and by rotation.z around its view direction.  The rotation is about the camera's viewpoint, if 
	 * this.orbitMode is false, or about the world origin, if this.orbitMode is true.
	 * 
	 * @param parentWorld  The frame-to-world matrix of the camera's parent
	 * @param transformation  The camera's transformation matrix (in/out parameter)
	 * @param rotation  The rotation in degrees, as Euler angles (rotation angles about x, y, z axes)
	 */
	protected void rotate(Matrix4 parentWorld, Matrix4 transformation, Vector3 rotation) {
		// TODO#A3 SOLUTION START
		
		rotation = rotation.clone().mul((float)(Math.PI / 180.0));
		Matrix4 mRot = Matrix4.createRotationX(rotation.x);
		mRot.mulAfter(Matrix4.createRotationY(rotation.y));
		mRot.mulAfter(Matrix4.createRotationZ(rotation.z));

		if (orbitMode) {
			Vector3 rotCenter = new Vector3(0,0,0);
			transformation.clone().invert().mulPos(rotCenter);
			parentWorld.clone().invert().mulPos(rotCenter);
			mRot.mulBefore(Matrix4.createTranslation(rotCenter.clone().negate()));
			mRot.mulAfter(Matrix4.createTranslation(rotCenter));
		}
		
		transformation.mulBefore(mRot);
		
		// SOLUTION END
	}

	
	/**
	 * Apply a translation to the camera.
	 * 
	 * Translate the camera by an offset measured in camera space, by modifying <b>transformation</b>.
	 * @param parentWorld  The frame-to-world matrix of the camera's parent
	 * @param transformation  The camera's transformation matrix (in/out parameter)
	 * @param motion  The translation in camera-space units
	 */
	protected void translate(Matrix4 parentWorld, Matrix4 transformation, Vector3 motion) {
		// TODO#A3 SOLUTION START
		
		//parentWorld.mulDir(motion);
		Matrix4 cam2World = new Matrix4(transformation).mulAfter(parentWorld);
		Vector3 oldCam = new Vector3(cam2World.getTrans()); //world coords of camera
		Vector3 camPos = oldCam.clone();
		cam2World.mulDir(motion);
		//call with motion and camPos in world space
		getNewPosition(camPos, motion, 0);
		
		//calculate motion camera should have in world space
		Vector3 newMotion = camPos.clone().sub(oldCam);
		
		//change newMotion back into camera space
		cam2World.clone().invert().mulDir(newMotion);
		
		float yNoTrans = cam2World.m[13];
		Matrix4 mTrans = Matrix4.createTranslation(newMotion);
		transformation.mulBefore(mTrans);
		transformation.m[13] = yNoTrans;
		// SOLUTION END
	}
	
	private ArrayList<ArrayList<Vector3>> findPossColliders(Vector3 camPos, double radius){
		Iterator<SceneObject> itr = scene.objects.iterator();
		
		//collisions will contain minCoord, maxCoord, normal for each square with an intersection
		ArrayList<ArrayList<Vector3>> collisions = new ArrayList<ArrayList<Vector3>>();
		
		while (itr.hasNext()){
			SceneObject sceneObj = itr.next();
			RenderObject temp = rEnv.findObject(sceneObj);

			if (temp.mesh!=null){
				//get min/max coords in world space
				Vector3 meshMax = new Vector3(temp.mesh.maxCoords);
				Vector3 meshMin = new Vector3(temp.mesh.minCoords);
				
				ArrayList<Vector3> boundingPoints = new ArrayList<Vector3>();
				boundingPoints.add(new Vector3(meshMin.x, meshMin.y, meshMin.z));
				boundingPoints.add(new Vector3(meshMin.x, meshMax.y, meshMin.z));
				boundingPoints.add(new Vector3(meshMax.x, meshMax.y, meshMin.z));
				boundingPoints.add(new Vector3(meshMax.x, meshMin.y, meshMin.z));
				boundingPoints.add(new Vector3(meshMin.x, meshMin.y, meshMax.z));
				boundingPoints.add(new Vector3(meshMin.x, meshMax.y, meshMax.z));
				boundingPoints.add(new Vector3(meshMax.x, meshMax.y, meshMax.z));
				boundingPoints.add(new Vector3(meshMax.x, meshMin.y, meshMax.z));
				
				Vector3 currMax = new Vector3(Float.NEGATIVE_INFINITY);
				Vector3 currMin = new Vector3(Float.POSITIVE_INFINITY);
				 
				for (int i=0; i<boundingPoints.size(); i+=1){
					Vector3 curPoint = boundingPoints.get(i);
				    temp.mWorldTransform.mulPos(curPoint);
				    if (curPoint.x>currMax.x) currMax.x = curPoint.x;
				    if (curPoint.y>currMax.y) currMax.y = curPoint.y;
				    if (curPoint.z>currMax.z) currMax.z = curPoint.z;
				    
				    if (curPoint.x<currMin.x) currMin.x = curPoint.x;
				    if (curPoint.y<currMin.y) currMin.y = curPoint.y;
				    if (curPoint.z<currMin.z) currMin.z = curPoint.z;
				}

				//check that the camera is within the listed planes of bounding box
				Boolean in_left   = currMin.x<camPos.x;
				Boolean in_right  = currMax.x>camPos.x;
				Boolean in_front  = currMax.z>camPos.z;
				Boolean in_back   = currMin.z<camPos.z;
				Boolean in_top    = currMax.y>camPos.y;
				Boolean in_bottom = currMin.y<camPos.y;
				
//				//top
//				if (Math.abs(currMax.y-camPos.y)<radius && in_left && in_right && in_front && in_back){
//					ArrayList<Vector3> tempVecs = new ArrayList<Vector3>();
//					tempVecs.add(new Vector3(currMin.x, currMax.y, currMin.z)); //minCoord
//					tempVecs.add(new Vector3(currMax.x, currMax.y, currMax.z)); //maxCoord
//					tempVecs.add(new Vector3(0,1,0)); //normal
//					collisions.add(tempVecs);
//				}
//				
//				//bottom
//				if (Math.abs(currMin.y-camPos.y)<radius && in_left && in_right && in_front && in_back){
//					ArrayList<Vector3> tempVecs = new ArrayList<Vector3>();
//					tempVecs.add(new Vector3(currMin.x, currMin.y, currMin.z)); //minCoord
//					tempVecs.add(new Vector3(currMax.x, currMin.y, currMax.z)); //maxCoord
//					tempVecs.add(new Vector3(0, -1, 0)); //normal
//					collisions.add(tempVecs);
//				}
				
				//left
				if (Math.abs(currMin.x-camPos.x)<radius && in_front && in_back){ //in_top && in_bottom && in_front && in_back){
					ArrayList<Vector3> tempVecs = new ArrayList<Vector3>();
					tempVecs.add(new Vector3(currMin.x, currMin.y, currMin.z)); //minCoord
					tempVecs.add(new Vector3(currMin.x, currMax.y, currMax.z)); //maxCoord
					tempVecs.add(new Vector3(-1, 0, 0));//normal
					collisions.add(tempVecs);
				}
				
				//right
				if (Math.abs(currMax.x-camPos.x)<radius && in_front && in_back){//in_top && in_bottom && in_front && in_back){
					ArrayList<Vector3> tempVecs = new ArrayList<Vector3>();
					tempVecs.add(new Vector3(currMax.x, currMin.y, currMin.z)); //minCoord
				    tempVecs.add(new Vector3(currMax.x, currMax.y, currMax.z)); //maxCoord
				    tempVecs.add(new Vector3(1,0,0));
				    collisions.add(tempVecs);
				}
				
				//front
				if (Math.abs(currMax.z-camPos.z)<radius && in_left && in_right){//in_top && in_bottom && in_left && in_right){
					ArrayList<Vector3> tempVecs = new ArrayList<Vector3>();
					tempVecs.add(new Vector3(currMin.x, currMin.y, currMax.z)); //minCoord
				    tempVecs.add(new Vector3(currMax.x, currMax.y, currMax.z)); //maxCoord
				    tempVecs.add(new Vector3(0,0,1)); //normal
				    collisions.add(tempVecs);
				}
				
				//back
				if (Math.abs(currMin.z-camPos.z)<radius && in_left && in_right){//in_top && in_bottom && in_left && in_right){
					ArrayList<Vector3> tempVecs = new ArrayList<Vector3>();
					tempVecs.add(new Vector3(currMin.x, currMin.y, currMin.z)); //minCoord
				    tempVecs.add(new Vector3(currMax.x, currMax.y, currMin.z)); //maxCoord
				    tempVecs.add(new Vector3(0,0,-1)); //normal
				    collisions.add(tempVecs);
				}
			}
		}
		
		return collisions;
		
	}
	
	private void getNewPosition(Vector3 camPos, Vector3 velocity, int depth){
		if (depth>5) return; //reached maximum recursion depth
		float EPSILON = (float) 0.005;
		double radius = 2;
		
		if (velocity.len()<EPSILON){
			//velocity vector is smaller than our bound, so don't bother
			return;
		}
		//newCam holds the would be position of camera, then find possible collisions with that position
		Vector3 newCam = new Vector3(camPos);
		newCam.add(velocity);
		ArrayList<ArrayList<Vector3>> collisions = findPossColliders(newCam, radius);
		
		if (collisions.size() == 0){
			//no collisions, so we can move the camera as expected
			camPos.add(velocity);
			return;
		}
		
		float nearestDistance = Float.POSITIVE_INFINITY;
		Vector3 nearestIntersectionPoint = new Vector3();
		Vector3 nearestPolygonIntersectionPoint = new Vector3();
		camPos.y = 0;
		velocity.y = 0;
		for(int i = 0; i < collisions.size(); i+=1){
			ArrayList<Vector3> currCollider = collisions.get(i);
			Vector3 currMin = currCollider.get(0).clone();
			Vector3 currMax = currCollider.get(1).clone();
			Vector3 currNorm= currCollider.get(2).clone();
			currMin.y = 0; currMax.y = 0; currNorm.y = 0;
			
			float pDist = (float) intersect(currMin, currNorm, camPos, currNorm.clone().negate());
			Vector3 sphereIntersectionPoint = new Vector3();
			Vector3 planeIntersectionPoint = new Vector3();
			pDist = Math.abs(pDist);
			if (pDist<=radius){
				//plane is embedded in sphere
				//planeIntersectionPoint = camPos - currNorm with length set to pDist
				planeIntersectionPoint.set(currNorm.clone().negate().normalize().mul(pDist).add(camPos));
			}else{
				sphereIntersectionPoint.set(camPos.clone().sub(currNorm));
				
				Vector3 rV = velocity.clone();
				rV.normalize();
				
				float t = (float)intersect(currMax, currNorm, sphereIntersectionPoint, rV);
				if (t < 0){
					t = -t;continue; //we are moving away from the object, so don't worry about it
				}
				Vector3 V = velocity.clone().normalize().mul(t);
				planeIntersectionPoint = sphereIntersectionPoint.clone().add(V);
			}
			
			Vector3 polygonIntersectionPoint = new Vector3(planeIntersectionPoint);
			
			//clip the polygonIntersectionPoint so it is within the current polygon
			polygonIntersectionPoint.set(Math.max(polygonIntersectionPoint.x, currMin.x), 
										Math.max(polygonIntersectionPoint.y, currMin.y), 
										Math.max(polygonIntersectionPoint.z, currMin.z));
			
			polygonIntersectionPoint.set(Math.min(polygonIntersectionPoint.x, currMax.x), 
										Math.min(polygonIntersectionPoint.y, currMax.y), 
										Math.min(polygonIntersectionPoint.z, currMax.z));
			
			float t = (float) intersectSphere(polygonIntersectionPoint, velocity.clone().negate(), camPos, radius);
			if (t>=0 && t<=velocity.len()){
				//there was an intersection, so find where it was
				if (t<nearestDistance){
					nearestDistance = t;
					nearestIntersectionPoint = polygonIntersectionPoint.add(velocity.clone().negate().normalize().mul(t));
					nearestPolygonIntersectionPoint.set(polygonIntersectionPoint);
				}
				
			}
		}
		
		
		if (Float.isInfinite(nearestDistance)){
			//only happens if there were no collisions, so return
			camPos.add(velocity);
			return;
		}
		//move along velocity vector until we hit an object
		Vector3 V = new Vector3(velocity);
		V.normalize();
		V.mul(nearestDistance-EPSILON);
		camPos.add(V);
		
		//still want to travel velocity.len-nearestDistance, so slide along 
		// plane of object we intersected with
		V.normalize();
		V.mul(velocity.len()-nearestDistance);
		Vector3 destPoint = nearestPolygonIntersectionPoint.clone().add(V);
		
		//calculate a point on the sliding plane and it's normal
		Vector3 slideOrigin = new Vector3(nearestPolygonIntersectionPoint);
		Vector3 slideNormal = new Vector3(nearestPolygonIntersectionPoint);
		slideNormal.sub(camPos);
		
		//project destination point onto sliding plane
		float t = (float) intersect(slideOrigin, slideNormal.normalize(), destPoint, slideNormal.normalize());
		slideNormal.mul(t);
		
		Vector3 destinationProj = new Vector3(destPoint);
		destinationProj.add(slideNormal);
		
		Vector3 newVelocity = destinationProj.clone().sub(nearestPolygonIntersectionPoint);
		
		getNewPosition(camPos, newVelocity, depth+1);
		
		return;
	}
	//takes in a point on a plane (pO), the plane's normal (pN), a ray origin (rO),
	// and the ray direction vector (rV). Calculates the t value where the given 
	// ray intersects the given plane
	private double intersect(Vector3 pO, Vector3 pN, Vector3 rO, Vector3 rV){
		Vector3 pNnorm = pN.clone().normalize();
		Vector3 rVnorm = rV.clone().normalize();
//		double d = -(pNnorm.dot(pO));
//		double numer = pNnorm.dot(rO) + d;
//		double denom = pNnorm.dot(rVnorm);
//		return -(numer/denom);
		
		Vector3 num = pO.clone().sub(rO);
		double top = num.dot(pNnorm);
		Vector3 den = rVnorm.sub(rO);
		double bottom = den.dot(pNnorm);
		return top/bottom;
	}
	//rO = ray origin, rV = ray vector, sO = sphere origin, sR = sphere radius
	private double intersectSphere(Vector3 rO, Vector3 rV, Vector3 sO, double sR){
//		Vector3 Q = rO.clone().sub(sO);
//		double disc = (rV.dot(Q)*rV.dot(Q)) - (rV.dot(rV)*(Q.dot(Q)-sR*sR));
//		if (disc< 0) return -1;
//		
//		double a = -(rV.dot(Q));
//		return (a - Math.sqrt(disc))/(rV.dot(rV));
		
		Vector3 Q = sO.clone().sub(rO);
		double c = Q.len();
		double v = Q.dot(rV.clone().normalize());
		double d = sR*sR - (c*c-v*v);
		if (d<0) return -1;
		double t1 = v-Math.sqrt(d); double t2 = v+Math.sqrt(d);
		if (t1>0 && t2>0){ return Math.min(t1, t2);}
		else if (t1>0 && t2<0) return t1;
		else if (t1<0 && t2>0) return t2;
		else return Math.max(t1, t2);
		//return Math.min(v-Math.sqrt(d), v+Math.sqrt(d));
		
	}
}
