package drawing;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Polygon;
import java.util.*;

//torward the vanishing is the y axis (negative is moving towards it)
//straight up is the z axis
//horizontal is the x axis
public class Perspective {
	// the vanishing x,y are in screen pixels
	int vanishingX;
	int vanishingY;
	// a base point preferrable (0,0,0)
	Point zero;
	private double translateX;
	private double translateY;
	private double translateZ;

	private Color outlineColor;

	double scale;
	double scaleY;

	Graphics g;

	// these are for drawing
	ConvexPolygon polygon;
	ArrayList<Point> shape;
	ArrayList<Drawable> drawables = new ArrayList<Drawable>(10000);
	int maxWidth, maxHeight;

	public void setColor(Color c) {
		g.setColor(c);
	}

	public Color getColor() {
		return g.getColor();
	}

	public void shiftZero(int x, int y) {
		zero.x += x;
		zero.y += y;
	}

	public void shiftVanishing(int x, int y) {
		vanishingX += x;
		vanishingY += y;
	}

	public int getVanishingX() {
		return vanishingX;
	}

	public int getVanishingY() {
		return vanishingY;
	}

	public void setOutline(Color c) {
		outlineColor = c;
	}

	public Perspective(double screenXZero, double screenYZero, int vanishX,
			int vanishY) {
		zero = new Point(screenXZero, screenYZero, 0, 0, 0);
		vanishingX = vanishX;
		vanishingY = vanishY;
		scale = 1;
		scaleY = 1;
	}

	static final double scaleConstant = 50; 
	public void update(Graphics g1, double x, double y, double z, double s,
			int maxScreenWidth, int maxScreenHeight) {
		g = g1;
		translateX = x;
		translateY = y;
		translateZ = z;
		scale = Perspective.absoluteCoefficientOfVanishing * scaleConstant * s;
		scaleY = scaleConstant * s;
		drawables.clear();
		maxWidth = maxScreenWidth;
		maxHeight = maxScreenHeight;
	}

	public void startNewConvexPolygon(int size) {
		polygon = new ConvexPolygon(size);
	}

	public void finishConvexPolygon() {
		if (!polygon.faces.isEmpty())
			drawables.add(polygon);
		polygon = null;
	}

	public ArrayList<Point> getCurrentShape() {
		return shape;
	}

	public Point getZero() {
		return zero;
	}

	public void setShape(ArrayList<Point> current) {
		shape = current;
	}

	public void startNewShape(double startX, double startY, double startZ,
			int size) {
		shape = new ArrayList<Point>(size);
		addPoint(getScreenPoint(startX, startY, startZ));
	}

	public void startNewShape(Point p, int size) {
		shape = new ArrayList<Point>(size);
		addPoint(p);
	}

	public void addPoint(Point p) {
		shape.add(p);
	}

	public Polygon get2DPolygon(ArrayList<Point> shapes) {
		Polygon p = new Polygon();
		for (int i = 0; i < shapes.size(); i++)
			p.addPoint((int) shapes.get(i).x, (int) shapes.get(i).y);
		return p;
	}

	public void drawShape(ArrayList<Point> shape1) {
		addShape(new Shape(shape1), false);
	}

	public void fillShape(ArrayList<Point> shape1) {
		addShape(new Shape(shape1), true);
	}

	private boolean addShape(Shape s, boolean toFill) {
		if (s.getTotalArea() >= 3 && s.inBounds(maxWidth, maxHeight)) {
			if (polygon == null)
				drawables.add(s);
			else
				polygon.addShape(s);
			s.color = g.getColor();
			s.fill = toFill;
			s.outline = outlineColor;
			return true;
		}
		return false;
	}

	public void drawShape(Shape shape1) {
		drawShape(shape1.points);
	}

	public void fillShape(Shape shape1) {
		fillShape(shape1.points);
	}

	public void fillShapeAbsolute(ArrayList<Point> shapes) {
		g.fillPolygon(get2DPolygon(shapes));
	}

	public void drawShapeAbsolute(ArrayList<Point> shapes) {
		g.drawPolygon(get2DPolygon(shapes));
	}

	public ArrayList<Shape> createForm(ArrayList<Point> shape1,
			ArrayList<Point> shape2) {
		ArrayList<Shape> returnValue = new ArrayList<Shape>(shape1.size() + 2);
		boolean repeat = false;
		for (int i = 0; i < shape1.size() && !repeat;) {
			startNewShape(shape1.get(i), 4);
			addPoint(shape2.get(i));
			i++;
			if (i == shape1.size()) {
				i = 0;
				repeat = true;
			}
			addPoint(shape2.get(i));
			addPoint(shape1.get(i));
			returnValue.add(new Shape(getCurrentShape()));
		}
		return returnValue;
	}

	public void drawForm(ArrayList<Shape> form) {
		for (int i = 0; i < form.size(); i++)
			drawShape(form.get(i));
	}

	public void fillForm(ArrayList<Shape> form) {
		for (int i = 0; i < form.size(); i++)
			fillShape(form.get(i));
	}

	public Point lastPoint() {
		return shape.get(shape.size() - 1);
	}

	public void drawYLine(double length) {
		addPoint(getScreenPointNoTranslate(lastPoint().gameX, lastPoint().gameY
				+ length, lastPoint().gameZ));
	}

	public void drawXLine(double length) {
		addPoint(getScreenPointNoTranslate(lastPoint().gameX + length,
				lastPoint().gameY, lastPoint().gameZ));
	}

	public void drawZLine(double length) {
		addPoint(getScreenPointNoTranslate(lastPoint().gameX,
				lastPoint().gameY, lastPoint().gameZ + length));
	}

	public void createNgon(double x, double y, double z, double radius,
			double angle, int n) {
		startNewShape(x + (radius * Math.cos(angle)),
				y - (radius * Math.sin(angle)), z, n);
		for (int i = 1; i < n; i++) {
			double newAngle = angle + (Math.PI * 2 * i) / n;
			addPoint(getScreenPoint(x + (radius * Math.cos(newAngle)), y
					- (radius * Math.sin(newAngle)), z));
		}
	}

	public void fillDome(double x, double y, double z, double radius,
			double angle, int sides, int levels) {
		createNgon(x, y, z, radius, angle, sides);
		ArrayList<Point> bottom = getCurrentShape();
		for (int i = 1; i <= levels; i++) {
			double heightAboveZ = (radius * i) / levels;
			double innerRadius = Math.sqrt((radius * radius)
					- (heightAboveZ * heightAboveZ));
			createNgon(x, y, z + heightAboveZ, innerRadius, angle, sides);
			ArrayList<Point> top = getCurrentShape();
			fillForm(createForm(top, bottom));
			bottom = top;
		}
	}

	public void fillSphere(double x, double y, double z, double radius,
			double angle, int sides, int levels) {
		createNgon(x, y, z - radius, 0, angle, sides);
		ArrayList<Point> bottom = getCurrentShape();
		for (int i = -levels + 1; i <= levels; i++) {
			double heightAboveZ = (radius * i) / levels;
			double innerRadius = Math.sqrt((radius * radius)
					- (heightAboveZ * heightAboveZ));
			createNgon(x, y, z + heightAboveZ, innerRadius, angle, sides);
			ArrayList<Point> top = getCurrentShape();
			fillForm(createForm(top, bottom));
			bottom = top;
		}
	}
	
	public void drawSphere(double x, double y, double z, double radius,
			double angle, int sides, int levels) {
		createNgon(x, y, z - radius, 0, angle, sides);
		ArrayList<Point> bottom = getCurrentShape();
		for (int i = -levels + 1; i <= levels; i++) {
			double heightAboveZ = (radius * i) / levels;
			double innerRadius = Math.sqrt((radius * radius)
					- (heightAboveZ * heightAboveZ));
			createNgon(x, y, z + heightAboveZ, innerRadius, angle, sides);
			ArrayList<Point> top = getCurrentShape();
			drawForm(createForm(top, bottom));
			bottom = top;
		}
	}

	public static final double absoluteCoefficientOfVanishing = 1 / Math.log(2);

	public double getScreenY(double y, int startHeight) {
		// pos y direction is away from point
		// minimum Y is equal to vanishingY so...
		// startHeight - vanishingY = the max
		return startHeight
				+ ((startHeight - vanishingY) * (Math.pow(2, y / scaleY) - 1));
	}

	public Point getScreenPointNoTranslate(double x, double y, double z) {
		// all are in gameCoordinates
		double startX = zero.x + (x * scale);
		double startY = zero.y - (z * scale);
		if (startX == vanishingX)
			return new Point(vanishingX, getScreenY(y, (int) startY), x, y, z);

		// these parts are in screen coordinates
		double slope = (startY - vanishingY) / (startX - vanishingX);
		double endY = getScreenY(y, (int) startY);
		return new Point(((endY - vanishingY) / slope) + vanishingX, endY, x,
				y, z);
	}

	public Point getScreenPoint(double x, double y, double z) {
		return getScreenPointNoTranslate(x + translateX, y + translateY, z
				+ translateZ);
	}

	void update(ArrayList<Point> points) {
		for (int i = 0; i < points.size(); i++)
			points.get(i).reEvalutate(this);
	}

	public static double distanceTo(double startX, double startY, double endX,
			double endY) {
		return Math.sqrt(Math.pow(endX - startX, 2)
				+ Math.pow(endY - startY, 2));
	}

	// this returns the Y value of the intersection
	public static double intersectionTwoLines(double slope1,
			double yIntercept1, double slope2, double yIntercept2) {
		return slope1 * ((yIntercept2 - yIntercept1) / (slope1 - slope2))
				+ yIntercept1;
	}

	// return the x value of a line for a certain y value
	public static double getXValue(double y, double slope, double yIntercept) {
		return (y - yIntercept) / slope;
	}

	// distance from a point to a line
	public static double distanceToLine(double x, double y, double slope,
			double yIntercept) {
		return distanceToLine(x, y, slope, -1, yIntercept);
	}

	public static double distanceToLine(double x, double y, double a, double b,
			double c) {
		return Math.abs(a * x + b * y + c)
				/ (Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2)));
	}

	public void draw() {
		for (int i = 0; i < drawables.size(); i++)
			drawables.get(i).updateCompareValues(this);
		if (drawables.size() > 0) {
			Collections.sort(drawables);
			for (int i = 0; i < drawables.size(); i++) {
				drawables.get(i).draw(this);
			}
		}
	}
}
