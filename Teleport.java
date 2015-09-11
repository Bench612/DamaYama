import java.awt.Color;

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