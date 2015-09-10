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

class MapPanel extends DamaPanel implements MouseMotionListener, MouseListener,
		AdjustmentListener, MouseWheelListener {
	int startX, startY, endX, endY;
	int mousePositionX, mousePositionY;
	int xOff = 0;
	int yOff = 0;
	char currentItem = ' ';
	boolean dragging = false;
	boolean allowCompleteControl = false;
	JScrollBar upDown, leftRight;
	ArrayList<Effect> effects;

	public MapPanel(boolean completeControl, int drawT) {
		super(new BorderLayout());
		effects = new ArrayList<Effect>();

		drawType = drawT;

		addMouseMotionListener(this);
		addMouseListener(this);
		addMouseWheelListener(this);
		allowCompleteControl = completeControl;

		upDown = new JScrollBar(JScrollBar.VERTICAL);
		add(upDown, BorderLayout.EAST);
		upDown.addAdjustmentListener(this);
		upDown.setVisible(false);

		leftRight = new JScrollBar(JScrollBar.HORIZONTAL);
		leftRight.addAdjustmentListener(this);
		add(leftRight, BorderLayout.SOUTH);
		leftRight.setVisible(false);
	}

	int dragStartShiftX;
	int dragStartShiftY;
	double slantYOff = 0;

	public void mouseDragged(MouseEvent e) {
		if (drawType == slant) {
			if (dragging == false) {
				dragStartShiftX = e.getX();
				dragStartShiftY = e.getY();
			} else {
				if (e.getX() != dragStartShiftX) {
					if (e.isMetaDown()) {
						p.zero.x += e.getX() - dragStartShiftX;
					} else {
						p.vanishingX += e.getX() - dragStartShiftX;
						p.vanishingY += e.getY() - dragStartShiftY;
						dragStartShiftY = e.getY();
					}
					dragStartShiftX = e.getX();
				}
				if (e.getY() != dragStartShiftY && e.isMetaDown()) {
					p.zero.y += e.getY() - dragStartShiftY;
					dragStartShiftY = e.getY();
				}
			}
		}
		dragging = true;
		updateNewMousePosition(e);
		if (canEdit(endX, endY)) {
			if (e.isControlDown())
				Map.getCurrent().remove(endX, endY);
			else if (e.isShiftDown())
				try {
					Space newSpace = Map.getSpace(currentItem, endX, endY,
							Map.getCurrent().spaces[endX][endY].z);
					if (newSpace.allowUserEdit() || allowCompleteControl)
						Map.getCurrent().spaces[endX][endY] = newSpace;
				} catch (IOException e1) {
					e1.printStackTrace();
				}
		}
		repaint();
	}

	private void updateNewMousePosition(MouseEvent e) {
		int origMouseX = endX;
		int origMouseY = endY;
		updateCurrentMouse(e);
		if ((origMouseX != endX || origMouseY != endY) && inBounds(endX, endY)) {
			this.setToolTipText(Map.getCurrent().spaces[endX][endY]
					.getDescription());
		}
	}

	public void mouseMoved(MouseEvent e) {
		updateNewMousePosition(e);
		repaint();
	}

	public void mouseClicked(MouseEvent e) {
		updateNewMousePosition(e);
		if (canEdit(endX, endY)) {
			if (e.isControlDown())
				Map.getCurrent().remove(endX, endY);
			else
				try {
					Space newSpace = Map.getSpace(currentItem, endX, endY,
							(int) Map.getCurrent().spaces[endX][endY]
									.maxHeight());
					if (newSpace.allowUserEdit() || allowCompleteControl)
						Map.getCurrent().spaces[endX][endY] = newSpace;
				} catch (IOException e1) {
					e1.printStackTrace();
				}
		}
		repaint();
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
		if (drawType == topDown) {
			startX = Map.getCurrent().getX(e.getX(), xOff);
			startY = Map.getCurrent().getY(e.getY(), yOff);
		}
	}

	private void updateCurrentMouse(MouseEvent e) {
		mousePositionX = e.getX();
		mousePositionY = e.getY();
		if (Map.getCurrent() != null) {
			endX = Map.getCurrent().getX(mousePositionX, xOff);
			endY = Map.getCurrent().getY(mousePositionY, yOff);
		}
	}

	private boolean canEdit(int x, int y) {
		return inBounds(x, y)
				&& (allowCompleteControl || Map.getCurrent().spaces[x][y]
						.allowUserEdit()) && drawType == topDown;
	}

	public void mouseReleased(MouseEvent e) {
		dragging = false;
		updateCurrentMouse(e);
		if (canEdit(startX, startY)) {
			if ((startX != endX || startY != endY)) {
				if (inBounds(endX, endY)) {
					if (canEdit(endX, endY)) {
						if (e.isAltDown())
							Map.getCurrent().spaces[endX][endY] = Map
									.getCurrent().spaces[startX][startY]
									.createCopy(
											endX,
											endY,
											Map.getCurrent().spaces[startX][startY].z);
						else
							Map.getCurrent().swap(startX, startY, endX, endY);
					}
				} else if (!e.isAltDown())
					Map.getCurrent().remove(startX, startY);
				else if (allowCompleteControl) {
					int shiftX, shiftY, finalX, finalY;
					if (endX < 0) {
						finalX = -endX + Map.getCurrent().spaces.length;
						shiftX = -endX;
					} else {
						if (endX >= Map.getCurrent().spaces.length)
							finalX = endX + 1;
						else
							finalX = Map.getCurrent().spaces.length;
						shiftX = 0;
					}

					if (endY < 0) {
						finalY = -endY + Map.getCurrent().spaces[0].length;
						shiftY = -endY;
					} else {
						if (endY >= Map.getCurrent().spaces[0].length)
							finalY = endY + 1;
						else
							finalY = Map.getCurrent().spaces[0].length;
						shiftY = 0;
					}

					Map.getCurrent().stretch(finalX, finalY, shiftX, shiftY);
					refreshScrollBars();
				}
			} else if (e.isControlDown())
				Map.getCurrent().remove(startX, startY);
		}
		repaint();
	}

	protected boolean inBounds(int x, int y) {
		return x >= 0 && x < Map.getCurrent().spaces.length && y >= 0
				&& y < Map.getCurrent().spaces[x].length;
	}

	public void updateOffsets() {
		if (drawType == topDown) {
			if (getWidth() > Map.getCurrent().spaces.length * Space.SIZE)
				xOff = (getWidth() - Map.getCurrent().spaces.length
						* Space.SIZE) / 2;
			if (leftRight.isVisible())
				xOff = -leftRight.getValue() + Space.SIZE;
			else
				refreshLeftRight();

			if (getHeight() > Map.getCurrent().spaces[0].length * Space.SIZE)
				yOff = (getHeight() - Map.getCurrent().spaces[0].length
						* Space.SIZE) / 2;
			else if (upDown.isVisible())
				yOff = -upDown.getValue() + Space.SIZE;
			else
				refreshUpDown();
		} else {
			xSlantOff = -Map.getCurrent().spaces.length / 2 - 1;
			ySlantOff = -Map.getCurrent().spaces[0].length / 2
					+ additionalSlantYOff;
			zSlantOff = 0;
		}
	}

	public void refreshLeftRight() {
		if (getWidth() <= Map.getCurrent().spaces.length * Space.SIZE
				&& drawType == topDown) {
			leftRight.setValues(0, 20, 0,
					-(getWidth() - Map.getCurrent().spaces.length * Space.SIZE)
							+ Space.SIZE * 2);
			leftRight.setVisible(true);
		} else
			leftRight.setVisible(false);
		validate();
	}

	public void refreshUpDown() {
		if (getHeight() <= Map.getCurrent().spaces[0].length * Space.SIZE
				&& drawType == topDown) {
			upDown.setValues(0, 20, 0,
					-(getHeight() - Map.getCurrent().spaces[0].length
							* Space.SIZE)
							+ Space.SIZE * 2);
			upDown.setVisible(true);
		} else
			upDown.setVisible(false);
		validate();
	}

	public void refreshScrollBars() {
		refreshUpDown();
		refreshLeftRight();
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		updateOffsets();
		switch (drawType) {
		case topDown: {
			drawTopDown(g);
		}
			break;
		case slant: {
			if (p == null)
				p = new Perspective(getWidth() / 2, getHeight() / 2,
						getWidth() / 2 - 1500, getHeight() / 2 - 3000);
			p.update(g, xSlantOff, ySlantOff, zSlantOff, 1);
			drawSlant(g);
		}
			break;
		}
	}

	int drawType;
	public static final int topDown = 0, slant = 1;
	Perspective p;
	double xSlantOff, ySlantOff, zSlantOff;

	public void drawSlant(Graphics g) {
		g.setColor(Color.black);
		g.fillOval((int) p.zero.x, (int) p.zero.y, 3, 3);
		g.setColor(Color.RED);
		g.fillOval(p.vanishingX, p.vanishingY, 3, 3);

		for (int b = 0; b < Map.getCurrent().spaces[0].length; b++) {
			for (int i = 0; i < Map.getCurrent().spaces.length; i++) {
				Map.getCurrent().spaces[i][b].drawSlantOverlap(p);
			}
			for (int i = 0; i < Map.getCurrent().spaces.length; i++) {
				Map.getCurrent().spaces[i][b].drawSlant(p);
			}
			for (int i = 0; i < Map.getCurrent().spaces.length; i++) {
				Map.getCurrent().spaces[i][b].drawSlantNoOverlap(p);
			}
		}
		for (int i = 0; i < effects.size(); i++)
			effects.get(i).drawSlant(p);
		p.draw(getWidth(), getHeight());
	}

	public void drawTopDown(Graphics g) {
		Map.getCurrent().draw(g, xOff, yOff);
		Map.getCurrent().drawGrid(g, xOff, yOff);
		if (dragging && inBounds(startX, startY) && canEdit(startX, startY)) {
			Map.getCurrent().spaces[startX][startY].draw(g, mousePositionX
					- Space.SIZE / 2, mousePositionY - Space.SIZE / 2);
			g.setColor(new Color(0, 255, 0, 40));
			g.fillRect(mousePositionX - Space.SIZE / 2, mousePositionY
					- Space.SIZE / 2, Space.SIZE, Space.SIZE);
		}
		for (int i = 0; i < effects.size(); i++)
			effects.get(i).draw(g, xOff, yOff);
	}

	public void adjustmentValueChanged(AdjustmentEvent arg0) {
		repaint();
	}

	double additionalSlantYOff = 0;

	public void mouseWheelMoved(MouseWheelEvent e) {
		if (drawType == slant) {
			additionalSlantYOff -= e.getWheelRotation() * 0.3;
			repaint();
		}
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

class MapOpenFrame extends JFrame {
	public MapOpenFrame() {
		super("Select a Map");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		try {
			setContentPane(new MapOpenPanel(new Scanner(new File(
					Map.mapFileName)), this));
		} catch (FileNotFoundException e) {
			System.out
					.println("Could not load map properly...\nAttempting to write new file");
			try {
				FileWriter writer = new FileWriter(Map.mapFileName);
				writer.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			try {
				setContentPane(new MapOpenPanel(new Scanner(new File(
						Map.mapFileName)), this));
			} catch (FileNotFoundException e1) {
				System.out.println("Could not write file");
				e1.printStackTrace();
			}
		} catch (IOException e) {
		}
		this.setBounds(0, 0, 200, 200);
		setVisible(true);
	}
}

class MapOpenPanel extends DamaPanel implements ActionListener {
	JComboBox maps;
	JButton openMap;
	Window container;
	public static String fileName = "MyMap.txt";
	public static final String defaultFileName = "MyMap.txt";

	public MapOpenPanel(Scanner scanner, JFrame jf) {
		super(new FlowLayout());
		container = jf;
		try {
			maps = new JComboBox(Map.getMapNames(scanner));
		} catch (IOException e) {
			maps = new JComboBox();
			System.out.println("No maps found");
		}
		maps.setEditable(true);

		openMap = new JButton("Open Map");
		openMap.addActionListener(this);
		add(maps);
		add(openMap);
	}

	public static Map openMap(String s) {
		Scanner scanner = null;
		try {
			scanner = new Scanner(new File(s));
		} catch (FileNotFoundException e) {
		}
		try {
			return new Map(scanner);
		} catch (IOException e) {
		}
		return null;
	}

	public void actionPerformed(ActionEvent e) {
		Map newMap = openMap(maps.getSelectedItem().toString());
		if (newMap != null) {
			Map.setNewMap(newMap);
			fileName = maps.getSelectedItem() + "";
			container.dispose();
		} else {
			JOptionPane.showMessageDialog(this, "Error in loading file");
		}
	}
}