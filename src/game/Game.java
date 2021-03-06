package game;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.ArrayList;

import javax.swing.JFrame;

import editor.TileType;
import utils.Reader;

public class Game extends JFrame implements Runnable {

	private static final long serialVersionUID = 1L;
	public static int MAP_WIDTH  = 15;
	public static int MAP_HEIGHT = 15;
	public static final int SCREEN_WIDTH  = 640;
	public static final int SCREEN_HEIGHT = 480;
	public static final int TEXTURE_SIZE  = 64;
	
	private float playerX, playerY;

	private Thread thread;
	private boolean running;
	
	private int pixels[];
	private BufferedImage image; 
	private ArrayList<Texture> textures;
	private Camera camera;
	private Screen screen;
	
	public static int[][] map = 
	{
		{1,1,1,1,1,1,1,1,2,2,2,2,2,2,2},
		{1,0,0,0,0,0,0,0,2,0,0,0,0,0,2},
		{1,0,3,3,3,3,3,0,0,0,0,0,0,0,2},
		{1,0,3,0,0,0,3,0,2,0,0,0,0,0,2},
		{1,0,3,0,0,0,3,0,2,2,2,0,2,2,2},
		{1,0,3,0,0,0,3,0,2,0,0,0,0,0,2},
		{1,0,3,3,0,3,3,0,2,0,0,0,0,0,2},
		{1,0,0,0,0,0,0,0,2,0,0,0,0,0,2},
		{1,1,1,1,1,1,1,1,4,4,4,0,4,4,4},
		{1,0,0,0,0,0,1,4,0,0,0,0,0,0,4},
		{1,0,0,0,0,0,1,4,0,0,0,0,0,0,4},
		{1,0,0,2,0,0,1,4,0,3,3,3,3,0,4},
		{1,0,0,0,0,0,1,4,0,3,3,3,3,0,4},
		{1,0,0,0,0,0,0,0,0,0,0,0,0,0,4},
		{1,1,1,1,1,1,1,4,4,4,4,4,4,4,4}
	};
	
	public Game() {
		playerX = 4.5f; // These values are just for default map!
		playerY = 4.5f;
		
		thread = new Thread(this);
		image = new BufferedImage(SCREEN_WIDTH, SCREEN_HEIGHT, BufferedImage.TYPE_INT_RGB);
		pixels = ((DataBufferInt)image.getRaster().getDataBuffer()).getData(); // "Connect" image and pixels
		
		loadMap();
		MAP_WIDTH = map.length;
		MAP_HEIGHT = map[0].length;
		camera = new Camera(playerX, playerY, 1, 0, 0, -0.66);
		addKeyListener(camera);
		
		initTextures();

		screen = new Screen(map, textures);
		
		// Init JFrame
		setTitle("Rayze");
		setSize(SCREEN_WIDTH, SCREEN_HEIGHT);
		setLocationRelativeTo(null);
		setResizable(false);
		setBackground(Color.BLACK);
		setVisible(true);
		
		start();
	}
	
	/**
	 * NOTE: For now you can change maps by editing this method.
	 */
	private void loadMap() {
		map = Reader.readMapFromFile("maps/testMap.txt");
		
		// Set player position
		for (int i = 0; i < map.length; ++i) {
			for (int j = 0; j < map[i].length; ++j) {
				if (map[i][j] == TileType.PLAYER.getValue()) {
					playerX = i + 1; // @Hack, not sure if this is going to work all the time
					playerY = j + 1;
				}
			}
		}
	}
	
	/**
	 *  Initializes array list of textures and adds new textures to it.
	 */
	private void initTextures() {
		textures = new ArrayList<Texture>();
		textures.add(new Texture("res/bluestone.jpg", TEXTURE_SIZE));
		textures.add(new Texture("res/greystone.jpg", TEXTURE_SIZE));
		textures.add(new Texture("res/redbrick.jpg",  TEXTURE_SIZE));
		textures.add(new Texture("res/wood.jpg",      TEXTURE_SIZE));
	}
	
	/**
	 * Starts new thread (game thread).
	 */
	private synchronized void start() {
		running = true;
		thread.start();
	}
	
	/**
	 * Stops game thread.
	 */
	private synchronized void stop() {
		running = false;
		
		try {
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Renders image to the screen.
	 */
	private void render() {
		BufferStrategy bs = getBufferStrategy();
		
		if (bs == null) {
			createBufferStrategy(3);
			return;
		}
		
		Graphics g = bs.getDrawGraphics();
		g.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), null);
		bs.show();
	}

	/**
	 * Keeps track of FPS counter. FPS is set to 60.
	 * It allows us to render 60 images in one second.
	 */
	@Override
	public void run() {
		long lastTime = System.nanoTime();
		final double ns = 1000000000.0 / 60.0;  
		double delta = 0;
		requestFocus();
		
		while(running) {
			long now = System.nanoTime();
			delta = delta + ((now-lastTime) / ns);
			lastTime = now;
			
			while (delta >= 1) {
				camera.update(map);
				screen.update(camera, pixels);
				delta--;
			}

			render(); 
		}
	}
	
	public static void main(String[] args) {
		new Game();
	}
}
