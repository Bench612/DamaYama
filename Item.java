import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

import drawing.*;
public class Item {
	double x, y, z, width, height;
	Color color;
	ArrayList<Space> inside;

	public Item(MovingGameObject drop, Color c) {
		color = c;
		width = 0.25;
		height = 0.25;
		x = drop.x + drop.width / 2 - width / 2;
		y = drop.y + drop.height / 2 - height / 2;
		PlayPanel.currentRunning.items.add(this);
		inside = new ArrayList<Space>();
		for (int i = 0; i < drop.squaresX.length; i++)
			for (int b = 0; b < drop.squaresY.length; b++)
				inside.add(Map.getCurrent().spaces[MovingGameObject
						.getXIndex(drop.squaresX[i])][MovingGameObject
						.getYIndex(drop.squaresY[i])]);
		for (int i = 0; i < inside.size(); i++) {
			if (!inside.get(i).items.contains(this)) {
				inside.get(i).items.add(this);
				z = Math.max(inside.get(i).maxHeight(), z);
			}
		}
	}

	public void pickup(Player p) {
		PlayPanel.currentRunning.items.remove(this);
		for (int i = 0; i < inside.size(); i++)
			inside.get(i).items.remove(this);
		PlayPanel.currentRunning
				.displayMessage(p + " picked up " + this, color.darker());
	}

	public void draw(Graphics g, int xOff, int yOff) {
		g.setColor(color);
		g.fillRect((int) (x * Space.SIZE) + xOff,
				(int) (y * Space.SIZE) + yOff, (int) (width * Space.SIZE),
				(int) (height * Space.SIZE));
	}

	public void drawSlant(Perspective p) {
		p.setColor(color);
		p.setOutline(Color.black);
		Map.drawSquare(p, x, y, z, width);
		ArrayList<Point> bottom = p.getCurrentShape();
		Map.drawSquare(p, x, y, z + height, width);
		ArrayList<Point> top = p.getCurrentShape();
		p.fillForm(p.createForm(top, bottom));
		p.fillShape(top);
	}

	public boolean contains(double x1, double y1) {
		return x1 >= x && x1 <= x + width && y1 >= y && y1 <= y1 + height;
	}

	public String getDescription() {
		return this.toString();
	}
}

class Upgrade extends Item {
	Weapon w;
	String message;

	public Upgrade(MovingGameObjectWeapon drop) {
		super(drop, Color.blue);
		w = drop.weapon;
		message = getDescription() + " but no " + w.toString()
				+ " to upgrade yet.";
	}

	public void pickup(Player p) {
		if (p.upgradeWeapon(w.getClass()))
			super.pickup(p);
		else if (p == PlayPanel.currentRunning.getPlayer()) {
			if (!PlayPanel.currentRunning.containsMessage(message))
				PlayPanel.currentRunning.displayMessage(message, color);
		}
	}

	public String toString() {
		return w + " Upgrade";
	}
}

class Health extends Item {
	MovingGameObject parent;

	public Health(MovingGameObject drop) {
		super(drop, Color.green);
		parent = drop;
	}

	public void pickup(Player p) {
		super.pickup(p);
		p.changeHealth(0.1, parent);
	}

	public String toString() {
		return "Health";
	}
}

class Ammo extends Item {
	public Ammo(MovingGameObjectWeapon drop) {
		super(drop, Color.red);
		weapon = drop.weapon;
		weapon.ammo = Math.max(weapon.ammo, weapon.maxAmmo / 4);
	}

	public String toString() {
		return weapon + " Ammo";
	}

	public void pickup(Player p) {
		super.pickup(p);
		Weapon origWeapon = p.getWeapon(weapon.getClass());
		if (origWeapon == null)
			p.weapons.add(weapon);
		else
			origWeapon.ammo += weapon.ammo;
	}

	Weapon weapon;
}