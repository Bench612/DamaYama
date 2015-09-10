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
		} else if (e.getKeyCode() == KeyEvent.VK_TAB) {

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

class BoxHeadPanel extends PlayPanel {
	static int wave = 0;

	public BoxHeadPanel() {
		super();
		innerPanel = new StatPanel(this);
		running = true;
		new Thread(this).start();
	}

	public void start() {
		super.start();
		players = new ArrayList<Player>();
		if (allKeys == null) {
			players.add(new Player(this, 0));
			directionX = new int[1];
			directionY = new int[1];
		} else {
			for (int i = 0; i < allKeys.length; i++)
				players.add(new Player(this, i));
			mainSleepSpeed = normalMainSleepSpeed / allKeys.length;
		}
		for (int i = 0; i < players.size(); i++)
			addGameObject(players.get(i));
		wave = -1;
		startNewWave();
		for (int i = 0; i < 1; i++)
			addGameObject(new BasicEnemy());
	}

	int spawnsThisWave = 0;

	private void startNewWave() {
		wave++;
		spawnsThisWave = 0;
		displayMessage("Wave " + wave);
	}

	public double numberForWave() {
		return 10 * Math.pow(2, wave);
	}

	protected void addGameObject(MovingGameObject object) {
		super.addGameObject(object);
		spawnsThisWave += object.getPointCount();
	}

	public void updateSpawns() {
		super.updateSpawns();
		if (spawnsThisWave >= numberForWave()) {
			if (gameObjects.size() == players.size())
				startNewWave();
		} else if (timeSinceLastSpawn > 1 / (spawnCoeffecient * (wave + 1) / 100)) {
			double random = DamaYama.random();
			if (random > .4)
				addGameObject(new BasicEnemy());
			else if (random > .2)
				addGameObject(new SporeParent());
			else if (random > .11)
				addGameObject(new Demon());
			else
				addGameObject(new Jackal());
			timeSinceLastSpawn = 0;
		} else
			timeSinceLastSpawn++;
	}

	int timeSinceLastSpawn = 0;

	double spawnCoeffecient = 1;
	double decayCoeffecient = 0.00003;

	public void paintComponent(Graphics g) {
		if (!started)
			return;
		super.paintComponent(g);
		Font original = g.getFont();
		Font fontLarge = original.deriveFont(32.0f);
		g.setFont(fontLarge);
		int height = g.getFontMetrics().getAscent();
		g.setColor(Color.black);
		g.drawString(
				getPlayer().weapon + ": Ammo - " + getPlayer().weapon.ammo, g
						.getFontMetrics().getHeight() + 5, g.getFontMetrics()
						.getHeight() + 5);
		g.setFont(original);
		int y = drawMessages(g, getWidth() / 2, height + 10);
		g.setColor(Color.blue.darker().darker());
		FontMetrics fm = g.getFontMetrics();
		g.drawString(chatString, getWidth() / 2 - fm.stringWidth(chatString)
				/ 2, y + fm.getHeight());
		Polygon outline = new Polygon();
		Polygon poly = getPlayer().weapon.drawStatDiagram(5, 5, g
				.getFontMetrics().getHeight() * 2, g.getFontMetrics()
				.getHeight() * 2, outline);
		g.drawPolygon(outline);
		g.setColor(Color.red);
		g.fillPolygon(poly);
	}

	public void addNewWeapon(Weapon w) {
		if (getPlayer().getWeapon(w.getClass()) == null)
			getPlayer().addNewWeapon(w);
	}
}

class ReplayPanel extends MultiPlayerPanel {
	public ReplayPanel(String file) {
		super(file);
	}

	public boolean write(byte[] bytes) {
		return true;
	}

	public void keyPressed(KeyEvent e) {
		super.keyPressed(e);
		if (e.getKeyCode() == KeyEvent.VK_RIGHT
				|| e.getKeyCode() == KeyEvent.VK_D) {
			playerIndex++;
			if (playerIndex >= players.size())
				playerIndex = 0;
		} else if (e.getKeyCode() == KeyEvent.VK_LEFT
				|| e.getKeyCode() == KeyEvent.VK_A) {
			playerIndex--;
			if (playerIndex < 0)
				playerIndex = players.size() - 1;
		} else if (e.getKeyCode() == KeyEvent.VK_W
				|| e.getKeyCode() == KeyEvent.VK_UP) {
			mainSleepSpeed /= 2;
			mainSleepSpeed = Math.max(mainSleepSpeed, 5);
		} else if (e.getKeyCode() == KeyEvent.VK_S
				|| e.getKeyCode() == KeyEvent.VK_DOWN) {
			mainSleepSpeed *= 2;
			mainSleepSpeed = Math.min(mainSleepSpeed, 80);
		}
	}
}

class MultiPlayerPanel extends BoxHeadPanel {
	public static final String extension = ".game";
	static File lastGameReplay;
	String fileName;
	boolean host, addedIntoGame = false;
	int currentUpdatingIndex = 0;// it is a player index for updating
	long bytesRead = 0;

	protected boolean saveTempReplay() {
		try {
			lastGameReplay = new File("LastReplay.txt");
			copyFile(new File(fileName), lastGameReplay);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	public static final void copyFile(File original, File newFile)
			throws IOException {
		FileInputStream stream = new FileInputStream(original);
		FileWriter writer = new FileWriter(newFile);
		while (stream.available() > 0)
			writer.write(stream.read());
		stream.close();
		writer.close();
	}

	public void tryClose() {
		super.tryClose();
		while (!saveTempReplay()) {
			System.out.println("failed to save replay");
		}
	}

	public void paintComponent(Graphics g) {
		if (addedIntoGame && started) {
			super.paintComponent(g);
		} else {
			g.setColor(Color.white);
			g.fillRect(0, 0, getWidth(), getHeight());
			Font orig = g.getFont();
			g.setFont(orig.deriveFont(32f));
			String message;
			if (addedIntoGame)
				message = "Please wait... Waiting for other players";
			else if (doneHosting || !host)
				message = "Please wait... Adding into game";
			else
				message = "Please wait... Hosting";
			g.setColor(Color.black);
			g.drawString(message, 0, g.getFontMetrics().getHeight());
			g.setFont(orig);
		}
	}

	String toWriteTo = "";// writes on a file after its done hosting

	public MultiPlayerPanel(int players, String file, String fileToWriteOn) {
		host = true;
		fileName = file;
		toWriteTo = fileToWriteOn;
		allKeys = new boolean[players][keys.length];
		directionX = new int[keys.length];
		directionY = new int[keys.length];
		allPreviousKeys = new boolean[players][previousKeys.length];
	}

	public void focusLost(FocusEvent e) {
	}

	public MultiPlayerPanel(String file) {
		host = false;
		fileName = file;
	}

	public static final int maxArray = 7;

	public boolean write(boolean[] keys) {
		try {
			FileWriter w = new FileWriter(fileName, true);
			for (int startIndex = 0; startIndex < keys.length; startIndex += maxArray)
				write(w, keys, startIndex, startIndex + maxArray);
			w.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean write(int a, int b) {
		try {
			FileWriter w = new FileWriter(fileName, true);
			if (a < 0)
				w.write(negative);
			else
				w.write(positive);
			w.write(Math.abs(a));
			if (b < 0)
				w.write(negative);
			else
				w.write(positive);
			w.write(Math.abs(b));
			w.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	// end index is non inclusive
	private static void write(FileWriter w, boolean[] keys, int startIndex,
			int endIndex) {
		if (keys.length < endIndex)
			endIndex = keys.length;
		byte number = 0;
		for (int i = startIndex; i < endIndex; i++) {
			if (keys[i])
				number += Math.pow(2, i - startIndex);
		}
		try {
			w.write(number);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	final static int negative = 0;
	final static int positive = 1;

	public long readNewDirections(int index, boolean limit) {
		long timesSlept = 0;
		closeInputStream();
		resetInputStream();
		try {
			while (inputStream.available() < 4) {
				closeInputStream();
				if (timesSlept > 1000 && limit)
					return timesSlept;
				try {
					timesSlept++;
					timeSinceUpdate += sleep;
					Thread.sleep(sleep);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				resetInputStream();
			}
			try {
				int a2 = inputStream.read();
				int a = inputStream.read();
				int b2 = inputStream.read();
				int b = inputStream.read();
				directionX[index] = a;
				if (a2 == negative)
					directionX[index] *= -1;
				directionY[index] = b;
				if (b2 == negative)
					directionY[index] *= -1;
				bytesRead += 4;
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		closeInputStream();
		return timesSlept;
	}

	public long read(boolean[] allKeys, boolean limit) {
		long timesSlept = 0;
		closeInputStream();
		resetInputStream();
		try {
			while (inputStream.available() < Math.ceil(allKeys.length
					/ (double) maxArray)) {
				closeInputStream();
				if (timesSlept > 1000 && limit)
					return timesSlept;
				try {
					timesSlept++;
					timeSinceUpdate += sleep;
					Thread.sleep(sleep);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				resetInputStream();
			}
			for (int startIndex = 0; startIndex < allKeys.length; startIndex += maxArray)
				read(inputStream, allKeys, startIndex, startIndex + maxArray);
		} catch (IOException e) {
			e.printStackTrace();
		}
		closeInputStream();
		return timesSlept;
	}

	// end index is non inclusive
	private void read(FileInputStream stream, boolean[] allKeys,
			int startIndex, int endIndex) {
		if (allKeys.length < endIndex)
			endIndex = allKeys.length;
		try {
			int number = stream.read();
			bytesRead++;
			for (int i = endIndex - 1; i >= startIndex; i--) {
				if ((number / Math.pow(2, i - startIndex) < 1))
					allKeys[i] = false;
				else {
					allKeys[i] = true;
					number -= Math.pow(2, i - startIndex);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static final int sleep = 2;
	FileInputStream inputStream = null;

	public void update() {
		if (addedIntoGame && started) {
			// currentUpdatingIndex should be zero at the start!
			while (currentUpdatingIndex < playerIndex) {
				if (readPart())
					return;
			}
			writePart();
			// now finishes reading the rest
			while (currentUpdatingIndex < allKeys.length) {
				if (readPart())
					return;
			}
			currentUpdatingIndex = 0;
			super.update();
		}
	}

	private boolean writePart() {
		// writes my current keys
		while (!write(keys)) {
			try {
				Thread.sleep(sleep);
			} catch (InterruptedException e) {
				e.printStackTrace();
				return false;
			}
		}
		while (!write(newShootDirectionX, newShootDirectionY)) {
			try {
				Thread.sleep(sleep);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return true;
	}

	private boolean readPart() {
		if (read(allKeys[currentUpdatingIndex], true) > 5000 / sleep) {
			JOptionPane.showMessageDialog(this, "Latency too high!");
			tryClose();
			return true;
		}
		if (readNewDirections(currentUpdatingIndex, true) > 5000 / sleep) {
			JOptionPane.showMessageDialog(this, "Latency too high!");
			tryClose();
			return true;
		}
		currentUpdatingIndex++;
		return false;
	}

	public void updatePlayers() {
		for (int i = 0; i < players.size(); i++) {
			players.get(i).update(allKeys[i], allPreviousKeys[i],
					directionX[i], directionY[i]);
		}
	}

	public static final int seedParts = 3;

	boolean doneHosting = false;

	public void innerRun() {
		if (!addedIntoGame) {
			if (host && !doneHosting) {
				try {
					FileWriter writer = new FileWriter(fileName);
					writer.write(allKeys.length);
					for (int i = 0; i < seedParts; i++)
						writer.write((byte) (Math.random() * 256));
					Map.getCurrent().write(writer);
					writer.close();
					FileWriter clear = new FileWriter(chatFile);
					clear.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				doneHosting = true;
			}
			repaint();
			while (!write(keys)) {
				try {
					Thread.sleep(sleep);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			repaint();
			// now tries to get its currentIndex
			while (inputStream == null) {
				playerIndex = -1;
				resetInputStream();
				int players;
				try {
					players = inputStream.read();
					bytesRead++;
					allKeys = new boolean[players][keys.length];
					directionX = new int[keys.length];
					directionY = new int[keys.length];
					allPreviousKeys = new boolean[players][previousKeys.length];
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				try {
					long seed = 0;
					for (int i = 0; i < seedParts; i++) {
						seed += inputStream.read();
						bytesRead++;
					}
					DamaYama.setSeed(seed);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				// now loads the map
				try {
					bytesRead += Map.setNewMapKeep(inputStream);
					Map.getCurrent().compile();
				} catch (IOException e) {
					e.printStackTrace();
				}
				closeInputStream();
				addedIntoGame = true;
				if (host) {
					if (toWriteTo.length() > 1) {
						FileWriter writer = null;
						while (writer == null) {
							try {
								writer = new FileWriter(toWriteTo);
							} catch (IOException e) {
								e.printStackTrace();
							}
							if (writer == null)
								try {
									Thread.sleep(sleep);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
						}
						try {
							writer.write(fileName);
							writer.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
				// now waits for other players
				for (currentUpdatingIndex = 0; currentUpdatingIndex < allKeys.length; currentUpdatingIndex++) {
					if ((read(allPreviousKeys[currentUpdatingIndex], false) >= 1)
							&& playerIndex == -1)
						playerIndex = currentUpdatingIndex - 1;
				}
				if (playerIndex == -1)
					playerIndex = allKeys.length - 1;
				System.out.println("player #" + playerIndex + " out of "
						+ allKeys.length);
				// now resets the currentUpdatingIndex
				currentUpdatingIndex = 0;
			}
		}
	}

	private void closeInputStream() {
		try {
			inputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void resetInputStream() {
		try {
			long actualSkip = -1;
			inputStream = new FileInputStream(fileName);
			actualSkip = inputStream.skip(bytesRead);
			while (actualSkip != bytesRead) {
				closeInputStream();
				System.out.println(actualSkip + "actual Skip" + bytesRead);
				try {
					Thread.sleep(sleep);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				inputStream = new FileInputStream(fileName);
				actualSkip = inputStream.skip(bytesRead);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

class Effect {
	int timeLeft;

	public Effect(int timeLimit) {
		timeLeft = timeLimit;
	}

	public void draw(Graphics g, int xOff, int yOff) {
		timeLeft--;
		if (timeLeft <= 0)
			PlayPanel.currentRunning.effects.remove(this);
	}
	public void drawSlant(Perspective p){
		timeLeft--;
		if (timeLeft <= 0)
			PlayPanel.currentRunning.effects.remove(this);
	}
}
class BoxEffect extends Effect{
	double x, y, z;
	Color color;
	public BoxEffect(double x1, double y1, double z1, Color color) {
		super(5);
		x = x1;
		y = y1;
		z = z1;
		this.color = color;
	}
	
	static Color clear = new Color(0,0,0,0);
	public void drawSlant(Perspective p){
		p.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 55 + (int)(200 * timeLeft / 5)));
		p.setOutline(clear);
		Map.drawSquare(p, x, y, z, 1);
		ArrayList<Point> top = p.getCurrentShape();
		Map.drawSquare(p, x, y, z + 2, 1);
		ArrayList<Point> bottom = p.getCurrentShape();
		p.fillForm(p.createForm(top, bottom));
		p.fillShape(top);
		super.drawSlant(p);
	}
	
}
class SphereEffect extends Effect{
	double x, y, z, radius;

	public SphereEffect(double x1, double y1, double z1, double radius1) {
		super(3);
		x = x1;
		y = y1;
		z = z1;
		radius = radius1;
	}

	public void draw(Graphics g, int xOff, int yOff) {
		g.setColor(sphereColor);
		g.fillOval((int)((x- radius)*Space.SIZE)+ xOff , (int)((y - radius)*Space.SIZE) + yOff, (int)(radius * 2 * Space.SIZE), (int) (radius *  2 * Space.SIZE));
		super.draw(g, xOff, yOff);
	}
	
	static Color sphereColor = new Color(183,29,29,100);
	static Color clear = new Color(0,0,0,0);
	public void drawSlant(Perspective p){
		p.setColor(sphereColor);
		p.setOutline(clear);
		p.drawSphere(x, y, z, radius, 0, 6, 4);
		super.drawSlant(p);
	}
	
}
class CircleEffect extends Effect {
	double x, y, z, radius;

	public CircleEffect(double x1, double y1, double z1, double radius1) {
		super(3);
		x = x1;
		y = y1;
		z = z1;
		radius = radius1;
	}

	public void draw(Graphics g, int xOff, int yOff) {
		g.setColor(explosionColor);
		g.fillOval((int)((x- radius)*Space.SIZE)+ xOff , (int)((y - radius)*Space.SIZE) + yOff, (int)(radius * 2 * Space.SIZE), (int) (radius *  2 * Space.SIZE));
		super.draw(g, xOff, yOff);
	}
	static Color explosionColor = new Color(255,250,205,170);
	public void drawSlant(Perspective p){
		p.setColor(explosionColor);
		p.setOutline(explosionColor);
		p.drawDome(x, y, z, radius, 0, 20, 15);
		super.drawSlant(p);
	}
}