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
import cs4620.scene.ViewScreen;
import cs4620.scene.form.ScenePanel;
import cs4620.scene.form.SimpleMeshWindow;
import egl.math.Matrix4;
import egl.math.Vector3;
import egl.math.Vector3i;
import egl.math.Vector4;

public class CameraController {
	
	public boolean shader = false;
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
		//if(Keyboard.isKeyDown(Keyboard.KEY_SPACE)) { motion.add(0, 1, 0); }
		
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
			rotation.add(0, -0.03f * (thisMouseX - mouse_x), 0);
			rotation.add(0.03f * (mouse_y - thisMouseY), 0, 0);
			
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
		Matrix4 mRotx = Matrix4.createRotationX(rotation.x);
				
		Matrix4 mRoty = (Matrix4.createRotationY(rotation.y));
		Matrix4 mRot = mRotx.clone().mulAfter(mRoty);
		//mRot.mulAfter(Matrix4.createRotationZ(rotation.z));
		
//		if (orbitMode) {
//			Vector3 rotCenter = new Vector3(0,0,0);
//			transformation.clone().invert().mulPos(rotCenter);
//			parentWorld.clone().invert().mulPos(rotCenter);
//			mRot.mulBefore(Matrix4.createTranslation(rotCenter.clone().negate()));
//			mRot.mulAfter(Matrix4.createTranslation(rotCenter));
//		}
		
		Matrix4 wouldBe = new Matrix4(transformation);
		wouldBe.mulBefore(mRot);
		wouldBe.mulAfter(parentWorld);
		Vector3 camPos = new Vector3(wouldBe.getTrans());
		
			Matrix4 cameraTrans = Matrix4.createTranslation(camera.mWorldTransform.getTrans());
			Matrix4 cameraTransn = Matrix4.createTranslation(camera.mWorldTransform.getTrans().negate());
			mRoty.mulBefore(cameraTransn);
			mRoty.mulAfter((cameraTrans));
			transformation.mulAfter(mRoty);
			transformation.mulBefore(mRotx);
			float cosAngle = transformation.getRow(1).y;
			 float sinAngle = transformation.getRow(2).y;
			if(Math.atan2(cosAngle, sinAngle) < 0) 
				{  transformation.mulBefore(Matrix4.createRotationX(rotation.x).invert());}
	

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
		
		//Matrix4 wouldBe = new Matrix4(transformation);
		//wouldBe.mulBefore(mTrans);
		//wouldBe.mulAfter(parentWorld);
		
		parentWorld.mulDir(motion);
		Vector3 camPos = new Vector3(transformation.clone().mulAfter(parentWorld).getTrans()); //world coords of camera
		//Vector3 newMotion = getNewPosition(camPos, motion);
		
				
		if (getNewPosition(camPos, motion)){
			shader = false;
			float yNoTrans = transformation.clone().mulAfter(parentWorld).m[13];
			Matrix4 mTrans = Matrix4.createTranslation(motion);
			transformation.mulBefore(mTrans);
			transformation.m[13] = yNoTrans;
		}
		else{shader = true;}
		// SOLUTION END
	}
	
	private ArrayList<RenderObject> findPossColliders(Vector3 camPos, double radius){
		Iterator<SceneObject> itr = scene.objects.iterator();
		
		ArrayList<RenderObject> possCollisions = new ArrayList<RenderObject>();
		
		//collisions will contain minCoord, maxCoord, normal for each square with an intersection
		//ArrayList<ArrayList<Vector3>> collisions = new ArrayList<ArrayList<Vector3>>();
		
		while (itr.hasNext()){
			SceneObject sceneObj = itr.next();
			RenderObject temp = rEnv.findObject(sceneObj);
			Boolean objIntersect = false;

			
			if (temp.mesh!=null){
				//get min/max coords in world space
				//TODO: Check all 8 bounding coords rather than just two
				Vector3 meshMax = new Vector3(temp.mesh.maxCoords);
				Vector3 meshMin = new Vector3(temp.mesh.minCoords);
				temp.mWorldTransform.mulPos(meshMax);
				temp.mWorldTransform.mulPos(meshMin);
				
				Vector3 currMax = new Vector3();
				Vector3 currMin = new Vector3();
				
				currMax.x = (meshMax.x>meshMin.x)? meshMax.x : meshMin.x;
				currMin.x = (meshMax.x<meshMin.x)? meshMax.x : meshMin.x;
				
				currMax.y = (meshMax.y>meshMin.y)? meshMax.y : meshMin.y;
				currMin.y = (meshMax.y<meshMin.y)? meshMax.y : meshMin.y;
				
				currMax.z = (meshMax.z>meshMin.z)? meshMax.z : meshMin.z;
				currMin.z = (meshMax.z<meshMin.z)? meshMax.z : meshMin.z;

				Boolean in_left   = currMin.x<camPos.x;
				Boolean in_right  = currMax.x>camPos.x;
				Boolean in_front  = currMax.z>camPos.z;
				Boolean in_back   = currMin.z<camPos.z;
				Boolean in_top    = currMax.y>camPos.y;
				Boolean in_bottom = currMin.y<camPos.y;

				ArrayList<Boolean> collisions = new ArrayList<Boolean>();
				//top
				if (Math.abs(currMax.y-camPos.y)<radius && in_left && in_right && in_front && in_back){
					objIntersect = true; collisions.add(true);
					
				}else { collisions.add(false);}
				
				//bottom
				if (Math.abs(currMin.y-camPos.y)<radius && in_left && in_right && in_front && in_back){
					objIntersect = true; collisions.add(true);
				}else{ collisions.add(false);}
				
				//left
				if (Math.abs(currMin.x-camPos.x)<radius && in_top && in_bottom && in_front && in_back){
					objIntersect = true; collisions.add(true);
				}else{ collisions.add(false);}
				
				//right
				if (Math.abs(currMax.x-camPos.x)<radius && in_top && in_bottom && in_front && in_back){
				    objIntersect = true; collisions.add(true);
				}else{ collisions.add(false);}
				
				//front
				if (Math.abs(currMax.z-camPos.z)<radius && in_top && in_bottom && in_left && in_right){
				    objIntersect = true; collisions.add(true);
				}else{ collisions.add(false);}
				
				//back
				if (Math.abs(currMin.z-camPos.z)<radius && in_top && in_bottom && in_left && in_right){
				    objIntersect = true; collisions.add(true);
				}else{ collisions.add(false);}
				
				if (objIntersect){
//					for (int i=0; i<temp.indices.size(); i++){
//						Vector3i curInd = temp.indices.get(i);
//						Vector3 v1 = temp.vertices.get(curInd.x);
//						Vector3 v2 = temp.vertices.get(curInd.y);
//						Vector3 v3 = temp.vertices.get(curInd.z);
//						
//						
//					}
					ViewScreen.intersected = temp.mesh.sceneMesh.file;

					possCollisions.add(temp);
				}
			}
		}
		
		return possCollisions;
		
	}
	
	private boolean getNewPosition(Vector3 camPos, Vector3 velocity){
		float EPSILON = (float) 0.005;
		double radius = 1.0;
		
		if (velocity.len()<EPSILON){
			//velocity vector is smaller than our bound, so don't bother
			//return new Vector3(0);
			return true;
		}
		Vector3 newCam = new Vector3(camPos);
		newCam.add(velocity);
		ArrayList<RenderObject> possCollisions;
		float nearestDistance;
		possCollisions = findPossColliders(newCam, radius);
		
		if (possCollisions.size() == 0){
			//no collisions, so we can move the camera as expected
			//return velocity;
			return true;
		}else{
			return false;
		}
		
//		//move along velocity vector until we hit an object
//		Vector3 V = new Vector3(velocity);
//		V.normalize();
//		V.mul(nearestDistance-EPSILON);
//		Vector3 distToMove = new Vector3(V);
//		
//		//still want to travel velocity.len-nearestDistance, so slide along 
//		// plane of object we intersected with
//		V.normalize();
//		V.mul(velocity.len()-nearestDistance);
//		Vector3 destPoint = nearestIntersectionPoint.add(V);
//		
//		//calculate a point on the sliding plane and it's normal
//		Vector3 slideOrigin = new Vector3();//nearestIntersectionPoint);
//		Vector3 slideNormal = new Vector3();//nearestIntersectionPoint);
//		slideNormal.sub(camPos);
//		
//		//project destination point onto sliding plane
//		float t = (float) intersect(slideOrigin, slideNormal.normalize(), destPoint, slideNormal.normalize());
//		slideNormal.mul(t);
//		
//		Vector3 destinationProj = new Vector3(destPoint);
//		destinationProj.add(slideNormal);
//		
//		Vector3 newVelocity = destinationProj.sub(nearestIntersectionPoint);
//		Vector3 additionalDist = getNewPosition(camPos, newVelocity);
//		
//		return distToMove.add(additionalDist);
	}
	//takes in a point on a plane (pO), the plane's normal (pN), a ray origin (rO),
	// and the ray direction vector (rV). Calculates the t value where the given 
	// ray intersects the given plane
	private double intersect(Vector3 pO, Vector3 pN, Vector3 rO, Vector3 rV){
		Vector3 num = pO.clone().sub(rO);
		return num.dot(pN)/rV.dot(pN);
	}
}
