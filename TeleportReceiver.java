import java.awt.Color;

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
		if (mgo.trySwitchSquare(x + .5 - mgo.width / 2,
				y + .5 - mgo.height / 2, true)) {
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

	public boolean canEnter(MovingGameObject movingGameObject,
			boolean ignoreHeight) {
		return super.canEnter(movingGameObject, ignoreHeight) || ignoreHeight;
	}
}