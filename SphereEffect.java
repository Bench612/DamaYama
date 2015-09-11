import java.awt.Color;
import java.awt.Graphics;

class SphereEffect extends Effect{
	double x, y, z, radius;

	public SphereEffect(double x1, double y1, double z1, double radius1) {
		super(3);
		x = x1;
		y = y1;
		z = z1;
		radius = radius1;
	}

	public void draw(Graphics g, int xOff, int yOff) {
		g.setColor(sphereColor);
		g.fillOval((int)((x- radius)*Space.SIZE)+ xOff , (int)((y - radius)*Space.SIZE) + yOff, (int)(radius * 2 * Space.SIZE), (int) (radius *  2 * Space.SIZE));
		super.draw(g, xOff, yOff);
	}
	
	static Color sphereColor = new Color(183,29,29,100);
	static Color clear = new Color(0,0,0,0);
	public void drawSlant(Perspective p){
		p.setColor(sphereColor);
		p.setOutline(clear);
		p.drawSphere(x, y, z, radius, 0, 6, 4);
		super.drawSlant(p);
	}
	
}