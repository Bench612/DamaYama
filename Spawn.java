import java.awt.Color;

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