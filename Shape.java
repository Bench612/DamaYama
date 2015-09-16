import java.awt.Color;
import java.util.ArrayList;


class Shape implements Comparable<Shape> {
	ArrayList<Point> points;

	public Shape() {
		points = new ArrayList<Point>();
	}

	public Shape(ArrayList<Point> ps) {
		points = ps;
	}

	Color color;
	Color outline;
	boolean fill;

	public double getTotalArea() {
		if (points.size() > 0) {
			double minX = points.get(0).x, maxX = minX, maxY = points.get(0).y, minY = maxY;
			for (int i = 1; i < points.size(); i++) {
				minX = Math.min(points.get(i).x, minX);
				minY = Math.min(points.get(i).y, minY);
				maxX = Math.max(points.get(i).x, maxX);
				maxY = Math.max(points.get(i).y, maxY);
			}
			return (Math.max(maxX - minX, 1) * Math.max(maxY - minY, 1));
		} else
			return 0;
	}

	public boolean inBounds(int maxWidth, int maxHeight) {
		for (int i = 0; i < points.size(); i++)
			if (points.get(i).x >= 0 && points.get(i).x <= maxWidth
					&& points.get(i).y <= maxHeight && points.get(i).y >= 0)
				return true;
		return false;
	}

	double compareX, compareY, compareZ;

	public void update(Perspective p) {
		compareY = points.get(0).gameY;
		compareZ = points.get(0).gameZ;
		compareX = Math.abs(p.vanishingX - points.get(0).x);
		for (int i = 1; i < points.size(); i++) {
			compareX = Math.max(compareX, Math.abs(p.vanishingX - points.get(i).x));
			compareY = Math.min(compareY,points.get(i).gameY);
			compareZ += points.get(i).gameZ;
		}
		compareZ /= points.size();
	}
	

	public int compareTo(Shape other) {
		if (compareY > other.compareY)
			return 1;
		if (compareY != other.compareY)
			return -1;
		if (compareZ > other.compareZ)
			return 1;
		if (compareZ != other.compareZ)
			return -1;
		if (compareX < other.compareX)
			return 1;
		if (compareX != other.compareX)
			return -1;
		if (!fill && other.fill)
			return 1;
		return 0; // because they are equal
	}
}
