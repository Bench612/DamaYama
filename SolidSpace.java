
class SolidSpace extends Space {
	public SolidSpace(int x, int y) {
		super(x, y);
	}

	public SolidSpace(int x1, int y1, int i) {
		super(x1, y1, i);
	}

	public boolean canLeave(MovingGameObject movingGameObject) {
		return true;
	}

	public void enter(MovingGameObject mgo) {
		if (!objects.contains(mgo))
			objects.add(mgo);
	}

	public void leave(MovingGameObject mgo) {
		objects.remove(mgo);
	}

	public boolean allowPass(Weapon w) {
		return false;
	}
}