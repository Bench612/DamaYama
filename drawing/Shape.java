package drawing;

import java.awt.Color;
import java.util.ArrayList;

class Shape extends Drawable {
	ArrayList<Point> points;

	public Shape(int size) {
		points = new ArrayList<Point>(size);
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

	void updateCompareValues(Perspective p) {
		minY = points.get(0).gameY;
		maxY = minY;
		
		minZ = points.get(0).gameZ;
		maxZ = minZ;
		
		minX = Math.abs(p.vanishingX - points.get(0).x);
		maxX = minX;
		for (int i = 1; i < points.size(); i++) {
			double x = Math.abs(p.vanishingX - points.get(i).x);
			minX = Math.min(x, minX);
			maxX = Math.max(x, maxX);
			double y = points.get(i).gameY;
			minY = Math.min(y, minY);
			maxY = Math.max(y, maxY);
			double z = points.get(i).gameZ;
			minZ = Math.min(z, minZ);
			maxZ = Math.max(z, maxZ);
		}
	}

	@Override
	void draw(Perspective p) {
		if (fill) {
			p.setColor(color);
			p.fillShapeAbsolute(points);
		}
		p.setColor(outline);
		p.drawShapeAbsolute(points);
	}

}
