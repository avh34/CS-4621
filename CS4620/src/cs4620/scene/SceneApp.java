package cs4620.scene;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import blister.FalseFirstScreen;
import blister.MainGame;
import blister.ScreenList;
import cs4620.common.Scene;
import cs4620.scene.form.ControlWindow;

public class SceneApp extends MainGame {
	/**
	 * The Thread That Runs The Other Window
	 */
	Thread tWindow = null;
	
	/**
	 * Window That Modifies The Underlying Data Of The Scene
	 */
	ControlWindow otherWindow = null;
	
	/**
	 * Scene
	 */
	public Scene scene;
	
	public ControlWindow getOther(){
		return otherWindow;
	}
	
	public SceneApp() {
		super("", 800, 600);
		
		scene = new Scene();
		otherWindow = new ControlWindow(this);
		otherWindow.setLocationRelativeTo(null);
		/*try {
			Clip sound = AudioSystem.getClip();
			File scene_file = new File("data/Audraulic_-_08_-_Distant_Winds.wav");
			//String file = scene_file.getAbsolutePath();
			AudioInputStream song = (AudioSystem.getAudioInputStream(scene_file));
			sound.open(song);
			sound.loop(Clip.LOOP_CONTINUOUSLY);
			//sound.start();
			
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (UnsupportedAudioFileException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	}
	
	@Override
	protected void buildScreenList() {
		screenList = new ScreenList(this, 0,
			new FalseFirstScreen(1),
			new ViewScreen()
			);
	}
	@Override
	protected void fullInitialize() {
		
	}
	@Override
	protected void fullLoad() {
        tWindow = new Thread(new ControlThread(this));
        tWindow.run();
	}
	@Override
	public void exit() {
		if(otherWindow != null) {
			tWindow.interrupt();
		}
		super.exit();
	}

	public static void main(String[] args) {
		SceneApp app = new SceneApp();
		app.run();
		app.dispose();
	}
	
	class ControlThread implements Runnable {
		SceneApp app;
		
		ControlThread(SceneApp a) {
			app = a;
		}
		
		@Override
		public void run() {
			app.otherWindow.run();
		}
		
	}
}
