import java.awt.Color;
import java.awt.Graphics;

class CircleEffect extends Effect {
	double x, y, z, radius;

	public CircleEffect(double x1, double y1, double z1, double radius1) {
		super(3);
		x = x1;
		y = y1;
		z = z1;
		radius = radius1;
	}

	public void draw(Graphics g, int xOff, int yOff) {
		g.setColor(explosionColor);
		g.fillOval((int)((x- radius)*Space.SIZE)+ xOff , (int)((y - radius)*Space.SIZE) + yOff, (int)(radius * 2 * Space.SIZE), (int) (radius *  2 * Space.SIZE));
		super.draw(g, xOff, yOff);
	}
	static Color explosionColor = new Color(255,250,205,170);
	public void drawSlant(Perspective p){
		p.setColor(explosionColor);
		p.setOutline(explosionColor);
		p.drawDome(x, y, z, radius, 0, 20, 15);
		super.drawSlant(p);
	}
}