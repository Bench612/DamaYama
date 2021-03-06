import java.util.*;

import drawing.*;
import drawing.Point;

import javax.swing.*;

import java.awt.event.*;
import java.awt.image.MemoryImageSource;
import java.io.*;
import java.awt.*;

public class DamaYama extends JFrame {
	private static Random random = new Random();
	public static Color orange = new Color(235, 169, 116);
	public static Color blue = new Color(65, 173, 214);
	String username;

	public DamaYama(String s) {
		super(s);
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		logo = toolkit.getImage("damayama_logoLarge.png");
		icon = toolkit.getImage("damayama_logoSmall.png");
		this.setIconImage(icon);
	}

	public static void main(String[] args) {
		if (args.length == 1 && args[0].equals("master")) {
			new MapOpenFrame(new MasterDama());
		} else {
			if (args.length != 0)
				Lobby.actualGameName = args[0];
			new BasicDama();
		}
	}

	public static double random() {
		return random.nextDouble();
	}

	public static void setSeed(long llong) {
		random.setSeed(llong);
	}

	public void switchTo(DamaPanel p) {
		setContentPane(p);
		validate();
	}

	public void reset() {
		Map.setNewMap(this, Map.getCurrent());
		validate();
	}

	static Image logo;
	static Image icon;
}

class BasicDama extends DamaYama implements WindowListener {
	public BasicDama() {
		super("Dama Yama");
		setContentPane(new MainMenuPanel(this));
		this.setBounds(0, 0, 0, 0);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setResizable(false);
		this.setUndecorated(true);
		Toolkit kit = Toolkit.getDefaultToolkit();
		// GraphicsEnvironment.getLocalGraphicsEnvironment()
		// .getDefaultScreenDevice().setFullScreenWindow(this);// makes it
		// fullscreen
		setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);
		username = Math.random() * 100 + "";
		if (username.equals(""))
			username = "Guest";
		setVisible(true);
	}

	public void reset() {
		switchTo(new MainMenuPanel(this));
	}

	class MainMenuPanel extends ShinyPanel implements Runnable, FocusListener,
			MouseListener, MouseMotionListener {
		Perspective perspective;
		String[] menuItems;
		DamaYama frame;

		public MainMenuPanel(DamaYama frame) {
			super(30);
			this.frame = frame;
			for (int i = 0; i < 2; i++) {
				pointX.add(-1000d);
				pointY.add(-1000d);
			}
			colorsStart = new ArrayList<Color>(2);
			colorsStart.add(DamaYama.orange);
			colorsStart.add(DamaYama.blue);
			colorsFinal = new ArrayList<Color>(2);
			colorsFinal.add(DamaYama.orange);
			colorsFinal.add(DamaYama.blue);
			addFocusListener(this);
			menuItems = new String[] { "Single Player", "Multiplayer",
					"Map Editor", "Replay", "Exit" };
			forms = new ArrayList<ArrayList<Shape>>(100);
			addMouseListener(this);
			addMouseMotionListener(this);
			new Thread(this).start();
		}

		double sinStretch = 4;
		double sinOffset = Math.random() * Math.PI * 2;
		double theta = Math.random() * Math.PI * 2;
		double maxRadius;

		public void run() {
			if (running)
				return;
			running = true;
			while (!this.isFocusOwner() && running) {
				requestFocus();
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			while (running && !toStop) {
				try {
					if (getWidth() > 0) {
						update();
						// now move the points
						theta += 0.01;
						sinOffset += 0.01;
						double centerX = getWidth() / 3.0;
						double centerY = getHeight() / 2.0;
						maxRadius = getWidth() / 6;
						double sinRadius = maxRadius * 0.25;
						double mainRadius = maxRadius * 0.75;
						double drawRadius = mainRadius
								+ (sinRadius * Math.sin((sinStretch * theta)
										+ sinOffset));
						for (int i = 0; i < 2; i++) {// makes the first 2 spin
							// around
							pointX.set(i,
									centerX + Math.cos(theta + i * Math.PI)
											* drawRadius);
							pointY.set(i,
									centerY + Math.sin(theta + i * Math.PI)
											* drawRadius);
						}
						previousX = mouseX;
						previousY = mouseY;

						rotation += 0.0035;
						rotation %= Math.PI * 2;
						Thread.sleep(20);
						repaint();
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			running = false;
			toStop = false;
		}

		double rotation = 0;
		int stringHeight;

		final Color faintBlue = new Color(15, 20, 25);
		final Color faintOrange = new Color(50, 40, 30);

		public void paintComponent(Graphics g) {
			clearScreen(g);
			if (perspective == null) {
				if (getWidth() > 0 && getHeight() > 0) {
					perspective = new Perspective(0, getHeight(),
							getWidth() * 5 / 6, getHeight() / 8);
				} else {
					super.paintComponent(g);
					return;
				}
			}
			perspective.update(g, 0, -125, 0, 1, getWidth(), getHeight());
			double centerX = 5;
			double centerY = 45;
			double radius = 35;
			perspective.setOutline(faintOrange);
			drawJackal(centerX + Math.cos(rotation) * radius,
					centerY + Math.sin(rotation) * radius, 0, 4, rotation);
			drawDemon(centerX + Math.cos(rotation + Math.PI / 2) * radius,
					centerY + Math.sin(rotation + Math.PI / 2) * radius, 0, 4,
					rotation + Math.PI / 2);
			drawSporeParent(
					centerX + Math.cos(rotation + Math.PI / 4) * radius,
					centerY + Math.sin(rotation + Math.PI / 4) * radius, 0, 4,
					rotation + Math.PI / 4);
			drawBasicEnemy(centerX + Math.cos(rotation + 3 * Math.PI / 4)
					* radius, centerY + Math.sin(rotation + 3 * Math.PI / 4)
					* radius, 0, 4, rotation + 3 * Math.PI / 4);
			drawJackal(centerX + Math.cos(rotation + Math.PI) * radius, centerY
					+ Math.sin(rotation + Math.PI) * radius, 0, 4, rotation
					+ Math.PI);
			drawDemon(centerX + Math.cos(rotation + 3 * Math.PI / 2) * radius,
					centerY + Math.sin(rotation + 3 * Math.PI / 2) * radius, 0,
					4, rotation + 3 * Math.PI / 2);
			drawSporeParent(centerX + Math.cos(rotation + 5 * Math.PI / 4)
					* radius, centerY + Math.sin(rotation + 5 * Math.PI / 4)
					* radius, 0, 4, rotation + 5 * Math.PI / 4);
			drawBasicEnemy(centerX + Math.cos(rotation + 7 * Math.PI / 4)
					* radius, centerY + Math.sin(rotation + 7 * Math.PI / 4)
					* radius, 0, 4, rotation + 7 * Math.PI / 4);
			perspective.setOutline(faintBlue);

			for (int x = 10; x < 100; x += 8) {
				for (int y = -75; y < 125; y += 8) {
					perspective.createNgon(x, y, 0, 4, Math.PI / 4, 4);
					perspective.drawShape(perspective.getCurrentShape());
				}
			}
			for (int x = -158; x < 10; x += 8) {
				for (int y = -75; y < 125; y += 8) {
					perspective.createNgon(x, y, 0, 4, 0, 4);
					perspective.drawShape(perspective.getCurrentShape());
				}
			}
			perspective.draw();
			super.paintComponent(g);
			if (DamaYama.logo.getWidth(this) > 0) {
				double scale = maxRadius * 1.5 / DamaYama.logo.getWidth(this);
				int logoHeight = (int) (scale * DamaYama.logo.getHeight(this));
				// diff sized ratio than regular
				g.drawImage(DamaYama.logo,
						(int) (getWidth() / 3.0 - maxRadius),
						(int) ((getHeight() - logoHeight) / 2),
						(int) (maxRadius * 2), logoHeight, this);
			}

			// now draw the actual menu (finally)
			g.setColor(fadeBlue);
			int rectWidth = getWidth() / 5;
			int rectHeight = getHeight() / 3;
			int rectX = getWidth() - rectWidth - 20;
			int rectY = getHeight() - rectHeight - 20;
			g.fillRoundRect(rectX, rectY, rectWidth, rectHeight, roundingSize,
					roundingSize);
			stringHeight = g.getFontMetrics().getHeight();
			double increment = ((double) (rectHeight - roundingSize) - stringHeight
					* menuItems.length)
					/ (menuItems.length);
			for (int i = 0; i < menuItems.length; i++) {
				int topY = rectY + (int) (increment / 2)
						+ (int) (i * increment) + (int) (i * stringHeight)
						+ roundingSize / 2;
				if (mouseX > rectX && mouseX < rectX + rectWidth
						&& mouseY > topY && mouseY < topY + stringHeight) {
					g.setColor(DamaYama.orange);
					int xInc = Math.max(rectWidth / 128, 1);
					for (int x = rectX; x < rectX + rectWidth; x += xInc) {
						g.setColor(getColor(DamaYama.orange, Math.min(Math.max(
								255 - (((x - rectX) * 255) / rectWidth), 0),
								255)));
						g.fillRect(x, topY, xInc, stringHeight);
					}
				}
				g.setColor(Color.white);
				g.drawString(menuItems[i], rectX + roundingSize / 2, topY
						+ stringHeight);
			}
		}

		Color fadeBlue = getColor(DamaYama.blue.darker(), 150);

		int roundingSize = 40;
		ArrayList<ArrayList<Shape>> forms;

		public void focusGained(FocusEvent arg0) {
			if (!running)
				new Thread(this).start();
		}

		public void focusLost(FocusEvent e) {
			toStop = true;
		}

		int mouseX, mouseY;
		double previousX, previousY;

		private void updateMouse(MouseEvent e) {
			mouseX = e.getX();
			mouseY = e.getY();
			repaint();
		}

		public void mouseClicked(MouseEvent e) {

		}

		public void mouseEntered(MouseEvent e) {
		}

		public void mouseExited(MouseEvent e) {
		}

		public void mousePressed(MouseEvent e) {
			int rectWidth = getWidth() / 5;
			int rectHeight = getHeight() / 3;
			int rectX = getWidth() - rectWidth - 20;
			int rectY = getHeight() - rectHeight - 20;
			double increment = ((double) (rectHeight - roundingSize) - stringHeight
					* menuItems.length)
					/ (menuItems.length);
			for (int i = 0; i < menuItems.length; i++) {
				int topY = rectY + (int) (increment / 2)
						+ (int) (i * increment) + (int) (i * stringHeight)
						+ roundingSize / 2;
				if (mouseX > rectX && mouseX < rectX + rectWidth
						&& mouseY > topY && mouseY < topY + stringHeight) {
					switch (i) {
					case 0:
						switchTo(new SinglePlayerLobby(frame));
						break;
					case 2:
						new MasterDama();
						break;
					case 1:
						switchTo(new Lobby(frame));
						break;
					case 3:
						switchTo(new ReplayPanel(frame, Lobby.actualGameName));
						break;
					case 4:
						System.exit(0);
					}
				}
			}
		}

		public void mouseReleased(MouseEvent e) {
		}

		public void mouseDragged(MouseEvent e) {
			updateMouse(e);
		}

		public void mouseMoved(MouseEvent e) {
			updateMouse(e);
		}

		private void drawBasicEnemy(double x, double y, double z, double width,
				double direction) {
			Perspective p = perspective;
			double height = width;
			// draw head
			p.createNgon(x + width / 2, y + height / 2, z + width * 2 * 0.7,
					width / 2, direction + Math.PI / 4, 4);
			ArrayList<drawing.Point> bottom = p.getCurrentShape();
			p.createNgon(x + width / 2, y + height / 2, z + width * 2,
					width / 2, direction + Math.PI / 4, 4);
			ArrayList<drawing.Point> top = p.getCurrentShape();
			p.drawForm(p.createForm(top, bottom));

			// drawBody
			p.createNgon(x + width / 2, y + height / 2, z, width / 2 * 0.75,
					direction + Math.PI / 4, 4);
			bottom = p.getCurrentShape();
			p.createNgon(x + width / 2, y + height / 2, z + width * 2 * 0.6,
					width / 2 * 0.75, direction + Math.PI / 4, 4);
			top = p.getCurrentShape();
			p.drawForm(p.createForm(top, bottom));
		}

		private void drawSporeParent(double x, double y, double z,
				double width, double direction) {

			Perspective p = perspective;
			double height = width;

			p.createNgon(x + width / 2, y + height / 2, z, width / 2.5,
					direction + Math.PI / 4, 4);
			ArrayList<Point> bottom = p.getCurrentShape();
			p.createNgon(x + width / 2, y + height / 2, z + (width * 2) * 0.5,
					width / 2 * 0.25, direction + Math.PI / 4, 4);
			ArrayList<Point> top = p.getCurrentShape();
			p.drawForm(p.createForm(top, bottom));

			p.drawSphere(x + width / 2, y + height / 2, z + (width * 2) * 0.8,
					width / 2, direction + Math.PI / 4, 8, 4);

			p.drawSphere(x + width / 2 + Math.cos(direction + 2.6) * width
					* 0.3387, y + height / 2 + Math.sin(direction + 2.6)
					* height * 0.3387, z + (width * 2) * 0.8, width / 4,
					direction + Math.PI / 4, 8, 4);
			p.drawSphere(x + width / 2 + Math.cos(direction - 2.6) * width
					* 0.387, y + height / 2 + Math.sin(direction - 2.6)
					* height * 0.387, z + (width * 2) * 0.8, width / 4,
					direction + Math.PI / 4, 8, 4);
			p.drawSphere(x + width / 2 + Math.cos(direction + Math.PI / 4)
					* width * 0.282,
					y + height / 2 + Math.sin(direction + Math.PI / 4) * height
							* 0.282, z + (width * 2) * 0.8 - height * 0.2,
					width / 4, direction + Math.PI / 4, 8, 4);
			p.drawSphere(x + width / 2 + Math.cos(direction - 0.52) * width
					* 0.394, y + height / 2 + Math.sin(direction - 0.52)
					* height * 0.394, z + (width * 2) * 0.8 + height * 0.3,
					width / 4, direction + Math.PI / 4, 8, 4);
		}

		private void drawDemon(double x, double y, double z, double width,
				double direction) {

			Perspective p = perspective;
			double height = width;
			double attackRange = width / 2;

			// drawBody
			p.createNgon(x + width / 2, y + height / 2, z, width / 2 * 0.75,
					direction + Math.PI / 4, 4);
			ArrayList<Point> bottom = p.getCurrentShape();
			p.createNgon(x + width / 2, y + height / 2, z + (width * 2) * 0.6,
					width / 2 * 0.75, direction + Math.PI / 4, 4);
			ArrayList<Point> top = p.getCurrentShape();
			p.drawForm(p.createForm(top, bottom));

			// draw head
			p.createNgon(x + width / 2, y + height / 2, z + (width * 2) * 0.7,
					width / 2, direction + Math.PI / 5, 5);
			bottom = p.getCurrentShape();
			p.createNgon(x + width / 2, y + height / 2, z + (width * 2),
					width / 2, direction + Math.PI / 5, 5);
			top = p.getCurrentShape();
			p.drawForm(p.createForm(top, bottom));

			double attackPerc = 0;

			// draw arm1
			p.createNgon(x + width / 2 + Math.cos(direction + Math.PI / 2)
					* (width / 2) + Math.cos(direction) * attackPerc
					* attackRange,
					y + height / 2 - Math.sin(direction + Math.PI / 2)
							* (height / 2) + Math.sin(direction) * attackPerc
							* attackRange,
					z + ((width * 2) * 0.2) * (1 - attackPerc) + (width * 2)
							* 0.4 * attackPerc, 0, direction + Math.PI / 4, 4);
			bottom = p.getCurrentShape();
			p.createNgon(x + width / 2 + Math.cos(direction + Math.PI / 2)
					* (width / 2),
					y + height / 2 - Math.sin(direction + Math.PI / 2)
							* (height / 2), z + (width * 2) * 0.5, 0.1,
					direction + Math.PI / 4, 4);
			top = p.getCurrentShape();
			p.drawForm(p.createForm(top, bottom));

			// draw arm2
			p.createNgon(x + width / 2 + Math.cos(direction - Math.PI / 2)
					* (width / 2) + Math.cos(direction) * attackPerc
					* attackRange,
					y + height / 2 - Math.sin(direction - Math.PI / 2)
							* (height / 2) + Math.sin(direction) * attackPerc
							* attackRange,
					z + ((width * 2) * 0.2) * (1 - attackPerc) + (width * 2)
							* 0.4 * attackPerc, 0, direction - Math.PI / 4, 4);
			bottom = p.getCurrentShape();
			p.createNgon(x + width / 2 + Math.cos(direction - Math.PI / 2)
					* (width / 2),
					y + height / 2 - Math.sin(direction - Math.PI / 2)
							* (height / 2), z + (width * 2) * 0.5, 0.1,
					direction - Math.PI / 4, 4);
			top = p.getCurrentShape();
			p.drawForm(p.createForm(top, bottom));
		}

		private void drawJackal(double x, double y, double z, double width,
				double direction) {

			Perspective p = perspective;
			double height = width;
			double attackRange = width / 2;

			// draw head
			p.createNgon(x + width / 2, y + height / 2, z + (width * 2) * 0.8,
					width / 2, direction, 3);
			ArrayList<Point> bottom = p.getCurrentShape();
			p.createNgon(x + width / 2, y + height / 2, z + (width * 2),
					width / 2, direction, 3);
			ArrayList<Point> top = p.getCurrentShape();
			p.drawForm(p.createForm(top, bottom));

			// drawBody
			p.createNgon(x + width / 2, y + height / 2, z, width / 4 * 0.75,
					direction + Math.PI / 4, 8);
			bottom = p.getCurrentShape();
			p.createNgon(x + width / 2, y + height / 2, z + (width * 2) * 0.6,
					width / 4 * 0.75, direction + Math.PI / 4, 8);
			top = p.getCurrentShape();
			p.drawForm(p.createForm(top, bottom));

			double attackPerc = 0;
			// draw arm1
			p.createNgon(x + width / 2 + Math.cos(direction + Math.PI / 2)
					* (width / 2) + Math.cos(direction) * attackPerc
					* attackRange,
					y + height / 2 - Math.sin(direction + Math.PI / 2)
							* (height / 2) + Math.sin(direction) * attackPerc
							* attackRange,
					z + ((width * 2) * 0.2) * (1 - attackPerc) + (width * 2)
							* 0.4 * attackPerc, 0, direction + Math.PI / 4, 4);
			bottom = p.getCurrentShape();
			p.createNgon(x + width / 2 + Math.cos(direction + Math.PI / 2)
					* (width / 2),
					y + height / 2 - Math.sin(direction + Math.PI / 2)
							* (height / 2), z + (width * 2) * 0.5, 0.1,
					direction + Math.PI / 4, 4);
			top = p.getCurrentShape();
			p.drawForm(p.createForm(top, bottom));

			// draw arm2
			p.createNgon(x + width / 2 + Math.cos(direction - Math.PI / 2)
					* (width / 2) + Math.cos(direction) * attackPerc
					* attackRange,
					y + height / 2 - Math.sin(direction - Math.PI / 2)
							* (height / 2) + Math.sin(direction) * attackPerc
							* attackRange,
					z + ((width * 2) * 0.2) * (1 - attackPerc) + (width * 2)
							* 0.4 * attackPerc, 0, direction - Math.PI / 4, 4);
			bottom = p.getCurrentShape();
			p.createNgon(x + width / 2 + Math.cos(direction - Math.PI / 2)
					* (width / 2),
					y + height / 2 - Math.sin(direction - Math.PI / 2)
							* (height / 2), z + (width * 2) * 0.5, 0.1,
					direction - Math.PI / 4, 4);
			top = p.getCurrentShape();
			p.drawForm(p.createForm(top, bottom));

			// draw shield
			p.startNewShape(x + width / 2 + Math.cos(direction + Math.PI / 4)
					* (width / 2 + 0.01),
					y + height / 2 - Math.sin(direction + Math.PI / 4)
							* (height / 2 + 0.01), z, 4);
			p.addPoint(p.getScreenPoint(
					x + width / 2 + Math.cos(direction + Math.PI / 4)
							* (width / 2 + 0.01),
					y + height / 2 - Math.sin(direction + Math.PI / 4)
							* (height / 2 + 0.01), z + (width * 2) * 0.6));
			p.addPoint(p.getScreenPoint(
					x + width / 2 + Math.cos(direction - Math.PI / 4)
							* (width / 2 + 0.01),
					y + height / 2 - Math.sin(direction - Math.PI / 4)
							* (height / 2 + 0.01), z + (width * 2) * 0.6));
			p.addPoint(p.getScreenPoint(
					x + width / 2 + Math.cos(direction - Math.PI / 4)
							* (width / 2 + 0.01),
					y + height / 2 - Math.sin(direction - Math.PI / 4)
							* (height / 2 + 0.01), z));
			p.drawShape(p.getCurrentShape());
		}
	}

	public void windowActivated(WindowEvent e) {
	}

	public void windowClosed(WindowEvent e) {
	}

	public void windowClosing(WindowEvent e) {
	}

	public void windowDeactivated(WindowEvent e) {
	}

	public void windowDeiconified(WindowEvent e) {
	}

	public void windowIconified(WindowEvent e) {
	}

	public void windowOpened(WindowEvent e) {
	}

}

class DamaPanel extends JPanel {
	public DamaPanel(LayoutManager layout) {
		super(layout);
	}

	public DamaPanel() {
	}

	public void close() {

	}
}

class ShinyPanel extends DamaPanel {
	ArrayList<Double> pointX;
	ArrayList<Double> pointY;
	ArrayList<Particle> particles;
	ArrayList<Color> colorsStart;
	ArrayList<Color> colorsFinal;

	public ShinyPanel(int particlesUpdate) {
		pointX = new ArrayList<Double>();
		pointY = new ArrayList<Double>();
		particles = new ArrayList<Particle>(11000);
		setBackground(Color.black);
		particlesPerUpdate = particlesUpdate;
	}

	int particlesPerUpdate = 30;

	protected void clearScreen(Graphics g) {
		super.paintComponent(g);
	}

	public void paintComponent(Graphics g) {
		for (int i = 0; i < particles.size(); i++)
			particles.get(i).draw(g);
	}

	public static Color getColor(Color orig, int alpha) {
		return new Color(orig.getRed(), orig.getGreen(), orig.getBlue(), alpha);
	}

	public void update() {
		for (int i = 0; i < particles.size(); i++)
			particles.get(i).update();
		for (int i = 0; i < particles.size();)
			if (particles.get(i).y > getHeight() || particles.get(i).time <= 0)
				particles.remove(i);
			else
				i++;
		for (int b = 0; b < pointX.size(); b++)
			shoot(pointX.get(b), pointY.get(b), colorsStart.get(b),
					colorsFinal.get(b), 0, 0, particles);
	}

	protected void shoot(double x, double y, Color start, Color end,
			double velocityX, double velocityY, ArrayList<Particle> toAdd) {
		for (int i = 0; i < particlesPerUpdate; i++) {
			toAdd.add(new Particle(Math.random() * 2 * Math.PI, x, y, start,
					end, velocityX, velocityY));
		}
	}

	class Particle {
		double x, y, velocityX, velocityY;
		Color startColor;
		Color finalColor;
		double time;
		int startTime;

		public Particle(double angle, double x1, double y1, Color c, Color c2,
				double velocityOffX, double velocityOffY) {
			startColor = new Color(baseColorPart(c.getRed()),
					baseColorPart(c.getGreen()), baseColorPart(c.getBlue()));
			finalColor = new Color(baseColorPart(c2.getRed()),
					baseColorPart(c2.getGreen()), baseColorPart(c2.getBlue()));
			time = (int) (Math.random() * 35 + 70);
			startTime = (int) time;
			velocityX = Math.cos(angle) * initialVelocity + velocityOffX;
			velocityY = Math.sin(angle) * initialVelocity + velocityOffY;
			previousX = (int) (x = x1);
			previousY = (int) (y = y1);
		}

		private int getColorPart(int start, int finalC) {
			double frac = time / startTime;
			double oppFrac = 1 - frac;
			return (int) Math.min(
					Math.max((start * frac + finalC * oppFrac), 0), 255);
		}

		int previousX;
		int previousY;

		public void update() {
			time--;
			previousX = (int) x;
			previousY = (int) y;
			x += velocityX;
			y += velocityY;
			velocityY += acceleration;
		}

		public void draw(Graphics g) {
			g.setColor(new Color(getColorPart(startColor.getRed(),
					finalColor.getRed()), getColorPart(startColor.getGreen(),
					finalColor.getGreen()), getColorPart(startColor.getBlue(),
					finalColor.getBlue()), getColorPart(255, 0)));
			g.drawLine((int) x, (int) y, previousX, previousY);
		}
	}

	boolean running = false;
	boolean toStop = false;

	double acceleration = 0.01;
	double initialVelocity = 1;
	static int colorRange = 120;

	public static int baseColorPart(int value) {
		return (int) Math.min(Math.max(
				(Math.random() * colorRange + value - colorRange / 2), 0), 255);
	}
}

class MasterDama extends DamaYama implements ActionListener {

	public MasterDama() {
		super("Master Dama Yama");
		username = "Da Masta";
		JMenuBar menuBar = new JMenuBar();
		JMenu file = new JMenu("File");
		JMenu edit = new JMenu("Edit");
		JMenu game = new JMenu("Game");

		JMenuItem newFile = new JMenuItem("New");
		newFile.addActionListener(this);
		newFile.setActionCommand("new");
		newFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,
				ActionEvent.ALT_MASK + ActionEvent.SHIFT_MASK));
		file.add(newFile);

		file.addSeparator();

		JMenuItem open = new JMenuItem("Open");
		open.addActionListener(this);
		open.setActionCommand("open");
		open.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
				ActionEvent.CTRL_MASK));
		file.add(open);

		file.addSeparator();

		JMenuItem save = new JMenuItem("Save");
		save.addActionListener(this);
		save.setActionCommand("save");
		save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
				ActionEvent.CTRL_MASK));
		save.setMnemonic('s');
		file.add(save);

		JMenuItem current = new JMenuItem("Current");
		current.addActionListener(this);
		current.setActionCommand("current");
		current.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E,
				ActionEvent.CTRL_MASK));
		current.setMnemonic('c');
		edit.add(current);

		edit.addSeparator();

		JMenuItem stretch = new JMenuItem("Stretch");
		stretch.addActionListener(this);
		stretch.setActionCommand("stretch");
		stretch.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
				ActionEvent.SHIFT_MASK));
		stretch.setMnemonic('s');
		edit.add(stretch);

		JMenuItem play = new JMenuItem("Play");
		play.addActionListener(this);
		play.setActionCommand("play");
		play.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));
		play.setMnemonic('p');
		game.add(play);

		game.addSeparator();

		JMenuItem host = new JMenuItem("Host Game");
		host.addActionListener(this);
		host.setActionCommand("host");
		host.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0));
		host.setMnemonic('h');
		game.add(host);

		JMenuItem join = new JMenuItem("Join Game");
		join.addActionListener(this);
		join.setActionCommand("join");
		join.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0));
		join.setMnemonic('j');
		game.add(join);

		JMenuItem replay = new JMenuItem("Replay Game");
		replay.addActionListener(this);
		replay.setActionCommand("replay");
		replay.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0));
		replay.setMnemonic('r');
		game.add(replay);

		menuBar.add(file);
		menuBar.add(edit);
		menuBar.add(game);
		this.setJMenuBar(menuBar);
		this.setBounds(0, 0, 1000, 500);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setVisible(true);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("save"))
			new MapSaveFrame(Map.getCurrent());
		else if (e.getActionCommand().equals("new"))
			new MapNewFrame(this);
		else if (e.getActionCommand().equals("open"))
			new MapOpenFrame(this);
		else if (e.getActionCommand().equals("stretch")) {
			String stretch = JOptionPane.showInputDialog(this,
					"By what factor would you like to stretch the map?");
			try {
				Map.getCurrent().stretch(Integer.parseInt(stretch));
			} catch (Exception e2) {
			}
		} else if (e.getActionCommand().equals("play")) {
			setContentPane(new BoxHeadPanel(this));
			validate();
		} else if (e.getActionCommand().equals("host")) {
			String s = JOptionPane.showInputDialog(
					"What game name would you like to host?", "Game.txt");
			String s2 = JOptionPane.showInputDialog("How many players?", "2");
			try {
				setContentPane(new MultiPlayerPanel(this, Integer.parseInt(s2),
						s, ""));
			} catch (Exception e2) {
			}
			validate();
		} else if (e.getActionCommand().equals("join")) {
			String s = JOptionPane.showInputDialog(
					"What game name would you like to join?", "Game.txt");
			setContentPane(new MultiPlayerPanel(this, s));
			validate();
		} else if (e.getActionCommand().equals("replay")) {
			String s = JOptionPane.showInputDialog(
					"What game name would you like to replay?",
					"LastReplay.txt");
			setContentPane(new ReplayPanel(this, s));
			validate();
		} else if (e.getActionCommand().equals("current")) {
			Map.setNewMap(this, Map.getCurrent());
		}
	}
}