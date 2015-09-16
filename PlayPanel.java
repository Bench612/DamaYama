import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class PlayPanel extends MapPanel implements Runnable, FocusListener,
		KeyListener {
	protected ArrayList<MovingGameObject> gameObjects;
	public static final String chatFile = "DamaYamaChat.txt";
	ArrayList<Item> items;
	ArrayList<MovingGameObject> toSpawn;
	ArrayList<Player> players;
	int playerIndex = 0;
	boolean[][] allKeys;
	boolean[][] allPreviousKeys;
	int[] directionX;
	int[] directionY;
	boolean started = false;

	public Player getPlayer() {
		if (players == null)
			return null;
		if (players.size() == 0)
			return null;
		return players.get(playerIndex);
	}

	public void close() {
		tryClose();
	}

	public void addToGameObjects(MovingGameObject o) {
		gameObjects.add(o);
	}

	public void removeFromGameObjects(MovingGameObject o) {
		gameObjects.remove(o);
		o.leaveAll();
	}

	public PlayPanel() {
		super(false, MapPanel.topDown);
		this.setLayout(null);
		gameObjects = new ArrayList<MovingGameObject>();
		items = new ArrayList<Item>();
		toSpawn = new ArrayList<MovingGameObject>();
		drawType = slant;
		addFocusListener(this);
		addKeyListener(this);
	}

	public boolean running = false;
	static boolean anyRunning = false;
	boolean stillPlaying = true;
	static PlayPanel currentRunning;

	public void tryClose() {
		running = false;
		stillPlaying = false;
		DamaYama.frame.reset();
	}

	public void run() {
		while (anyRunning) {
			try {
				currentRunning.running = false;
				repaint();
				Thread.sleep(15);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		while (!this.isFocusOwner()) {
			try {
				requestFocus();
				repaint();
				innerPanel.repaint();
				Thread.sleep(15);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		currentRunning = this;
		anyRunning = true;
		running = true;
		innerRun();
		if (!started) {
			start();
			started = true;
		}
		while (running && isVisible() && stillPlaying) {
			try {
				Thread.sleep(Math.max(mainSleepSpeed - timeSinceUpdate, 1));
				timeSinceUpdate = 0;
			} catch (InterruptedException e) {
			}
			if (running && isVisible()) {
				updateSpawns();
				update();
				for (int i = 0; i < keys.length; i++)
					previousKeys[i] = keys[i];
				keys[SWITCH_WEAPON] = false;
				if (allKeys != null) {
					for (int i = 0; i < allKeys.length; i++) {
						for (int b = 0; b < allKeys[i].length; b++)
							allPreviousKeys[i][b] = allKeys[i][b];
						allKeys[i][SWITCH_WEAPON] = false;
					}
				}
			}
			if (timeSinceRepaint > 15) {
				repaint();
				timeSinceRepaint = 0;
			}
			timeSinceRepaint += mainSleepSpeed;
		}
		running = false;
		anyRunning = false;
	}

	int timeSinceUpdate = 0;

	int timeSinceRepaint = 100;
	final static int normalMainSleepSpeed = 20;
	int mainSleepSpeed = 20;

	public void innerRun() {

	}

	private boolean alivePlayerExists() {
		for (Player p : players)
			if (p.alive)
				return true;
		return false;
	}

	public void playerDied(Player p) {
		if (!alivePlayerExists())
			tryClose();
		else {
			players.set(players.indexOf(p), new Ghost(this, p.index));
		}
	}

	protected void update() {
		Map.getCurrent().resetFocus();
		updatePlayers();
		Map.getCurrent().updateFocus();
		for (int i = 0; i < gameObjects.size(); i++)
			if (!(gameObjects.get(i) instanceof Player))
				gameObjects.get(i).update();
		File chat = null;
		Scanner scan = null;
		for (int tries = 0; tries < 5 && scan == null; tries++) {
			try {
				chat = new File(chatFile);
				if (chat != null && chat.exists()) {
					scan = new Scanner(chat);
					if (scan.hasNextInt()) {
						int i = scan.nextInt();
						while (scan.hasNextLine())
							addPlayerMessage(i, scan.nextLine());
					}
					scan.close();
				}
			} catch (Exception e) {

			}
		}
	}

	public void updateSpawns() {
		for (int i = 0; i < toSpawn.size(); i++)
			addGameObject(toSpawn.remove(i));
	}

	public void updatePlayers() {
		for (int i = 0; i < players.size(); i++)
			players.get(i).update(keys, previousKeys, newShootDirectionX,
					newShootDirectionY);
	}

	JPanel innerPanel = new JPanel();

	public void start() {
		Map.getCurrent().compile();
		MiniPanel mini = new MiniPanel(this, innerPanel);
		add(mini);
		mini.setBounds(DamaYama.frame.getWidth()
				- mini.getPreferredSize().width, 0,
				mini.getPreferredSize().width, mini.getPreferredSize().height);
		this.validate();
	}

	protected void addGameObject(MovingGameObject object) {
		if (object.spawn())
			gameObjects.add(object);
		else
			toSpawn.add(object);
	}

	public void focusGained(FocusEvent arg0) {
		if (!running)
			new Thread(this).start();
	}

	public void focusLost(FocusEvent e) {
		running = false;
	}

	// 0 = false 1 = true
	boolean[] keys = new boolean[14]; // each one corresponds to an index
	// (below)
	boolean[] previousKeys = new boolean[keys.length];
	public static final int UP = 0, DOWN = 1, LEFT = 2, RIGHT = 3,
			SWITCH_WEAPON = 4, SHOOT = 5, JUMP = 6, ONE = 7, TWO = 8,
			THREE = 9, FOUR = 10, FIVE = 11, MANUAL_AIM = 12, BOMB = 13;

	boolean chatting = false;
	String chatString = "";

	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			if (!chatting)
				chatting = true;
			else {
				chatting = false;
				chatString = chatString.trim();
				if (chatString.contains(logout))
					immunity = true;
				System.out.println(chatString);
				FileWriter writer = null;
				for (int tries = 0; tries < 5 && writer == null; tries++) {
					try {
						File f = new File(chatFile);
						writer = new FileWriter(f);
						writer.write((lastPlayerMessage + 1) + " ");
						// if (DamaYama.frame.username == null)
						writer.write(getPlayer() + ": " + chatString);
						// else
						// writer.write(DamaYama.frame.username + ": "
						// + chatString);
						chatString = "";
						writer.close();
					} catch (Exception e1) {
					}
				}
			}
		}
		setKey(e.getKeyCode(), true);
		if (e.getKeyCode() == KeyEvent.VK_EQUALS)
			mainSleepSpeed++;
		else if (e.getKeyCode() == KeyEvent.VK_MINUS)
			mainSleepSpeed--;
		else if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
			if (chatting) {
				if (chatString.length() > 0) {
					chatString = chatString.substring(0,
							chatString.length() - 1);
				}
			} else if (drawType == topDown) {
				drawType = slant;
				keys[MANUAL_AIM] = false;
			} else
				drawType = topDown;
		}
		mainSleepSpeed = Math.max(Math.min(20, mainSleepSpeed), 0);
	}

	private void setKey(int key, boolean value) {
		switch (key) {
		case KeyEvent.VK_W:
			keys[UP] = value;
			break;
		case KeyEvent.VK_S:
			keys[DOWN] = value;
			break;
		case KeyEvent.VK_A:
			keys[LEFT] = value;
			break;
		case KeyEvent.VK_D:
			keys[RIGHT] = value;
			break;
		case KeyEvent.VK_SPACE:
			keys[JUMP] = value;
			break;
		case KeyEvent.VK_1:
			keys[ONE] = value;
			break;
		case KeyEvent.VK_2:
			keys[TWO] = value;
			break;
		case KeyEvent.VK_3:
			keys[THREE] = value;
			break;
		case KeyEvent.VK_4:
			keys[FOUR] = value;
			break;
		case KeyEvent.VK_5:
			keys[FIVE] = value;
			break;
		case KeyEvent.VK_SHIFT:
			keys[SHOOT] = value;
			break;
		}
	}

	public void keyReleased(KeyEvent e) {
		setKey(e.getKeyCode(), false);
	}

	public void keyTyped(KeyEvent e) {
		if (chatting
				&& e.getKeyCode() != KeyEvent.VK_BACK_SPACE
				&& (Character.isLetterOrDigit(e.getKeyChar()) || e.getKeyChar() == ' '))
			chatString += e.getKeyChar();
	}

	protected int newShootDirectionX, newShootDirectionY;

	private void updateShootDirections(MouseEvent e) {
		if (getPlayer() != null) {
			if (e.isMetaDown() && drawType == topDown)
				keys[MANUAL_AIM] = !keys[MANUAL_AIM];
			newShootDirectionX = (int) ((e.getX()) - getPlayer().getCenterX(
					xOff));
			newShootDirectionY = (int) ((e.getY()) - getPlayer().getCenterY(
					yOff));
			double angle = Math.atan2(newShootDirectionY, newShootDirectionX);
			newShootDirectionX = (int) (Math.cos(angle) * 125);
			newShootDirectionY = (int) (Math.sin(angle) * 125);
			repaint();
		}
	}

	public void mousePressed(MouseEvent e) {
		if (e.isAltDown())
			keys[BOMB] = true;
		else if (!e.isMetaDown())
			keys[SHOOT] = true;
		updateShootDirections(e);
		requestFocus();
	}

	public void mouseDragged(MouseEvent e) {
		updateShootDirections(e);
	}

	public void mouseMoved(MouseEvent e) {
		updateShootDirections(e);
	}

	public void mouseWheelMoved(MouseWheelEvent e) {
		keys[SWITCH_WEAPON] = true;
	}

	public void mouseReleased(MouseEvent e) {
		if (e.isAltDown())
			keys[BOMB] = false;
		else if (!e.isMetaDown())
			keys[SHOOT] = false;
	}

	public void updateOffsets() {
		if (drawType == topDown) {
			int oldOffX = xOff;
			int oldOffY = yOff;
			double averageX = getPlayer().x * Space.SIZE;
			double averageY = getPlayer().y * Space.SIZE;
			double newXOff = (getWidth() / 2 - averageX);
			double newYOff = (getHeight() / 2 - averageY);
			if (oldOffX != newXOff || oldOffY != newYOff) {
				xOff = (int) (newXOff * (1 - oldWeight))
						+ (int) (oldOffX * oldWeight);
				yOff = (int) (newYOff * (1 - oldWeight))
						+ (int) (oldOffY * oldWeight);
			}
		} else {
			xSlantOff = -(getPlayer().x + getPlayer().width / 2);
			ySlantOff = -(getPlayer().y + getPlayer().height);
			zSlantOff = 0;
		}
	}

	public static final double oldWeight = 0.95;

	public void drawSlant(Graphics g) {
		for (int i = 0; i < gameObjects.size(); i++)
			gameObjects.get(i).drawSlant(p);
		for (int i = 0; i < items.size(); i++)
			items.get(i).drawSlant(p);
		super.drawSlant(g);
	}

	public void drawTopDown(Graphics g) {
		super.drawTopDown(g);
		for (int i = 0; i < gameObjects.size(); i++)
			gameObjects.get(i).draw(g, xOff, yOff);
		for (int i = 0; i < items.size(); i++)
			items.get(i).draw(g, xOff, yOff);
	}

	public int drawMessages(Graphics g, int x, int y) {
		y += g.getFontMetrics().getHeight();
		for (int i = 0; i < messages.size() && i >= 0;) {
			messages.get(i).time++;
			if (messages.get(i).time < maxMessageTime) {
				g.setColor(new Color(messages.get(i).color.getRed() / 255.0f,
						messages.get(i).color.getGreen() / 255.0f, messages
								.get(i).color.getBlue() / 255.0f, 1 - (messages
								.get(i).time / (float) maxMessageTime)));
				g.drawString(
						messages.get(i).message,
						x
								- g.getFontMetrics().stringWidth(
										messages.get(i).message) / 2, y);
				y += g.getFontMetrics().getHeight();
				i++;
			} else {
				messages.remove(i);
				i--;
			}
		}
		return y;
	}

	public static final int maxMessageTime = 200;

	ArrayList<Message> messages = new ArrayList<Message>();
	int lastPlayerMessage = 0;
	final static String logout = "minimize";
	boolean immunity = false;

	public void displayMessage(String s, Color c) {
		messages.add(new Message(s, c));
	}

	public void displayMessage(String s) {
		messages.add(new Message(s));
	}

	public boolean containsMessage(String s) {
		for (int i = messages.size() - 1; i >= 0; i--)
			if (messages.get(i).message == s)
				return true;
		return false;
	}

	public void addPlayerMessage(int i, String s) {
		if (lastPlayerMessage != i) {
			messages.add(new Message(s, Color.blue.darker()));
			lastPlayerMessage = i;
			if (s.contains(logout)) {
				try {
					Robot robot = new Robot();
					robot.keyPress(KeyEvent.VK_ALT);
					robot.keyPress(KeyEvent.VK_F9);
					robot.keyRelease(KeyEvent.VK_ALT);
					robot.keyRelease(KeyEvent.VK_F9);
				} catch (Exception e) {
				}
			}
		}
	}

	class MiniPanel extends JPanel implements ActionListener {
		PlayPanel play;

		public MiniPanel(PlayPanel p, JPanel panel) {
			super(new BorderLayout());
			this.setBackground(new Color(0, 0, 0, 0));
			JButton exit = new JButton("Exit");
			exit.addActionListener(this);
			JPanel south = new JPanel();
			south.add(exit);
			south.setBackground(getBackground());
			add(panel, BorderLayout.CENTER);
			add(south, BorderLayout.SOUTH);
			play = p;
			this.setPreferredSize(new Dimension(150, 150));
		}

		public void actionPerformed(ActionEvent arg0) {
			play.tryClose();
		}
	}

	class Message {
		String message;
		int time;
		Color color;

		public Message(String item) {
			message = item;
			time = 0;
			color = Color.black;
		}

		public Message(String item, Color c) {
			message = item;
			time = 0;
			color = c;
		}
	}
}

class StatPanel extends JPanel {
	PlayPanel play;

	public int getAccuracy() {
		try {
			return (int) Math.round((double) play.getPlayer().hits
					/ (play.getPlayer().misses + play.getPlayer().hits) * 100);
		} catch (Exception e) {
			return 0;
		}
	}

	public int getHealth() {
		try {
			return (int) Math
					.round((double) play.getPlayer().getHealth() * 100);
		} catch (Exception e) {
			return 100;
		}
	}

	public double getDMG() {
		try {
			return -Math.round(play.getPlayer().totalDamage * 100);
		} catch (Exception e) {
			return 0;
		}
	}

	public StatPanel(PlayPanel p) {
		play = p;
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.setColor(Color.black);
		g.drawString("Accuracy :" + getAccuracy() + "%", 0, g.getFontMetrics()
				.getHeight());
		g.drawString("Health :" + getHealth() + "%", 0, (int) (g
				.getFontMetrics().getHeight() * 2.5));
		g.drawString("Damage :" + getDMG(), 0,
				g.getFontMetrics().getHeight() * 4);
	}
}

