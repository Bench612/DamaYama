package drawing;

import java.util.ArrayList;
import java.util.Collections;

public class ConvexPolygon extends Drawable {

	ArrayList<Shape> faces;

	public ConvexPolygon(int size) {
		faces = new ArrayList<Shape>(size);
	}

	@Override
	public boolean inBounds(int maxWidth, int maxHeight) {
		for (Shape s : faces)
			if (!s.inBounds(maxWidth, maxHeight))
				return false;
		return true;
	}

	@Override
	void updateCompareValues(Perspective p) {
		for (Shape s : faces)
			s.updateCompareValues(p);
		Collections.sort(faces);
		minY = faces.get(0).minY;
		maxY = faces.get(0).maxY;

		minX = faces.get(0).minX;
		maxX = faces.get(0).maxX;

		minZ = faces.get(0).minZ;
		maxZ = faces.get(0).maxZ;
		for (int i = 1; i < faces.size(); i++) {
			minY = Math.min(faces.get(i).minY, minY);
			maxY = Math.max(faces.get(i).maxY, maxY);

			minX = Math.min(faces.get(i).minX, minX);
			maxX = Math.max(faces.get(i).maxX, maxX);

			minZ = Math.min(faces.get(i).minZ, minZ);
			maxZ = Math.max(faces.get(i).maxZ, maxZ);
		}
	}

	void addShape(Shape s) {
		faces.add(s);
	}

	@Override
	void draw(Perspective p) {
		for (int i = 0; i < faces.size(); i++)
			faces.get(i).draw(p);
	}
}
