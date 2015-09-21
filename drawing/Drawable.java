package drawing;

public abstract class Drawable implements Comparable<Drawable> {

	double minX, minY, minZ;
	double maxX, maxY, maxZ;

	public int compareTo(Drawable other) {
		if (minY < other.minY)
			return -1;
		if (minY > other.minY)
			return 1;
		if (minX < other.minX)
			return 1;
		if (minX > other.minX)
			return -1;
		if (minZ + maxZ < other.minZ + other.maxZ)
			return -1;
		if (minZ + maxZ > other.minZ + other.maxZ)
			return 1;
		return 0; // because they are equal
	}

	public abstract boolean inBounds(int maxWidth, int maxHeight);

	abstract void updateCompareValues(Perspective p);

	abstract void draw(Perspective p);
}
