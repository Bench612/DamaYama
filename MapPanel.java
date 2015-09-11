import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JScrollBar;

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
