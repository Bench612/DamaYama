import java.awt.Color;
import drawing.*;
import java.util.ArrayList;

class BoxEffect extends Effect{
	double x, y, z;
	Color color;
	public BoxEffect(double x1, double y1, double z1, Color color) {
		super(5);
		x = x1;
		y = y1;
		z = z1;
		this.color = color;
	}
	
	static Color clear = new Color(0,0,0,0);
	public void drawSlant(Perspective p){
		p.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 55 + (int)(200 * timeLeft / 5)));
		p.setOutline(clear);
		Map.drawSquare(p, x, y, z, 1);
		ArrayList<Point> top = p.getCurrentShape();
		Map.drawSquare(p, x, y, z + 2, 1);
		ArrayList<Point> bottom = p.getCurrentShape();
		p.fillForm(p.createForm(top, bottom));
		p.fillShape(top);
		super.drawSlant(p);
	}
	
}