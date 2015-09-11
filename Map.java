import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;

public class Map {
	private static Map current;
	public static final String mapFileName = "Maps.txt";
	Space[][] spaces;

	public Map(Space[][] space) {
		spaces = space;
	}

	public static Map getCurrent() {
		return current;
	}

	ArrayList<Spawn> spawns;
	ArrayList<TeleportReceiver> teleportReceivers;
	ArrayList<Teleport> teleports;

	public void compile() {
		spawns = new ArrayList<Spawn>();
		teleportReceivers = new ArrayList<TeleportReceiver>();
		teleports = new ArrayList<Teleport>();
		for (int i = 0; i < spaces.length; i++)
			for (int b = 0; b < spaces[i].length; b++) {
				spaces[i][b].reset();
				if (spaces[i][b].getClass().equals(Spawn.class))
					spawns.add((Spawn) spaces[i][b]);
				else if (spaces[i][b].getClass().equals(TeleportReceiver.class))
					teleportReceivers.add((TeleportReceiver) spaces[i][b]);
				else if (spaces[i][b].getClass().equals(Teleport.class))
					teleports.add((Teleport) spaces[i][b]);
			}
		focus = new ArrayList<Space>();
	}

	public boolean teleport(MovingGameObject object, Teleport original) {
		if (teleportReceivers.size() > 0) {
			int startIndex = (int) (DamaYama.random() * teleportReceivers
					.size());
			int index = startIndex;
			while (!teleportReceivers.get(index).receive(object)) {
				index++;
				if (index >= teleportReceivers.size())
					index = 0;
				if (index == startIndex) {
					return false;
				}
			}
			PlayPanel.currentRunning.effects.add(new BoxEffect(original.x,
					original.y, original.z, Teleport.teleportColor));
			PlayPanel.currentRunning.effects.add(new BoxEffect(object.x,
					object.y, object.z, TeleportReceiver.teleportReceiverColor));
			return true;
		}
		return false;
	}

	public boolean spawn(MovingGameObject object) {
		if (spawns.size() > 0) {
			int startIndex = (int) (DamaYama.random() * spawns.size());
			int index = startIndex;
			while (!spawns.get(index).spawn(object)) {
				index++;
				if (index >= spawns.size())
					index = 0;
				if (index == startIndex)
					return false;
			}
			PlayPanel.currentRunning.effects.add(new BoxEffect(spawns.get(index).x,spawns.get(index).y,spawns.get(index).z,Spawn.spawnColor));
			return true;
		}
		return false;
	}

	ArrayList<Space> focus;

	public void resetFocus() {
		focus.clear();
		for (int i = 0; i < spaces.length; i++)
			for (int j = 0; j < spaces[i].length; j++)
				spaces[i][j].steps = -1;
	}

	public void addFocus(double x, double y) {
		int ix = MovingGameObject.getXIndex(x);
		int iy = MovingGameObject.getYIndex(y);
		if (inBounds(ix, iy)) {
			focus.add(spaces[ix][iy]);
		}
	}

	public void updateFocus() {
		ArrayList<Space> spacesUpdated = new ArrayList<Space>(20);
		ArrayList<Space> spacesUpdatedNew = new ArrayList<Space>(20);
		spacesUpdated.addAll(focus);
		for (int i = 0; i < focus.size(); i++) {
			focus.get(i).steps = 0;
		}
		while (!spacesUpdated.isEmpty()) {
			for (Iterator<Space> i = spacesUpdated.iterator(); i.hasNext();) {
				Space space = i.next();
				// check all the neighbors
				for (int xOff = -1; xOff <= 1; xOff++) {
					for (int yOff = -1; yOff <= 1; yOff++) {
						if (inBounds(space.x + xOff, space.y + yOff)) {
							Space neighbor = spaces[space.x + xOff][space.y
									+ yOff];
							if ((neighbor.steps == -1 || neighbor.steps > space.steps
									+ Math.abs(xOff) + Math.abs(yOff))
									&& neighbor.maxHeight() + 0.5f >= space
											.maxHeight()
									&& (!(neighbor instanceof Teleport) || teleportReceivers
											.isEmpty())) {
								spaces[space.x + xOff][space.y + yOff].steps = space.steps
										+ Math.abs(xOff) + Math.abs(yOff);
								// should also check if it has already been
								// added before adding it again?
								spacesUpdatedNew
										.add(spaces[space.x + xOff][space.y
												+ yOff]);
							}
						}
					}
				}
				if (space instanceof TeleportReceiver) {
					for (int j = 0; j < teleports.size(); j++) {
						if ((teleports.get(j).steps == -1 || teleports.get(j).steps > space.steps
								+ this.teleportReceivers.size())) {
							teleports.get(j).steps = space.steps + space.steps
									+ this.teleportReceivers.size();
							spacesUpdatedNew.add(teleports.get(j));
						}
					}
				}
			}
			spacesUpdated.clear();
			ArrayList<Space> temp = spacesUpdatedNew;
			spacesUpdatedNew = spacesUpdated;
			spacesUpdated = temp;
		}
	}

	public boolean inBounds(int x, int y) {
		return x >= 0 && y >= 0 && x < spaces.length && y < spaces[x].length;
	}

	public Map(Scanner scan) throws IOException {
		ArrayList<String> lines = new ArrayList<String>();
		int maxWidth = 0;
		while (scan.hasNextLine()) {
			String s = scan.nextLine();
			if (s.length() > maxWidth)
				maxWidth = s.length();
			lines.add(s);
		}
		if (lines.size() == 0)
			throw new IOException("Empty Map File!");
		spaces = new Space[maxWidth / 2][lines.size()];
		for (int i = 0; i < maxWidth; i += 2)
			for (int b = 0; b < spaces[i / 2].length; b++) {
				if (i < lines.get(b).length()) {
					try {
						spaces[i / 2][b] = getSpace(
								lines.get(b).charAt(i),
								i / 2,
								b,
								Integer.parseInt(lines.get(b).charAt(i + 1)
										+ ""));
					} catch (NumberFormatException e) {
						spaces[i / 2][b] = getSpace(lines.get(b).charAt(i),
								i / 2, b, 0);
						System.out.println("no number found afterwards");
					}
				} else
					spaces[i / 2][b] = getDefaultSpace(i / 2, b);
			}
	}

	public static long setNewMapKeep(FileInputStream stream) throws IOException {
		long returnValue = 0;
		int width = stream.read();
		returnValue++;
		int height = stream.read();
		returnValue++;
		Space[][] spaces = new Space[width][height];
		for (int i = 0; i < width; i++)
			for (int b = 0; b < height; b++) {
				spaces[i][b] = getSpace((char) stream.read(), i, b,
						stream.read());
				returnValue += 2;
			}
		setNewMapKeep(new Map(spaces));
		return returnValue;
	}

	public void write(FileWriter writer) throws IOException {
		writer.write((byte) spaces.length);
		writer.write((byte) spaces[0].length);
		for (int i = 0; i < spaces.length; i++)
			for (int b = 0; b < spaces[i].length; b++) {
				writer.write((byte) spaces[i][b].toString().charAt(0));
				writer.write(spaces[i][b].z);
			}
	}

	public static Space getDefaultSpace(int x, int y) {
		return new EmptySpace(x, y);
	}

	protected static Space getSpace(char c, int x, int y, int z)
			throws IOException {
		switch (c) {
		case ' ':
			return new EmptySpace(x, y);
		case '=':
			return new Wall(x, y, z);
		case 's':
			return new Spawn(x, y, z);
		case '^':
			return new TallWall(x, y, z);
		case '!':
			return new Teleport(x, y, z);
		case '0':
			return new TeleportReceiver(x, y, z);
		case 'r':
			return new Ramp(x, y, z);
		}
		throw new IOException(c + " is not a valid character for a map");
	}

	public static void setNewMap(Map map) {
		current = map;
		DamaYama.frame.setContentPane(new MapEditorPanel(
				MapEditorPanel.allItems, MapEditorPanel.allTypes, true));
		DamaYama.repaintStatic();
	}

	public static void setNewMapKeep(Map map) {
		current = map;
	}

	public void drawGrid(Graphics g, int xOff, int yOff) {
		g.setColor(Color.BLACK);
		for (int i = 0; i < spaces.length; i++)
			for (int b = 0; b < spaces[0].length; b++)
				g.drawRect(xOff + i * Space.SIZE, yOff + b * Space.SIZE,
						Space.SIZE, Space.SIZE);
	}

	public void drawSlantGrid(Perspective p) {
		p.g.setColor(Color.black);
		for (int i = 0; i < spaces.length; i++)
			for (int b = 0; b < spaces[i].length; b++) {
				drawSquare(p, i, b, 0, 1);
				p.drawShape(p.getCurrentShape());
			}
	}

	public static void drawSquare(Perspective p, double x, double y, double z,
			double width) {
		p.startNewShape(x, y, z, 4);
		p.drawXLine(width);
		p.drawYLine(width);
		p.drawXLine(-width);
	}

	public void draw(Graphics g, int xOff, int yOff) {
		for (int i = 0; i < spaces.length; i++)
			for (int b = 0; b < spaces[0].length; b++)
				spaces[i][b].drawOffset(g, xOff, yOff);
	}

	public int getX(int pixelX, int xOff) {
		return (pixelX - xOff) / Space.SIZE;
	}

	public int getY(int pixelY, int yOff) {
		return (pixelY - yOff) / Space.SIZE;
	}

	public void swap(int startX, int startY, int endX, int endY) {
		Space temp = spaces[endX][endY];
		spaces[endX][endY] = spaces[startX][startY];
		spaces[startX][startY] = temp;
		spaces[endX][endY].x = endX;
		spaces[endX][endY].y = endY;
		spaces[startX][startY].x = startX;
		spaces[startX][startY].y = startY;
	}

	public void remove(int x, int y) {
		spaces[x][y] = Map.getDefaultSpace(x, y);
	}

	public static String[] getMapNames(Scanner scanner) throws IOException {
		ArrayList<String> mapNames = new ArrayList<String>(2);
		while (scanner.hasNextLine()) {
			String s = scanner.nextLine();
			if (s.length() > 0) {
				if (!mapNames.contains(s)) {
					if (new File(s).exists())
						mapNames.add(s);
				} else
					System.out.println("'" + s + "' : is not a valid map name");
			}
		}
		if (mapNames.size() == 0)
			throw new IOException("No maps found");
		String[] names = new String[mapNames.size()];
		for (int i = 0; i < names.length; i++)
			names[i] = mapNames.get(i);

		return names;
	}

	public void stretch(int factor) {
		Space[][] stretch = new Space[spaces.length * factor][spaces[0].length
				* factor];
		for (int i = 0; i < spaces.length; i++) {
			int minI = i * factor;
			int maxI = Math.min((i + 1) * factor, stretch.length);
			for (int b = 0; b < spaces[i].length; b++) {
				int minB = b * factor;
				int maxB = (b + 1) * factor;
				for (int i2 = minI; i2 < maxI; i2++)
					for (int b2 = minB; b2 < maxB; b2++)
						stretch[i2][b2] = spaces[i][b].createCopy(i2, b2,
								Map.getCurrent().spaces[i][b].z);
			}
		}
		spaces = stretch;
	}

	public void stretch(int endX, int endY, int shiftX, int shiftY) {
		// to anchor left, shiftx = 0; to anchor right, shift x = amount
		// to anchor up, shift y = 0; to anchor down, shift y = amount
		Space[][] stretch = new Space[endX][endY];
		for (int i = 0; i < endX; i++) {
			if (i - shiftX >= 0 && i - shiftX < spaces.length) {
				for (int b = 0; b < endY; b++)
					if (b - shiftY >= 0 && b - shiftY < spaces[0].length) {
						stretch[i][b] = spaces[i - shiftX][b - shiftY];
						stretch[i][b].x = i;
						stretch[i][b].y = b;
					} else
						stretch[i][b] = getDefaultSpace(i, b);
			} else
				for (int b = 0; b < endY; b++)
					stretch[i][b] = getDefaultSpace(i, b);
		}
		spaces = stretch;
	}
}


class MapEditorPanel extends DamaPanel implements ActionListener,
		MouseWheelListener {
	MapPanel mapPanel;
	JComboBox item;
	JTextField width;
	JTextField height;
	char[] types;

	public static final String[] allItems = new String[] { "Space", "Wall",
			"Tall Wall", "Spawn", "Ramp", "Teleporter", "Teleport Receiver" };
	public static final char[] allTypes = new char[] { ' ', '=', '^', 's', 'r',
			'!', '0' };

	public MapEditorPanel(String[] itemNames, char[] matchingChars,
			boolean allowCompleteControl) {
		super(new BorderLayout());
		mapPanel = new MapPanel(allowCompleteControl, MapPanel.topDown);
		add(mapPanel, BorderLayout.CENTER);

		JPanel westPanel = new JPanel(new BorderLayout(0, 5));

		JPanel editSize = new JPanel(new BorderLayout(0, 3));
		JPanel size = new JPanel(new GridLayout(0, 1));
		JPanel widthPanel = new JPanel(new BorderLayout());
		widthPanel.add(new JLabel("Width : "), BorderLayout.WEST);
		width = new JTextField("" + Map.getCurrent().spaces.length);
		widthPanel.add(width);
		JPanel heightPanel = new JPanel(new BorderLayout());
		heightPanel.add(new JLabel("Height : "), BorderLayout.WEST);
		height = new JTextField("" + Map.getCurrent().spaces[0].length);
		heightPanel.add(height);
		JPanel applyPanel = new JPanel();
		JButton apply = new JButton("Apply Changes");
		apply.addActionListener(this);
		apply.setActionCommand("apply");
		applyPanel.add(apply);
		size.add(widthPanel);
		size.add(heightPanel);
		editSize.add(size);
		editSize.add(applyPanel, BorderLayout.SOUTH);
		westPanel.add(editSize, BorderLayout.NORTH);

		JPanel selection = new JPanel(new BorderLayout(0, 10));
		item = new JComboBox(itemNames);
		types = matchingChars;
		// add items here when you want
		item.addActionListener(this);
		selection.add(item, BorderLayout.NORTH);
		JTextArea instructions = new JTextArea(
				"Click + Drag = Swap Items\nClick + Alt + Drag = Copy Item\nClick + Control = Delete Item\nShift + Drag = Make multiple of Item\nAlt + Drag + Release = Extend Map Size");
		instructions.setEditable(false);
		selection.add(instructions, BorderLayout.CENTER);
		westPanel.add(selection, BorderLayout.CENTER);

		JButton toggleView = new JButton("Toggle View");
		toggleView.addActionListener(this);
		toggleView.setActionCommand("toggle");
		westPanel.add(toggleView, BorderLayout.SOUTH);

		add(westPanel, BorderLayout.WEST);

		addMouseWheelListener(this);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == item) {
			mapPanel.currentItem = types[item.getSelectedIndex()];
		} else if (e.getActionCommand().equals("toggle")) {
			if (mapPanel.drawType == MapPanel.slant)
				mapPanel.drawType = MapPanel.topDown;
			else
				mapPanel.drawType = MapPanel.slant;
			mapPanel.refreshScrollBars();
		} else if (e.getActionCommand().equals("apply")) {
			try {
				int endX = Integer.parseInt(width.getText());
				int endY = Integer.parseInt(height.getText());
				if (endX > 0 && endY > 0) {
					Map.getCurrent().stretch(endX, endY, 0, 0);
					mapPanel.refreshScrollBars();
				}
			} catch (NumberFormatException e1) {
				e1.printStackTrace();
			}
			width.setText(Map.getCurrent().spaces.length + "");
			height.setText(Map.getCurrent().spaces[0].length + "");
		}

		repaint();
	}

	public void mouseWheelMoved(MouseWheelEvent e) {
		if (!mapPanel.upDown.isVisible()) {
			if (!mapPanel.leftRight.isVisible()) {
				int newIndex = item.getSelectedIndex() + e.getWheelRotation();
				if (newIndex >= 0 && newIndex <= item.getComponentCount())
					item.setSelectedIndex(newIndex);
			} else
				mapPanel.leftRight.setValue(mapPanel.leftRight.getValue()
						+ e.getWheelRotation() * Space.SIZE / 2);
		} else
			mapPanel.upDown.setValue(mapPanel.upDown.getValue()
					+ e.getWheelRotation() * Space.SIZE / 2);
	}
}

class MapNewFrame extends JFrame {
	public MapNewFrame() {
		super("Create a Map");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setContentPane(new MapNewPanel(this));
		this.setBounds(0, 0, 300, 120);
		setVisible(true);
	}
}

class MapNewPanel extends DamaPanel implements ActionListener {
	JTextField width;
	JTextField height;
	JFrame container;

	public MapNewPanel(JFrame cont) {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		container = cont;

		JPanel widthPanel = new JPanel(new BorderLayout());
		widthPanel.add(new JLabel("Width : "), BorderLayout.WEST);
		width = new JTextField("1");
		widthPanel.add(width);
		JPanel p = new JPanel(new BorderLayout());
		p.add(widthPanel, BorderLayout.NORTH);

		JPanel heightPanel = new JPanel(new BorderLayout());
		heightPanel.add(new JLabel("Height : "), BorderLayout.WEST);
		height = new JTextField("1");
		heightPanel.add(height);

		p.add(heightPanel, BorderLayout.SOUTH);
		add(p);

		JButton create = new JButton("Create");
		create.addActionListener(this);
		add(create);
	}

	public void actionPerformed(ActionEvent arg0) {
		try {
			int w = Integer.parseInt(width.getText());
			int h = Integer.parseInt(height.getText());
			if (w > 0 && h > 0) {
				Space[][] mapSpace = new Space[w][h];
				for (int i = 0; i < w; i++)
					for (int b = 0; b < h; b++)
						mapSpace[i][b] = Map.getDefaultSpace(i, b);
				Map.setNewMap(new Map(mapSpace));
				MapOpenPanel.fileName = MapOpenPanel.defaultFileName;
				container.dispose();
				return;
			}
		} catch (Exception e) {
		}
		JOptionPane.showMessageDialog(this, "Bad Input values!");
	}
}

class MapSaveFrame extends JFrame {
	public MapSaveFrame(Map map) {
		super("Save a Map");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setContentPane(new MapSavePanel(map, this));
		this.setBounds(0, 0, 200, 200);
		setVisible(true);
	}
}

class MapSavePanel extends DamaPanel implements ActionListener {
	Map map;
	JComboBox fileName;
	JButton saveButton;
	JFrame container;

	public MapSavePanel(Map ma, JFrame cont) {
		map = ma;
		container = cont;
		JPanel file = new JPanel(new BorderLayout());
		file.add(new JLabel("File Name:"), BorderLayout.WEST);
		try {
			fileName = new JComboBox(Map.getMapNames(new Scanner(new File(
					Map.mapFileName))));
		} catch (FileNotFoundException e) {
			fileName = new JComboBox();
		} catch (IOException e) {
			fileName = new JComboBox();
		}
		fileName.insertItemAt(MapOpenPanel.fileName, 0);
		fileName.setSelectedIndex(0);
		fileName.setEditable(true);
		file.add(fileName);
		add(file);

		saveButton = new JButton("Save / Save As");
		saveButton.addActionListener(this);
		add(saveButton);
	}

	public static boolean saveMap(Map map, String fileName) {
		try {
			FileWriter writer = new FileWriter(fileName);
			System.out.println("Successful writer");
			for (int b = 0; b < map.spaces[0].length; b++) {
				for (int i = 0; i < map.spaces.length; i++)
					writer.write(map.spaces[i][b].toString()
							+ map.spaces[i][b].z, 0, 2);
				writer.write("\n", 0, 1);
			}
			writer.close();
			boolean alreadyContainsFileName = false;
			Scanner scan = new Scanner(new File(Map.mapFileName));
			while (scan.hasNextLine() && !alreadyContainsFileName) {
				if (scan.nextLine().trim().equals(fileName))
					alreadyContainsFileName = true;
			}
			if (!alreadyContainsFileName) {
				FileWriter nameWriter;
				nameWriter = new FileWriter(Map.mapFileName, true);
				nameWriter.write("\n" + fileName, 0,
						((fileName + "\n").length()));
				nameWriter.close();
			}
			return true;
		} catch (Exception e) {
			System.out.println("Could not save file");
			return false;
		}
	}

	public void actionPerformed(ActionEvent e) {
		if (saveMap(map, fileName.getSelectedItem().toString())) {
			JOptionPane.showMessageDialog(this, "Save Successful!");
			container.dispose();
		} else
			JOptionPane.showMessageDialog(this, "Pick a different file name");
	}
}
