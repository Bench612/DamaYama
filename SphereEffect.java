import java.awt.Color;
import drawing.*;
import java.awt.Graphics;

class SphereEffect extends Effect{
	double x, y, z, radius;
	Color color;

	public SphereEffect(double x1, double y1, double z1, double radius1, Color c) {
		super(3);
		color = c;
		x = x1;
		y = y1;
		z = z1;
		radius = radius1;
	}

	public void draw(Graphics g, int xOff, int yOff) {
		g.setColor(color);
		g.fillOval((int)((x- radius)*Space.SIZE)+ xOff , (int)((y - radius)*Space.SIZE) + yOff, (int)(radius * 2 * Space.SIZE), (int) (radius *  2 * Space.SIZE));
		super.draw(g, xOff, yOff);
	}
	
	static Color clear = new Color(0,0,0,0);
	public void drawSlant(Perspective p){
		p.setColor(color);
		p.setOutline(clear);
		p.fillSphere(x, y, z, radius, 0, 6, 4);
		super.drawSlant(p);
	}
	
}