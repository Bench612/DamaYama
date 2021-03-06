import java.awt.*;

import drawing.*;
import drawing.Point;

import java.util.ArrayList;

public class Space {
	public static final int SIZE = 32;
	int x, y, z;
	double height;
	public int steps = -1; // steps to get to a target
	public ArrayList<MovingGameObject> objects = new ArrayList<MovingGameObject>();
	public ArrayList<Item> items = new ArrayList<Item>();

	protected Space(int x1, int y1) {
		x = x1;
		y = y1;
		z = 0;
	}

	protected Space(int x1, int y1, int z1) {
		x = x1;
		y = y1;
		z = z1;
	}

	public double maxHeight() {
		return z + height;
	}

	ArrayList<Point> ceil;
	ArrayList<Point> base;
	ArrayList<Shape> mainForm;

	static final Color wallColor = new Color(150, 155, 150);

	public void drawSlant(Perspective p) {
		Map.drawSquare(p, x, y, z + height, 1);
		p.setOutline(wallColor.darker());
		if (maxHeight() != 0) {
			p.startNewConvexPolygon(5);
			ceil = p.getCurrentShape();
			p.fillShape(ceil);
			p.setColor(wallColor);
			Map.drawSquare(p, x, y, 0, 1);
			base = p.getCurrentShape();
			// p.drawShape(base);
			p.fillForm(p.createForm(ceil, base));
			p.finishConvexPolygon();
		} else {
			ceil = p.getCurrentShape();
			p.fillShape(ceil);
		}
	}

	public void draw(Graphics g, int x, int y) {
		// g.setColor(Color.black);
		// g.drawString(this.x + " " + this.y, x, y + Space.SIZE);
		// g.drawString(objects.size() + " ", x, y + Space.SIZE);
		// g.setColor(Color.black);
		// g.drawString(steps + " ", x, y + g.getFontMetrics().getHeight());
	}

	public void drawOffset(Graphics g, int xO, int yO) {
		draw(g, xO + x * SIZE, yO + y * SIZE);
	}

	public boolean canLeave(MovingGameObject movingGameObject) {
		return true;
	}

	public void leave(MovingGameObject movingGameObject) {
	}

	public void enter(MovingGameObject movingGameObject) {
	}

	public Space createCopy(int x, int y, int z) {
		return new Space(x, y, z);
	}

	public String getDescription() {
		return "A space of unknown ability.";
	}

	public boolean allowUserEdit() {
		return true;
	}

	public void reset() {
		objects.clear();
		items.clear();
		steps = -1;
	}

	public boolean allowPass(Weapon w) {
		return true;
	}

	public void performAction(MovingGameObject object) {
	}

	public boolean canEnter(MovingGameObject movingGameObject,
			boolean ignoreHeight) {
		return movingGameObject.z >= maxHeight() || ignoreHeight;
	}

	public boolean couldEnter(MovingGameObject mgo) {
		return mgo.z + maxJump >= maxHeight();
	}

	static double maxWalk = 0.5;
	static double maxJump = 0.75;

	protected boolean okayFocus(Space o) {
		return o.maxHeight() > maxHeight() - maxJump;
	}
}
