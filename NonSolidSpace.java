
class NonSolidSpace extends Space {
	public NonSolidSpace(int x, int y) {
		super(x, y);
	}

	public NonSolidSpace(int x, int y, int z) {
		super(x, y, z);
	}

	public void enter(MovingGameObject mgo) {
		if (!objects.contains(mgo))
			objects.add(mgo);
	}

	public void leave(MovingGameObject mgo) {
		objects.remove(mgo);
	}

	public boolean allowUserEdit() {
		return objects.size() == 0;
	}
}