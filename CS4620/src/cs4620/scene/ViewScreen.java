package cs4620.scene;

import java.awt.AWTException;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.IntBuffer;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Cursor;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;

import blister.GameScreen;
import blister.GameTime;
import blister.ScreenState;
import blister.MainGame.WindowResizeArgs;
import blister.input.KeyboardEventDispatcher;
import blister.input.KeyboardKeyEventArgs;
import cs4620.common.Material;
import cs4620.common.Scene;
import cs4620.common.Scene.NameBindMaterial;
import cs4620.common.SceneObject;
import cs4620.common.UniqueContainer;
import cs4620.common.event.SceneDataType;
import cs4620.common.event.SceneEvent;
import cs4620.common.event.SceneObjectResourceEvent;
import cs4620.common.event.SceneReloadEvent;
import cs4620.gl.CameraController;
import cs4620.gl.GridRenderer;
import cs4620.gl.RenderCamera;
import cs4620.gl.RenderController;
import cs4620.gl.Renderer;
import cs4620.gl.manip.ManipController;
import cs4620.scene.form.RPMaterialData;
import cs4620.scene.form.RPMeshData;
import cs4620.scene.form.RPTextureData;
import cs4620.scene.form.ScenePanel;
import egl.GLError;
import egl.math.Vector2;
import egl.math.Vector3;
import ext.csharp.ACEventFunc;
import ext.java.Parser;


public class ViewScreen extends GameScreen {
	Renderer renderer = new Renderer();
	int cameraIndex = 0;
	boolean pick;
	int prevCamScroll = 0;
	boolean wasPickPressedLast = false;
	boolean showGrid = false;
	
	
	SceneApp app;
	ScenePanel sceneTree;
	RPMeshData dataMesh;
	RPMaterialData dataMaterial;
	RPTextureData dataTexture;
	
	DisplayMode prevDisplay =Display.getDisplayMode();

	
	RenderController rController;
	CameraController camController;
	ManipController manipController;
	GridRenderer gridRenderer;
	
	@Override
	public int getNext() {
		return getIndex();
	}
	@Override
	protected void setNext(int next) {
	}

	@Override
	public int getPrevious() {
		return -1;
	}
	@Override
	protected void setPrevious(int previous) {
	}

	@Override
	public void build() {
						
		app = (SceneApp)game;
		renderer = new Renderer();	
	}
	
	@Override
	public void destroy(GameTime gameTime) {
	}

	/**
	 * Add Scene Data Hotkeys
	 */
	private final ACEventFunc<KeyboardKeyEventArgs> onKeyPress = new ACEventFunc<KeyboardKeyEventArgs>() {
		@Override
		public void receive(Object sender, KeyboardKeyEventArgs args) {
			switch (args.key) {
			case Keyboard.KEY_M:
				if(!args.getAlt()) return;
				if(dataMaterial != null) {
					app.otherWindow.tabToForefront("Material");
					dataMaterial.addBasic();
				}
				break;
//			case Keyboard.KEY_G:
//				showGrid = !showGrid;
//				break;
			case Keyboard.KEY_F3:
				FileDialog fd = new FileDialog(app.otherWindow);
				fd.setVisible(true);
				for(File f : fd.getFiles()) {
					String file = f.getAbsolutePath();
					if(file != null) {
						Parser p = new Parser();
						Object o = p.parse(file, Scene.class);
						if(o != null) {
							app.otherWindow.dispose();
							Scene old = app.scene;
							app.scene = (Scene)o;
							if(old != null) old.sendEvent(new SceneReloadEvent(file));
							return;
						}
					}
				}
				break;
			case Keyboard.KEY_F4:
				try {
					app.scene.saveData("data/scenes/Saved.xml");
				} catch (ParserConfigurationException | TransformerException e) {
					e.printStackTrace();
				}
				break;
				
			case Keyboard.KEY_1:
				changeShader(0);
				break;
				
			case Keyboard.KEY_2:
				changeShader(1);
				break;
				
			case Keyboard.KEY_3:
				changeShader(2);
				break;
				
			case Keyboard.KEY_4:
				changeShader(3);
				break;
			
			case Keyboard.KEY_5:
				changeShader(4);
				break;
					
			case Keyboard.KEY_6:
				app.otherWindow.dispose();
				
				
			case Keyboard.KEY_ESCAPE:
				
				try{
				Robot mouseMover = new Robot();
				float centery = Display.getY() + Display.getDisplayMode().getHeight()/ 2;
				float centerx = Display.getX() + Display.getDisplayMode().getWidth()/ 2;
				 mouseMover.mouseMove((int) centerx, (int) centery);

				} catch (AWTException e) {
					e.printStackTrace();
				}

				try {
					Mouse.setNativeCursor(null);
				} catch (LWJGLException e) {
					e.printStackTrace();
				}
				camController.isHighlighted();
				camController.changeWindow();
				break;
			default:
				break;
				
			case Keyboard.KEY_F:
				try {
					if(Display.isFullscreen()){
						Display.setDisplayMode(prevDisplay);
						//prevDisplay = Display.getDisplayMode();
						Display.setFullscreen(false);
					}
					else{

						Display.setDisplayModeAndFullscreen(Display.getDesktopDisplayMode());
						Display.setFullscreen(true);
//					for(DisplayMode i: Display.getAvailableDisplayModes()){
//						if (i.isFullscreenCapable() && i.getHeight() == height){
//							//prevDisplay = Display.getDisplayMode();
//							Display.setDisplayMode(i);
//							Display.setFullscreen(true);
//							break;
//							}
//						}
					}
					
					try{
						Robot mouseMover = new Robot();
						float centery = Display.getY() + Display.getDisplayMode().getHeight()/ 2;
						float centerx = Display.getX() + Display.getDisplayMode().getWidth()/ 2;
						 mouseMover.mouseMove((int) centerx, (int) centery);

						} catch (AWTException e) {
							e.printStackTrace();
						}
					camController.changeWindow();
					}
				catch (LWJGLException e1) {
					e1.printStackTrace();
				}
			}
		}
	};
	
	@Override
	public void onEntry(GameTime gameTime) {	
		
		cameraIndex = 0;
		rController = new RenderController(app.scene, new Vector2(app.getWidth(), app.getHeight()));
		renderer.buildPasses(rController.env.root);
		camController = new CameraController(app.scene, rController.env, null);
		createCamController();
		manipController = new ManipController(rController.env, app.scene, app.otherWindow);
		gridRenderer = new GridRenderer();
		KeyboardEventDispatcher.OnKeyPressed.add(onKeyPress);
		manipController.hook();
		
		Object tab = app.otherWindow.tabs.get("Object");
		if(tab != null) sceneTree = (ScenePanel)tab;
		tab = app.otherWindow.tabs.get("Material");
		if(tab != null) dataMaterial = (RPMaterialData)tab;
		tab = app.otherWindow.tabs.get("Mesh");
		if(tab != null) dataMesh = (RPMeshData)tab;
		tab = app.otherWindow.tabs.get("Texture");
		if(tab != null) dataTexture = (RPTextureData)tab;
		
		wasPickPressedLast = false;
		prevCamScroll = 0;
	}
	@Override
	public void onExit(GameTime gameTime) {
		KeyboardEventDispatcher.OnKeyPressed.remove(onKeyPress);
		rController.dispose();
		manipController.dispose();
	}

	private void createCamController() {
		if(rController.env.cameras.size() > 0) {
			RenderCamera cam = rController.env.cameras.get(cameraIndex);
			camController.camera = cam;
			
		}
		else {
			camController.camera = null;
		}
	}
	
	@Override
	public void update(GameTime gameTime) {
		
		pick = false;
		int curCamScroll = 0;
		
		if(Keyboard.isKeyDown(Keyboard.KEY_EQUALS)) curCamScroll++;
		if(Keyboard.isKeyDown(Keyboard.KEY_MINUS)) curCamScroll--;
		if(rController.env.cameras.size() != 0 && curCamScroll != 0 && prevCamScroll != curCamScroll) {
			if(curCamScroll < 0) curCamScroll = rController.env.cameras.size() - 1;
			cameraIndex += curCamScroll;
			cameraIndex %= rController.env.cameras.size();
			createCamController();
		}
		prevCamScroll = curCamScroll;
		if(camController.camera != null) {
			if(!camController.ishighlighted){
				try {
					Mouse.setNativeCursor(null);
				} catch (LWJGLException e) {
					e.printStackTrace();
				}
			}
				else{
			camController.update(gameTime.elapsed);
			manipController.checkMouse(Mouse.getX(), Mouse.getY(), camController.camera);
			}
		}
		
		
		if(Mouse.isButtonDown(1) || Mouse.isButtonDown(0) && (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL))) {
			if(!wasPickPressedLast) pick = true;
			wasPickPressedLast = true;
		}
		else wasPickPressedLast = false;
		
		// View A Different Scene
		if(rController.isNewSceneRequested()) {
			setState(ScreenState.ChangeNext);
		}
	}
	
	//Take an int value. 0 = CookTorrancess, 1 = Discrete, 2 = Gooch, 3 = Hatching
	public void changeShader(int shader){
		String shadername = "";
		switch(shader){
			case 0:
				shadername = "CookTorranceMaterial";
			break;
			case 1: 
				shadername = "DiscreteMaterial";
				break;
			case 2: 
				shadername = "GoochMaterial";
				break;
			case 3: 
				shadername = "HatchingMaterial";
				break;
			case 4:
				shadername = "TimeMaterial";
				break;

		}
		for (SceneObject s:app.scene.objects){
			if ((s.material != null) && (!s.material.equals("Ambient")) && (!s.mesh.equals("Room.obj"))) {
				s.setMaterial(shadername);
				app.scene.sendEvent(new SceneObjectResourceEvent(s, SceneObjectResourceEvent.Type.Material));}
		}
		
	}
	
	@Override
	public void draw(GameTime gameTime) {
		
		rController.update(renderer, camController);

		if(pick && camController.camera != null) {
			manipController.checkPicking(renderer, camController.camera, Mouse.getX(), Mouse.getY());
		}
		
		Vector3 bg = app.scene.background;
		GL11.glClearColor(bg.x, bg.y, bg.z, 0);
		GL11.glClearDepth(1.0);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		
		if(camController.camera != null){
			renderer.draw(camController.camera, rController.env.lights, gameTime.total);
			manipController.draw(camController.camera);
			if (showGrid)
				gridRenderer.draw(camController.camera);
		}
        GLError.get("draw");
	}
}
