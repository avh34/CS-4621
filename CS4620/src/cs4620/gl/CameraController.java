package cs4620.gl;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Robot;
import java.awt.Toolkit;
import java.nio.IntBuffer;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Cursor;

import java.util.Iterator;
import java.util.Set;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

import cs4620.common.Scene;
import cs4620.common.event.SceneTransformationEvent;
import cs4620.scene.form.ScenePanel;
import cs4620.scene.form.SimpleMeshWindow;
import egl.math.Matrix4;
import egl.math.Vector3;

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
			int thisMouseX = Mouse.getX();
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
		if (orbitMode) {
			Vector3 rotCenter = new Vector3(0,0,0);
			transformation.clone().invert().mulPos(rotCenter);
			parentWorld.clone().invert().mulPos(rotCenter);
			mRot.mulBefore(Matrix4.createTranslation(rotCenter.clone().negate()));
			mRot.mulAfter(Matrix4.createTranslation(rotCenter));
		}
		transformation.mulBefore(mRot);
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
		float radius = (float)0.5;
		
		Matrix4 mTrans = Matrix4.createTranslation(motion);
		Matrix4 wouldBe = new Matrix4(transformation);
		wouldBe.mulBefore(mTrans);
		wouldBe.mulAfter(parentWorld);
		
		Vector3 camPos = new Vector3(wouldBe.getTrans());
		Boolean intersect = false;
		
		Iterator<String> itr = rEnv.meshes.keySet().iterator();
		
		while (itr.hasNext()){
			RenderMesh temp = rEnv.meshes.get(itr.next());
			
			if (temp!=null){
				//TODO: make this less messy?
				Vector3 currMax = new Vector3(temp.maxCoords);
				Vector3 currMin = new Vector3(temp.minCoords);
				Boolean in_left   = (currMin.x-camPos.x)<radius;
				Boolean in_right  = (currMax.x-camPos.x)>radius;
				Boolean in_front  = (currMax.z-camPos.z)>radius;
				Boolean in_back   = (currMin.z-camPos.z)<radius;
				Boolean in_top    = (currMax.y-camPos.y)>radius;
				Boolean in_bottom = (currMin.y-camPos.y)<radius;
				
				//top
				if (Math.abs(currMax.y-camPos.y)<radius && in_left && in_right && in_front && in_back)
					intersect = true;
				//bottom
				if (Math.abs(currMin.y-camPos.y)<radius && in_left && in_right && in_front && in_back)
					intersect = true;
				//left
				if (Math.abs(currMin.x-camPos.x)<radius && in_top && in_bottom && in_front && in_back)
					intersect = true;
				//right
				if (Math.abs(currMax.x-camPos.x)<radius && in_top && in_bottom && in_front && in_back) 
				    intersect = true;
				//front
				if (Math.abs(currMax.z-camPos.z)<radius && in_top && in_bottom && in_left && in_right)
				    intersect = true;
				//back
				if (Math.abs(currMin.z-camPos.z)<radius && in_top && in_bottom && in_left && in_right)
				    intersect = true;
				//TODO: intersect with actual object instead of just bounding box
			}
		}
		if (!intersect){
			transformation.mulBefore(mTrans);
			
		}
		// SOLUTION END
	}
	
}
