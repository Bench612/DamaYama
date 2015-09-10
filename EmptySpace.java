import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

public class EmptySpace extends NonSolidSpace {
	public EmptySpace(int x, int y) {
		super(x, y);
	}

	public EmptySpace(int x, int y, int z) {
		super(x, y, z);
	}

	public String getDescription() {
		return "A space where a player can walk";
	}

	public void draw(Graphics g, int x, int y) {
		g.setColor(getColor());
		g.fillRect(x, y, SIZE, SIZE);
		super.draw(g, x, y);
	}

	public String toString() {
		return " ";
	}

	public void drawSlant(Perspective p) {
		p.setColor(getColor());
		super.drawSlant(p);
	}

	public static final Color defaultColor = new Color(238, 238, 238);

	public Color getColor() {
		return defaultColor;
	}

	public void drawSlantNoOverlap(Perspective p) {
		p.setColor(getColor());
		super.drawSlantNoOverlap(p);
	}

	public Space createCopy(int x, int y, int z) {
		return new EmptySpace(x, y, 0);
	}

	public boolean allowPass(Weapon w) {
		return true;
	}
}

class Spawn extends EmptySpace {
	public Spawn(int x, int y, int z) {
		super(x, y, z);
	}

	public String getDescription() {
		return "Where a player can spawn.";
	}

	public String toString() {
		return "s";
	}

	public Space createCopy(int x, int y, int z) {
		return new Spawn(x, y, z);
	}

	public static final Color spawnColor = new Color(150, 150, 170);

	public Color getColor() {
		return spawnColor;
	}

	public boolean spawn(MovingGameObject object) {
		return object.spawn(x, y);
	}

	public boolean allowUserEdit() {
		return false;
	}
}

class TeleportReceiver extends EmptySpace {

	public TeleportReceiver(int x, int y, int z) {
		super(x, y, z);
	}

	public static final Color teleportReceiverColor = new Color(200, 238, 238);

	public String getDescription() {
		return "A teleporter receiving pad.";
	}

	public String toString() {
		return "0";
	}

	public Color getColor() {
		return teleportReceiverColor;
	}

	public Space createCopy(int x, int y, int z) {
		return new TeleportReceiver(x, y, z);
	}

	public boolean receive(MovingGameObject mgo) {
		if (mgo.trySwitchSquare(x + .5 - mgo.width / 2, y + .5 - mgo.height / 2, true)) {
			mgo.x = x + .5 - mgo.width / 2;
			mgo.y = y + .5 - mgo.height / 2;
			mgo.z = z + 0.01;
			mgo.updateXSquares();
			mgo.updateYSquares();
			return true;
		}
		return false;
	}

	public boolean allowUserEdit() {
		return false;
	}

	public boolean canEnter(MovingGameObject movingGameObject, boolean ignoreHeight) {
		return super.canEnter(movingGameObject, ignoreHeight) || ignoreHeight;
	}
}

class Teleport extends EmptySpace {

	public Teleport(int x, int y, int z) {
		super(x, y, z);
	}

	public boolean allowUserEdit() {
		return false;
	}

	public static final Color teleportColor = new Color(200, 238, 200);

	public String getDescription() {
		return "A teleporter.";
	}

	public String toString() {
		return "!";
	}

	public Color getColor() {
		return teleportColor;
	}

	public Space createCopy(int x, int y, int z) {
		return new Teleport(x, y, z);

	}

	public void performAction(MovingGameObject mgo) {
		Map.getCurrent().teleport(mgo, this);
	}

	public boolean canEnter(MovingGameObject movingGameObject) {
		return true;
	}
}

class EmptySpacePatch {
	ArrayList<EmptySpace> spaces;
}