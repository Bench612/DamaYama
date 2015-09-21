import java.awt.*;

import drawing.*;
import drawing.Point;

import java.util.ArrayList;

public class Player extends MovingGameObjectWeapon {
	PlayPanel container;
	ArrayList<Weapon> weapons;
	String name;
	int index;

	public Player(PlayPanel container1, int index1) {
		super(12, 0.75, 0.75, 15);
		isFocus = true;
		index = index1;
		name = "Player" + (index + 1);
		container = container1;
		weapon = new BasicWeapon(this);
		weapons = new ArrayList<Weapon>(10);
		weapons.add(weapon);
	}

	double totalDamage = 0;
	boolean autoAim = false;
	private int shootDirectionX, shootDirectionY;

	public int getShootDirectionX() {
		if (autoAim)
			return super.getShootDirectionX();
		return shootDirectionX;
	}

	public int getShootDirectionY() {
		if (autoAim)
			return super.getShootDirectionY();
		return shootDirectionY;
	}

	public String toString() {
		return name;
	}

	public boolean upgradeWeapon(Class c) {
		Weapon w = getWeapon(c);
		if (w != null) {
			w.upgrade();
			return true;
		}
		return false;
	}

	public Weapon getWeapon(Class c) {
		for (int i = 0; i < weapons.size(); i++)
			if (c.isInstance((weapons.get(i))))
				return weapons.get(i);
		return null;
	}

	boolean alive = true;

	public void die() {
		super.die();
		alive = false;
		container.playerDied(this);
	}

	public int getCenterX(int xOff) {
		return (int) (x * Space.SIZE) + xOff + (int) (width * Space.SIZE) / 2;
	}

	public int getCenterY(int yOff) {
		return (int) (y * Space.SIZE) + yOff + (int) (height * Space.SIZE) / 2;
	}

	public void draw(Graphics g, int xOff, int yOff) {
		int centerX = getCenterX(xOff);
		int centerY = getCenterY(yOff);
		if (this == PlayPanel.currentRunning.getPlayer()) {
			double angle = Math.atan2(getShootDirectionY(),
					getShootDirectionX());
			g.setColor(Color.yellow);
			g.drawLine(
					centerX,
					centerY,
					centerX
							+ (int) (Math.cos(angle - weapon.accuracy / 2) * 50),
					centerY
							+ (int) (Math.sin(angle - weapon.accuracy / 2) * 50));
			g.drawLine(
					centerX,
					centerY,
					centerX
							+ (int) (Math.cos(angle + weapon.accuracy / 2) * 50),
					centerY
							+ (int) (Math.sin(angle + weapon.accuracy / 2) * 50));
			g.drawLine(centerX, centerY, centerX + getShootDirectionX(),
					centerY + getShootDirectionY());
		}
		g.setColor(Color.red);
		g.drawLine(centerX, centerY, centerX + getShootDirectionX(), centerY
				+ getShootDirectionY());
		if (this != PlayPanel.currentRunning.getPlayer())
			g.setColor(Color.gray);
		else {
			g.setColor(Color.black);
		}
		if (bomb != null) {
			g.fillRect((int) (bomb.x * Space.SIZE) + xOff,
					(int) (bomb.y * Space.SIZE) + yOff,
					(int) (bomb.width * Space.SIZE),
					(int) (bomb.height * Space.SIZE));
		}
		super.draw(g, xOff, yOff);
		g.setColor(Color.white);
		java.awt.FontMetrics fm = g.getFontMetrics();
		String s = index + 1 + " ";
		g.drawString(s, centerX, centerY);
		drawHealthBar(g, xOff, yOff);
	}

	public void addNewWeapon(Weapon w) {
		weapons.add(w);
		PlayPanel.currentRunning
				.displayMessage("New Weapon " + w + " Unlocked");
	}

	protected boolean moveDown(double amount) {
		if (!onGround())
			amount /= 8;
		return super.moveDown(amount);
	}

	protected boolean moveRight(double amount) {
		if (!onGround())
			amount /= 8;
		return super.moveRight(amount);
	}

	Bomb bomb;

	class Bomb {
		double x, y, z;
		double height = 4.0 / Space.SIZE, width = 4.0 / Space.SIZE;
		int time = 0;

		public Bomb(double x1, double y1, double z1) {
			x = x1 - width / 2;
			y = y1 - height / 2;
			z = z1;
		}
	}

	final static int explosionRange = 2;
	final static double explosionDamage = .3;

	public void update(boolean[] keys, boolean[] previousKeys) {
		autoAim = !keys[PlayPanel.MANUAL_AIM];
		Space previousSpace = currentSpace();
		super.update();
		if (keys[PlayPanel.BOMB] && !previousKeys[PlayPanel.BOMB]) {
			if (bomb == null) {
				bomb = new Bomb(x + width / 2, y + height / 2, currentSpace()
						.maxHeight());
			} else {
				explode(bomb.x, bomb.y, bomb.z, bomb.width, bomb.height,
						explosionRange, explosionDamage);
				bomb = null;
			}
		}
		if (keys[PlayPanel.UP])
			moveUp();
		else if (keys[PlayPanel.DOWN])
			moveDown();

		if (keys[PlayPanel.LEFT])
			moveLeft();
		else if (keys[PlayPanel.RIGHT])
			moveRight();

		if (keys[PlayPanel.SHOOT]) {
			tryFireWeapon();
			if (weapon.ammo <= weapon.bulletsPerShot) {
				weapon = weapons.get(0);
				PlayPanel.currentRunning.displayMessage("Out of ammo. "
						+ weapon + " Selected");
			}
		}
		if (keys[PlayPanel.JUMP])
			this.jump();
		if (keys[PlayPanel.SWITCH_WEAPON]
				&& !previousKeys[PlayPanel.SWITCH_WEAPON]) {
			int currentIndex = weapons.indexOf(weapon);
			if (currentIndex != -1) {
				currentIndex++;
				if (currentIndex >= weapons.size())
					currentIndex = 0;
				weapon = weapons.get(currentIndex);
				PlayPanel.currentRunning.displayMessage(weapon + " Selected");
			}
		}
		if (keys[PlayPanel.ONE])
			trySwitchTo(BasicWeapon.class);
		else if (keys[PlayPanel.TWO])
			trySwitchTo(SMG.class);
		else if (keys[PlayPanel.THREE])
			trySwitchTo(Sniper.class);
		else if (keys[PlayPanel.FOUR])
			trySwitchTo(Shotgun.class);
		else if (keys[PlayPanel.FIVE])
			trySwitchTo(MachineGun.class);
		for (int i = 0; i < squaresX.length; i++)
			for (int b = 0; b < squaresY.length; b++) {
				Space s = Map.getCurrent().spaces[getXIndex(squaresX[i])][getYIndex(squaresY[i])];
				for (int z = 0; z < s.items.size(); z++) {
					if (this.distanceTo(s.items.get(z).x, s.items.get(z).y) < width
							/ 2 + s.items.get(z).width / 2)
						s.items.get(z).pickup(this);
				}
			}
		if (previousSpace != currentSpace() || previousSpace.steps == -1)
			addAsFocus();
	}

	public void update(boolean[] keys, boolean[] previousKeys, int dX, int dY) {
		shootDirectionX = dX;
		shootDirectionY = dY;
		update(keys, previousKeys);
	}

	long hits = 0;
	long misses = 0;

	protected void tryFireWeapon() {
		int oldSize = didHit.size();
		super.tryFireWeapon();
		for (int i = oldSize - 1; i < didHit.size() && i >= 0; i++) {
			if (didHit.get(i))
				hits++;
			else
				misses++;
		}
	}

	static Color aimColor = new Color(255, 255, 30, 100);

	public void drawSlant(Perspective p) {
		super.drawSlant(p);
		if (this == PlayPanel.currentRunning.getPlayer()) {

			p.setOutline(aimColor);
			p.setColor(aimColor);
			double angle = Math.atan2(getShootDirectionY(),
					getShootDirectionX());
			p.startNewShape(x + width / 2, y + height / 2, z + getHeight() / 2,
					2);
			p.addPoint(p.getScreenPoint(
					x + width / 2
							+ (Math.cos(angle - (weapon.accuracy + weapon.spread) / 2) * 3),
					y + width / 2
							+ (Math.sin(angle - (weapon.accuracy + weapon.spread) / 2) * 3),
					z + getHeight() / 2));
			p.addPoint(p.getScreenPoint(
					x + width / 2
							+ (Math.cos(angle + (weapon.accuracy + weapon.spread) / 2) * 3),
					y + width / 2
							+ (Math.sin(angle + (weapon.accuracy + weapon.spread) / 2) * 3),
					z + getHeight() / 2));
			p.fillShape(p.getCurrentShape());

			p.setColor(Color.black);
			p.setOutline(Color.DARK_GRAY);
		} else {
			p.setColor(Color.gray);
			p.setOutline(Color.DARK_GRAY);
		}

		// draw head
		p.drawNgon(x + width / 2, y + height / 2, z + getHeight() * 0.7,
				width / 2, direction + Math.PI / 4, 4);
		ArrayList<drawing.Point> bottom = p.getCurrentShape();
		p.drawNgon(x + width / 2, y + height / 2, z + getHeight(), width / 2,
				direction + Math.PI / 4, 4);
		ArrayList<drawing.Point> top = p.getCurrentShape();
		p.fillForm(p.createForm(top, bottom));
		p.fillShape(top);
		p.fillShape(bottom);

		// drawBody
		p.startNewConvexPolygon(6);
		p.drawNgon(x + width / 2, y + height / 2, z, width / 2 * 0.75,
				direction + Math.PI / 4, 4);
		bottom = p.getCurrentShape();
		p.drawNgon(x + width / 2, y + height / 2, z + getHeight() * 0.6,
				width / 2 * 0.75, direction + Math.PI / 4, 4);
		top = p.getCurrentShape();
		p.fillForm(p.createForm(top, bottom));
		p.fillShape(top);
		p.fillShape(bottom);
		p.finishConvexPolygon();

		double attackPerc = 0;
		double attackRange = width * 0.5;

		// draw arm1
		p.startNewConvexPolygon(6);
		p.drawNgon(x + width / 2 + Math.cos(direction + Math.PI / 2)
				* (width / 2) + Math.cos(direction) * attackPerc * attackRange,
				y + height / 2 - Math.sin(direction + Math.PI / 2)
						* (height / 2) + Math.sin(direction) * attackPerc
						* attackRange, z + (getHeight() * 0.2)
						* (1 - attackPerc) + getHeight() * 0.4 * attackPerc, 0,
				direction + Math.PI / 4, 4);
		bottom = p.getCurrentShape();
		p.drawNgon(x + width / 2 + Math.cos(direction + Math.PI / 2)
				* (width / 2),
				y + height / 2 - Math.sin(direction + Math.PI / 2)
						* (height / 2), z + getHeight() * 0.5, 0.1, direction
						+ Math.PI / 4, 4);
		top = p.getCurrentShape();
		p.fillForm(p.createForm(top, bottom));
		p.fillShape(top);
		p.fillShape(bottom);
		p.finishConvexPolygon();

		// draw arm2
		p.startNewConvexPolygon(6);
		p.drawNgon(x + width / 2 + Math.cos(direction - Math.PI / 2)
				* (width / 2) + Math.cos(direction) * attackPerc * attackRange,
				y + height / 2 - Math.sin(direction - Math.PI / 2)
						* (height / 2) + Math.sin(direction) * attackPerc
						* attackRange, z + (getHeight() * 0.2)
						* (1 - attackPerc) + getHeight() * 0.4 * attackPerc, 0,
				direction - Math.PI / 4, 4);
		bottom = p.getCurrentShape();
		p.drawNgon(x + width / 2 + Math.cos(direction - Math.PI / 2)
				* (width / 2),
				y + height / 2 - Math.sin(direction - Math.PI / 2)
						* (height / 2), z + getHeight() * 0.5, 0.1, direction
						- Math.PI / 4, 4);
		top = p.getCurrentShape();
		p.fillForm(p.createForm(top, bottom));
		p.fillShape(top);
		p.fillShape(bottom);
		p.finishConvexPolygon();

		// draw bomb
		if (bomb != null) {
			p.startNewConvexPolygon(5);
			Map.drawSquare(p, bomb.x, bomb.y, bomb.z, bomb.width);
			top = p.getCurrentShape();
			Map.drawSquare(p, bomb.x, bomb.y, bomb.z + bomb.height, bomb.width);
			bottom = p.getCurrentShape();
			p.fillForm(p.createForm(top, bottom));
			p.fillShape(top);
			p.finishConvexPolygon();
		}
		drawHealthBarSlant(p);
	}

	private void trySwitchTo(Class c) {
		Weapon prev = weapon;
		weapon = getWeapon(c);
		if (weapon == null)
			weapon = prev;
	}
}

class Ghost extends Player {
	public Ghost(PlayPanel container1, int index) {
		super(container1, index);
	}

	protected boolean moveDown(double amount) {
		y += amount;
		return true;
	}

	protected boolean moveRight(double amount) {
		x += amount;
		return true;
	}
}