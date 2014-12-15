package cs4620.scene.form;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.lwjgl.opengl.Display;

import cs4620.common.Scene;
import cs4620.common.event.SceneReloadEvent;
import cs4620.scene.SceneApp;
import ext.java.Parser;

public class VictoryScreen extends JFrame{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6940716105036651901L;
	public VictoryScreen(SceneApp a){
	super("");
	setSize(700, 500);
	setResizable(false);
	setLocationRelativeTo(null);
	//JLabel background = new JLabel(new ImageIcon(backgroundImage));
	//add(background);
	//setVisible(true);
	
	//button_panel.paintComponents();
	
	add(new Panel(a, this));
	setUndecorated(true);
	setVisible(true);


}
}


class Panel extends JPanel{
	/**
	 * 
	 */
	private static final long serialVersionUID = -3436617975926493477L;
	private Image backgroundImage = new ImageIcon("data/4621_Victory.png").getImage();

	final JFrame frame;
	final SceneApp scene;
	public void paintComponent(Graphics g){
		g.drawImage(backgroundImage, 0, 0, null);

	}
	public Panel(SceneApp a, JFrame j){
		super(new BorderLayout());
		frame = j;
		 scene = a;
		
		JPanel southPanel = new JPanel();
		southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.Y_AXIS));
		
		JPanel actionPanel = new JPanel();
		actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.X_AXIS));
		
		JButton restart = new JButton("Play Again");
		restart.setAlignmentX(Component.CENTER_ALIGNMENT);
		restart.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				File scene_file = new File("data/scenes/shader-assignment/SceneSomething.xml");
				String file = scene_file.getAbsolutePath();
				if(file != null) {
					Parser p = new Parser();
					Object o = p.parse(file, Scene.class);
					if(o != null) {
						try{
							scene.getOther().dispose();
							
							Robot mouseMover = new Robot();
							Dimension screen_size = Toolkit.getDefaultToolkit().getScreenSize();
							float centery = Display.getY() + Display.getDisplayMode().getHeight()/ 2;
							float centerx = Display.getX() + Display.getDisplayMode().getWidth()/ 2;
							 mouseMover.mouseMove((int) centerx, (int) centery);

							} catch (AWTException a) {
								a.printStackTrace();
							}
						Scene old = scene.scene;
						scene.scene = (Scene)o;
						if(old != null) old.sendEvent(new SceneReloadEvent(file));
						frame.dispose();
						return;}}
			}
			
		});
		actionPanel.setOpaque(false);
		southPanel.setOpaque(false);
		actionPanel.add(restart);
		southPanel.add(actionPanel);
		add(southPanel, BorderLayout.SOUTH);
		setOpaque(false);
		
	}
}
