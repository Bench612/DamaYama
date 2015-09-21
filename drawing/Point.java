package drawing;

public class Point {
	double x;
	double y;

	double gameX;
	double gameY;
	double gameZ;

	Point(double x1, double y1, double gX, double gY, double gZ) {
		x = x1;
		y = y1;

		gameX = gX;
		gameY = gY;
		gameZ = gZ;
	}

	void reEvalutate(Perspective p) {
		Point p1 = p.getScreenPointNoTranslate(gameX, gameY, gameZ);
		x = p1.x;
		y = p1.y;
	}
}