import java.awt.Color;
import drawing.*;
import java.awt.Graphics;
import java.util.ArrayList;

public class Wall extends SolidSpace {
	public Wall(int x1, int y1, int z1) {
		super(x1, y1, z1);
		height = 1;
	}

	public void draw(Graphics g, int x, int y) {
		g.setColor(Color.gray);
		g.fillRect(x, y, SIZE, SIZE);
		super.draw(g, x, y);
	}

	public String getDescription() {
		return "A wall";
	}

	public String toString() {
		return "=";
	}

	public Space createCopy(int x, int y, int z) {
		return new Wall(x, y, z);
	}

	public boolean allowUserEdit() {
		return false;
	}

	public void drawSlant(Perspective p) {
		p.setColor(EmptySpace.defaultColor);
		super.drawSlant(p);
	}

	public boolean allowPass(Weapon w) {
		return w.owner.z >= maxHeight();
	}
}

class Ramp extends Wall {

	public Ramp(int x1, int y1, int z1) {
		super(x1, y1, z1);
		height = 0.5;
	}

	public void draw(Graphics g, int x, int y) {
		g.setColor(Color.LIGHT_GRAY);
		g.fillRect(x, y, SIZE, SIZE);
	}

	public String getDescription() {
		return "A step to allow a player to go up";
	}

	public String toString() {
		return "r";
	}

	public Space createCopy(int x, int y, int z) {
		return new Ramp(x, y, z);
	}
}

class TallWall extends Wall {
	public TallWall(int x1, int y1, int z) {
		super(x1, y1, z);
		height = 2;
	}

	public void draw(Graphics g, int x, int y) {
		super.draw(g, x, y);
		g.setColor(Color.gray.brighter());
		g.fillPolygon(new int[] { x, x, x + SIZE / 2 }, new int[] { y,
				y + SIZE, y + SIZE / 2 }, 3);
		g.fillPolygon(new int[] { x + SIZE, x + SIZE, x + SIZE / 2 },
				new int[] { y, y + SIZE, y + SIZE / 2 }, 3);
	}

	public Space createCopy(int x, int y, int z) {
		return new TallWall(x, y, z);
	}

	public void drawSlant(Perspective p) {
		super.drawSlant(p);
	}

	public String getDescription() {
		return "A taller wall than normal";
	}

	public String toString() {
		return "^";
	}

	public boolean canEnter(MovingGameObject movingGameObject) {
		return false;
	}

	public void updateFocus(Space sender, int setValue) {
	}
}